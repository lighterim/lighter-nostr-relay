package com.prosilion.superconductor.util;

import com.prosilion.superconductor.entity.join.subscriber.AbstractFilterType;
import com.prosilion.superconductor.plugin.filter.FilterPlugin;
import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import lombok.Getter;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;

import static java.util.Objects.nonNull;

/**
 * 发布者过滤器条件集合
 */
@Component
public class FilterMatcher {
    private final List<FilterPlugin<AbstractFilterType>> filterPlugins;

    @Autowired
    public FilterMatcher(List<FilterPlugin<AbstractFilterType>> filterPlugins) {
        this.filterPlugins = filterPlugins;
    }

    /**
     * 接收者 & 发布者过滤双向匹配（取交集）的过程
     * @param filters 接收者过滤器。
     * @param eventToCheck
     * @return 匹配到的事件（event)的集合。
     * @param <U>
     */
    public <U> List<AddNostrEvent<GenericEvent>> intersectFilterMatches(Filters filters, AddNostrEvent<GenericEvent> eventToCheck) {
        List<FilterMatcher.Combo<U>> combos = new ArrayList<>();
        //filterPlugins的元素是发布者？？？？
        filterPlugins.forEach(filterPlugin -> combos.add(
                        new Combo(
                                // filterPlugin.getPluginFilters(filters) --> 结构(发布者relayer一些event + 接收者的过滤器集合)
                                Optional.ofNullable(filterPlugin.getPluginFilters(filters)).orElseGet(ArrayList::new),
                                //规则
                                filterPlugin.getBiPredicate()
                        )
                )
        );

        // combos：结构:{（可能要推的event(List) + 接收者条件:filters)}
        Set<AddNostrEvent<GenericEvent>> nostrEvents = getFilterMatchingEvents(combos, eventToCheck);
        if (withinRange(filters.getSince(), filters.getUntil(), eventToCheck.event().getCreatedAt())) {
            nostrEvents.add(eventToCheck);
        }

        return nostrEvents.stream().limit(Optional.ofNullable(filters.getLimit()).orElse(10)).toList();
    }

    private boolean withinRange(Long since, Long until, Long createdAt) {
        if (!nonNull(since) && !nonNull(until))
            return false;
        if ((nonNull(since) && !nonNull(until)) && (since < createdAt))
            return true;
        if ((!nonNull(since) && (until >= createdAt)))
            return true;
        if (nonNull(since) && nonNull(until)) {
            return ((since < createdAt) && (until >= createdAt));
        }
        return false;
    }

    /**
     * 双向匹配逻辑
     * @param combos
     * @param eventToCheck
     * @return
     * @param <U>
     */
    private <U> Set<AddNostrEvent<GenericEvent>> getFilterMatchingEvents(List<Combo<U>> combos, AddNostrEvent<GenericEvent> eventToCheck) {
//    return combos
//        .stream()
//        .map(combo ->
//            filterTypeMatchesEventAttribute(
//                combo.getSubscriberFilterType(),
//                combo.getBiPredicate(),
//                eventToCheck))
//        .takeWhile(aBoolean -> aBoolean.equals(true))
//        .map(result -> eventToCheck)
//        .collect(Collectors.toSet());

//    boolean allMatch = combos.stream().allMatch(combo -> filterTypeMatchesEventAttribute(combo, eventToCheck));
//    return allMatch ? Set.of(eventToCheck) : Set.of();

        boolean anyMatch = combos.stream().anyMatch(combo -> filterTypeMatchesEventAttribute(combo, eventToCheck));
        return anyMatch ? Set.of(eventToCheck) : Set.of();
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

