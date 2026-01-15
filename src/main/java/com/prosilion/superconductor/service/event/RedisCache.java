package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.entity.TakeIntentEventEntity;
import com.prosilion.superconductor.util.BusinessException;
import com.prosilion.superconductor.util.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.Kind;
import nostr.event.Side;
import nostr.event.TradeStatus;
import nostr.event.impl.*;
import nostr.event.tag.*;
import nostr.id.Identity;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
// TODO: caching currently non-critical although ready for implementation anytime
public class RedisCache<T extends GenericEvent> {

    private final Map<Kind, EventEntityServiceIF<T>> eventEntityServiceMap;
    private final IntentEntityService postEventEntityService;
    private final TradeEntityService tradeEntityService;
    private final TradeMessageEntityService tradeMessageEntityService;
    private final ProfileEntityService profileEntityService;
    private final AccountMessageEntityService accountMessageEntityService;
    private final AddressBookMessageEntityService addressBookMessageEntityService;
    private final EventEntityService<T> eventEntityService;
    @Value("${notice.lighter.im.pubkey:aaad79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984}")
    private String noticePusherPubkey;


    @Autowired
    public RedisCache(List<EventEntityServiceIF<T>> eventEntityServiceList) {
//    this.eventEntityService = eventEntityService;
        eventEntityServiceMap = eventEntityServiceList.stream().collect(
                Collectors.toMap(EventEntityServiceIF<T>::getKind, Function.identity())
        );
        postEventEntityService = (IntentEntityService) eventEntityServiceMap.get(Kind.POST_INTENT);
        tradeEntityService = (TradeEntityService) eventEntityServiceMap.get(Kind.TAKE_INTENT);
        tradeMessageEntityService = (TradeMessageEntityService) eventEntityServiceMap.get(Kind.TRADE_MESSAGE);
        profileEntityService = (ProfileEntityService) eventEntityServiceMap.get(Kind.SET_METADATA);
        eventEntityService = (EventEntityService<T>) eventEntityServiceMap.get(Kind.TEXT_NOTE);
        accountMessageEntityService = (AccountMessageEntityService) eventEntityServiceMap.get(Kind.ACCOUNT_INTENT);
        addressBookMessageEntityService = (AddressBookMessageEntityService) eventEntityServiceMap.get(Kind.ADDRESS_BOOK_INTENT);
    }

//  public Map<Kind, Map<Long, T>> getAll() {
//    return eventEntityService.getAll().entrySet().stream()
//        .filter(kindMapEntry -> !kindMapEntry.getKey().equals(Kind.CLIENT_AUTH))
//        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//  }

    @NotNull
    private static TradeKeyTag buildTradeKey(TakeIntentEvent event) {
        TakeTag takeTag = event.getTakeTag();
        Side side = takeTag.getSide();
        Identity identity = Identity.generateRandomIdentity();
        String hexPrivateKey = identity.getPrivateKey().toHexString();
        String encPrivKeyForBuyer, encPrivKeyForSeller;
        if (side == Side.BUY) {
            // taker.side = buyer
            encPrivKeyForBuyer = encryptWithPublicKey(hexPrivateKey, takeTag.getTakerPubkey());
            encPrivKeyForSeller = encryptWithPublicKey(hexPrivateKey, takeTag.getMakerPubkey());
        } else {
            // taker.side = seller
            encPrivKeyForBuyer = encryptWithPublicKey(hexPrivateKey, takeTag.getMakerPubkey());
            encPrivKeyForSeller = encryptWithPublicKey(hexPrivateKey, takeTag.getTakerPubkey());
        }
        return new TradeKeyTag(encPrivKeyForBuyer, encPrivKeyForSeller, "", "", identity.getPublicKey().toHexString());
    }

    private static String encryptWithPublicKey(String plainText, String tradePubKey) {
//        try {
//            return Identity.encryptWithPublicKey(plainText, new PublicKey(tradePubKey));
//        } catch (Exception e) {
//            log.error("encPrivateKeyWithPubKey:" + e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
        return plainText;
    }

