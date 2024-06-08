package com.prosilion.superconductor.repository.join;

import com.prosilion.superconductor.entity.join.standard.EventEntityStandardTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface EventEntityStandardTagEntityRepository<T extends EventEntityStandardTagEntity> extends JpaRepository<T, Long> {
  List<T> getAllByEventId(Long eventId);
  Character getCode();
}