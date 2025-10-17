package com.prosilion.superconductor.util;

import io.micrometer.common.util.StringUtils;
import nostr.base.ElementAttribute;
import nostr.event.BaseTag;
import nostr.event.impl.GenericTag;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class TagUtil {

    public static String createDigest(String message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(message.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

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
