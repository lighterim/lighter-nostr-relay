package com.prosilion.superconductor.service.event;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.prosilion.superconductor.service.request.NotifierService;
import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.event.Kind;
import nostr.event.impl.*;
import nostr.event.message.EventMessage;
import nostr.event.tag.*;
import nostr.event.util.Nip05Validator;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
        try {
            // 1. event properties
            takeIntentEvent.validate();

            TakeTag take = takeIntentEvent.getTakeTag();
            if (!isValidNip05(take.getTakerNip05(), take.getTakerPubkey())) {
                // 2. taker nip05 & pubkey
                log.warn("invalid nip05: {}, {}", take.getMakerNip05(), take.getMakerPubkey());
                throw new RuntimeException(String.format("invalid nip05: %s, %s", take.getMakerNip05(), take.getMakerPubkey()));
            }

            // 3. make.intent & take.make
            String makeEventId = take.getIntentEventId();
            GenericEvent event = redisCache.getEventEntityByEventId(Kind.POST_INTENT, makeEventId);
            if(isSkipCheckTake){
                return;
            }
            if (event instanceof PostIntentEvent postIntentEvent) {
                MakeTag make = postIntentEvent.getSideTag();
                // 3.1 nip05, pubkey
                if (!take.getMakerNip05().equals(make.getMakerNip05()) || !take.getMakerPubkey().equals(make.getMakerPubkey())) {
                    String msg = String.format("invalid intent.make nip05:%s, pubkey:%s, event id:%s", take.getMakerNip05(), take.getMakerPubkey(), makeEventId);
                    log.warn(msg);
                    throw new RuntimeException(msg);
                }

                //3.2 token
                TokenTag token = postIntentEvent.getTokenTag();
                TokenTag takeToken = takeIntentEvent.getTokenTag();
                if (!takeToken.getSymbol().equals(token.getSymbol())
                        || !takeToken.getChain().equals(token.getChain())
                        || !takeToken.getNetwork().equals(token.getNetwork())
                        || !takeToken.getAddress().equals(token.getAddress())) {
                    String msg = String.format("invalid intent token: %s, %s, %s, %s, event id:%s", takeToken.getSymbol(), takeToken.getChain(), takeToken.getNetwork(), takeToken.getAddress(), makeEventId);
                    log.warn(msg);
                    throw new RuntimeException(msg);
                }

                //3.3 quote
                QuoteTag quote = postIntentEvent.getQuoteTag();
                QuoteTag takeQuote = takeIntentEvent.getQuoteTag();
                if (!takeQuote.getCurrency().equals(quote.getCurrency()) || takeQuote.getNumber().compareTo(quote.getNumber()) < 0) {
                    String msg = String.format("invalid intent quote: %s, %s, event id:%s", takeQuote.getNumber(), takeQuote.getCurrency(), makeEventId);
                    log.warn(msg);
                    throw new RuntimeException(msg);
                }

                //3.4 TODO: payment
//                PaymentTag payment = postIntentEvent.
            }
            log.warn("unknown event id: {}", makeEventId);
            throw new RuntimeException(String.format("unknown event id: %s", makeEventId));
        } catch (Throwable e) {
            log.warn("exception on validate:"+e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    @NotNull
    private Boolean isValidNip05(String nip05, String pubkey) {
        try {
//            return nip05ValidatorCache.get(Pair.of(nip05, pubkey));
            return Boolean.TRUE;
        } catch (Exception ex) {
            log.warn(String.format("%s: %s, %s", ex.getMessage(), nip05, pubkey), ex);
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private void validatePostIntentEvent(PostIntentEvent postIntentEvent) {
        try {
            MakeTag make = postIntentEvent.getSideTag();
            if (!isValidNip05(make.getMakerNip05(), make.getMakerPubkey())) {
                log.warn("invalid nip05: {}, {}", make.getMakerNip05(), make.getMakerPubkey());
                throw new RuntimeException(String.format("invalid nip05: %s, %s", make.getMakerNip05(), make.getMakerPubkey()));
            }
            postIntentEvent.validate();
        } catch (Throwable ex) {
            log.warn("unknown validate post intent error: {}", ex.getMessage(), ex);
            throw new RuntimeException(String.format("validate intent event error:%s", ex.getMessage()));
        }
    }
}
