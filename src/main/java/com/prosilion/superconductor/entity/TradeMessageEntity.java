package com.prosilion.superconductor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.Side;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.TakeIntentEvent;
import nostr.event.impl.TradeMessageEvent;
import nostr.event.tag.*;
import nostr.util.NostrUtil;

import java.math.BigDecimal;
import java.util.List;


@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "trade_message")
public class TradeMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer nip;
    private Integer kind;
    private String eventIdString;
    private String takeIntentEventId;
    private String nip05;
    private String pubKey;
    @Lob
    private String content;
    private String signature;
    private Long createAt;
    public TradeMessageEntity(Integer nip, Integer kind, String eventIdString, String takeIntentEventId, String nip05, String pubKey, String content, String signature, Long createAt){
        this.nip = nip;
        this.kind = kind;
        this.eventIdString = eventIdString;
        this.takeIntentEventId = takeIntentEventId;
        this.nip05 = nip05;
        this.pubKey = pubKey;
        this.content = content;
        this.signature = signature;
        this.createAt = createAt;
    }

    public <T extends GenericEvent> T convertEntityToDto(){
        byte[] rawData = NostrUtil.hexToBytes(signature);
        final Signature sig = new Signature();
        sig.setRawData(rawData);


        TradeMessageEvent takeEvent = new TradeMessageEvent(
                new PublicKey(pubKey),
                List.of(new CreatedByTag(takeIntentEventId, nip05, pubKey)),
                content
        );
        takeEvent.setSignature(sig);
        takeEvent.setCreatedAt(createAt);
        return (T)takeEvent;
    }
}
