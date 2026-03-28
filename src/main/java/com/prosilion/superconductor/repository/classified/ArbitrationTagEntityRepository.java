package com.prosilion.superconductor.repository.classified;

import com.prosilion.superconductor.entity.standard.ArbitrationTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArbitrationTagEntityRepository<T extends ArbitrationTagEntity> extends AbstractTagEntityRepository<T> {


}
