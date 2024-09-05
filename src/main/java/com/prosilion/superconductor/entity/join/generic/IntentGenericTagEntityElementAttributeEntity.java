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
@Table(name = "intent_generic_tag_element_attribute_join")
public class IntentGenericTagEntityElementAttributeEntity implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long genericTagId;
  private Long elementAttributeId;

  public IntentGenericTagEntityElementAttributeEntity(Long genericTagId, Long elementAttributeId) {
    this.genericTagId = genericTagId;
    this.elementAttributeId = elementAttributeId;
  }
}