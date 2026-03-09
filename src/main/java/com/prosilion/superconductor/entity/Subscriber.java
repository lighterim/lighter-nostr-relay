package com.prosilion.superconductor.entity;

import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
public class Subscriber implements Serializable {
    /**
     * hash of the subscriber(subscriberId + sessionId)
     **/
    private Long subscriberSessionHash;

    private String subscriberId;
    private String sessionId;
    private boolean active;

    public Subscriber(@NonNull String subscriberId, @NonNull String sessionId, boolean active) {
        this.subscriberSessionHash = Hashing.murmur3_128().hashString(subscriberId.concat(sessionId), StandardCharsets.UTF_8).asLong();
        this.subscriberId = subscriberId;
        this.sessionId = sessionId;
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Subscriber that = (Subscriber) o;
        return Objects.equals(subscriberSessionHash, that.subscriberSessionHash) && Objects.equals(subscriberId, that.subscriberId) && Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriberSessionHash, subscriberId, sessionId);
    }
}
