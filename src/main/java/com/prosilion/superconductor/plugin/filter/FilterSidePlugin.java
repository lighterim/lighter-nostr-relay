package com.prosilion.superconductor.plugin.filter;

import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.PostIntentEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiPredicate;

//@Component
public class FilterSidePlugin implements FilterPlugin<List<String>> {
    @Override
    public String getCode() {
        return "#side";
    }

    @Override
    public BiPredicate<List<String>, AddNostrEvent<GenericEvent>> getBiPredicate() {
        return (values, u) ->
                values != null
                        && u.event() instanceof PostIntentEvent
                        && values.contains(((PostIntentEvent)u.event()).getSideTag().getSide().getSide());

    }

    @Override
    public List<List<String>> getPluginFilters(Filters filters) {
        return filters.getSide() == null ? List.of():List.of(filters.getSide());
    }
}
