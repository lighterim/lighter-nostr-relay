package com.prosilion.superconductor.pubsub;

import lombok.NonNull;
import nostr.event.impl.GenericEvent;
import org.jetbrains.annotations.NotNull;

public record FireNostrEvent<T extends GenericEvent>(@NonNull Long subscriberId, @NonNull T event) {
}
