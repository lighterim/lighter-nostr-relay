package com.prosilion.superconductor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "trade_message")
public class TradeMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventIdString;
    private String tradeEventId;
    private String userId;
    private String pubKey;
    @Lob
    private String content;
    private String signature;
    private Long createAt;
    public TradeMessageEntity(String eventIdString, String tradeEventId, String userId, String pubKey, String content, String signature, Long createAt){
        this.eventIdString = eventIdString;
        this.tradeEventId = tradeEventId;
        this.userId = userId;
        this.pubKey = pubKey;
        this.content = content;
        this.signature = signature;
        this.createAt = createAt;
    }
}
