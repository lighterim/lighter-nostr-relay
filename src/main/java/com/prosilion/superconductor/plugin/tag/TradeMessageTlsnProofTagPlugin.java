package com.prosilion.superconductor.plugin.tag;

import com.prosilion.superconductor.dto.classified.TlsnProofTagDto;
import com.prosilion.superconductor.entity.join.classified.TradeMessageEntityTlsnProofTagEntity;
import com.prosilion.superconductor.entity.standard.TlsnProofTagEntity;
import com.prosilion.superconductor.repository.classified.TlsnProofTagEntityRepository;
import com.prosilion.superconductor.repository.join.classified.TradeMessageEntityTlsnProofTagEntityRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import nostr.event.tag.TlsnProofTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static nostr.event.NIP77Event.TLSN_PROOF_TAG_CODE;

@Component
public class TradeMessageTlsnProofTagPlugin<
    P extends TlsnProofTag,
    Q extends TlsnProofTagEntityRepository<R>,
    R extends TlsnProofTagEntity,
    S extends TradeMessageEntityTlsnProofTagEntity,
    T extends TradeMessageEntityTlsnProofTagEntityRepository<S>>
    implements TradeMessageTagPlugin<P, Q, R, S, T> {

  private final TlsnProofTagEntityRepository<R> tlsnProofTagEntityRepository;
  private final TradeMessageEntityTlsnProofTagEntityRepository<S> join;

  @Autowired
  public TradeMessageTlsnProofTagPlugin(@Nonnull TlsnProofTagEntityRepository<R> paymentTagEntityRepository, @NonNull TradeMessageEntityTlsnProofTagEntityRepository<S> join) {
    this.tlsnProofTagEntityRepository = paymentTagEntityRepository;
    this.join = join;
  }

  @Override
  public String getCode() {
    return TLSN_PROOF_TAG_CODE;
  }

  @Override
  public R convertDtoToEntity(P tlsnProofTag) {
    return (R) getTagDto(tlsnProofTag).convertDtoToEntity();
  }

  @Override
  public TlsnProofTagDto getTagDto(P tlsnProofTag) {
    return new TlsnProofTagDto(tlsnProofTag);
  }

  @Override
  public S getEventEntityTagEntity(Long eventId, Long tlsnProofTagId) {
    return (S) new TradeMessageEntityTlsnProofTagEntity(eventId, tlsnProofTagId);
  }

  @Override
  public T getEventEntityStandardTagEntityRepositoryJoin() {
    return (T) join;
  }

  @Override
  public Q getStandardTagEntityRepository() {
    return (Q) tlsnProofTagEntityRepository;
  }
}
