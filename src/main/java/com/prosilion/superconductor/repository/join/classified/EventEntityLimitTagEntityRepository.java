package com.prosilion.superconductor.repository.join.classified;

import com.prosilion.superconductor.entity.join.classified.EventEntityLimitTagEntity;
import com.prosilion.superconductor.entity.join.classified.EventEntityQuoteTagEntity;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventEntityLimitTagEntityRepository<T extends EventEntityLimitTagEntity> extends EventEntityAbstractTagEntityRepository<T> {

    default String getCode(){
        return "limit";
    }
}
