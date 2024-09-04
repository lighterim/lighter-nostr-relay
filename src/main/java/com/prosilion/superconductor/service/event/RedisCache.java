package com.prosilion.superconductor.service.event;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.Kind;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.PostIntentEvent;
import nostr.event.impl.TakeIntentEvent;
import nostr.event.impl.TradeMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.function.Function;
import java.util.Map;

import java.util.stream.Collectors;

@Slf4j
@Service
// TODO: caching currently non-critical although ready for implementation anytime
public class RedisCache<T extends GenericEvent> {
    //  private final EventEntityService<T> eventEntityService;
    private final Map<Kind, EventEntityServiceIF<T>> eventEntityServiceMap;
    private final PostEventEntityService postEventEntityService;
    private final TakeEventEntityService takeEventEntityService;
    private final TradeMessageEntityService tradeMessageEntityService;

    private final EventEntityService<T> eventEntityService;


    @Autowired
    public RedisCache(List<EventEntityServiceIF<T>> eventEntityServiceList) {
//    this.eventEntityService = eventEntityService;
        eventEntityServiceMap = eventEntityServiceList.stream().collect(
                Collectors.toMap(EventEntityServiceIF<T>::getKind, Function.identity())
        );
        postEventEntityService = (PostEventEntityService) eventEntityServiceMap.get(Kind.POST_INTENT);
        takeEventEntityService = (TakeEventEntityService) eventEntityServiceMap.get(Kind.TAKE_INTENT);
        tradeMessageEntityService = (TradeMessageEntityService) eventEntityServiceMap.get(Kind.TRADE_MESSAGE);
        eventEntityService = (EventEntityService<T>) eventEntityServiceMap.get(Kind.TEXT_NOTE);
    }

//  public Map<Kind, Map<Long, T>> getAll() {
//    return eventEntityService.getAll().entrySet().stream()
//        .filter(kindMapEntry -> !kindMapEntry.getKey().equals(Kind.CLIENT_AUTH))
//        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//  }


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

        Map<Kind, Map<Long, TakeIntentEvent>> takeEventMap = takeEventEntityService.getAll();
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

        return map;
    }

    private Map<Long, GenericEvent> convertToGenericEventMap(Map<Long, ? extends GenericEvent> sourceMap) {
        return sourceMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    protected Long saveEventEntity(@NonNull GenericEvent event) {
        Kind kind = Kind.valueOf(event.getKind());
        Long id = switch (kind){
            case POST_INTENT -> postEventEntityService.saveEventEntity((PostIntentEvent) event);
            case TAKE_INTENT -> takeEventEntityService.saveEventEntity((TakeIntentEvent) event);
            case TRADE_MESSAGE -> tradeMessageEntityService.saveEventEntity((TradeMessageEvent) event);
            default -> eventEntityService.saveEventEntity(event);
        };
        return id;
    }

    public T getEventEntityByEventId(Kind kind, String eventId) {
        GenericEvent event = switch (kind){
            case POST_INTENT -> postEventEntityService.getEventByEventIdString(eventId);
            case TAKE_INTENT -> takeEventEntityService.getEventByEventIdString(eventId);
            case TRADE_MESSAGE -> tradeMessageEntityService.getEventByEventIdString(eventId);
            default -> eventEntityService.getEventByEventIdString(eventId);
        };
        return (T)event;
    }

    public T getEventEntityById(Kind kind, Long id){
        GenericEvent event = switch (kind){
            case POST_INTENT -> postEventEntityService.getEventById(id);
            case TAKE_INTENT -> takeEventEntityService.getEventById(id);
            case TRADE_MESSAGE -> tradeMessageEntityService.getEventById(id);
            default -> eventEntityService.getEventById(id);

        };
        return (T)event;
    }
}