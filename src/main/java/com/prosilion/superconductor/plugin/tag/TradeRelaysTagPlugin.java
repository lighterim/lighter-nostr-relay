package com.prosilion.superconductor.plugin.tag;

import com.prosilion.superconductor.dto.classified.TradeRelaysTagDto;
import com.prosilion.superconductor.entity.join.classified.TradeEntityRelaysTagEntity;
import com.prosilion.superconductor.entity.standard.TradeRelaysTagEntity;
import com.prosilion.superconductor.repository.join.standard.TradeEntityRelaysTagEntityRepository;
import com.prosilion.superconductor.repository.standard.TradeRelaysTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.RelaysTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TradeRelaysTagPlugin<
    P extends RelaysTag,
    Q extends TradeRelaysTagEntityRepository<R>,
    R extends TradeRelaysTagEntity,
    S extends TradeEntityRelaysTagEntity,
    T extends TradeEntityRelaysTagEntityRepository<S>>
    implements TradeTagPlugin<P, Q, R, S, T> {

  private final TradeRelaysTagEntityRepository<R> relaysTagEntityRepository;
  private final TradeEntityRelaysTagEntityRepository<S> join;

  @Autowired
  public TradeRelaysTagPlugin(@Nonnull TradeRelaysTagEntityRepository<R> intentRelaysTagEntityRepository, @NonNull TradeEntityRelaysTagEntityRepository<S> join) {
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
  public TradeRelaysTagDto getTagDto(P relaysTag) {
    return new TradeRelaysTagDto(relaysTag);
  }

  @Override
  public S getEventEntityTagEntity(Long eventId, Long relaysTagId) {
    return (S) new TradeEntityRelaysTagEntity(eventId, relaysTagId);
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
