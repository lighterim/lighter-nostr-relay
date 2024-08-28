package com.prosilion.superconductor.repository;

import com.prosilion.superconductor.entity.EventEntity;
import com.prosilion.superconductor.entity.TradeEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TradeEventEntityRepository extends JpaRepository<TradeEventEntity, Long> {
    Optional<TradeEventEntity> findByEventIdString(String eventIdString);

    @Override
    Optional<TradeEventEntity> findById(Long aLong);
}
