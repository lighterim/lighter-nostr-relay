package com.prosilion.superconductor.entity.join.subscriber;

import com.prosilion.superconductor.pubsub.AddNostrEvent;
import nostr.base.ElementAttribute;
import nostr.base.GenericTagQuery;
import nostr.event.BaseTag;
import nostr.event.NIP77Event;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.tag.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@Component
public class FilterGenericTagPlugin<T extends SubscriberFilterGenericTag> implements FilterPlugin<T>{
    @Override
    public String getCode() {
        return "genericTag";
    }

    @Override
    public BiPredicate<GenericTagQuery, AddNostrEvent<GenericEvent>> getBiPredicate() {
        return (t, u) -> {
            String tagName = t.getTagName();
            List<String> value = t.getValue();
            if (tagName == null || value == null || value.isEmpty())
                return true;

            List<BaseTag> baseTags = u.event().getTags();
            List<GenericTag> genericTags = baseTags.stream()
                    .filter(GenericTag.class::isInstance)
                    .map(GenericTag.class::cast)
                    .toList();

            if (tagName.equals("side")) {
                BaseTag makeTag = baseTags.stream().filter(it->it instanceof MakeTag).findFirst().orElse(null);
                if (makeTag == null)
                    return false;

                MakeTag make = (MakeTag) makeTag;
                if (value.contains(make.getSide().getSide())) {
                    return true;
                }
                return false;
            } else if(tagName.equals("symbol")){
                BaseTag tokenTag = baseTags.stream().filter(it->it instanceof TokenTag).findFirst().orElse(null);
                if (tokenTag == null)
                    return false;

                TokenTag token = (TokenTag) tokenTag;
                if (value.contains(token.getSymbol())) {
                    return true;
                }

                return false;
            } else if(tagName.equals("currency")){
                BaseTag quoteTag = baseTags.stream().filter(i -> i instanceof QuoteTag).findFirst().orElse(null);
                if(quoteTag == null){
                    return false;
                }
                QuoteTag quote = (QuoteTag) quoteTag;
                if(value.contains(quote.getCurrency())){
                    return true;
                }
                return false;
            } else if(tagName.equals("payment_method")){
                List<PaymentTag> paymentMethods = baseTags.stream().filter(i->i instanceof PaymentTag).map(PaymentTag.class::cast).toList();
                if(paymentMethods.isEmpty()){
                    return false;
                }

                for(PaymentTag p : paymentMethods){
                    if(value.contains(p.getMethod())){
                        return true;
                    }
                }
                return false;

            }else{
                if (genericTags.isEmpty())
                    return false;
                List<GenericTag> targetTags = genericTags.stream().filter(it->it.getCode()!=null && it.getCode().equals(tagName)).toList();
                if (targetTags.isEmpty())
                    return false;

                return targetTags.stream().anyMatch(it->{
                    List<ElementAttribute> attributes = it.getAttributes();
                    if (attributes == null || attributes.isEmpty()){
                        return false;
                    }
                    for (ElementAttribute element: attributes) {
                        if (element != null && value.contains(element.getValue())) {
                            return true;
                        }
                    }
                    return false;
                });
            }

        };
    }

    @Override
    public List<GenericTagQuery> getPluginFilters(Filters filters) {
        if (filters.getGenericTagQuery() == null) {
            if (filters.getGenericTagQueryList() == null) {
                return null;
            }
            return filters.getGenericTagQueryList();
        }else {
            return new ArrayList<>(Collections.singleton(filters.getGenericTagQuery()));
        }
    }
}
