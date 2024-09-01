package com.prosilion.superconductor.service.event;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.Kind;
import nostr.event.impl.GenericEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
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

    public <T extends GenericEvent> Map<Kind, Map<Long, GenericEvent>> getAll() {
//    return eventEntityService.getAll();
        return eventEntityService.getAll();
    }

    protected Long saveEventEntity(@NonNull GenericEvent event) {
        return eventEntityService.saveEventEntity(event);

    }
}