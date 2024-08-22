package com.prosilion.superconductor.dto;

import com.prosilion.superconductor.dto.classified.SideTagDto;
import com.prosilion.superconductor.entity.classified.SideTagEntity;
import com.prosilion.superconductor.entity.join.classified.EventEntitySideTagEntity;
import com.prosilion.superconductor.repository.classified.SideTagEntityRepository;
import com.prosilion.superconductor.repository.join.classified.EventEntitySideTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.SideTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SideTagPlugin<
    P extends SideTag,
    Q extends SideTagEntityRepository<R>,
    R extends SideTagEntity,
    S extends EventEntitySideTagEntity,
    T extends EventEntitySideTagEntityRepository<S>>
    implements TagPlugin<P, Q, R, S, T> {

  private final SideTagEntityRepository<R> sideTagEntityRepository;
  private final EventEntitySideTagEntityRepository<S> join;

  @Autowired
  public SideTagPlugin(@Nonnull SideTagEntityRepository<R> sideTagEntityRepository, @NonNull EventEntitySideTagEntityRepository<S> join) {
    this.sideTagEntityRepository = sideTagEntityRepository;
    this.join = join;
  }

  @Override
  public String getCode() {
    return "quote";
  }

  @Override
  public R convertDtoToEntity(P sideTag) {
    return (R) getTagDto(sideTag).convertDtoToEntity();
  }

  @Override
  public SideTagDto getTagDto(P sideTag) {
    return new SideTagDto(sideTag);
  }

  @Override
  public S getEventEntityTagEntity(Long eventId, Long sideTagId) {
    return (S) new EventEntitySideTagEntity(eventId, sideTagId);
  }

  @Override
  public T getEventEntityStandardTagEntityRepositoryJoin() {
    return (T) join;
  }

  @Override
  public Q getStandardTagEntityRepositoryRxR() {
    return (Q) sideTagEntityRepository;
  }
}
