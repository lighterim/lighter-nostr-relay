package com.prosilion.superconductor.repository.join.classified;

import com.prosilion.superconductor.entity.join.classified.TradeMessageEntityLedgerTagEntity;
import com.prosilion.superconductor.entity.join.classified.TradeMessageEntityTlsnProofTagEntity;
import com.prosilion.superconductor.repository.join.TradeMessageEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

import static nostr.event.NIP77Event.TLSN_PROOF_TAG_CODE;

@Repository
public interface TradeMessageEntityTlsnProofTagEntityRepository<T extends TradeMessageEntityTlsnProofTagEntity> extends TradeMessageEntityAbstractTagEntityRepository<T> {

    default String getCode(){
        return TLSN_PROOF_TAG_CODE;
    }
}
