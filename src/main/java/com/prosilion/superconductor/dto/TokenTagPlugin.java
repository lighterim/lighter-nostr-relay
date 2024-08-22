package com.prosilion.superconductor.dto;

import com.prosilion.superconductor.dto.classified.QuoteTagDto;
import com.prosilion.superconductor.dto.classified.TokenTagDto;
import com.prosilion.superconductor.entity.classified.QuoteTagEntity;
import com.prosilion.superconductor.entity.classified.TokenTagEntity;
import com.prosilion.superconductor.entity.join.classified.EventEntityQuoteTagEntity;
import com.prosilion.superconductor.entity.join.classified.EventEntityTokenTagEntity;
import com.prosilion.superconductor.repository.classified.QuoteTagEntityRepository;
import com.prosilion.superconductor.repository.classified.TokenTagEntityRepository;
import com.prosilion.superconductor.repository.join.classified.EventEntityQuoteTagEntityRepository;
import com.prosilion.superconductor.repository.join.classified.EventEntityTokenTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.QuoteTag;
import nostr.event.tag.TokenTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenTagPlugin<
    P extends TokenTag,
    Q extends TokenTagEntityRepository<R>,
    R extends TokenTagEntity,
    S extends EventEntityTokenTagEntity,
    T extends EventEntityTokenTagEntityRepository<S>>
    implements TagPlugin<P, Q, R, S, T> {

  private final TokenTagEntityRepository<R> tokenTagEntityRepository;
  private final EventEntityTokenTagEntityRepository<S> join;

  @Autowired
  public TokenTagPlugin(@Nonnull TokenTagEntityRepository<R> tokenTagEntityRepository, @NonNull EventEntityTokenTagEntityRepository<S> join) {
    this.tokenTagEntityRepository = tokenTagEntityRepository;
    this.join = join;
  }

  @Override
  public String getCode() {
    return "quote";
  }

  @Override
  public R convertDtoToEntity(P tag) {
    return (R) getTagDto(tag).convertDtoToEntity();
  }

  @Override
  public TokenTagDto getTagDto(P tag) {
    return new TokenTagDto(tag);
  }

  @Override
  public S getEventEntityTagEntity(Long eventId, Long tokenTagId) {
    return (S) new EventEntityTokenTagEntity(eventId, tokenTagId);
  }

  @Override
  public T getEventEntityStandardTagEntityRepositoryJoin() {
    return (T) join;
  }

  @Override
  public Q getStandardTagEntityRepositoryRxR() {
    return (Q) tokenTagEntityRepository;
  }
}
