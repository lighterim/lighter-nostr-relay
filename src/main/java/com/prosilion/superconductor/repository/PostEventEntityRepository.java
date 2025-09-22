package com.prosilion.superconductor.repository;

import com.prosilion.superconductor.entity.IntentEventEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostEventEntityRepository extends JpaRepository<IntentEventEntity, Long> {

    //  @Cacheable("events")
//  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @NotNull
    List<IntentEventEntity> findAll();

    //  @Cacheable("events")
//  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//    List<PostIntentEventEntity> findByContent(String content);

    Optional<IntentEventEntity> findByEventIdString(String eventIdString);

    Optional<IntentEventEntity> findById(Long id);
}
