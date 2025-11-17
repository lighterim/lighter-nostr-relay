package com.prosilion.superconductor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.Side;
import nostr.event.TradeStatus;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.TakeIntentEvent;
import nostr.event.tag.*;
import nostr.util.NostrUtil;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "trade")
public class TakeIntentEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String takeSide;
    private String makeIntentEventId;
    /** The volume means volume of trade symbol **/
    @Column(precision = 36, scale = 0)
    private BigDecimal volume;
    private String buyer;
    private String buyerPubKey;
    private String seller;
    private String sellerPubKey;
    private BigDecimal buyerFeeRate;
    private BigDecimal sellerFeeRate;
    private String payer;

    private String symbol;
    private String chain;
    private String network;
    private String tokenAddr;
    private BigInteger chainId;
    private String expireTime;

    /** The price for trade symbol based currency */
    @Column(precision = 36, scale = 4)
    private BigDecimal price;
    private String currency;
    private BigInteger quoteDeadline;
    private String quoteSignature;
    @Column(precision = 36, scale = 4)
    private BigDecimal usdRate;
    private Integer slippageBP;

    /** limit **/
    @Column(precision = 36, scale=0)
    private BigDecimal lowLimit;
    @Column(precision = 36, scale=0)
    private BigDecimal upLimit;

    private String paymentMethod;
    private String paymentAccount;
    private String paymentQrCode;
    private String paymentMemo;

    /** eip712 **/
    private String walletAddress;
    private String domainVersion;
    private String domainAppName;
    private String contractAddress;
    private String eip712Signature;

    /** permit2 **/
    private String nonce;
    private String permit2Sign;
    private String spender;
    private String permit2WalletAddress;
    private String permit2ContractAddress;
    private String permit2DomainAppName;

    /** trade key for participant and public key of current trade **/
    private String keyForBuyer;
    private String keyForSeller;
    private String keyForWitness;
    private String keyForSomeone;
    private String tradePubKey;

    @Column(nullable = false)
    private String status = TradeStatus.TakeEvent.getValue();

    private Integer kind;
    private Integer nip;
    private String eventIdString;
    private String content;
    private String signature;
    private Long createAt;

    /** other tag list. I.E. relays **/
    @Transient
    private List<BaseTag> tags;

    public TakeIntentEventEntity(
            Integer nip,
            Integer kind,
            String eventIdString,
            String takeSide,
            String makeIntentEventId,
         BigDecimal volume,
         String buyerId,
         String buyerPubKey,
         String sellerId,
         String sellerPubKey,
         BigDecimal sellerFeeRate,
         BigDecimal buyerFeeRate,
         String payer,
         Integer visibleStatus,
         String tokenAddr,
         String symbol,
         BigInteger chainId,
         String expireTime,
         String chain,
         String network,
         BigDecimal price,
         String currency,
         BigDecimal usdRate,
         Integer slippageBP,
         BigInteger timestamp,
         String quoteSignature,
         String paymentMethod,
         String paymentAccount,
         String paymentQrCode,
         String paymentMemo,
         String keyForBuyer,
         String keyForSeller,
         String keyForWitness,
         String keyForSomeone,
         String tradePubKey,
         String tradeStatus,
         String content,
         String signature,
         Long createAt,
            BigDecimal lowLimit,
            BigDecimal upLimit,
            String walletAddress,
            String domainVersion,
            String domainAppName,
            String contractAddress,
            String eip712Signature,
            String nonce,
            String permit2Sign,
            String spender,
            String permit2WalletAddress,
            String permit2ContractAddress,
            String permit2DomainAppName
            ){
        this.takeSide = takeSide;
        this.makeIntentEventId = makeIntentEventId;
        this.volume = volume;
        this.buyer = buyerId;
        this.buyerPubKey = buyerPubKey;
        this.seller = sellerId;
        this.sellerPubKey = sellerPubKey;
        this.sellerFeeRate = sellerFeeRate;
        this.buyerFeeRate = buyerFeeRate;
        this.payer = payer;

        this.symbol = symbol;
        this.chain = chain;
        this.network = network;
        this.tokenAddr = tokenAddr;
        this.chainId = chainId;
        this.expireTime = expireTime;

        this.price = price;
        this.currency = currency;
        this.quoteDeadline = timestamp;
        this.quoteSignature = quoteSignature;
        this.usdRate = usdRate;
        this.slippageBP = slippageBP;

        this.lowLimit = lowLimit;
        this.upLimit = upLimit;

        this.paymentMethod = paymentMethod;
        this.paymentAccount = paymentAccount;
        this.paymentQrCode = paymentQrCode;
        this.paymentMemo = paymentMemo;

        this.walletAddress = walletAddress;
        this.domainVersion = domainVersion;
        this.domainAppName = domainAppName;
        this.contractAddress = contractAddress;
        this.eip712Signature = eip712Signature;

        this.nonce = nonce;
        this.permit2Sign = permit2Sign;
        this.spender = spender;
        this.permit2ContractAddress = permit2ContractAddress;
        this.permit2DomainAppName = permit2DomainAppName;
        this.permit2WalletAddress = permit2WalletAddress;

        this.keyForBuyer = keyForBuyer;
        this.keyForSeller = keyForSeller;
        this.keyForWitness = keyForWitness;
        this.keyForSomeone = keyForSomeone;
        this.tradePubKey = tradePubKey;
        if(StringUtils.hasText(tradeStatus)) {
            this.status = tradeStatus;
        }
        this.kind = kind;
        this.nip = nip;
        this.eventIdString = eventIdString;
        this.content = content;
        this.signature = signature;
        this.createAt = createAt;
    }

    public <T extends GenericEvent> T convertEntityToDto(){
        byte[] rawData = NostrUtil.hexToBytes(signature);
        final Signature sig = new Signature();
        sig.setRawData(rawData);

        Side side = Side.valueOf(this.takeSide.toUpperCase());
        TakeIntentEvent takeEvent = null;
        switch (side){
            case BUY -> takeEvent = new TakeIntentEvent(
                    id,
                    new PublicKey(buyerPubKey),
                    nip,
                    List.of(
                            new TakeTag(side, makeIntentEventId, seller, sellerPubKey, volume.stripTrailingZeros(), buyer, buyerPubKey, sellerFeeRate, buyerFeeRate, payer, 1),
                            new TokenTag(symbol, chain, network, tokenAddr, BigDecimal.ZERO.stripTrailingZeros(), chainId, expireTime, null),
                            new QuoteTag(price.stripTrailingZeros(), currency, usdRate.stripTrailingZeros(), quoteDeadline, quoteSignature, slippageBP),
                            new PaymentTag(paymentMethod, paymentAccount, paymentQrCode, paymentMemo),
                            new LimitTag(lowLimit.stripTrailingZeros(), upLimit.stripTrailingZeros()),
                            new EIP712Tag(walletAddress, domainVersion, domainAppName, contractAddress, eip712Signature),
                            new Permit2Tag(nonce, permit2Sign, payer, spender, permit2WalletAddress, permit2ContractAddress, permit2DomainAppName),
                            new TradeKeyTag(keyForBuyer, keyForSeller, keyForWitness, keyForSomeone, tradePubKey)
                    ),
                    eventIdString,
                    content,
                    TradeStatus.forValue(status),
                    createAt
            );
            case SELL -> takeEvent = new TakeIntentEvent(
                    id,
                    new PublicKey(sellerPubKey),
                    nip,
                    List.of(
                            new TakeTag(side, makeIntentEventId, buyer, buyerPubKey, volume.stripTrailingZeros(), seller, sellerPubKey, sellerFeeRate, buyerFeeRate, payer, 1),
                            new TokenTag(symbol, chain, network, tokenAddr, BigDecimal.ZERO.stripTrailingZeros(), chainId, expireTime, null),
                            new QuoteTag(price.stripTrailingZeros(), currency, usdRate.stripTrailingZeros(), quoteDeadline, quoteSignature, slippageBP),
                            new PaymentTag(paymentMethod, paymentAccount, paymentQrCode, paymentMemo),
                            new LimitTag(lowLimit.stripTrailingZeros(), upLimit.stripTrailingZeros()),
                            new EIP712Tag(walletAddress, domainVersion, domainAppName, contractAddress, eip712Signature),
                            new Permit2Tag(nonce, permit2Sign, payer, spender, permit2WalletAddress, permit2ContractAddress, permit2DomainAppName),
                            new TradeKeyTag(keyForBuyer, keyForSeller, keyForWitness, keyForSomeone, tradePubKey)
                    ),
                    eventIdString,
                    content,
                    TradeStatus.forValue(status),
                    createAt
            );
        }

        return (T)takeEvent;
    }
}
