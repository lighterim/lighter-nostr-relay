package com.prosilion.superconductor.repository.join;

import com.prosilion.superconductor.entity.join.IntentEntityAbstractTagEntity;
import com.prosilion.superconductor.entity.join.ProfileEntityAbstractTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface ProfileEntityAbstractTagEntityRepository<T extends ProfileEntityAbstractTagEntity> extends JpaRepository<T, Long> {
  List<T> findByProfileId(Long eventId);
  String getCode();
}