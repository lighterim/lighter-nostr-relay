package com.prosilion.superconductor.repository.classified;

import com.prosilion.superconductor.entity.standard.TokenTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenTagEntityRepository<T extends TokenTagEntity> extends AbstractTagEntityRepository<T> {
}
