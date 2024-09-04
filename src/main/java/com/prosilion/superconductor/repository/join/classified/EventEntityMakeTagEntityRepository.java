package com.prosilion.superconductor.repository.join.classified;

import com.prosilion.superconductor.entity.join.classified.EventEntityMakeTagEntity;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

import static nostr.event.NIP77Event.MAKE_TAG_CODE;

@Repository
public interface EventEntityMakeTagEntityRepository<T extends EventEntityMakeTagEntity> extends EventEntityAbstractTagEntityRepository<T> {

    default String getCode(){
        return MAKE_TAG_CODE;
    }
}
