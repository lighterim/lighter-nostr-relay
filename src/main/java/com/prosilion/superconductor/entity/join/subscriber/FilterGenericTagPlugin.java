package com.prosilion.superconductor.entity.join.subscriber;

import com.prosilion.superconductor.pubsub.AddNostrEvent;
import nostr.base.ElementAttribute;
import nostr.base.GenericTagQuery;
import nostr.event.BaseTag;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

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
            if (genericTags.isEmpty())
                return false;

            if (tagName.equals("user_pub_key")) {
                List<GenericTag> targetTags = genericTags.stream().filter(it->it.getCode()!=null && (it.getCode().equals("buyer_pub_key") || it.getCode().equals("seller_pub_key"))).toList();
                if (targetTags.isEmpty())
                    return false;

                return targetTags.stream().anyMatch(it->{
                    List<ElementAttribute> attributes = it.getAttributes();
                    if (attributes != null && !attributes.isEmpty()){
                        for (ElementAttribute element: attributes) {
                            if (element != null && value.contains(element.getValue())) {
                                return true;
                            }
                        }
                    }
                    return false;
                });
            }else{
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
