package com.prosilion.superconductor.repository.join.classified;

import com.prosilion.superconductor.entity.join.classified.EventEntityQuoteTagEntity;
import com.prosilion.superconductor.entity.join.classified.EventEntityTokenTagEntity;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventEntityTokenTagEntityRepository<T extends EventEntityTokenTagEntity> extends EventEntityAbstractTagEntityRepository<T> {

    default String getCode(){
        return "token";
    }
}
