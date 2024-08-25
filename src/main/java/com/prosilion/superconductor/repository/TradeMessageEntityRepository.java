package com.prosilion.superconductor.repository;

import com.prosilion.superconductor.entity.TradeMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeMessageEntityRepository extends JpaRepository<TradeMessageEntity, Long> {
}
