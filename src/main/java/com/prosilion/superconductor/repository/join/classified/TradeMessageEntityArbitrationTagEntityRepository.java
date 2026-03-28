package com.prosilion.superconductor.repository.join.classified;

import com.prosilion.superconductor.entity.join.classified.TradeMessageEntityArbitrationTagEntity;
import com.prosilion.superconductor.repository.join.TradeMessageEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

import static nostr.event.NIP77Event.ARBITRATION_TAG_CODE;

@Repository
public interface TradeMessageEntityArbitrationTagEntityRepository<T extends TradeMessageEntityArbitrationTagEntity> extends TradeMessageEntityAbstractTagEntityRepository<T> {

    default String getCode(){
        return ARBITRATION_TAG_CODE;
    }
}
