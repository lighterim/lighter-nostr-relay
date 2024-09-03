package com.prosilion.superconductor.repository.classified;

import com.prosilion.superconductor.entity.standard.QuoteTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteTagEntityRepository<T extends QuoteTagEntity> extends AbstractTagEntityRepository<T> {
}
