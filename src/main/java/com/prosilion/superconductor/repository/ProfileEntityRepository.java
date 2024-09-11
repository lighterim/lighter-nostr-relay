package com.prosilion.superconductor.repository;

import com.prosilion.superconductor.entity.ProfileEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileEntityRepository extends JpaRepository<ProfileEntity, Long> {

    //  @Cacheable("events")
//  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @NotNull
    List<ProfileEntity> findAll();

    //  @Cacheable("events")
//  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//    List<PostIntentEventEntity> findByContent(String content);

    Optional<ProfileEntity> findByEventIdString(String eventIdString);

    Optional<ProfileEntity> findByNip05(String nip05);

    Optional<ProfileEntity> findById(Long id);
}
