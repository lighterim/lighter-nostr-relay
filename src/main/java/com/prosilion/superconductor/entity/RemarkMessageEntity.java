package com.prosilion.superconductor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.AccountIntentEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.RemarkIntentEvent;
import nostr.event.tag.AccountTag;
import nostr.event.tag.RemarkTag;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "remark_intent", indexes={
        @Index(name="IX_REMARK_ADDRESS", columnList = "address"),
        @Index(name="IX_REMARK_CREATED_BY", columnList = "created_by" )
})
public class RemarkMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private Integer kind;
    private Integer nip;
    private String eventIdString;
    private String content;

    private String nftId;
    private String name;
    private String address;
    private BigInteger chainId;
    private String remarkPubkey;
    private String createdBy;
    private Long createdAt;

    /** relays **/
    @Transient
    private List<BaseTag> tags;

    public RemarkMessageEntity(Integer nip, Integer kind, String eventIdString, String content, String nftId, String name, String remarkPubkey, BigInteger chainId, String address, String createdBy, Long createdAt) {
        this.nip = nip;
        this.kind = kind;
        this.content = content;
        this.eventIdString = eventIdString;
        this.nftId = nftId;
        this.name = name;
        this.createdBy = createdBy;
        this.address = address;
        this.chainId = chainId;
        this.remarkPubkey = remarkPubkey;
        this.createdAt = createdAt;
    }

    public <T extends GenericEvent> T convertEntityToDto() {
        RemarkIntentEvent event = new RemarkIntentEvent(
                new PublicKey(remarkPubkey), List.of(
                new RemarkTag(name, address, remarkPubkey, nftId, chainId, createdBy)
        ), content, eventIdString, nip, createdAt);
        event.setPubKey(new PublicKey(remarkPubkey));
        event.setKind(Kind.REMARK_INTENT.getValue());
        event.setCreatedAt(createdAt);
        event.setNip(nip);

        List<BaseTag> tagList = new ArrayList<>(tags);
        RemarkTag remarkTag = new RemarkTag(name, address, remarkPubkey, nftId, chainId, createdBy);
        tagList.add(remarkTag);

        event.setTags(tagList);

        return (T)event;
    }
}
