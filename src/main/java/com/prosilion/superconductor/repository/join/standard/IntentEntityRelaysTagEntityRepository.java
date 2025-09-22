package com.prosilion.superconductor.repository.join.standard;

import com.prosilion.superconductor.entity.join.classified.IntentEntityRelaysTagEntity;
import com.prosilion.superconductor.repository.join.IntentEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntentEntityRelaysTagEntityRepository<T extends IntentEntityRelaysTagEntity> extends IntentEntityAbstractTagEntityRepository<T> {
  default String getCode() {
    return "relays";
  }
}