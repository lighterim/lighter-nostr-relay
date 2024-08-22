package com.prosilion.superconductor.dto;

import com.prosilion.superconductor.dto.classified.PriceTagDto;
import com.prosilion.superconductor.dto.classified.QuoteTagDto;
import com.prosilion.superconductor.entity.classified.PriceTagEntity;
import com.prosilion.superconductor.entity.classified.QuoteTagEntity;
import com.prosilion.superconductor.entity.join.classified.EventEntityPriceTagEntity;
import com.prosilion.superconductor.entity.join.classified.EventEntityQuoteTagEntity;
import com.prosilion.superconductor.repository.classified.PriceTagEntityRepository;
import com.prosilion.superconductor.repository.classified.QuoteTagEntityRepository;
import com.prosilion.superconductor.repository.join.classified.EventEntityPriceTagEntityRepository;
import com.prosilion.superconductor.repository.join.classified.EventEntityQuoteTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.PriceTag;
import nostr.event.tag.QuoteTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QuoteTagPlugin<
    P extends QuoteTag,
    Q extends QuoteTagEntityRepository<R>,
    R extends QuoteTagEntity,
    S extends EventEntityQuoteTagEntity,
    T extends EventEntityQuoteTagEntityRepository<S>>
    implements TagPlugin<P, Q, R, S, T> {

  private final QuoteTagEntityRepository<R> quoteTagEntityRepository;
  private final EventEntityQuoteTagEntityRepository<S> join;

  @Autowired
  public QuoteTagPlugin(@Nonnull QuoteTagEntityRepository<R> quoteTagEntityRepository, @NonNull EventEntityQuoteTagEntityRepository<S> join) {
    this.quoteTagEntityRepository = quoteTagEntityRepository;
    this.join = join;
  }

  @Override
  public String getCode() {
    return "quote";
  }

  @Override
  public R convertDtoToEntity(P quoteTag) {
    return (R) getTagDto(quoteTag).convertDtoToEntity();
  }

  @Override
  public QuoteTagDto getTagDto(P quoteTag) {
    return new QuoteTagDto(quoteTag);
  }

  @Override
  public S getEventEntityTagEntity(Long eventId, Long quoteTagId) {
    return (S) new EventEntityQuoteTagEntity(eventId, quoteTagId);
  }

  @Override
  public T getEventEntityStandardTagEntityRepositoryJoin() {
    return (T) join;
  }

  @Override
  public Q getStandardTagEntityRepositoryRxR() {
    return (Q) quoteTagEntityRepository;
  }
}
