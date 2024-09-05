package com.prosilion.superconductor.entity.join.classified;

import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import com.prosilion.superconductor.entity.join.IntentEntityAbstractTagEntity;
import com.prosilion.superconductor.entity.join.TradeMessageEntityAbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "trade_message_relays_tag_join")
public class TradeMessageEntityRelaysTagEntity extends TradeMessageEntityAbstractTagEntity {
  private Long relaysId;

  public <T extends EventEntityAbstractTagEntity> TradeMessageEntityRelaysTagEntity(Long tradeMessageId, Long relaysId) {
    super.setTradeMessageId(tradeMessageId);
    this.relaysId = relaysId;
  }
}