package com.prosilion.superconductor.repository;

import com.prosilion.superconductor.entity.IntentEventEntity;
import com.prosilion.superconductor.entity.TakeIntentEventEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TakeEventEntityRepository extends JpaRepository<TakeIntentEventEntity, Long> {
    Optional<TakeIntentEventEntity> findByEventIdString(String eventIdString);

    @NotNull
    List<TakeIntentEventEntity> findAll(Specification<TakeIntentEventEntity> spec);
    List<TakeIntentEventEntity> findByStatusIn(Collection<String> statusList);
}
