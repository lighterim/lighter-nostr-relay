package com.prosilion.superconductor.repository;

import com.prosilion.superconductor.entity.AccountMessageEntity;
import com.prosilion.superconductor.entity.RemarkMessageEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RemarkEventEntityRepository extends JpaRepository<RemarkMessageEntity, Long> {

    //  @Cacheable("events")
//  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @NotNull
    List<RemarkMessageEntity> findAll();

    //  @Cacheable("events")
//  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//    List<PostIntentEventEntity> findByContent(String content);

    Optional<RemarkMessageEntity> findById(Long id);

    Optional<RemarkMessageEntity> findByEventIdString(String eventIdString);
}
