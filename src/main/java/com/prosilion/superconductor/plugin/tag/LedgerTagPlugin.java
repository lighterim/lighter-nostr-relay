package com.prosilion.superconductor.plugin.tag;

import com.prosilion.superconductor.dto.classified.LedgerTagDto;
import com.prosilion.superconductor.entity.join.classified.TradeMessageEntityLedgerTagEntity;
import com.prosilion.superconductor.entity.standard.LedgerTagEntity;
import com.prosilion.superconductor.repository.classified.LedgerTagEntityRepository;
import com.prosilion.superconductor.repository.join.classified.TradeMessageEntityLedgerTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.LedgerTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static nostr.event.NIP77Event.LEDGER_TAG_CODE;

@Component
public class LedgerTagPlugin<
    P extends LedgerTag,
    Q extends LedgerTagEntityRepository<R>,
    R extends LedgerTagEntity,
    S extends TradeMessageEntityLedgerTagEntity,
    T extends TradeMessageEntityLedgerTagEntityRepository<S>>
    implements TagPlugin<P, Q, R, S, T> {

  private final LedgerTagEntityRepository<R> ledgerTagEntityRepository;
  private final TradeMessageEntityLedgerTagEntityRepository<S> join;

  @Autowired
  public LedgerTagPlugin(@Nonnull LedgerTagEntityRepository<R> paymentTagEntityRepository, @NonNull TradeMessageEntityLedgerTagEntityRepository<S> join) {
    this.ledgerTagEntityRepository = paymentTagEntityRepository;
    this.join = join;
  }

  @Override
  public String getCode() {
    return LEDGER_TAG_CODE;
  }

  @Override
  public R convertDtoToEntity(P ledgerTag) {
    return (R) getTagDto(ledgerTag).convertDtoToEntity();
  }

  @Override
  public LedgerTagDto getTagDto(P ledgerTag) {
    return new LedgerTagDto(ledgerTag);
  }

  @Override
  public S getEventEntityTagEntity(Long eventId, Long ledgerTagId) {
    return (S) new TradeMessageEntityLedgerTagEntity(eventId, ledgerTagId);
  }

  @Override
  public T getEventEntityStandardTagEntityRepositoryJoin() {
    return (T) join;
  }

  @Override
  public Q getStandardTagEntityRepository() {
    return (Q) ledgerTagEntityRepository;
  }
}
