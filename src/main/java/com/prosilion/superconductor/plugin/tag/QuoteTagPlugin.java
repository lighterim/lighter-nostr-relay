package com.prosilion.superconductor.plugin.tag;

import com.prosilion.superconductor.dto.classified.QuoteTagDto;
import com.prosilion.superconductor.entity.standard.QuoteTagEntity;
import com.prosilion.superconductor.entity.join.classified.IntentEntityQuoteTagEntity;
import com.prosilion.superconductor.repository.classified.QuoteTagEntityRepository;
import com.prosilion.superconductor.repository.join.classified.EventEntityQuoteTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.QuoteTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static nostr.event.NIP77Event.QUOTE_TAG_CODE;

@Component
public class QuoteTagPlugin<
    P extends QuoteTag,
    Q extends QuoteTagEntityRepository<R>,
    R extends QuoteTagEntity,
    S extends IntentEntityQuoteTagEntity,
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
    return QUOTE_TAG_CODE;
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
    return (S) new IntentEntityQuoteTagEntity(eventId, quoteTagId);
  }

  @Override
  public T getEventEntityStandardTagEntityRepositoryJoin() {
    return (T) join;
  }

  @Override
  public Q getStandardTagEntityRepository() {
    return (Q) quoteTagEntityRepository;
  }
}
