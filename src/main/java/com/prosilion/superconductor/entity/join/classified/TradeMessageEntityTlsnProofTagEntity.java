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
@Table(name = "trade_message_tlsn_proof_tag_join")
public class TradeMessageEntityTlsnProofTagEntity extends TradeMessageEntityAbstractTagEntity {

    private Long tlsnProofId;

    public <T extends EventEntityAbstractTagEntity> TradeMessageEntityTlsnProofTagEntity(Long tradeMessageId, Long tlsnProofId){
        super.setTradeMessageId(tradeMessageId);
        this.tlsnProofId = tlsnProofId;
    }

}
