package com.prosilion.superconductor.plugin.filter;

import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.PostIntentEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiPredicate;

//@Component
public class FilterCurrencyPlugin implements FilterPlugin<List<String>> {
    @Override
    public String getCode() {
        return "#currency";
    }

    @Override
    public BiPredicate<List<String>, AddNostrEvent<GenericEvent>> getBiPredicate() {
        return (currencies, u) ->
                currencies != null
                        && u.event() instanceof PostIntentEvent
                        && currencies.contains(((PostIntentEvent)u.event()).getQuoteTag().getCurrency());

    }

    @Override
    public List<List<String>> getPluginFilters(Filters filters) {
        return filters.getCurrency() == null ? List.of():List.of(filters.getCurrency());
    }
}
