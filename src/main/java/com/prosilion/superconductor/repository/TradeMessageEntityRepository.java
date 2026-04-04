package com.prosilion.superconductor.repository;

import com.prosilion.superconductor.entity.TradeMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeMessageEntityRepository extends JpaRepository<TradeMessageEntity, Long> {
    Optional<TradeMessageEntity> findByEventIdString(String eventIdString);

    List<TradeMessageEntity> findByTakeIntentEventIdIn(Collection<String> ids);
}
