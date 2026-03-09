package com.prosilion.superconductor.plugin.filter;

import com.prosilion.superconductor.entity.join.subscriber.GenericFiltersFilter;
import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import nostr.base.GenericTagQuery;
import nostr.event.Kind;
import nostr.event.impl.*;
import nostr.event.query.CompositionQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiPredicate;

import static nostr.event.Kind.TEXT_NOTE;

@Component
public class GenericFilterPlugin<T extends GenericFiltersFilter> implements FilterPlugin<T> {

    @Override
    public String getCode() {
        return "filters";
    }

    @Override
    public BiPredicate<T, AddNostrEvent<GenericEvent>> getBiPredicate() {
        return (abstractFilterType, u) -> {
            GenericEvent event = u.event();
            Filters t = abstractFilterType.getFilters();
            Kind kind = event.getKind() == null ? TEXT_NOTE : Kind.valueOf(event.getKind());
            if (!t.getKinds().contains(kind)) {
                return false;
            }

            return switch (kind) {
                case POST_INTENT -> getBiPredicate(t, (PostIntentEvent) event);
                case TAKE_INTENT -> getBiPredicate(t, (TakeIntentEvent) event);
                case TRADE_MESSAGE -> getBiPredicate(t, (TradeMessageEvent) event);
                default -> false;
            };
        };
    }

    private boolean getBiPredicate(Filters filters, TakeIntentEvent takeEvent) {
        return false;
    }

    private boolean getBiPredicate(Filters filters, TradeMessageEvent tradeMessageEvent) {
        return false;
    }

    private boolean getBiPredicate(Filters filters, PostIntentEvent e) {
        List<Integer> chainIds = filters.getChainId();
        List<String> sides = filters.getSide();
        List<String> symbols = filters.getSymbol();
        List<String> currencies = filters.getCurrency();
        List<String> paymentMethods = filters.getPaymentMethod();
        return matches(chainIds, e.getTokenTag().getChainId().intValue())
                && matches(sides, e.getSideTag().getSide().getSide())
                && matches(symbols, e.getTokenTag().getSymbol())
                && matches(currencies, e.getQuoteTag().getCurrency())
                && matches(paymentMethods, e.getPaymentTag().getMethod());
    }

    private <D> boolean matches(List<D> values, D d){
        if(values == null || values.isEmpty()){
            return true;
        }
        return values.contains(d);
    }

    @Override
    public List<T> getPluginFilters(Filters filters) {
        return (List<T>) List.of(new GenericFiltersFilter(0L, filters));
    }
}
