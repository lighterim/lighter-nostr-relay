package com.prosilion.superconductor.repository.classified;

import com.prosilion.superconductor.entity.standard.LedgerTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerTagEntityRepository<T extends LedgerTagEntity> extends AbstractTagEntityRepository<T> {
}
