package com.prosilion.superconductor.service.request;

import com.prosilion.superconductor.entity.Subscriber;
import com.prosilion.superconductor.service.event.RedisCache;
import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nostr.base.BaseKey;
import nostr.base.PublicKey;
import nostr.event.Kind;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.TakeTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static nostr.event.Kind.*;


@Slf4j
@Service
public class NotifierService<T extends GenericEvent> {
  private final SubscriberNotifierService<T> subscriberNotifierService;
  private final RedisCache<T> redisCache;

  @Autowired
  public NotifierService(SubscriberNotifierService<T> subscriberNotifierService, RedisCache<T> redisCache) {
    this.subscriberNotifierService = subscriberNotifierService;
    this.redisCache = redisCache;
  }

    /**
     * 某个事件发生时，通知相关的订阅者。
     * @param addNostrEvent
     */
  public void nostrEventHandler(@NonNull AddNostrEvent<T> addNostrEvent) {
    subscriberNotifierService.nostrEventHandler(addNostrEvent);
  }

//  public void  subscriptionEventHandler(@NonNull Long subscriberId) {
//    redisCache.getAll().forEach((kind, eventMap) ->
//        eventMap.forEach((eventId, event) ->
//            subscriberNotifierService.subscriptionEventHandler(subscriberId, new AddNostrEvent<>((T)event))));


    /**
     * 订阅发生时。
     * 这个接口的定义存在问题？
     * 它应该处理当前订阅id & 对应的filters（隐含查询的数据结果），并通知订阅者所有匹配结果。目前的实现太粗放。
     * @param subscriberSessionHash
     */
  public void subscriptionEventHandler(@NonNull Long subscriberSessionHash) {
    redisCache.getAll().forEach(
            // all events for all kind.
            (kind, eventMap) -> eventMap.forEach(
                    // all events for single Kind.
                    (eventId, event) -> subscriberNotifierService.newSubscriptionHandler(
                            subscriberSessionHash, new AddNostrEvent<>((T)event)
                    )
            )
    );
    subscriberNotifierService.broadcastEose(subscriberSessionHash);
  }

  public void subscriptionEventHandler(String sessionId, String subscriberId, List<Filters> filtersList) {
      try {
          subscriberNotifierService.newSubscriptionHandler(sessionId, subscriberId, filtersList);
          Long subscriberSessionHash = new Subscriber(subscriberId, sessionId, true).getSubscriberSessionHash();
          for (Filters filters : filtersList) {
              List<GenericEvent> events = retrieveEventByFilters(filters);
              events.forEach(e -> subscriberNotifierService.nostrEventHandler(new AddNostrEvent<>((T)e)));
          }
          subscriberNotifierService.broadcastEose(subscriberSessionHash);
      }
      catch (Exception e) {
          log.warn(e.getMessage(), e);
      }
  }

    private List<GenericEvent> retrieveEventByFilters(Filters filters) {
        List<Kind> kinds = filters.getKinds();
        if(
                kinds == null
                || kinds.isEmpty()
                || (!kinds.contains(POST_INTENT) && !kinds.contains(TAKE_INTENT) && !kinds.contains(TRADE_MESSAGE))
        ) {
            throw new IllegalArgumentException("invalid filters: " + kinds);
        }
        Kind kind = kinds.get(0);

        return switch (kind) {
            case POST_INTENT -> retrievePostIntentEvent(filters);
            case TAKE_INTENT -> retrieveTakeIntentEvent(filters);
            case TRADE_MESSAGE -> retrieveTradeMesageEvent(filters);
            default -> throw new IllegalArgumentException("unknown kind: " + kind);
        };
        
    }

    private List<GenericEvent> retrieveTradeMesageEvent(Filters filters) {
        List<String> takeIntentEventIds = filters.getReferencedEvents() == null ? new ArrayList<>() : filters.getReferencedEvents().stream().map(GenericEvent::getId).toList();
        if(takeIntentEventIds.isEmpty() || filters.getAuthors().isEmpty()) {
            return new ArrayList<>();
        }
        String pubkey = filters.getAuthors().getFirst().toHexString();
        List<String> eventIds = redisCache.getMyTakeIntentIdsByPubKey(pubkey, takeIntentEventIds);
        return redisCache.getTradeMessageByTakeIntentIds(eventIds);
    }

    private List<GenericEvent> retrieveTakeIntentEvent(Filters filters) {
      //TODO: 需要校验当前用户pubkey和消息中pubkey的确定关系？
        String pubkey = filters.getAuthors().getFirst().toHexString();
        return redisCache.getTakeIntentEvent(pubkey);
//        return pubkeys.contains(takeTag.getTakerPubkey()) || pubkeys.contains(takeTag.getMakerPubkey());
    }

    private List<GenericEvent> retrievePostIntentEvent(Filters filters) {
        Integer chainId = filters.getChainId().getFirst();
        String strSide = filters.getSide()==null||filters.getSide().isEmpty()?null:filters.getSide().getFirst();
        String symbol = filters.getSymbol()==null||filters.getSymbol().isEmpty()?null:filters.getSymbol().getFirst();
        String currency = filters.getCurrency()==null||filters.getCurrency().isEmpty()?null:filters.getCurrency().getFirst();
        String paymentMethod = filters.getPaymentMethod()==null||filters.getPaymentMethod().isEmpty()?null:filters.getPaymentMethod().getFirst();
        String createdBy =  filters.getCreatedBy()==null?null:filters.getCreatedBy().getFirst();
        return redisCache.getIntentEvent(chainId, strSide, symbol, currency, paymentMethod, createdBy);
    }
}
