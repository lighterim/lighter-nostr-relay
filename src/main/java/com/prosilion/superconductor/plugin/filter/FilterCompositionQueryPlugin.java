package com.prosilion.superconductor.plugin.filter;

import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import nostr.base.GenericTagQuery;
import nostr.event.Kind;
import nostr.event.impl.*;
import nostr.event.query.CompositionQuery;
import nostr.event.tag.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiPredicate;

import static nostr.event.Kind.TEXT_NOTE;

@Component
public class FilterCompositionQueryPlugin<T extends CompositionQuery> implements FilterPlugin<T> {

    private static boolean anyMatch(PostIntentEvent event, List<GenericTagQuery> anyMatchList) {
        for (GenericTagQuery t : anyMatchList) {
            String tagName = t.getTagName();
            List<String> values = t.getValue();
            switch (tagName) {
                case "side" -> {
                    MakeTag makeTag = event.getSideTag();
                    return makeTag != null && values.contains(makeTag.getSide().getSide());
                }
                case "symbol" -> {
                    TokenTag token = event.getTokenTag();
                    return token != null && values.contains(token.getSymbol());
                }
                case "currency" -> {
                    QuoteTag quote = event.getQuoteTag();
                    return quote != null && values.contains(quote.getCurrency());
                }
                case "paymentMethod" -> {
                    List<PaymentTag> paymentMethods = event.getPaymentTags();
                    for (PaymentTag p : paymentMethods) {
                        if (values.contains(p.getMethod())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean allMatch(PostIntentEvent event, List<GenericTagQuery> allMatchList) {
        for (GenericTagQuery t : allMatchList) {
            String tagName = t.getTagName();
            List<String> values = t.getValue();
            switch (tagName) {
                case "side" -> {
                    MakeTag makeTag = event.getSideTag();
                    if (makeTag == null || !values.contains(makeTag.getSide().getSide())) {
                        return false;
                    }
                }
                case "symbol" -> {
                    TokenTag token = event.getTokenTag();
                    if (token == null || !values.contains(token.getSymbol())) {
                        return false;
                    }
                }
                case "currency" -> {
                    QuoteTag quote = event.getQuoteTag();
                    if (quote == null || !values.contains(quote.getCurrency())) {
                        return false;
                    }
                }
                case "paymentMethod" -> {
                    List<PaymentTag> paymentMethods = event.getPaymentTags();
                    if (paymentMethods == null || paymentMethods.isEmpty() || !contains(values, paymentMethods)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean contains(List<String> values, List<PaymentTag> paymentMethods){
        for (PaymentTag p : paymentMethods) {
            if (values.contains(p.getMethod())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BiPredicate<T, AddNostrEvent<GenericEvent>> getBiPredicate() {
        return (t, u) -> {
            GenericEvent event = u.event();
            Kind kind = event.getKind() == null ? TEXT_NOTE : Kind.valueOf(event.getKind());
            if (kind != t.getKind()) {
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

    private boolean getBiPredicate(CompositionQuery query, TradeMessageEvent event) {
        final List<GenericTagQuery> anyMatchList = query.getAnyMatchList();
        for (GenericTagQuery t : anyMatchList) {
            String tagName = t.getTagName();
            List<String> values = t.getValue();
            if ("eventIdString".equals(tagName)) {
                return values.contains(event.getCreatedByTag().getTakeIntentEventId());
            }
        }
        return false;
    }

    private boolean getBiPredicate(CompositionQuery query, TakeIntentEvent event) {
        final List<GenericTagQuery> anyMatchList = query.getAnyMatchList();
        TakeTag takeTag = event.getTakeTag();
        for (GenericTagQuery t : anyMatchList) {
            String tagName = t.getTagName();
            List<String> values = t.getValue();
            if ("pubkey".equals(tagName)) {
                return values.contains(takeTag.getMakerPubkey()) || values.contains(takeTag.getTakerPubkey());
            } else if ("nip05".equals(tagName)) {
                return values.contains(takeTag.getMakerNip05()) || values.contains(takeTag.getTakerNip05());
            }
        }
        return false;
    }

    private boolean getBiPredicate(CompositionQuery query, PostIntentEvent event) {
        List<GenericTagQuery> anyMatchList = query.getAnyMatchList();
        List<GenericTagQuery> allMatchList = query.getAllMatchList();
        if ((anyMatchList == null || anyMatchList.isEmpty()) && (allMatchList == null || allMatchList.isEmpty())) {
            return true;
        }
        if (anyMatchList != null && !anyMatchList.isEmpty()) {
            boolean result = anyMatch(event, anyMatchList);
            if(!result || (allMatchList == null || allMatchList.isEmpty())){
                return false;
            }
        }

        return allMatch(event, allMatchList);

    }

    @Override
    public List<T> getPluginFilters(Filters filters) {
        return (List<T>) List.of(filters.getCompositionQuery());
    }

    @Override
    public String getCode() {
        return "composition";
    }

}
