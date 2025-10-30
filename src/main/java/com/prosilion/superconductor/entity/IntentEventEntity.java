package com.prosilion.superconductor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.IntentType;
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
    private IntentType intentType;

    /** token **/
    private String symbol;
    private String chain;
    private String network;
    private String tokenAddress;
    @Column(precision = 36, scale=0)
    private BigDecimal amount;
    private BigInteger chainId;
    private String expireTime;

    /** quote **/
    @Column(precision = 36, scale=0)
    private BigDecimal price;
    private String currency;
    private BigInteger quoteDeadline;
    private String quoteSignature;
    private BigDecimal usdRate;

    /** limit **/
    @Column(precision = 36, scale=0)
    private BigDecimal lowLimit;
    @Column(precision = 36, scale=0)
    private BigDecimal upLimit;

    /** eip712 **/
    private String walletAddress;
    private String domainVersion;
    private String domainAppName;
    private String contractAddress;
    private String eip712Signature;

    /** permit2 **/
    private String nonce;
    private String permit2Sign;
    private String payer;
    private String spender;

    @Lob
    private String content;
    private String signature;
    private String eventIdString;
    private Integer kind;
    private Integer nip;
    private Long createdAt;

    /** relays **/
    @Transient
    private List<BaseTag> tags;

    public IntentEventEntity(String side, String nip05, String pubkey, IntentType intentType,
                             String symbol, String chain, String network, String address, BigDecimal amount, BigInteger chainId, String expireTime,
                             String walletAddress, String domainVersion,String domainAppName, String contractAddress, String eip712Signature,
                             BigDecimal price, String currency, BigInteger quoteDeadline, String quoteSignature, BigDecimal usdRate,
                             BigDecimal lowLimit, BigDecimal upLimit,
                             String nonce, String permit2Sign, String payer, String spender,
                             String signature, String eventId, Integer kind, Integer nip, Long createdAt, String content) {
        this.side = side;
        this.nip05 = nip05;
        this.pubkey = pubkey;
        this.intentType = intentType;

        this.symbol = symbol;
        this.chain = chain;
        this.network = network;
        this.tokenAddress = address;
        this.amount = amount;
        this.chainId = chainId;
        this.expireTime = expireTime;

        this.walletAddress = walletAddress;
        this.domainVersion = domainVersion;
        this.domainAppName = domainAppName;
        this.contractAddress = contractAddress;
        this.eip712Signature = eip712Signature;

        this.price = price;
        this.currency = currency;
        this.quoteDeadline = quoteDeadline;
        this.quoteSignature = quoteSignature;
        this.usdRate = usdRate;

        this.lowLimit = lowLimit;
        this.upLimit = upLimit;

        this.nonce = nonce;
        this.permit2Sign = permit2Sign;
        this.payer = payer;
        this.spender = spender;

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
        MakeTag make = new MakeTag(Side.valueOf(side.toUpperCase()), nip05, pubkey, intentType);
        TokenTag token = new TokenTag(symbol, chain, network, tokenAddress, amount.stripTrailingZeros(), chainId, expireTime);
        QuoteTag quote = new QuoteTag(price, currency, usdRate, quoteDeadline, quoteSignature);
        EIP712Tag eip712Tag = new EIP712Tag(walletAddress, domainVersion, domainAppName, contractAddress, eip712Signature);
        LimitTag limit = new LimitTag(lowLimit.stripTrailingZeros(), upLimit.stripTrailingZeros());
        Permit2Tag permit2Tag = new Permit2Tag(nonce, permit2Sign, payer, spender);

        tagList.add(make);
        tagList.add(token);
        tagList.add(quote);
        tagList.add(permit2Tag);
        tagList.add(limit);
        tagList.add(eip712Tag);

        event.setTags(tagList);

        return (T)event;
    }
}
