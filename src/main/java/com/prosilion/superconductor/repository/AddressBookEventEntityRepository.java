package com.prosilion.superconductor.repository;

import com.prosilion.superconductor.entity.AddressBookMessageEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressBookEventEntityRepository extends JpaRepository<AddressBookMessageEntity, Long> {

    //  @Cacheable("events")
//  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @NotNull
    List<AddressBookMessageEntity> findAll();

    //  @Cacheable("events")
//  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//    List<PostIntentEventEntity> findByContent(String content);

    Optional<AddressBookMessageEntity> findById(Long id);

    Optional<AddressBookMessageEntity> findByEventIdString(String eventIdString);
}
