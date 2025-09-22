package com.prosilion.superconductor.repository.join;

import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import com.prosilion.superconductor.entity.join.IntentEntityAbstractTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface IntentEntityAbstractTagEntityRepository<T extends IntentEntityAbstractTagEntity> extends JpaRepository<T, Long> {
  List<T> findByIntentId(Long eventId);
  String getCode();
}