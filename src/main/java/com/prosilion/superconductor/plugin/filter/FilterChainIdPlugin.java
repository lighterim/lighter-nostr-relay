package com.prosilion.superconductor.plugin.filter;

import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.PostIntentEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

//@Component
public class FilterChainIdPlugin implements FilterPlugin<List<Integer>> {
    @Override
    public String getCode() {
        return "#chainId";
    }

    @Override
    public BiPredicate<List<Integer>, AddNostrEvent<GenericEvent>> getBiPredicate() {
        return (chainIds, u) ->
                chainIds != null
                        && u.event() instanceof PostIntentEvent
                        && chainIds.contains(((PostIntentEvent)u.event()).getTokenTag().getChainId().intValue());

    }

    @Override
    public List<List<Integer>> getPluginFilters(Filters filters) {
        return filters.getChainId() == null ? List.of():List.of(filters.getChainId());
    }
}
