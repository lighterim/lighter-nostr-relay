package com.prosilion.superconductor.util;

import com.prosilion.superconductor.entity.join.subscriber.AbstractFilterType;
import com.prosilion.superconductor.plugin.filter.FilterPlugin;
import com.prosilion.superconductor.service.request.CachedSubscriberService;
import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiPredicate;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * matcher for filters
 */
@Slf4j
@Component
public class FilterMatcher {
    private final List<FilterPlugin<AbstractFilterType>> filterPlugins;
    private final CachedSubscriberService cachedSubscriberService;

    @Autowired
    public FilterMatcher(List<FilterPlugin<AbstractFilterType>> filterPlugins, CachedSubscriberService cachedSubscriberService) {
        this.filterPlugins = filterPlugins;
        this.cachedSubscriberService = cachedSubscriberService;
    }

    /**
     * 接收者 & 发布者过滤双向匹配（取交集）的过程
     *
     * @param filters      接收者过滤器。
     * @param eventToCheck
     * @param <U>
     * @return 匹配到的事件（event)的集合。
     */
    public <U> List<AddNostrEvent<GenericEvent>> intersectFilterMatches(Filters filters, AddNostrEvent<GenericEvent> eventToCheck) {
//        List<FilterMatcher.Combo<U>> combos = new ArrayList<>();
//        filterPlugins.forEach(filterPlugin -> combos.add(
//                        new Combo(
//                                // filterPlugin.getPluginFilters(filters) --> 结构(发布者relayer一些event + 接收者的过滤器集合)
//                                Optional.ofNullable(filterPlugin.getPluginFilters(filters)).orElseGet(ArrayList::new),
//                                //规则
//                                filterPlugin.getBiPredicate()
//                        )
//                )
//        );
        boolean result = filterPlugins.stream().anyMatch(filterPlugin -> {
                    List<AbstractFilterType> fList = filterPlugin.getPluginFilters(filters);
                    if(fList == null || fList.isEmpty()) {
                        return false;
                    }
                    log.info("intersectFilterMatches: fList={}, filterPlugin:{},{}", fList, filterPlugin, filterPlugin.getClass());
                    return fList.stream().anyMatch(
                            filter -> filterPlugin.getBiPredicate().test(filter, eventToCheck)
                    );
                }

        );

        return result ? new ArrayList<>(List.of(eventToCheck)) : List.of();
//
//
//
//        // combos：结构:{（可能要推的event(List) + 接收者条件:filters)}
//        Set<AddNostrEvent<GenericEvent>> nostrEvents = getFilterMatchingEvents(combos, filters, eventToCheck);
//        return nostrEvents.stream().limit(Optional.ofNullable(filters.getLimit()).orElse(10)).toList();
    }

    private boolean withinRange(Long since, Long until, Long createdAt) {
        if (isNull(since) && isNull(until))
            return true;
        if ((nonNull(since) && isNull(until)) && (since < createdAt))
            return true;
        if (isNull(since) && until >= createdAt)
            return true;
        if (nonNull(since) && nonNull(until)) {
            return ((since < createdAt) && (until >= createdAt));
        }
        return false;
    }

    /**
     * 双向匹配逻辑
     *
     * @param combos
     * @param eventToCheck
     * @param <U>
     * @return
     */
    private <U> Set<AddNostrEvent<GenericEvent>> getFilterMatchingEvents(List<Combo<U>> combos, Filters filters, AddNostrEvent<GenericEvent> eventToCheck) {
        boolean anyMatch = combos.stream().anyMatch(combo -> filterTypeMatchesEventAttribute(combo, eventToCheck));
        return (anyMatch && withinRange(filters.getSince(), filters.getUntil(), eventToCheck.event().getCreatedAt())) ? new HashSet<>(Set.of(eventToCheck)) : Set.of();
    }

    private <U> boolean filterTypeMatchesEventAttribute(Combo<U> combo, AddNostrEvent<GenericEvent> eventToCheck) {
        //TODO: convert to stream
        for (U testable : combo.getSubscriberFilterType()) {
            //核心双向匹配逻辑：test(testable规则，eventToCheck）
            if (combo.getBiPredicate().test(testable, eventToCheck))
                return true;
        }
        return false;
    }

    @Getter
    public static class Combo<U> {
        List<U> subscriberFilterType;
        BiPredicate<U, AddNostrEvent<GenericEvent>> biPredicate;

        public Combo(List<U> subscriberFilterType, BiPredicate<U, AddNostrEvent<GenericEvent>> biPredicate) {
            this.subscriberFilterType = subscriberFilterType;
            this.biPredicate = biPredicate;
        }
    }

}

