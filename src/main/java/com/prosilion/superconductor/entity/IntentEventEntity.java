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
        @Index(name="IX_INTENT_CURRENCY", columnList = "quoteCurrency")
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
    @Column(precision = 36, scale=18)
    private BigDecimal amount;
    private BigInteger chainId;
    private String expireTime;

    /** quote **/
    @Column(precision = 36, scale=18)
    private BigDecimal price;
    private String quoteCurrency;
    private BigInteger quoteDeadline;
    private String quoteSignature;
    @Column(precision = 36, scale=18)
    private BigDecimal quoteUsdRate;
    private Integer quoteSlippageBP;

    /** limit **/
    @Column(precision = 36, scale=18)
    private BigDecimal lowLimit;
    @Column(precision = 36, scale=18)
    private BigDecimal upLimit;

    /** eip712 **/
    private String eip712WalletAddress;
    private String eip712DomainVersion;
    private String eip712DomainAppName;
    private String eip712ContractAddress;
    private String eip712Signature;

    /** permit2 **/
    private String permit2Nonce;
    private String permit2Sign;
    private String payer;
    private String permit2Spender;
    private String permit2WalletAddress;
    private String permit2DomainAppName;
    private String permit2ContractAddress;

    @Lob
    private String content;
    private String signature;
    private String eventIdString;
    private Integer kind;
    private Integer nip;
    private Long createdAt;

    @Column(precision = 36, scale=18)
    private BigDecimal tradedAmount; //只针对bulk_sell

    /** relays **/
    @Transient
    private List<BaseTag> tags;

    private Integer status;

    @Version
    private Long version; // 版本号字段

    public IntentEventEntity(String side, String nip05, String pubkey, IntentType intentType,
                             String symbol, String chain, String network, String address, BigDecimal amount, BigInteger chainId, String expireTime,
                             String walletAddress, String domainVersion,String domainAppName, String contractAddress, String eip712Signature,
                             BigDecimal price, String currency, BigInteger quoteDeadline, String quoteSignature, BigDecimal usdRate, Integer slippageBP,
                             BigDecimal lowLimit, BigDecimal upLimit,
                             String nonce, String permit2Sign, String payer, String spender,String permit2WalletAddress, String permit2DomainAppName, String permit2ContractAddress,
                             String signature, String eventId, Integer kind, Integer nip, Long createdAt, String content, Integer status, BigDecimal tradedAmount) {
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

        this.eip712WalletAddress = walletAddress;
        this.eip712DomainVersion = domainVersion;
        this.eip712DomainAppName = domainAppName;
        this.eip712ContractAddress = contractAddress;
        this.eip712Signature = eip712Signature;

        this.price = price;
        this.quoteCurrency = currency;
        this.quoteDeadline = quoteDeadline;
        this.quoteSignature = quoteSignature;
        this.quoteUsdRate = usdRate;
        this.quoteSlippageBP = slippageBP;

        this.lowLimit = lowLimit;
        this.upLimit = upLimit;

        this.permit2Nonce = nonce;
        this.permit2Sign = permit2Sign;
        this.payer = payer;
        this.permit2Spender = spender;
        this.permit2WalletAddress = permit2WalletAddress;
        this.permit2DomainAppName = permit2DomainAppName;
        this.permit2ContractAddress = permit2ContractAddress;

        this.signature = signature;
        this.eventIdString = eventId;
        this.kind = kind;
        this.nip = nip;
        this.createdAt = createdAt;
        this.content = content;
        this.status = status;
        this.tradedAmount = tradedAmount;

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
        TokenTag token = new TokenTag(symbol, chain, network, tokenAddress, amount.stripTrailingZeros(), chainId, expireTime, tradedAmount);
        QuoteTag quote = new QuoteTag(price, quoteCurrency, quoteUsdRate, quoteDeadline, quoteSignature, quoteSlippageBP);
        EIP712Tag eip712Tag = new EIP712Tag(eip712WalletAddress, eip712DomainVersion, eip712DomainAppName, eip712ContractAddress, eip712Signature);
        LimitTag limit = new LimitTag(lowLimit.stripTrailingZeros(), upLimit.stripTrailingZeros());
        Permit2Tag permit2Tag = new Permit2Tag(permit2Nonce, permit2Sign, payer, permit2Spender, permit2WalletAddress, permit2ContractAddress, permit2DomainAppName);

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
