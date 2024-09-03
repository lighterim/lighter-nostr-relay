package com.prosilion.superconductor.repository.classified;

import com.prosilion.superconductor.entity.standard.MakeTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MakeTagEntityRepository<T extends MakeTagEntity> extends AbstractTagEntityRepository<T> {
}
