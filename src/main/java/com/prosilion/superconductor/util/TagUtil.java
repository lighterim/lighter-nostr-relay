package com.prosilion.superconductor.util;

import io.micrometer.common.util.StringUtils;
import nostr.base.ElementAttribute;
import nostr.event.BaseTag;
import nostr.event.impl.GenericTag;

import java.util.Collections;
import java.util.List;

public class TagUtil {

    public static String getGenericTagAttributeValue(List<BaseTag> tags, String code, int attributeIndex){
        if (StringUtils.isEmpty(code))
            return null;

        if (tags == null || tags.isEmpty())
            return null;

        BaseTag tag = tags.stream().filter(it -> it.getCode().equals(code)).findFirst().orElse(null);
        if (tag == null)
            return null;

        if (tag instanceof GenericTag) {
            GenericTag genericTag = (GenericTag) tag;
            List<ElementAttribute> attributes = genericTag.getAttributes();
            if (attributes == null || attributes.isEmpty() || attributes.size() < attributeIndex + 1) {
                return null;
            }
            return attributes.get(attributeIndex).getValue().toString();
        } else {
            return null;
        }
    }
}
