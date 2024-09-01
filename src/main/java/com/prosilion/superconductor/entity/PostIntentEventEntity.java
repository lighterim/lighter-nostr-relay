package com.prosilion.superconductor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.PostIntentEvent;
import nostr.util.NostrUtil;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "intent")
public class PostIntentEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /** side **/
    private String side;
    private String nip05;
    private String pubkey;

    /** token **/
    private String symbol;
    private String chain;
    private String network;
    private String address;
    private BigDecimal amount;

    /** limit **/
    private String currency;
    private BigDecimal lowLimit;
    private BigDecimal upLimit;

    private String signature;
    private String eventIdString;
    private Integer kind;
    private Integer nip;
    private Long createdAt;

    @Lob
    private String content;

    @Transient
    private List<BaseTag> tags;

    public PostIntentEventEntity(String side, String nip05, String pubkey, String symbol, String chain, String network, String address, BigDecimal amount, String currency, BigDecimal lowLimit, BigDecimal upLimit, String signature, String eventId, Integer kind, Integer nip, Long createdAt, String content) {
        this.side = side;
        this.nip05 = nip05;
        this.pubkey = pubkey;
        this.symbol = symbol;
        this.chain = chain;
        this.network = network;
        this.address = address;
        this.currency = currency;
        this.lowLimit = lowLimit;
        this.upLimit = upLimit;
        this.signature = signature;
        this.eventIdString = eventId;
        this.kind = kind;
        this.nip = nip;
        this.createdAt = createdAt;
        this.content = content;

    }

    public <T extends GenericEvent> T convertEntityToDto() {
        byte[] rawData = NostrUtil.hexToBytes(signature);
        final Signature sig = new Signature();
        sig.setRawData(rawData);
        return (T)new PostIntentEvent(
               new PublicKey(pubkey),
               tags,
               content
        );

    }
}
