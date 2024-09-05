package com.prosilion.superconductor.repository.standard;

import com.prosilion.superconductor.entity.standard.IntentRelaysTagEntity;
import com.prosilion.superconductor.entity.standard.RelaysTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntentRelaysTagEntityRepository<T extends IntentRelaysTagEntity> extends AbstractTagEntityRepository<T> {
}