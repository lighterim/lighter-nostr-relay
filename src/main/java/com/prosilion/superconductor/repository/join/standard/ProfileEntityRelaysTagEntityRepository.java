package com.prosilion.superconductor.repository.join.standard;

import com.prosilion.superconductor.entity.join.classified.ProfileEntityRelaysTagEntity;
import com.prosilion.superconductor.repository.join.ProfileEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileEntityRelaysTagEntityRepository<T extends ProfileEntityRelaysTagEntity> extends ProfileEntityAbstractTagEntityRepository<T> {
    default String getCode() {
        return "relays";
    }
}