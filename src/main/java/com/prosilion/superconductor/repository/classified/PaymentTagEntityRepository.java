package com.prosilion.superconductor.repository.classified;

import com.prosilion.superconductor.entity.standard.IntentPaymentTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTagEntityRepository<T extends IntentPaymentTagEntity> extends AbstractTagEntityRepository<T> {
}
