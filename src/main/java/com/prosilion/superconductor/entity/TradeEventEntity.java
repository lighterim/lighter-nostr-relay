package com.prosilion.superconductor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "trade_event")
public class TradeEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventIdString;
    private String intentEventIdString;
    private String buyerId;
    private String buyerPubKey;
    private String sellerId;
    private String sellerPubKey;
    private String tokenAddr;
    private String symbol;
    private BigDecimal volume;
    private BigDecimal price;
    private String currency;
    private String taker;
    private String paymentMethod;
    private String paymentAccount;
    private String paymentQrCode;
    private String paymentMemo;
    private String signature;
    private Long createAt;

    public TradeEventEntity(String eventIdString,
                            String intentEventIdString,
                            String buyerId,
                            String buyerPubKey,
                            String sellerId,
                            String sellerPubKey,
                            String tokenAddr,
                            String symbol,
                            BigDecimal volume,
                            BigDecimal price,
                            String currency,
                            String taker,
                            String paymentMethod,
                            String paymentAccount,
                            String paymentQrCode,
                            String paymentMemo,
                            String signature,
                            Long createAt){
        this.eventIdString = eventIdString;
        this.intentEventIdString = intentEventIdString;
        this.buyerId = buyerId;
        this.buyerPubKey = buyerPubKey;
        this.sellerId = sellerId;
        this.sellerPubKey = sellerPubKey;
        this.tokenAddr = tokenAddr;
        this.symbol = symbol;
        this.volume = volume;
        this.price = price;
        this.currency = currency;
        this.taker = taker;
        this.paymentMethod = paymentMethod;
        this.paymentAccount = paymentAccount;
        this.paymentQrCode = paymentQrCode;
        this.paymentMemo = paymentMemo;
        this.signature = signature;
        this.createAt = createAt;
    }
}
