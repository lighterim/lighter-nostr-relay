package com.prosilion.superconductor.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
public class Subscriber implements Serializable {
  private Long subscriberSessionHash;

  private String subscriberId;
  private String subscriptionId;
  private String sessionId;
  private boolean active;

  public Subscriber(@NonNull String subscriberId, @NonNull String subscriptionId, @NonNull String sessionId, boolean active) {
    this.subscriberId = subscriberId;
    this.subscriptionId = subscriptionId;
    this.sessionId = sessionId;
    this.active = active;
  }
}
