package com.prosilion.superconductor.repository.standard;

import com.prosilion.superconductor.entity.standard.IntentRelaysTagEntity;
import com.prosilion.superconductor.entity.standard.TradeMessageRelaysTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeMessageRelaysTagEntityRepository<T extends TradeMessageRelaysTagEntity> extends AbstractTagEntityRepository<T> {
}