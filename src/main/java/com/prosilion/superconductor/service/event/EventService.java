package com.prosilion.superconductor.service.event;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.prosilion.superconductor.config.TokenConfig;
import com.prosilion.superconductor.service.request.NotifierService;
import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import com.prosilion.superconductor.util.*;
import jakarta.annotation.Resource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.crypto.schnorr.Schnorr;
import nostr.event.*;
import nostr.event.impl.*;
import nostr.event.message.EventMessage;
import nostr.event.tag.*;
import nostr.event.util.Nip05Validator;
import nostr.util.NostrUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class EventService<T extends EventMessage> implements EventServiceIF<T> {
    private final NotifierService<GenericEvent> notifierService;
    private final RedisCache<GenericEvent> redisCache;
    private final LoadingCache<Pair<String, String>, Boolean> nip05ValidatorCache;
    @Value("${nip05.validator.cache.max.size:100}")
    private int nip05CacheMaxSize;
    @Value("${nip05.validator.cache.minutes:5}")
    private int nip05CacheMinutes;
    @Value("${notice.lighter.im.pubkey:3bdb98ca4ccf6c4498e07130b2010193a97de6781d56fa776cd5eb20e8686134}")
    private String noticePusherPubkey;
    @Value("${check.take:true}")
    private boolean isSkipCheckTake;
    @Resource
    private TokenConfig tokenConfig;

    @Autowired
    RestClient restClient;

    @Autowired
    public EventService(NotifierService<GenericEvent> notifierService, RedisCache<GenericEvent> redisCache) {
        this.notifierService = notifierService;
        this.redisCache = redisCache;
        this.nip05ValidatorCache = CacheBuilder.newBuilder()
                .maximumSize(nip05CacheMaxSize)
                .expireAfterWrite(nip05CacheMinutes, TimeUnit.MINUTES)
                .build(new CacheLoader<Pair<String, String>, Boolean>() {
                    @NotNull
                    @Override
                    public Boolean load(@NotNull Pair<String, String> nip05AndPubkey) throws Exception {
                        try {
                            Nip05Validator.builder().nip05(nip05AndPubkey.getFirst()).publicKey(new PublicKey(nip05AndPubkey.getSecond())).build().validate();
                            return Boolean.TRUE;
                        } catch (Throwable ex) {
                            log.warn("nip05 validator {} error: {}", nip05AndPubkey, ex.getMessage(), ex);
                            return Boolean.FALSE;
                        }
                    }
                });
    }

    //  @Async
    public void processIncomingEvent(@NonNull T eventMessage) {
        log.info("processing incoming TEXT_NOTE: [{}]", eventMessage);
        GenericEvent event = (GenericEvent) eventMessage.getEvent();

        validateEventForwarding(event);

        TextNoteEvent textNoteEvent = new TextNoteEvent(
                event.getPubKey(),
                event.getTags(),
                event.getContent()
        );
        textNoteEvent.setId(event.getId());
        textNoteEvent.setCreatedAt(event.getCreatedAt());
        textNoteEvent.setSignature(event.getSignature());

        List<GenericEvent> refEventChanged= redisCache.saveEventEntity(event);
        notifierService.nostrEventHandler(new AddNostrEvent<>(event));
        for (GenericEvent refEvent : refEventChanged) {
            notifierService.nostrEventHandler(new AddNostrEvent<>(refEvent));
        }

    }

    private void validateEventForwarding(GenericEvent event) {
        boolean verify;
        try {
            event.updateSerializedEvent();
            verify = Schnorr.verify(NostrUtil.sha256(event.get_serializedEvent()), event.getPubKey().getRawData(), event.getSignature().getRawData());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SIG_SIGN_ERROR, "sig verify error.");
        }
        if(!verify) {
            throw new BusinessException(ErrorCode.SIG_SIGN_ERROR, "sig verify fail.");
        }
        if (event instanceof PostIntentEvent postIntentEvent) {
            validatePostIntentEvent(postIntentEvent);
            TokenTag tokenTag = postIntentEvent.getTokenTag();
            validateEIP712(postIntentEvent, SignerType.POST_EVENT, tokenConfig.getDecimals(tokenTag.getChainId().toString(), tokenTag.getSymbol()));
        } else if (event instanceof TakeIntentEvent takeIntentEvent) {
            validateTakeIntentEvent(takeIntentEvent);
        } else if (event instanceof TradeMessageEvent tradeMessageEvent) {
            validateTradeMessageEvent(tradeMessageEvent);
        }
    }

    private void resetCreatedByTagForNoticePusher(TradeMessageEvent tradeMessageEvent) {
        CreatedByTag createdBy = tradeMessageEvent.getCreatedByTag();
        if (!isValidNip05(createdBy.getNip05(), createdBy.getPubkey())) {
            // 2. taker nip05 & pubkey
            log.warn("invalid nip05: {}, {}", createdBy.getNip05(), createdBy.getPubkey());
            throw new RuntimeException(String.format("invalid nip05: %s, %s", createdBy.getNip05(), createdBy.getPubkey()));
        }

        LedgerTag ledger = tradeMessageEvent.getLedgerTag();
        ArbitrationTag arbitrationTag = tradeMessageEvent.getArbitrationTag();
        TlsnProofTag tlsnProofTag = tradeMessageEvent.getTlsnProofTag();
        TakeIntentEvent takeIntent = null;
        log.info("trade message event: [{}]", tradeMessageEvent);
        //push service/notice@lighter.im
        /**
         * [TradeMessageEvent(
         * createdByTag=CreatedByTag(
         * takeIntentEventId=, nip05=notice@lighter.im, pubkey=aaad79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984, tradeId=220),
         * ledgerTag=LedgerTag(
         * chain=Ethereum, network=Sepolia, txId=b0dbf2f267c6e26d6b0bbf71b324a9e44076bcae8a0a8aad1f65ddd552f81925, txUrl=https://sepolia.etherscan.io/address/b0dbf2f267c6e26d6b0bbf71b324a9e44076bcae8a0a8aad1f65ddd552f81925, tradeStatus=CreateEscrowEvent, escrowHash=8e445286b56a35810ba22f95e6bee625ff0fc365922f888815e18337cdfe58a2),
         * eip712Tag=null, escrowTag=null)]
         */
        log.info("createdBy.getPubkey().equals(noticePusherPubkey):{}, StringUtils.hasText(createdBy.getTakeIntentEventId()):{},  createdBy.getTradeId():{}", createdBy.getPubkey().equals(noticePusherPubkey), StringUtils.hasText(createdBy.getTakeIntentEventId()), createdBy.getTradeId() > 0L);
        if (
                (ledger != null || arbitrationTag!=null || tlsnProofTag!=null )
                        && createdBy.getPubkey().equals(noticePusherPubkey)
                        && !StringUtils.hasText(createdBy.getTakeIntentEventId())
                        && createdBy.getTradeId() > 0L
        ) {
            takeIntent = (TakeIntentEvent) redisCache.getEventEntityById(Kind.TAKE_INTENT, createdBy.getTradeId());
            log.info("reset trade message: takeIntent event: [{}]", takeIntent);
            tradeMessageEvent.setCreatedByTag(
                    CreatedByTag.builder().takeIntentEventId(takeIntent.getId()).nip05(createdBy.getNip05()).pubkey(createdBy.getPubkey())
                            .tradeId(createdBy.getTradeId()).build()
            );
            if(ledger != null && TradeStatus.CreateEscrowEvent.equals(ledger.getTradeStatus())) {
                    PaymentTag paymentTag = takeIntent.getPaymentTag();
                    String paymentInfo = String.format("\nPayment Method: %s\nPayment Qrcode: %s\nPayment Account: %s\nPayment Memo: %s", paymentTag.getMethod(), paymentTag.getQrCode(), paymentTag.getAccount(), paymentTag.getMemo());
                    tradeMessageEvent.setContent(tradeMessageEvent.getContent() + paymentInfo);
            }
            log.info("reset1 trade message event: [{}]", tradeMessageEvent);
        }
        EIP712Tag eip712Tag = tradeMessageEvent.getEip712Tag();
        if(eip712Tag!=null) {
            if(takeIntent==null) {
                // eventStringId, tradeId, escrowHash
                if(createdBy.getTradeId() > 0) {
                    takeIntent = (TakeIntentEvent) redisCache.getEventEntityById(Kind.TAKE_INTENT, createdBy.getTradeId());
                }
                else{
                    takeIntent = (TakeIntentEvent)  redisCache.getEventEntityByEventId(Kind.TAKE_INTENT, createdBy.getTakeIntentEventId());
                }
                if(takeIntent==null) {
                    throw new BusinessException(ErrorCode.TRADE_ID_NOT_FOUND, String.format("invalid tradeId: %d",  createdBy.getTradeId()));
                }
                // tradeId-->TakeIntentEvent-->EscrowParams---(escrow/signature)--->signature(63b)
            }

            // 调用端希望签名。
            if(!StringUtils.hasText(eip712Tag.getSign())) {
                EscrowTag escrowTag = EIP712Signer.getSignedEscrowTag(restClient,
                        takeIntent.getTokenTag(), takeIntent.getTakeTag(), takeIntent.getQuoteTag(),
                        takeIntent.getPermit2Tag(), takeIntent.getPaymentTag(), eip712Tag, tokenConfig, createdBy.getTradeId());
                tradeMessageEvent.setEip712Tag(
                        EIP712Tag.builder().walletAddress(eip712Tag.getWalletAddress())
                                .domainAppName(eip712Tag.getDomainAppName())
                                .domainVersion(eip712Tag.getDomainVersion())
                                .contractAddress(eip712Tag.getContractAddress())
                                .sign(escrowTag.getSignature()).build());
                tradeMessageEvent.setEscrowTag(escrowTag);
                log.info("reset2 trade message event: [{}]", tradeMessageEvent);
            }
            //TODO:  只有仲裁消息，对手同意仲裁消息需要eip712验证。
//            TokenTag tokenTag = takeIntent.getTokenTag();
//            int tokenDecimals = tokenConfig.getDecimals(tokenTag.getChainId().toString(), tokenTag.getSymbol());
//            validateEIP712(takeIntent, SignerType.TRADE_EVENT, tokenDecimals);

        }
    }

    private void validateTradeMessageEvent(TradeMessageEvent tradeMessageEvent) {
        tradeMessageEvent.validate();
        resetCreatedByTagForNoticePusher(tradeMessageEvent);
    }

    private void validateTakeIntentEvent(TakeIntentEvent takeIntentEvent) {
        // 1. event properties
        takeIntentEvent.validate();

        TakeTag takeTag = takeIntentEvent.getTakeTag();

        // 3. make.intent & take.make
        String makeEventId = takeTag.getIntentEventId();

        GenericEvent event = redisCache.getEventEntityByEventId(Kind.POST_INTENT, makeEventId);
        if (isSkipCheckTake) {
            return;
        }
        if (event instanceof PostIntentEvent postIntentEvent) {
            MakeTag make = postIntentEvent.getSideTag();
            // 3.0 take.side & make.side
            if (takeTag.getSide() == make.getSide()) {
                String msg = String.format("invalid intent.side: %s, and take.side:%s.", make.getSide(), takeTag.getSide());
                log.warn(msg);
                throw new BusinessException(ErrorCode.PARAM_ERROR, msg);
            }
            // 3.1 nip05, pubkey
            if (!takeTag.getMakerNip05().equals(make.getMakerNip05()) || !takeTag.getMakerPubkey().equals(make.getMakerPubkey())) {
                String msg = String.format("invalid intent.make nip05:%s, pubkey:%s, event id:%s", takeTag.getMakerNip05(), takeTag.getMakerPubkey(), makeEventId);
                log.warn(msg);
                throw new BusinessException(ErrorCode.PARAM_ERROR, msg);
            }

            //3.2 token
            TokenTag token = postIntentEvent.getTokenTag();
            TokenTag takeToken = takeIntentEvent.getTokenTag();
            if (!takeToken.getSymbol().equals(token.getSymbol())
                    || !takeToken.getChainId().equals(token.getChainId())
                    || !takeToken.getNetwork().equals(token.getNetwork())
                    || !takeToken.getAddress().equals(token.getAddress())) {
                String msg = String.format("invalid intent token: %s, %s, %s, %s, event id:%s", takeToken.getSymbol(), takeToken.getChain(), takeToken.getNetwork(), takeToken.getAddress(), makeEventId);
                log.warn(msg);
                throw new BusinessException(ErrorCode.PARAM_ERROR, msg);
            }

            //3.3 quote
            QuoteTag takeQuoteTag = takeIntentEvent.getQuoteTag();
            QuoteTag makerQuoteTag = postIntentEvent.getQuoteTag();
            if (!takeQuoteTag.getCurrency().equals(makerQuoteTag.getCurrency())) {
                String msg = String.format("invalid intent quote: %s, event id:%s", takeQuoteTag.getCurrency(), makeEventId);
                log.warn(msg);
                throw new BusinessException(ErrorCode.PARAM_ERROR, msg);
            }

            //3.4 payment
            PaymentTag takePayment = takeIntentEvent.getPaymentTag();
            PaymentTag makePaymentTag = postIntentEvent.getPaymentTag();
            String method = makePaymentTag.getMethod();
            if (!method.equals(takePayment.getMethod())) {
                String msg = String.format("take payment{%s} does not matches: %s", takePayment.getMethod(), method);
                log.warn(msg);
                throw new BusinessException(ErrorCode.PARAM_ERROR, msg);
            }

            LimitTag limitTag = postIntentEvent.getLimitTag();

            // volume limit
            BigDecimal volume = takeTag.getVolume();
            if(volume.compareTo(limitTag.getLowLimit()) < 0 || volume.compareTo(limitTag.getUpLimit()) > 0) {
                String msg = String.format("take volume:%.4f, does not matches. low:%.4f, up:%.4f. eventId: %s",
                        volume, limitTag.getLowLimit(), limitTag.getUpLimit(), makeEventId
                );
                log.warn(msg);
                throw new BusinessException(ErrorCode.PARAM_ERROR, msg);
            }

            BigDecimal takePrice = takeQuoteTag.getNumber().stripTrailingZeros();
            if (takeTag.getSide() == Side.BUY) {
                // 3.4.1 payment detail
                String account = makePaymentTag.getAccount();
                String qrCode = makePaymentTag.getQrCode();
                if (!account.equalsIgnoreCase(takePayment.getAccount()) || !qrCode.equalsIgnoreCase(takePayment.getQrCode())) {
                    String msg = String.format("take payment:%s, %s does not matches: %s, %s",
                            takePayment.getAccount(), takePayment.getQrCode(), account, qrCode
                    );
                    log.warn(msg);
                    throw new BusinessException(ErrorCode.PARAM_ERROR, msg);
                }

                if(makerQuoteTag.getNumber().compareTo(BigDecimal.ZERO) > 0  && takePrice.compareTo(makerQuoteTag.getNumber()) > 0) {
                    //taker.price > maker.price, 设置成低的那个价格
                    takeQuoteTag.setNumber(makerQuoteTag.getNumber());
                }
                //4. the maker is seller.
                //TODO: reset EIP712Tag & Permit2Tag with postIntentEvent(maker.intent)
                IntentType intentType = postIntentEvent.getSideTag().getIntentType();
                if(IntentType.SIGNATURE_SELL.equals(intentType)) {
                    takeIntentEvent.setPermit2Tag(postIntentEvent.getPermit2Tag());
                } else if(IntentType.BULK_SELL.equals(intentType)) {
                    takeIntentEvent.setEip712Tag(postIntentEvent.getEip712Tag());
                }
            } else {
                takeIntentEvent.setEip712Tag(postIntentEvent.getEip712Tag());
                validateEIP712(takeIntentEvent, SignerType.TAKE_EVENT, tokenConfig.getDecimals(token.getChainId().toString(), token.getSymbol()));
                //设置成高的那个价格
                if(makerQuoteTag.getNumber().compareTo(BigDecimal.ZERO) > 0
                        && takePrice.compareTo(makerQuoteTag.getNumber()) < 0) {
                    takeQuoteTag.setNumber(makerQuoteTag.getNumber());
                }
            }

            //校验实时价格
            BigDecimal makerPrice = makerQuoteTag.getNumber();
            if(makerPrice.compareTo(BigDecimal.ZERO) == 0) {
                if(!StringUtils.hasText(takeQuoteTag.getSignature())) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, String.format("QuoteTag signature is blank. eventId: %s", makeEventId));
                }
                BigInteger quoteDeadline = takeQuoteTag.getTimestamp();
                if(quoteDeadline.longValue() < (System.currentTimeMillis() / 1000)) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, String.format("QuoteTag makerPrice timestamp has expired. eventId: %s", makeEventId));
                }
                TokenTag makerTokenTag = postIntentEvent.getTokenTag();
                BigInteger chainId = makerTokenTag.getChainId();
                String tokenAddress = makerTokenTag.getAddress();
                String msg = String.format("%d%s%s%s%d%s", chainId, tokenAddress.toLowerCase(), quoteDeadline, takePrice.toPlainString(), makerQuoteTag.getSlippageBP(), postIntentEvent.getSideTag().getSide().getSide().toUpperCase());
                boolean verify = ED25519Signer.verify(msg, takeQuoteTag.getSignature());
                if(!verify) {
                    log.warn("price verification failed: {} msg:{}, signature: {}", takeIntentEvent.getId(),  msg, takeQuoteTag.getSignature());
                    throw new BusinessException(ErrorCode.PARAM_ERROR, String.format("Spot API makerPrice verify fail. msg: %s eventId: %s", msg, makeEventId));
                }
            }
            else{
                //限价单，补充正确的法币/美元汇率
                takeQuoteTag.setUsdRate(getUsdRateForLimitPrice(takeQuoteTag.getCurrency()));
            }
            //well done
            return;
        }
        log.warn("unknown event id: {}", makeEventId);
        throw new BusinessException(ErrorCode.PARAM_ERROR, String.format("unknown event id: %s", makeEventId));
    }

    @NotNull
    private Boolean isValidNip05(String nip05, String pubkey) {
        try {
//            return nip05ValidatorCache.get(Pair.of(nip05, pubkey));
            return Boolean.TRUE;
        } catch (Exception ex) {
            log.warn(String.format("%s: %s, %s", ex.getMessage(), nip05, pubkey), ex);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
    }

    private void validateEIP712(NIP77Event event, SignerType signerType, int tokenDecimals) {
        boolean isValid = EIP712Signer.verifySignature(event, signerType, tokenDecimals);
        if(!isValid) {
            log.warn("event-id:{}, verify sign fail", event.getId());
            //TDOD: onlyTest
//            throw new RuntimeException("verify sign fail");
        }
        else{
            log.info("event-id:{}, verify sign success", event.getId());
        }
    }

    private void validatePostIntentEvent(PostIntentEvent postIntentEvent) {
        try {
            postIntentEvent.validate();
            MakeTag make = postIntentEvent.getSideTag();
            if (!isValidNip05(make.getMakerNip05(), make.getMakerPubkey())) {
                log.warn("invalid nip05: {}, {}", make.getMakerNip05(), make.getMakerPubkey());
                throw new BusinessException(ErrorCode.PARAM_ERROR, String.format("invalid nip05: %s, %s", make.getMakerNip05(), make.getMakerPubkey()));
            }

            PaymentTag paymentTag = postIntentEvent.getPaymentTag();
            if (!StringUtils.hasText(paymentTag.getAccount()) || !StringUtils.hasText(paymentTag.getQrCode())) {
                String msg = String.format("invalid paymentTags: %s", paymentTag);
                log.warn(msg);
                throw new BusinessException(ErrorCode.PARAM_ERROR, msg);
            }

            //well done
        } catch (Throwable ex) {
            log.warn("unknown validate post intent error: {}", ex.getMessage(), ex);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, String.format("validate intent event error:%s", ex.getMessage()));
        }
    }

    private BigDecimal getUsdRateForLimitPrice(String currency){
        if("USD".equalsIgnoreCase(currency)){
            return BigDecimal.ONE;
        }
        try {
            String path = String.format("/api/forex/%s", currency);
            HttpResponse<String> response = restClient.get(path).join();
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                JsonObject spotObj = JsonParser.parseString(responseBody).getAsJsonObject();
                int code = spotObj.get("code").getAsInt();
                if (code != 0) {
                    throw new BusinessException(ErrorCode.INTERNAL_ERROR, String.format("usdRate error code:%s", code));
                }
                // {"code":0,"usd_rate":6.828701,"timestamp":1775749033}
                BigDecimal usdRate = spotObj.get("usd_rate").getAsBigDecimal();
                long timestamp = spotObj.get("timestamp").getAsLong();
                long nowSec = System.currentTimeMillis() / 1000;
                if (Math.abs(nowSec - timestamp) > 10) {
                    throw new BusinessException(ErrorCode.INTERNAL_ERROR, String.format("usdRate expire:%s vs %s", nowSec, timestamp));
                }
                return usdRate;
            }
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, String.format("usdRate network error.%s", response.statusCode()));
        }
        catch (Throwable ex) {
            log.warn("unknown usdRate error: {}", ex.getMessage(), ex);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, String.format("usdRate network error.%s", ex.getMessage()));
        }
    }
}