    public Map<Kind, Map<Long, GenericEvent>> getAll() {
        Map<Kind, Map<Long, GenericEvent>> map = new HashMap<>();
        for (Map.Entry<Kind, Map<Long, GenericEvent>> kindMapEntry : eventEntityService.getAll().entrySet()) {
            if (!kindMapEntry.getKey().equals(Kind.CLIENT_AUTH)) {
                if (map.put(kindMapEntry.getKey(), kindMapEntry.getValue()) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            }
        }

        Map<Kind, Map<Long, PostIntentEvent>> postEventMap = postEventEntityService.getAll();
        for (Map.Entry<Kind, Map<Long, PostIntentEvent>> kindMapEntry : postEventMap.entrySet()) {
            if (map.put(kindMapEntry.getKey(), convertToGenericEventMap(kindMapEntry.getValue())) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }

        Map<Kind, Map<Long, TakeIntentEvent>> takeEventMap = tradeEntityService.getAll();
        for (Map.Entry<Kind, Map<Long, TakeIntentEvent>> kindMapEntry : takeEventMap.entrySet()) {
            if (map.put(kindMapEntry.getKey(), convertToGenericEventMap(kindMapEntry.getValue())) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }

        Map<Kind, Map<Long, TradeMessageEvent>> tradeMessageMap = tradeMessageEntityService.getAll();
        for (Map.Entry<Kind, Map<Long, TradeMessageEvent>> kindMapEntry : tradeMessageMap.entrySet()) {
            if (map.put(kindMapEntry.getKey(), convertToGenericEventMap(kindMapEntry.getValue())) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }

//        Map<Kind, Map<Long, AccountIntentEvent>> accountMessageMap = accountMessageEntityService.getAll();
//        for (Map.Entry<Kind, Map<Long, AccountIntentEvent>> accountMapEntry : accountMessageMap.entrySet()) {
//            if (map.put(accountMapEntry.getKey(), convertToGenericEventMap(accountMapEntry.getValue())) != null) {
//                throw new IllegalStateException("Duplicate key");
//            }
//        }
//
//        Map<Kind, Map<Long, RemarkIntentEvent>> remarkMessageMap = remarkMessageEntityService.getAll();
//        for (Map.Entry<Kind, Map<Long, RemarkIntentEvent>> remarkMapEntry : remarkMessageMap.entrySet()) {
//            if (map.put(remarkMapEntry.getKey(), convertToGenericEventMap(remarkMapEntry.getValue())) != null) {
//                throw new IllegalStateException("Duplicate key");
//            }
//        }

        return map;
    }

    private Map<Long, GenericEvent> convertToGenericEventMap(Map<Long, ? extends GenericEvent> sourceMap) {
        return sourceMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    @Transactional
    public Long saveEventEntity(@NonNull GenericEvent event) {
        Kind kind = Kind.valueOf(event.getKind());
        Long id = switch (kind) {
            case SET_METADATA -> profileEntityService.saveEventEntity((MetadataEvent) event);
            case POST_INTENT -> postEventEntityService.saveEventEntity((PostIntentEvent) event);
            case ACCOUNT_INTENT -> accountMessageEntityService.saveEventEntity((AccountIntentEvent) event);
            case ADDRESS_BOOK_INTENT -> addressBookMessageEntityService.saveEventEntity((AddressBookIntentEvent) event);
            case TAKE_INTENT -> {

                TakeIntentEvent takeIntentEvent = (TakeIntentEvent) event;
                Long tradeId = takeIntentEvent.getTradeId();
                String takerPubkey = event.getPubKey().toString();
                if(takeIntentEvent.getTakeTag().getVisibleStatus()!=null) {
                    if (tradeId == 0L){
                        log.warn("takeIntentEvent.takeTag.visibleStatus is null{} and tradeId is {}", takeIntentEvent.getId(), tradeId);
                    }

                    TakeIntentEventEntity takeIntentEventEntity = tradeEntityService.getTakeIntentEventEntityById(tradeId);
                    TakeIntentEvent dbTakeIntentEvent = tradeEntityService.getTakeIntentEventByEntity(takeIntentEventEntity);

                    if (dbTakeIntentEvent == null || !dbTakeIntentEvent.getTakeTag().getTakerPubkey().equals(takerPubkey)) {
                        log.warn(
                                "No permission to set visibility. tradeId:{}, eventStringId:{}, takeTag:{} ",
                                tradeId, dbTakeIntentEvent==null ? "null":dbTakeIntentEvent.getId(), dbTakeIntentEvent==null ? null: dbTakeIntentEvent.getTakeTag()
                        );
                        throw new RuntimeException("No permission to set visibility");
                    }
                    postEventEntityService.updateIntentStatus(dbTakeIntentEvent);
                    tradeEntityService.updateTradeStatus(takeIntentEventEntity, TradeStatus.DropEvent);
                } else {
                    //takeIntentEvent.setTradeKeyTag(buildTradeKey(takeIntentEvent));
                    tradeId = tradeEntityService.saveEventEntity(takeIntentEvent);
                    takeIntentEvent.setTradeId(tradeId);
                    postEventEntityService.updateIntentStatus(takeIntentEvent);

                    EscrowTag escrowTag = getEscrowTag(takeIntentEvent);
                    takeIntentEvent.setEscrowTag(escrowTag);
                    tradeEntityService.updateEscrowSign(tradeId, escrowTag.getSignature());
                }
//                //when taker take Intent( of maker), retrieve original(maker) intent.
//                PostIntentEvent makerIntentEvent = (PostIntentEvent)getEventEntityByEventId(Kind.POST_INTENT, takeIntentEvent.getTakeTag().getIntentEventId());
//                takeIntentEvent.setLimitTag(makerIntentEvent.getLimitTag());
                yield tradeId;
            }
            case TRADE_MESSAGE -> saveTradeMessageEntity((TradeMessageEvent) event);
            default -> eventEntityService.saveEventEntity(event);
        };
        return id;
    }

    public EscrowTag getEscrowTag(TakeIntentEvent takeIntentEvent) {
        return tradeEntityService.getEscrowTag(takeIntentEvent);
    }

    private Long saveTradeMessageEntity(TradeMessageEvent event) {
        boolean isNoticePusher = noticePusherPubkey.equals(event.getCreatedByTag().getPubkey());
        if (isNoticePusher && event.getLedgerTag() != null) {
            setEncryptContentForNoticePusher(event);
        }
        CreatedByTag createdByTag = event.getCreatedByTag();
        long tradeId = createdByTag.getTradeId();
        TakeIntentEventEntity takeIntentEventEntity = tradeEntityService.getTakeIntentEventEntityById(tradeId);
        TakeIntentEvent takeIntentEvent = tradeEntityService.getTakeIntentEventByEntity(takeIntentEventEntity);
        if(takeIntentEvent!=null) {
            event.setCreatedByTag(CreatedByTag.builder()
                    .tradeId(tradeId)
                    .nip05(createdByTag.getNip05())
                    .pubkey(createdByTag.getPubkey())
                    .takeIntentEventId(takeIntentEvent.getTakeTag().getIntentEventId()).build());
        }
        if(!StringUtils.hasText(event.getCreatedByTag().getTakeIntentEventId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "CreatedByTag.TakeIntentEventId is null");
        }
        // 请求签名的30079消息，ledgerTag可能为空。
        if(event.getLedgerTag() != null && TradeStatus.CreateEscrowEvent.equals(event.getLedgerTag().getTradeStatus())) {
            if (takeIntentEvent != null) {
                PaymentTag paymentTag = takeIntentEvent.getPaymentTag();
                String paymentInfo = String.format("\nPayment Method: %s\nPayment Qrcode: %s\nPayment Account: %s\nPayment Memo: %s", paymentTag.getMethod(), paymentTag.getQrCode(), paymentTag.getAccount(), paymentTag.getMemo());
                event.setContent(event.getContent() + paymentInfo);
            }
            tradeEntityService.updateTradeEscrowHash(takeIntentEventEntity, event.getLedgerTag().getEscrowHash());
        }
        Long id = tradeMessageEntityService.saveEventEntity(event);
        if (isNoticePusher && event.getLedgerTag() != null && event.getLedgerTag().getTradeStatus() != null) {
            tradeEntityService.updateTradeStatus(takeIntentEventEntity, event.getLedgerTag().getTradeStatus());
        }
        return id;
    }

    private void setEncryptContentForNoticePusher(TradeMessageEvent event) {
        long tradeId = event.getCreatedByTag().getTradeId();
        TakeIntentEvent trade = (TakeIntentEvent) getEventEntityById(Kind.TAKE_INTENT, tradeId);
        if (trade != null) {
            String tradePubKey = trade.getTradeKeyTag().getPubkey();
            event.setContent(encryptWithPublicKey(event.getContent(), tradePubKey));
        } else {
            log.warn("The trade id: {} not found when encrypt content", tradeId);
        }
    }

    public T getEventEntityByEventId(Kind kind, String eventId) {
        GenericEvent event = switch (kind) {
            case SET_METADATA -> profileEntityService.getEventByEventIdString(eventId);
            case ACCOUNT_INTENT -> accountMessageEntityService.getEventByEventIdString(eventId);
            case POST_INTENT -> postEventEntityService.getEventByEventIdString(eventId);
            case TAKE_INTENT -> tradeEntityService.getEventByEventIdString(eventId);
            case TRADE_MESSAGE -> tradeMessageEntityService.getEventByEventIdString(eventId);
            default -> eventEntityService.getEventByEventIdString(eventId);
        };
        return (T) event;
    }

    public T getEventEntityById(Kind kind, Long id) {
        GenericEvent event = switch (kind) {
            case SET_METADATA -> profileEntityService.getEventById(id);
            case POST_INTENT -> postEventEntityService.getEventById(id);
            case ACCOUNT_INTENT -> accountMessageEntityService.getEventById(id);
            case TAKE_INTENT -> tradeEntityService.getEventById(id);
            case TRADE_MESSAGE -> tradeMessageEntityService.getEventById(id);
            default -> eventEntityService.getEventById(id);

        };
        return (T) event;
    }
}