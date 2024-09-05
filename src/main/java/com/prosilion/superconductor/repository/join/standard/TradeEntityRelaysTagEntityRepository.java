package com.prosilion.superconductor.repository.join.standard;

import com.prosilion.superconductor.entity.join.classified.TradeEntityRelaysTagEntity;
import com.prosilion.superconductor.repository.join.TradeEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeEntityRelaysTagEntityRepository<T extends TradeEntityRelaysTagEntity> extends TradeEntityAbstractTagEntityRepository<T> {
    default String getCode() {
        return "relays";
    }
}