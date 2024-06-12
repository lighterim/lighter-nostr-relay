package com.prosilion.superconductor.entity.join.generic;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "generic_tag-element_attribute-join")
public class GenericTagEntityElementAttributeEntity implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long genericTagId;
  private Long elementAttributeId;

  public GenericTagEntityElementAttributeEntity(Long genericTagId, Long elementAttributeId) {
    this.genericTagId = genericTagId;
    this.elementAttributeId = elementAttributeId;
  }
}