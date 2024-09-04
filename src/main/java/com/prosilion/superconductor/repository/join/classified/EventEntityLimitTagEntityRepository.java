package com.prosilion.superconductor.repository.join.classified;

import com.prosilion.superconductor.entity.join.classified.EventEntityLimitTagEntity;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

import static nostr.event.NIP77Event.LIMIT_TAG_CODE;

@Repository
public interface EventEntityLimitTagEntityRepository<T extends EventEntityLimitTagEntity> extends EventEntityAbstractTagEntityRepository<T> {

    default String getCode(){
        return LIMIT_TAG_CODE;
    }
}
