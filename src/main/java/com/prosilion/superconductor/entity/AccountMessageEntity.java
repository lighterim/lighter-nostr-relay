package com.prosilion.superconductor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.Side;
import nostr.event.impl.AccountIntentEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.PostIntentEvent;
import nostr.event.tag.*;
import nostr.util.NostrUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "nostr_account", indexes={
        @Index(name="IX_ACCOUNT_CHAIN_ID", columnList = "chain_id"),
        @Index(name="IX_ACCOUNT_NFT_ID", columnList = "nft_id" ),
        @Index(name="IX_ACCOUNT_NOSTR_PUBKEY", columnList = "nostr_pubkey")
})
public class AccountMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private Integer kind;
    private Integer nip;
    private String eventIdString;
    private String content;

    private String nftId;
    private String tba;
    private String nostrPubkey;
    private BigInteger chainId;
    private Long createdAt;
    private String ipfsHash;

    /** relays **/
    @Transient
    private List<BaseTag> tags;

    public AccountMessageEntity(Integer nip, Integer kind, String eventIdString, String content, String nftId, String tba, String nostrPubkey, String ipfsHash, BigInteger chainId, Long createdAt) {
        this.nip = nip;
        this.kind = kind;
        this.content = content;
        this.eventIdString = eventIdString;
        this.nftId = nftId;
        this.tba = tba;
        this.nostrPubkey = nostrPubkey;
        this.chainId = chainId;
        this.createdAt = createdAt;
        this.ipfsHash = ipfsHash;
    }

    public <T extends GenericEvent> T convertEntityToDto() {
        AccountIntentEvent event = new AccountIntentEvent(
                new PublicKey(nostrPubkey), List.of(
                new AccountTag(nftId, chainId, tba, nostrPubkey, ipfsHash)
        ), content, eventIdString, nip, createdAt);
        event.setPubKey(new PublicKey(nostrPubkey));
        event.setKind(Kind.ACCOUNT_INTENT.getValue());
        event.setCreatedAt(createdAt);
        event.setNip(nip);

        List<BaseTag> tagList = new ArrayList<>(tags);
        AccountTag accountTag = new AccountTag(nftId, chainId, tba, nostrPubkey, ipfsHash);
        tagList.add(accountTag);

        event.setTags(tagList);

        return (T)event;
    }
}
