package com.prosilion.superconductor.repository.classified;

import com.prosilion.superconductor.entity.standard.ProfilePaymentTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfilePaymentTagEntityRepository<T extends ProfilePaymentTagEntity> extends AbstractTagEntityRepository<T> {
}
