package com.prosilion.superconductor.repository.join;

import com.prosilion.superconductor.entity.join.IntentEntityAbstractTagEntity;
import com.prosilion.superconductor.entity.join.TradeMessageEntityAbstractTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface TradeMessageEntityAbstractTagEntityRepository<T extends TradeMessageEntityAbstractTagEntity> extends JpaRepository<T, Long> {
  List<T> findByTradeMessageId(Long tradeMessageId);
  String getCode();
}