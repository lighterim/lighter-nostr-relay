package com.prosilion.superconductor.plugin.tag;

import com.prosilion.superconductor.dto.classified.IntentRelaysTagDto;
import com.prosilion.superconductor.entity.join.classified.IntentEntityRelaysTagEntity;
import com.prosilion.superconductor.entity.standard.IntentRelaysTagEntity;
import com.prosilion.superconductor.repository.join.standard.IntentEntityRelaysTagEntityRepository;
import com.prosilion.superconductor.repository.standard.IntentRelaysTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.RelaysTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IntentRelaysTagPlugin<
    P extends RelaysTag,
    Q extends IntentRelaysTagEntityRepository<R>,
    R extends IntentRelaysTagEntity,
    S extends IntentEntityRelaysTagEntity,
    T extends IntentEntityRelaysTagEntityRepository<S>>
    implements IntentTagPlugin<P, Q, R, S, T> {

  private final IntentRelaysTagEntityRepository<R> relaysTagEntityRepository;
  private final IntentEntityRelaysTagEntityRepository<S> join;

  @Autowired
  public IntentRelaysTagPlugin(@Nonnull IntentRelaysTagEntityRepository<R> intentRelaysTagEntityRepository, @NonNull IntentEntityRelaysTagEntityRepository<S> join) {
    this.relaysTagEntityRepository = intentRelaysTagEntityRepository;
    this.join = join;
  }

  @Override
  public String getCode() {
    return "relays";
  }

  @Override
  public R convertDtoToEntity(P relaysTag) {
    return (R) getTagDto(relaysTag).convertDtoToEntity();
  }

  @Override
  public IntentRelaysTagDto getTagDto(P relaysTag) {
    return new IntentRelaysTagDto(relaysTag);
  }

  @Override
  public S getEventEntityTagEntity(Long eventId, Long relaysTagId) {
    return (S) new IntentEntityRelaysTagEntity(eventId, relaysTagId);
  }

  @Override
  public T getEventEntityStandardTagEntityRepositoryJoin() {
    return (T) join;
  }

  @Override
  public Q getStandardTagEntityRepository() {
    return (Q) relaysTagEntityRepository;
  }
}
