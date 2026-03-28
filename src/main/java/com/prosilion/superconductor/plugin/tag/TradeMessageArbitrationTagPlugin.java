package com.prosilion.superconductor.plugin.tag;

import com.prosilion.superconductor.dto.classified.ArbitrationTagDto;
import com.prosilion.superconductor.entity.join.classified.TradeMessageEntityArbitrationTagEntity;
import com.prosilion.superconductor.entity.standard.ArbitrationTagEntity;
import com.prosilion.superconductor.repository.classified.ArbitrationTagEntityRepository;
import com.prosilion.superconductor.repository.join.classified.TradeMessageEntityArbitrationTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.ArbitrationTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static nostr.event.NIP77Event.ARBITRATION_TAG_CODE;

@Component
public class TradeMessageArbitrationTagPlugin<
    P extends ArbitrationTag,
    Q extends ArbitrationTagEntityRepository<R>,
    R extends ArbitrationTagEntity,
    S extends TradeMessageEntityArbitrationTagEntity,
    T extends TradeMessageEntityArbitrationTagEntityRepository<S>>
    implements TradeMessageTagPlugin<P, Q, R, S, T> {

  private final ArbitrationTagEntityRepository<R> arbitrationTagEntityRepository;
  private final TradeMessageEntityArbitrationTagEntityRepository<S> join;

  @Autowired
  public TradeMessageArbitrationTagPlugin(@Nonnull ArbitrationTagEntityRepository<R> paymentTagEntityRepository, @NonNull TradeMessageEntityArbitrationTagEntityRepository<S> join) {
    this.arbitrationTagEntityRepository = paymentTagEntityRepository;
    this.join = join;
  }

  @Override
  public String getCode() {
    return ARBITRATION_TAG_CODE;
  }

  @Override
  public R convertDtoToEntity(P tag) {
    return (R) getTagDto(tag).convertDtoToEntity();
  }

  @Override
  public ArbitrationTagDto getTagDto(P tag) {
    return new ArbitrationTagDto(tag);
  }

  @Override
  public S getEventEntityTagEntity(Long eventId, Long arbitrationTagId) {
    return (S) new TradeMessageEntityArbitrationTagEntity(eventId, arbitrationTagId);
  }

  @Override
  public T getEventEntityStandardTagEntityRepositoryJoin() {
    return (T) join;
  }

  @Override
  public Q getStandardTagEntityRepository() {
    return (Q) arbitrationTagEntityRepository;
  }
}
