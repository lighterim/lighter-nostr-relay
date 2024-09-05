package com.prosilion.superconductor.plugin.tag;

import com.prosilion.superconductor.dto.classified.IntentRelaysTagDto;
import com.prosilion.superconductor.dto.classified.TradeMessageRelaysTagDto;
import com.prosilion.superconductor.entity.join.classified.IntentEntityRelaysTagEntity;
import com.prosilion.superconductor.entity.join.classified.TradeMessageEntityRelaysTagEntity;
import com.prosilion.superconductor.entity.standard.IntentRelaysTagEntity;
import com.prosilion.superconductor.entity.standard.TradeMessageRelaysTagEntity;
import com.prosilion.superconductor.repository.join.standard.IntentEntityRelaysTagEntityRepository;
import com.prosilion.superconductor.repository.join.standard.TradeMessageEntityRelaysTagEntityRepository;
import com.prosilion.superconductor.repository.standard.IntentRelaysTagEntityRepository;
import com.prosilion.superconductor.repository.standard.TradeMessageRelaysTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.RelaysTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TradeMessageRelaysTagPlugin<
    P extends RelaysTag,
    Q extends TradeMessageRelaysTagEntityRepository<R>,
    R extends TradeMessageRelaysTagEntity,
    S extends TradeMessageEntityRelaysTagEntity,
    T extends TradeMessageEntityRelaysTagEntityRepository<S>>
    implements TradeMessageTagPlugin<P, Q, R, S, T> {

  private final TradeMessageRelaysTagEntityRepository<R> relaysTagEntityRepository;
  private final TradeMessageEntityRelaysTagEntityRepository<S> join;

  @Autowired
  public TradeMessageRelaysTagPlugin(@Nonnull TradeMessageRelaysTagEntityRepository<R> intentRelaysTagEntityRepository, @NonNull TradeMessageEntityRelaysTagEntityRepository<S> join) {
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
  public TradeMessageRelaysTagDto getTagDto(P relaysTag) {
    return new TradeMessageRelaysTagDto(relaysTag);
  }

  @Override
  public S getEventEntityTagEntity(Long eventId, Long relaysTagId) {
    return (S) new TradeMessageEntityRelaysTagEntity(eventId, relaysTagId);
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
