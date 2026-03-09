package com.prosilion.superconductor.plugin.filter;

import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.PostIntentEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiPredicate;

//@Component
public class FilterSymbolPlugin implements FilterPlugin<List<String>> {
    @Override
    public String getCode() {
        return "#symbol";
    }

    @Override
    public BiPredicate<List<String>, AddNostrEvent<GenericEvent>> getBiPredicate() {
        return (symbol, u) ->
                symbol != null
                        && u.event() instanceof PostIntentEvent
                        && symbol.equals(((PostIntentEvent)u.event()).getTokenTag().getSymbol());

    }

    @Override
    public List<List<String>> getPluginFilters(Filters filters) {
        return filters.getSymbol() == null ? List.of():List.of(filters.getSymbol());
    }
}
