package com.prosilion.superconductor.entity.join.classified;

import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import com.prosilion.superconductor.entity.join.TradeMessageEntityAbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "trade_message_arbitration_tag_join")
public class TradeMessageEntityArbitrationTagEntity extends TradeMessageEntityAbstractTagEntity {

    private Long arbitrationTagId;

    public <T extends EventEntityAbstractTagEntity> TradeMessageEntityArbitrationTagEntity(Long tradeMessageId, Long arbitrationTagId){
        super.setTradeMessageId(tradeMessageId);
        this.arbitrationTagId = arbitrationTagId;
    }

}
