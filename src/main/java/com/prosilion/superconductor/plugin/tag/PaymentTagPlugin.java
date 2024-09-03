package com.prosilion.superconductor.plugin.tag;

import com.prosilion.superconductor.dto.classified.PaymentTagDto;
import com.prosilion.superconductor.entity.standard.PaymentTagEntity;
import com.prosilion.superconductor.entity.join.classified.EventEntityPaymentTagEntity;
import com.prosilion.superconductor.repository.classified.PaymentTagEntityRepository;
import com.prosilion.superconductor.repository.join.classified.EventEntityPaymentTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.PaymentTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentTagPlugin<
    P extends PaymentTag,
    Q extends PaymentTagEntityRepository<R>,
    R extends PaymentTagEntity,
    S extends EventEntityPaymentTagEntity,
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
    return "payment";
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
    return (S) new EventEntityPaymentTagEntity(eventId, paymentTagId);
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
