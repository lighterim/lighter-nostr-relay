package com.prosilion.superconductor.entity.join.classified;

import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import com.prosilion.superconductor.entity.join.IntentEntityAbstractTagEntity;
import com.prosilion.superconductor.entity.join.TradeEntityAbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "trade_relays_tag_join")
public class TradeEntityRelaysTagEntity extends TradeEntityAbstractTagEntity {
  private Long relaysId;

  public <T extends TradeEntityAbstractTagEntity> TradeEntityRelaysTagEntity(Long tradeId, Long relaysId) {
    super.setTradeId(tradeId);
    this.relaysId = relaysId;
  }
}