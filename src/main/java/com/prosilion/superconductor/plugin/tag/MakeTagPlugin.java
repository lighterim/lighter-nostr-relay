package com.prosilion.superconductor.plugin.tag;

import com.prosilion.superconductor.dto.classified.MakeTagDto;
import com.prosilion.superconductor.entity.standard.MakeTagEntity;
import com.prosilion.superconductor.entity.join.classified.EventEntityMakeTagEntity;
import com.prosilion.superconductor.repository.classified.MakeTagEntityRepository;
import com.prosilion.superconductor.repository.join.classified.EventEntitySideTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.MakeTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeTagPlugin<
    P extends MakeTag,
    Q extends MakeTagEntityRepository<R>,
    R extends MakeTagEntity,
    S extends EventEntityMakeTagEntity,
    T extends EventEntitySideTagEntityRepository<S>>
    implements TagPlugin<P, Q, R, S, T> {

  private final MakeTagEntityRepository<R> sideTagEntityRepository;
  private final EventEntitySideTagEntityRepository<S> join;

  @Autowired
  public MakeTagPlugin(@Nonnull MakeTagEntityRepository<R> sideTagEntityRepository, @NonNull EventEntitySideTagEntityRepository<S> join) {
    this.sideTagEntityRepository = sideTagEntityRepository;
    this.join = join;
  }

  @Override
  public String getCode() {
    return "side";
  }

  @Override
  public R convertDtoToEntity(P sideTag) {
    return (R) getTagDto(sideTag).convertDtoToEntity();
  }

  @Override
  public MakeTagDto getTagDto(P sideTag) {
    return new MakeTagDto(sideTag);
  }

  @Override
  public S getEventEntityTagEntity(Long eventId, Long sideTagId) {
    return (S) new EventEntityMakeTagEntity(eventId, sideTagId);
  }

  @Override
  public T getEventEntityStandardTagEntityRepositoryJoin() {
    return (T) join;
  }

  @Override
  public Q getStandardTagEntityRepository() {
    return (Q) sideTagEntityRepository;
  }
}
