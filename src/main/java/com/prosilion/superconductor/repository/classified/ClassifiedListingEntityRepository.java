package com.prosilion.superconductor.repository.classified;

import com.prosilion.superconductor.entity.classified.ClassifiedListingEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassifiedListingEntityRepository extends JpaRepository<ClassifiedListingEventEntity, Long> {
}