package com.prosilion.superconductor.repository.classified;

import com.prosilion.superconductor.entity.standard.LimitTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LimitTagEntityRepository<T extends LimitTagEntity> extends AbstractTagEntityRepository<T> {
}
