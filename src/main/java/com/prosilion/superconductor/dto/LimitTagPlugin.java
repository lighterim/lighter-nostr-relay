package com.prosilion.superconductor.dto;

import com.prosilion.superconductor.dto.classified.LimitTagDto;
import com.prosilion.superconductor.entity.classified.LimitTagEntity;
import com.prosilion.superconductor.entity.join.classified.EventEntityLimitTagEntity;
import com.prosilion.superconductor.repository.classified.LimitTagEntityRepository;
import com.prosilion.superconductor.repository.join.classified.EventEntityLimitTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.LimitTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LimitTagPlugin<
    P extends LimitTag,
    Q extends LimitTagEntityRepository<R>,
    R extends LimitTagEntity,
    S extends EventEntityLimitTagEntity,
    T extends EventEntityLimitTagEntityRepository<S>>
    implements TagPlugin<P, Q, R, S, T> {

  private final LimitTagEntityRepository<R> limitTagEntityRepository;
  private final EventEntityLimitTagEntityRepository<S> join;

  @Autowired
  public LimitTagPlugin(@Nonnull LimitTagEntityRepository<R> limitTagEntityRepository, @NonNull EventEntityLimitTagEntityRepository<S> join) {
    this.limitTagEntityRepository = limitTagEntityRepository;
    this.join = join;
  }

  @Override
  public String getCode() {
    return "limit";
  }

  @Override
  public R convertDtoToEntity(P limitTag) {
    return (R) getTagDto(limitTag).convertDtoToEntity();
  }

  @Override
  public LimitTagDto getTagDto(P limitTag) {
    return new LimitTagDto(limitTag);
  }

  @Override
  public S getEventEntityTagEntity(Long eventId, Long limitTagId) {
    return (S) new EventEntityLimitTagEntity(eventId, limitTagId);
  }

  @Override
  public T getEventEntityStandardTagEntityRepositoryJoin() {
    return (T) join;
  }

  @Override
  public Q getStandardTagEntityRepositoryRxR() {
    return (Q) limitTagEntityRepository;
  }
}
