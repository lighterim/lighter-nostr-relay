package com.prosilion.superconductor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.base.UserProfile;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.MetadataEvent;
import nostr.util.NostrUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "lighter_profile", indexes = {
        @Index(name = "IX_LIGHTER_PROFILE_EVENT_ID_STRING", columnList = "eventIdString"),
        @Index(name = "IX_LIGHTER_PROFILE_NIP05", columnList = "nip05", unique = true)
})
public class ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String nip05;
    private String pubkey;
    private String name;
    private String avatar;
    private String about;
    private String signature;
    private String eventIdString;
    private Integer kind;
    private Integer nip;
    private Long createdAt;

    @Lob
    private String content;

    /**
     * relays
     **/
    @Transient
    private List<BaseTag> tags;

    public ProfileEntity(String nip05, String pubkey, String name, String avatar,
                         String signature, String eventId, Integer kind, Integer nip, Long createdAt, String content) {
        this.name = name;
        this.avatar = avatar;
        this.nip05 = nip05;
        this.pubkey = pubkey;
        this.signature = signature;
        this.eventIdString = eventId;
        this.kind = kind;
        this.nip = nip;
        this.createdAt = createdAt;
        this.content = content;

    }

    public <T extends GenericEvent> T convertEntityToDto() {
        PublicKey publicKey = new PublicKey(pubkey);
        URL picture = null;
        try {
            picture = new URL(avatar);
        } catch (Exception ex) {
            log.warn("convert event:{}, url:{}", ex.getMessage(), avatar);
        }
        MetadataEvent event = new MetadataEvent(publicKey, new UserProfile(publicKey, name, nip05, about, picture));
        event.setId(eventIdString);
        event.setNip(nip);
        event.setCreatedAt(createdAt);

        byte[] rawData = NostrUtil.hexToBytes(signature);
        Signature signature = new Signature();
        signature.setRawData(rawData);
        event.setSignature(signature);

        List<BaseTag> tagList = new ArrayList<>(tags);
        event.setTags(tagList);
        return (T) event;
    }
}
