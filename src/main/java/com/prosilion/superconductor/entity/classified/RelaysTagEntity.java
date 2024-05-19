package com.prosilion.superconductor.entity.classified;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nostr.event.tag.RelaysTag;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "relays_tag")
public class RelaysTagEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String uri;

  public RelaysTag convertEntityToDto() {
    return RelaysTag.builder().relayUris(List.of(uri)).build();
  }
}
