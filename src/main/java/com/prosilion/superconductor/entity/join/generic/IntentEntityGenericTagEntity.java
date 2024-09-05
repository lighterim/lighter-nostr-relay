package com.prosilion.superconductor.entity.join.generic;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "intent_generic_tag_join")
public class IntentEntityGenericTagEntity implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long intentId;
  private Long genericTagId;

  public IntentEntityGenericTagEntity(Long intentId, Long genericTagId) {
    this.intentId = intentId;
    this.genericTagId = genericTagId;
  }
}