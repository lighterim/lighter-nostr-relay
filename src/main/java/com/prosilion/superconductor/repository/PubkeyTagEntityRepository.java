package com.prosilion.superconductor.repository;

import com.prosilion.superconductor.entity.standard.PubkeyTagEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface PubkeyTagEntityRepository<T extends PubkeyTagEntity> extends StandardTagEntityRepository<T> {
  default Character getCode() {
    return 'p';
  }
}