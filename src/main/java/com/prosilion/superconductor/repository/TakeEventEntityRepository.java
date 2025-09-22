package com.prosilion.superconductor.repository;

import com.prosilion.superconductor.entity.TakeIntentEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TakeEventEntityRepository extends JpaRepository<TakeIntentEventEntity, Long> {
    Optional<TakeIntentEventEntity> findByEventIdString(String eventIdString);

}
