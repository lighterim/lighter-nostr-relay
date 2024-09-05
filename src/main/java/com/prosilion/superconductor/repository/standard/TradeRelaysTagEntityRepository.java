package com.prosilion.superconductor.repository.standard;

import com.prosilion.superconductor.entity.standard.IntentRelaysTagEntity;
import com.prosilion.superconductor.entity.standard.TradeRelaysTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRelaysTagEntityRepository<T extends TradeRelaysTagEntity> extends AbstractTagEntityRepository<T> {
}