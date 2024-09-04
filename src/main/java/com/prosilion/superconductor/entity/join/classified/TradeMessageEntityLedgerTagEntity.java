package com.prosilion.superconductor.entity.join.classified;

import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "trade_message_ledger_tag_join")
public class TradeMessageEntityLedgerTagEntity extends EventEntityAbstractTagEntity {

    private Long ledgerTagId;

    public <T extends EventEntityAbstractTagEntity> TradeMessageEntityLedgerTagEntity(Long eventId, Long ledgerTagId){
        super.setEventId(eventId);
        this.ledgerTagId = ledgerTagId;
    }

}
