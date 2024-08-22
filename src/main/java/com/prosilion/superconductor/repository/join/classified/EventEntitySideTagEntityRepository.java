package com.prosilion.superconductor.repository.join.classified;

import com.prosilion.superconductor.entity.join.classified.EventEntityQuoteTagEntity;
import com.prosilion.superconductor.entity.join.classified.EventEntitySideTagEntity;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventEntitySideTagEntityRepository<T extends EventEntitySideTagEntity> extends EventEntityAbstractTagEntityRepository<T> {

    default String getCode(){
        return "side";
    }
}
