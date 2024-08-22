package com.prosilion.superconductor.repository.classified;

import com.prosilion.superconductor.entity.classified.QuoteTagEntity;
import com.prosilion.superconductor.entity.classified.SideTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SideTagEntityRepository<T extends SideTagEntity> extends AbstractTagEntityRepository<T> {
}
