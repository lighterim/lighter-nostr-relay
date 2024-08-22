package com.prosilion.superconductor.repository.classified;

import com.prosilion.superconductor.entity.classified.PaymentTagEntity;
import com.prosilion.superconductor.entity.classified.QuoteTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTagEntityRepository<T extends PaymentTagEntity> extends AbstractTagEntityRepository<T> {
}
