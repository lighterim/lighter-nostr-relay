package com.prosilion.superconductor.entity.join.classified;

import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import com.prosilion.superconductor.entity.join.IntentEntityAbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "intent_relays_tag_join")
public class IntentEntityRelaysTagEntity extends IntentEntityAbstractTagEntity {
  private Long relaysId;

  public <T extends IntentEntityAbstractTagEntity> IntentEntityRelaysTagEntity(Long eventId, Long relaysId) {
    super.setIntentId(eventId);
    this.relaysId = relaysId;
  }
}