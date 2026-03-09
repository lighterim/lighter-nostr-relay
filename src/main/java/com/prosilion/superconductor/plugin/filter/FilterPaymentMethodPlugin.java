package com.prosilion.superconductor.plugin.filter;

import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.PostIntentEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiPredicate;

//@Component
public class FilterPaymentMethodPlugin implements FilterPlugin<List<String>> {
    @Override
    public String getCode() {
        return "#pm";
    }

    @Override
    public BiPredicate<List<String>, AddNostrEvent<GenericEvent>> getBiPredicate() {
        return (paymentMethod, u) ->
                paymentMethod != null
                        && u.event() instanceof PostIntentEvent
                        && paymentMethod.equals(((PostIntentEvent)u.event()).getPaymentTag().getMethod());

    }

    @Override
    public List<List<String>> getPluginFilters(Filters filters) {
        return filters.getPaymentMethod() == null ? List.of():List.of(filters.getPaymentMethod());
    }
}
