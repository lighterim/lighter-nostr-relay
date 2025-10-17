package com.prosilion.superconductor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.AddressBookIntentEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.AddressBookTag;

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
public class AddressBookMessageEntity {

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

    public AddressBookMessageEntity(Integer nip, Integer kind, String eventIdString, String content, String nftId, String name, String remarkPubkey, BigInteger chainId, String address, String createdBy, Long createdAt) {
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
        AddressBookIntentEvent event = new AddressBookIntentEvent(
                new PublicKey(remarkPubkey), List.of(
                new AddressBookTag(name, address, remarkPubkey, nftId, chainId, createdBy)
        ), content, eventIdString, nip, createdAt);
        event.setPubKey(new PublicKey(remarkPubkey));
        event.setKind(Kind.ADDRESS_BOOK_INTENT.getValue());
        event.setCreatedAt(createdAt);
        event.setNip(nip);

        List<BaseTag> tagList = new ArrayList<>(tags);
        AddressBookTag remarkTag = new AddressBookTag(name, address, remarkPubkey, nftId, chainId, createdBy);
        tagList.add(remarkTag);

        event.setTags(tagList);

        return (T)event;
    }
}
