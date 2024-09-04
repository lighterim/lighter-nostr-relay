package com.prosilion.superconductor.repository.join.classified;

import com.prosilion.superconductor.entity.join.classified.EventEntityTokenTagEntity;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

import static nostr.event.NIP77Event.TOKEN_TAG_CODE;

@Repository
public interface EventEntityTokenTagEntityRepository<T extends EventEntityTokenTagEntity> extends EventEntityAbstractTagEntityRepository<T> {

    default String getCode(){
        return TOKEN_TAG_CODE;
    }
}
