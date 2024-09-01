package com.prosilion.superconductor.repository.join.classified;

import com.prosilion.superconductor.entity.join.classified.EventEntityMakeTagEntity;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventEntitySideTagEntityRepository<T extends EventEntityMakeTagEntity> extends EventEntityAbstractTagEntityRepository<T> {

    default String getCode(){
        return "side";
    }
}
