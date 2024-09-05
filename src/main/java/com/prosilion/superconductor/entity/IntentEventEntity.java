package com.prosilion.superconductor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.Side;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.PostIntentEvent;
import nostr.event.tag.LimitTag;
import nostr.event.tag.MakeTag;
import nostr.event.tag.QuoteTag;
import nostr.event.tag.TokenTag;
import nostr.util.NostrUtil;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "intent", indexes={
        @Index(name="IX_INTENT_EVENT_ID_STRING", columnList = "eventIdString", unique = true),
        @Index(name="IX_INTENT_SYMBOL", columnList = "symbol" ),
        @Index(name="IX_INTENT_SIDE", columnList = "side"),
        @Index(name="IX_INTENT_QUOTE_CURRENCY", columnList = "quoteCurrency")
})
public class IntentEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /** make **/
    private String side;
    private String nip05;
    private String pubkey;

    /** token **/
    private String symbol;
    private String chain;
    private String network;
    private String address;
    private BigDecimal amount;

    /** quote **/
    private BigDecimal price;
    private String quoteCurrency;

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

    /** relays **/
    @Transient
    private List<BaseTag> tags;

    public IntentEventEntity(String side, String nip05, String pubkey,
                             String symbol, String chain, String network, String address, BigDecimal amount,
                             BigDecimal price, String quoteCurrency,
                             String currency, BigDecimal lowLimit, BigDecimal upLimit,
                             String signature, String eventId, Integer kind, Integer nip, Long createdAt, String content) {
        this.side = side;
        this.nip05 = nip05;
        this.pubkey = pubkey;
        this.symbol = symbol;
        this.chain = chain;
        this.network = network;
        this.address = address;
        this.amount = amount;
        this.price = price;
        this.quoteCurrency = quoteCurrency;
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
        PostIntentEvent event = new PostIntentEvent();
        event.setPubKey(new PublicKey(pubkey));
        event.setId(eventIdString);
        event.setKind(Kind.POST_INTENT.getValue());
        event.setNip(nip);
        event.setCreatedAt(createdAt);
        event.setContent(content);

        byte[] rawData = NostrUtil.hexToBytes(signature);
        Signature signature = new Signature();
        signature.setRawData(rawData);
        event.setSignature(signature);

        List<BaseTag> tagList = new ArrayList<>(tags);
        MakeTag make = new MakeTag(Side.valueOf(side.toUpperCase()), nip05, pubkey);
        TokenTag token = new TokenTag(symbol, chain, network, address, amount);
        QuoteTag quote = new QuoteTag(price, quoteCurrency, BigDecimal.ZERO);
        if(StringUtils.hasLength(currency)) {
            LimitTag limit = new LimitTag(currency, lowLimit, upLimit);
            tagList.add(limit);
        }
        tagList.add(make);
        tagList.add(token);
        tagList.add(quote);

        event.setTags(tagList);

        return (T)event;
    }
}
