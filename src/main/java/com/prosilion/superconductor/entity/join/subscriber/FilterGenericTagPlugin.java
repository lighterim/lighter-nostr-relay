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
            if (tagName == null || value == null)
                return true;

//            String replacedTagName = tagName.replace("#", "");
            String replacedTagName = tagName;
            List<BaseTag> baseTags = u.event().getTags();
            List<GenericTag> genericTags = baseTags.stream()
                    .filter(GenericTag.class::isInstance)
                    .map(GenericTag.class::cast)
                    .toList();
            if (genericTags.isEmpty())
                return false;

            List<GenericTag> targetTags = genericTags.stream().filter(it->it.getCode()!=null && it.getCode().equals(replacedTagName)).toList();
            if (targetTags.isEmpty())
                return false;

            boolean bl = targetTags.stream().anyMatch(it->{
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

            return bl;
        };
    }

    @Override
    public List<GenericTagQuery> getPluginFilters(Filters filters) {
        return new ArrayList<>(Collections.singleton(filters.getGenericTagQuery()));
    }
}
