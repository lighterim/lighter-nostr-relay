package com.prosilion.superconductor.repository.join.classified;

import com.prosilion.superconductor.entity.join.classified.IntentEntityQuoteTagEntity;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

import static nostr.event.NIP77Event.QUOTE_TAG_CODE;

@Repository
public interface EventEntityQuoteTagEntityRepository<T extends IntentEntityQuoteTagEntity> extends EventEntityAbstractTagEntityRepository<T> {

    default String getCode(){
        return QUOTE_TAG_CODE;
    }
}
