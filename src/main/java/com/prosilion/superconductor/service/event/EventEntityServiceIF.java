package com.prosilion.superconductor.service.event;

import lombok.NonNull;
import nostr.event.Kind;
import nostr.event.impl.GenericEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public interface EventEntityServiceIF<T> {

    Kind getKind();

//    public void saveEventEntity(@NonNull GenericEvent event);
    Long saveEventEntity(@NonNull T event);

    public Map<Kind, Map<Long, T>> getAll();

    T getEventById(@NonNull Long id);

    T getEventByEventIdString(@NonNull String eventIdString);
}
