package com.prosilion.superconductor.repository.join.standard;

import com.prosilion.superconductor.entity.join.classified.IntentEntityRelaysTagEntity;
import com.prosilion.superconductor.entity.join.classified.TradeMessageEntityRelaysTagEntity;
import com.prosilion.superconductor.repository.join.IntentEntityAbstractTagEntityRepository;
import com.prosilion.superconductor.repository.join.TradeMessageEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeMessageEntityRelaysTagEntityRepository<T extends TradeMessageEntityRelaysTagEntity> extends TradeMessageEntityAbstractTagEntityRepository<T> {
  default String getCode() {
    return "relays";
  }
}