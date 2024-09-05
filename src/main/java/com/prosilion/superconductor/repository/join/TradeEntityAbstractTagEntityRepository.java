package com.prosilion.superconductor.repository.join;

import com.prosilion.superconductor.entity.join.TradeEntityAbstractTagEntity;
import com.prosilion.superconductor.entity.join.TradeMessageEntityAbstractTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface TradeEntityAbstractTagEntityRepository<T extends TradeEntityAbstractTagEntity> extends JpaRepository<T, Long> {
  List<T> findByTradeId(Long tradeId);
  String getCode();
}