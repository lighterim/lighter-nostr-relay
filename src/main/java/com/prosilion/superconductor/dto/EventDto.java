package com.prosilion.superconductor.dto;

import com.prosilion.superconductor.entity.EventEntity;
import com.prosilion.superconductor.entity.PostIntentEventEntity;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP01Event;
import nostr.event.impl.PostIntentEvent;
import nostr.event.tag.LimitTag;
import nostr.event.tag.MakeTag;
import nostr.event.tag.TokenTag;

import java.util.List;

public class EventDto extends NIP01Event {

  public EventDto(PublicKey pubKey, String eventId, Kind kind, Integer nip, Long createdAt, Signature signature, List<BaseTag> tags, String content) {
    super(pubKey, kind, tags);
    setId(eventId);
    setNip(nip);
    setCreatedAt(createdAt);
    setSignature(signature);
    setContent(content);
  }

  public EventEntity convertDtoToEntity() {
    return new EventEntity(getId(), getKind(), getNip(), getPubKey().toString(), getCreatedAt(), getSignature().toString(), getContent());
  }

  public static PostIntentEventEntity convertToEntity(PostIntentEvent event){
    MakeTag make = event.getSideTag();
    TokenTag token = event.getTokenTag();
    LimitTag limit = event.getLimitTag();

    return new PostIntentEventEntity(
            make.getSide().getSide(),
            make.getMakerNip05(),
            event.getPubKey().toBech32String(),

            token.getSymbol(),
            token.getChain(),
            token.getNetwork(),
            token.getAddress(),
            token.getAmount(),

            limit==null?null:limit.getCurrency(),
            limit==null||limit.getLowLimit()==null?null:limit.getLowLimit(),
            limit==null||limit.getUpLimit()==null?null:limit.getUpLimit(),

            event.getSignature().toString(),
            event.getId(),
            event.getKind(),
            event.getNip(),
            event.getCreatedAt(),

            event.getContent()

    );
  }
}
