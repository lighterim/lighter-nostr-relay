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
    private Integer kind;
    private Integer nip;
    private String eventIdString;

    private String takeSide;
    private String makeIntentEventId;
    /** The volume means volume of trade symbol **/
    @Column(precision = 36, scale = 18)
    private BigDecimal volume;
    private String buyerId;
    private String buyerPubKey;
    private String sellerId;
    private String sellerPubKey;
    private BigDecimal buyerFeeRate;
    private BigDecimal sellerFeeRate;

    private String chain;
    private String network;
    private String tokenAddr;
    private String symbol;
    private BigInteger chainId;
    private String expireTime;

    /** The price for trade symbol based currency */
    @Column(precision = 18, scale = 9)
    private BigDecimal price;
    private String currency;
    @Column(precision = 18, scale = 9)
    private BigDecimal usdRate;
    private String timestamp;
    private String quoteSignature;

    private String paymentMethod;
    private String paymentAccount;
    private String paymentQrCode;
    private String paymentMemo;

    /** trade key for participant and public key of current trade **/
    private String keyForBuyer;
    private String keyForSeller;
    private String keyForWitness;
    private String keyForSomeone;
    private String tradePubKey;

    @Column(nullable = false)
    private String status = TradeStatus.TakeEvent.getValue();

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
         String tokenAddr,
         String symbol,
         BigInteger chainId,
         String expireTime,
         String chain,
         String network,
         BigDecimal price,
         String currency,
         BigDecimal usdRate,
         String timestamp,
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
         Long createAt){
        this.kind = kind;
        this.nip = nip;
        this.eventIdString = eventIdString;
        this.takeSide = takeSide;
        this.makeIntentEventId = makeIntentEventId;
        this.volume = volume;
        this.buyerId = buyerId;
        this.buyerPubKey = buyerPubKey;
        this.sellerId = sellerId;
        this.sellerPubKey = sellerPubKey;
        this.sellerFeeRate = sellerFeeRate;
        this.buyerFeeRate = buyerFeeRate;
        this.chain = chain;
        this.chainId = chainId;
        this.expireTime = expireTime;
        this.network = network;
        this.tokenAddr = tokenAddr;
        this.symbol = symbol;
        this.price = price;
        this.currency = currency;
        this.usdRate = usdRate;
        this.timestamp = timestamp;
        this.quoteSignature = quoteSignature;
        this.paymentMethod = paymentMethod;
        this.paymentAccount = paymentAccount;
        this.paymentQrCode = paymentQrCode;
        this.paymentMemo = paymentMemo;
        this.keyForBuyer = keyForBuyer;
        this.keyForSeller = keyForSeller;
        this.keyForWitness = keyForWitness;
        this.keyForSomeone = keyForSomeone;
        this.tradePubKey = tradePubKey;
        if(StringUtils.hasText(tradeStatus)) {
            this.status = tradeStatus;
        }
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
                            new TakeTag(side, makeIntentEventId, sellerId, sellerPubKey, volume.stripTrailingZeros(), buyerId, buyerPubKey, sellerFeeRate, buyerFeeRate),
                            new TokenTag(symbol, chain, network, tokenAddr, BigDecimal.ZERO.stripTrailingZeros(), chainId, expireTime),
                            new QuoteTag(price.stripTrailingZeros(), currency, usdRate.stripTrailingZeros(), timestamp, quoteSignature),
                            new PaymentTag(paymentMethod, paymentAccount, paymentQrCode, paymentMemo),
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
                            new TakeTag(side, makeIntentEventId, buyerId, buyerPubKey, volume.stripTrailingZeros(), sellerId, sellerPubKey, sellerFeeRate, buyerFeeRate),
                            new TokenTag(symbol, chain, network, tokenAddr, BigDecimal.ZERO.stripTrailingZeros(), chainId, expireTime),
                            new QuoteTag(price.stripTrailingZeros(), currency, usdRate.stripTrailingZeros(), timestamp, quoteSignature),
                            new PaymentTag(paymentMethod, paymentAccount, paymentQrCode, paymentMemo),
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
