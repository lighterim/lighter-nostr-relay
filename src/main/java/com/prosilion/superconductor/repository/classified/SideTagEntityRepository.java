package com.prosilion.superconductor.repository.classified;

import com.prosilion.superconductor.entity.classified.MakeTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SideTagEntityRepository<T extends MakeTagEntity> extends AbstractTagEntityRepository<T> {
}
