package com.prosilion.superconductor.entity.standard;

import com.prosilion.superconductor.dto.standard.EventTagDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.event.BaseTag;
import nostr.event.Marker;
import nostr.event.tag.EventTag;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "event_tag")
public class EventTagEntity extends StandardTagEntityRxR {
  //  TODO: below annotations and id necessary for compilation even thuogh same is defined in StandardTagEntity
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String idEvent;
  private Marker marker;
  private String recommendedRelayUrl;

  public EventTagEntity(@NonNull EventTag eventTag) {
    this.idEvent = eventTag.getIdEvent();
    this.marker = eventTag.getMarker();
    this.recommendedRelayUrl = eventTag.getRecommendedRelayUrl();
  }

  public EventTagEntity(@NonNull String idEvent, Marker marker, String recommendedRelayUrl) {
    this.idEvent = idEvent;
    this.marker = marker;
    this.recommendedRelayUrl = recommendedRelayUrl;
  }

  @Override
  public String getCode() {
    return "e";
  }

  @Override
  public BaseTag getAsBaseTag() {
    return new EventTag(idEvent, recommendedRelayUrl, marker);
  }

  public EventTagDto convertEntityToDto() {
    return new EventTagDto(new EventTag(idEvent, recommendedRelayUrl, marker));
  }
}
