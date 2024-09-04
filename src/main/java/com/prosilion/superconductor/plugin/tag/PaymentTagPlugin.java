package com.prosilion.superconductor.plugin.tag;

import com.prosilion.superconductor.dto.classified.PaymentTagDto;
import com.prosilion.superconductor.entity.standard.PaymentTagEntity;
import com.prosilion.superconductor.entity.join.classified.TradeMessageEntityLedgerTagEntity;
import com.prosilion.superconductor.repository.classified.PaymentTagEntityRepository;
import com.prosilion.superconductor.repository.join.classified.EventEntityPaymentTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.PaymentTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static nostr.event.NIP77Event.PAYMENT_TAG_CODE;

@Component
public class PaymentTagPlugin<
    P extends PaymentTag,
    Q extends PaymentTagEntityRepository<R>,
    R extends PaymentTagEntity,
    S extends TradeMessageEntityLedgerTagEntity,
    T extends EventEntityPaymentTagEntityRepository<S>>
    implements TagPlugin<P, Q, R, S, T> {

  private final PaymentTagEntityRepository<R> paymentTagEntityRepository;
  private final EventEntityPaymentTagEntityRepository<S> join;

  @Autowired
  public PaymentTagPlugin(@Nonnull PaymentTagEntityRepository<R> paymentTagEntityRepository, @NonNull EventEntityPaymentTagEntityRepository<S> join) {
    this.paymentTagEntityRepository = paymentTagEntityRepository;
    this.join = join;
  }

  @Override
  public String getCode() {
    return PAYMENT_TAG_CODE;
  }

  @Override
  public R convertDtoToEntity(P paymentTag) {
    return (R) getTagDto(paymentTag).convertDtoToEntity();
  }

  @Override
  public PaymentTagDto getTagDto(P paymentTag) {
    return new PaymentTagDto(paymentTag);
  }

  @Override
  public S getEventEntityTagEntity(Long eventId, Long paymentTagId) {
    return (S) new TradeMessageEntityLedgerTagEntity(eventId, paymentTagId);
  }

  @Override
  public T getEventEntityStandardTagEntityRepositoryJoin() {
    return (T) join;
  }

  @Override
  public Q getStandardTagEntityRepository() {
    return (Q) paymentTagEntityRepository;
  }
}
