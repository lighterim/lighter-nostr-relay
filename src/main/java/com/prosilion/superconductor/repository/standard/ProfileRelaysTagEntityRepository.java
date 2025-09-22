package com.prosilion.superconductor.repository.standard;

import com.prosilion.superconductor.entity.standard.IntentRelaysTagEntity;
import com.prosilion.superconductor.entity.standard.ProfileRelaysTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRelaysTagEntityRepository<T extends ProfileRelaysTagEntity> extends AbstractTagEntityRepository<T> {
}