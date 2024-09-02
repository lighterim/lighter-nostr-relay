package com.prosilion.superconductor.entity.join.subscriber;

import com.prosilion.superconductor.pubsub.AddNostrEvent;
import nostr.base.ElementAttribute;
import nostr.base.GenericTagQuery;
import nostr.event.BaseTag;
import nostr.event.NIP77Event;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.tag.CreatedByTag;
import nostr.event.tag.MakeTag;
import nostr.event.tag.TakeTag;
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

            if (tagName.equals("user_pub_key")) {
                BaseTag takeTag = baseTags.stream().filter(it->it instanceof TakeTag).findFirst().orElse(null);
                if (takeTag == null)
                    return false;

                TakeTag take = (TakeTag) takeTag;
                if (value.contains(take.getMakerPubkey()) || value.contains(take.getTakerPubkey())) {
                    return true;
                }
                return false;
            } else if(tagName.equals("trade_event_id")){
                BaseTag createdByTag = baseTags.stream().filter(it->it instanceof CreatedByTag).findFirst().orElse(null);
                if (createdByTag == null)
                    return false;

                CreatedByTag createdBy = (CreatedByTag) createdByTag;
                if (value.contains(createdBy.getTakeIntentEventId()))
                    return true;

                return false;
            } else{
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
