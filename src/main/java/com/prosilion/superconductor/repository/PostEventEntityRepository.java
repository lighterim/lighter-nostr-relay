package com.prosilion.superconductor.repository;

import com.prosilion.superconductor.entity.EventEntity;
import com.prosilion.superconductor.entity.PostIntentEventEntity;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostEventEntityRepository extends JpaRepository<PostIntentEventEntity, Long> {

    //  @Cacheable("events")
//  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @NotNull
    List<PostIntentEventEntity> findAll();

    //  @Cacheable("events")
//  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//    List<PostIntentEventEntity> findByContent(String content);

    Optional<PostIntentEventEntity> findByEventIdString(String eventIdString);

    Optional<PostIntentEventEntity> findById(Long id);
}
