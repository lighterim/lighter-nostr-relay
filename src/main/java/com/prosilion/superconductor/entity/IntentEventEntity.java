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
import nostr.event.tag.*;
import nostr.util.NostrUtil;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
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
        @Index(name="IX_INTENT_CURRENCY", columnList = "currency")
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
    private BigInteger chainId;
    private String expireTime;

    /** quote **/
    private BigDecimal price;
    private String currency;
    private String timestamp;
    private String quoteSignature;

    /** limit **/
    private BigDecimal lowLimit;
    private BigDecimal upLimit;

    private String signature;
    private String eventIdString;
    private Integer kind;
    private Integer nip;
    private Long createdAt;

    /** eip712 **/
    private String walletAddress;
    private String domainVersion;
    private String domainAppName;
    private String contractAddress;
    private String sign;

    /** permit2 **/
    private String nonce;
    private String permit2Sign;
    private String payer;

    @Lob
    private String content;

    /** relays **/
    @Transient
    private List<BaseTag> tags;

    public IntentEventEntity(String side, String nip05, String pubkey,
                             String symbol, String chain, String network, String address, BigDecimal amount, BigInteger chainId, String expireTime,
                             String walletAddress, String domainVersion,String domainAppName, String contractAddress,
                             BigDecimal price,
                             String currency, BigDecimal lowLimit, BigDecimal upLimit,
                             String nonce, String permit2Sign, String payer,
                             String signature, String eventId, Integer kind, Integer nip, Long createdAt, String content) {
        this.side = side;
        this.nip05 = nip05;
        this.pubkey = pubkey;
        this.symbol = symbol;
        this.chain = chain;
        this.network = network;
        this.address = address;
        this.amount = amount;
        this.expireTime = expireTime;
        this.walletAddress = walletAddress;
        this.sign = sign;
        this.domainVersion = domainVersion;
        this.domainAppName = domainAppName;
        this.contractAddress = contractAddress;
        this.price = price;
        this.currency = currency;
        this.lowLimit = lowLimit;
        this.upLimit = upLimit;
        this.nonce = nonce;
        this.permit2Sign = permit2Sign;
        this.payer = payer;
        this.signature = signature;
        this.eventIdString = eventId;
        this.kind = kind;
        this.nip = nip;
        this.createdAt = createdAt;
        this.content = content;
        this.chainId = chainId;

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
        TokenTag token = new TokenTag(symbol, chain, network, address, amount.stripTrailingZeros(), chainId, expireTime);
        QuoteTag quote = new QuoteTag(price, currency, BigDecimal.ZERO, timestamp, quoteSignature);
        EIP712Tag eip712Tag = new EIP712Tag(walletAddress, domainVersion, domainAppName, contractAddress, sign);
        LimitTag limit = new LimitTag(
                lowLimit==null?null:lowLimit.stripTrailingZeros(),
                upLimit==null?null:upLimit.stripTrailingZeros()
        );
        Permit2Tag permit2Tag = new Permit2Tag(nonce, permit2Sign, payer);
        tagList.add(limit);
        tagList.add(eip712Tag);
        tagList.add(make);
        tagList.add(token);
        tagList.add(quote);
        tagList.add(permit2Tag);

        event.setTags(tagList);

        return (T)event;
    }
}
