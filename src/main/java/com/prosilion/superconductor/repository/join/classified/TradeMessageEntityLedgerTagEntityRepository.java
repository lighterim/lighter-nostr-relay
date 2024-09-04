package com.prosilion.superconductor.repository.join.classified;

import com.prosilion.superconductor.entity.join.classified.TradeMessageEntityLedgerTagEntity;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

import static nostr.event.NIP77Event.LEDGER_TAG_CODE;

@Repository
public interface TradeMessageEntityLedgerTagEntityRepository<T extends TradeMessageEntityLedgerTagEntity> extends EventEntityAbstractTagEntityRepository<T> {

    default String getCode(){
        return LEDGER_TAG_CODE;
    }
}
