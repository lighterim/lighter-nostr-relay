package com.prosilion.superconductor.plugin.filter;

import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import nostr.base.ElementAttribute;
import nostr.base.GenericTagQuery;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.*;
import nostr.event.tag.MakeTag;
import nostr.event.tag.PaymentTag;
import nostr.event.tag.QuoteTag;
import nostr.event.tag.TokenTag;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

import static nostr.event.Kind.TEXT_NOTE;

@Component
public class FilterGenericTagPlugin<T extends GenericTagQuery> implements FilterPlugin<T> {
    private static boolean defaultPredicate(String tagName, List<String> value, GenericEvent event) {
        List<BaseTag> baseTags = event.getTags();
        List<GenericTag> genericTags = baseTags.stream().filter(GenericTag.class::isInstance).map(GenericTag.class::cast).toList();

        if (genericTags.isEmpty()) return false;
        List<GenericTag> targetTags = genericTags.stream().filter(it -> it.getCode() != null && it.getCode().equals(tagName)).toList();
        if (targetTags.isEmpty()) return false;

        return targetTags.stream().anyMatch(it -> {
            List<ElementAttribute> attributes = it.getAttributes();
            if (attributes == null || attributes.isEmpty()) {
                return false;
            }
            for (ElementAttribute element : attributes) {
                if (element != null && value.contains(element.getValue())) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public String getCode() {
        return "genericTag";
    }

    @Override
    public BiPredicate<T, AddNostrEvent<GenericEvent>> getBiPredicate() {
        return (t, u) -> {
            String tagName = t.getTagName();
            List<String> value = t.getValue();
            if (tagName == null || value == null || value.isEmpty()) return true;

            return defaultPredicate(tagName, value, u.event());
        };
    }

    @Override
    public List<T> getPluginFilters(Filters filters) {
        if (filters.getGenericTagQuery() == null) {
            if (filters.getGenericTagQueryList() == null) {
                return null;
            }
            return (List<T>) filters.getGenericTagQueryList();
        } else {
            return (List<T>) new ArrayList<>(Collections.singleton(filters.getGenericTagQuery()));
        }
    }
}
