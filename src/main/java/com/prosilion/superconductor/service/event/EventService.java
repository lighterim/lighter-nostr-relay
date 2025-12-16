package com.prosilion.superconductor.service.event;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.prosilion.superconductor.service.request.NotifierService;
import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import com.prosilion.superconductor.util.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.event.Kind;
import nostr.event.NIP77Event;
import nostr.event.Side;
import nostr.event.impl.*;
import nostr.event.message.EventMessage;
import nostr.event.tag.*;
import nostr.event.util.Nip05Validator;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


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
    @Value("${notice.lighter.im.pubkey:aaad79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984}")
    private String noticePusherPubkey;
    @Value("${check.take:true}")
    private boolean isSkipCheckTake;

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

        Long id = redisCache.saveEventEntity(event);
//    if (event.getKind() == Kind.TAKE_INTENT.getValue()) {
//      notifierService.nostrEventHandler(new AddNostrEvent<>(tradeEventEntityService.getById(id)));
//    }else {
        notifierService.nostrEventHandler(new AddNostrEvent<>(event));
//    }
    }

    private void validateEventForwarding(GenericEvent event) {
        if (event instanceof PostIntentEvent postIntentEvent) {
            validatePostIntentEvent(postIntentEvent);
            validateEIP712(postIntentEvent, SignerType.POST_EVENT);
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
        if (ledger != null && createdBy.getPubkey().equals(noticePusherPubkey) && !StringUtils.hasText(createdBy.getTakeIntentEventId()) && createdBy.getTradeId() > 0L) {
            TakeIntentEvent takeIntent = (TakeIntentEvent) redisCache.getEventEntityById(Kind.TAKE_INTENT, createdBy.getTradeId());
            tradeMessageEvent.setCreatedByTag(
                    CreatedByTag.builder().takeIntentEventId(takeIntent.getId()).nip05(createdBy.getNip05()).pubkey(createdBy.getPubkey())
                            .tradeId(createdBy.getTradeId()).build()
            );
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
            QuoteTag postQuoteTag = postIntentEvent.getQuoteTag();
            if (!takeQuoteTag.getCurrency().equals(postQuoteTag.getCurrency())) {
                String msg = String.format("invalid intent quote: %s, event id:%s", takeQuoteTag.getCurrency(), makeEventId);
                log.warn(msg);
                throw new BusinessException(ErrorCode.PARAM_ERROR, msg);
            }

            //3.4 payment
            PaymentTag takePayment = takeIntentEvent.getPaymentTag();
            List<PaymentTag> makePaymentTags = postIntentEvent.getPaymentTags();
            List<String> methods = makePaymentTags.stream().map(PaymentTag::getMethod).toList();
            if (!methods.contains(takePayment.getMethod())) {
                String msg = String.format("take payment{%s} does not matches: %s", takePayment.getMethod(), methods);
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

            if (takeTag.getSide() == Side.BUY) {
                // 3.4.1 payment detail
                List<String> accounts = makePaymentTags.stream().map(PaymentTag::getAccount).toList();
                List<String> qrCodes = makePaymentTags.stream().map(PaymentTag::getQrCode).toList();
                if (!accounts.contains(takePayment.getAccount()) && !qrCodes.contains(takePayment.getQrCode())) {
                    String msg = String.format("take payment:%s, %s does not matches: %s, %s",
                            takePayment.getAccount(), takePayment.getQrCode(), accounts, qrCodes
                    );
                    log.warn(msg);
                    throw new BusinessException(ErrorCode.PARAM_ERROR, msg);
                }
                //设置成低的那个价格
                if(postQuoteTag.getNumber().compareTo(BigDecimal.ZERO) > 0
                        && takeQuoteTag.getNumber().compareTo(postQuoteTag.getNumber()) > 0) {
                    takeQuoteTag.setNumber(postQuoteTag.getNumber());
                }
                //4. seller permit2 TODO:
            } else {
                validateEIP712(takeIntentEvent, SignerType.TAKE_EVENT);
                //设置成高的那个价格
                if(postQuoteTag.getNumber().compareTo(BigDecimal.ZERO) > 0
                        && takeQuoteTag.getNumber().compareTo(postQuoteTag.getNumber()) < 0) {
                    takeQuoteTag.setNumber(postQuoteTag.getNumber());
                }
            }

            //校验实时价格
            BigDecimal price = postQuoteTag.getNumber();
            if(price.compareTo(BigDecimal.ZERO) == 0) {
                if(!StringUtils.hasText(takeQuoteTag.getSignature())) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, String.format("QuoteTag signature is blank. eventId: %s", makeEventId));
                }
                if(takeQuoteTag.getTimestamp().longValue() < (System.currentTimeMillis() / 1000)) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, String.format("QuoteTag price timestamp has expired. eventId: %s", makeEventId));
                }
                TokenTag postTokenTag = postIntentEvent.getTokenTag();
                String msg = String.format("%d%s%s%s%d", postTokenTag.getChainId(), postTokenTag.getAddress(), takeQuoteTag.getTimestamp(), takeQuoteTag.getNumber().toPlainString(), postQuoteTag.getSlippageBP());
                boolean verify = ED25519Signer.verify(msg, takeQuoteTag.getSignature());
                if(!verify) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, String.format("Spot API price verify fail. msg: %s eventId: %s", msg, makeEventId));
                }
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

    private void validateEIP712(NIP77Event event, SignerType signerType) {
        boolean isValid = true;//EIP712Signer.verifySignature(event, signerType);
        if(!isValid) {
            log.warn("event-id:{}, verify sign fail", event.getId());
            //TDOD: onlyTest
//            throw new RuntimeException("verify sign fail");
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

            List<PaymentTag> paymentTags = postIntentEvent.getPaymentTags();
            if (!paymentTags.stream().allMatch(p -> StringUtils.hasText(p.getAccount()) && StringUtils.hasText(p.getQrCode()))) {
                String msg = String.format("invalid paymentTags: %s", paymentTags);
                log.warn(msg);
                throw new BusinessException(ErrorCode.PARAM_ERROR, msg);
            }

            //well done
        } catch (Throwable ex) {
            log.warn("unknown validate post intent error: {}", ex.getMessage(), ex);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, String.format("validate intent event error:%s", ex.getMessage()));
        }
    }
}
