package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.pubsub.AddNostrEvent;
import com.prosilion.superconductor.service.NotifierService;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.Kind;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.TextNoteEvent;
import nostr.event.message.EventMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Getter
@Slf4j
@Service
public class EventService {
  private final NotifierService<GenericEvent> notifierService;
  private final RedisCache<GenericEvent> redisCache;
  private final TradeEventEntityService<? extends GenericEvent> tradeEventEntityService;

  @Autowired
  public EventService(NotifierService<GenericEvent> notifierService, RedisCache<GenericEvent> redisCache, TradeEventEntityService<? extends GenericEvent> tradeEventEntityService) {
    this.notifierService = notifierService;
    this.redisCache = redisCache;
    this.tradeEventEntityService = tradeEventEntityService;
  }

  //  @Async
  public void processIncomingEvent(@NonNull EventMessage eventMessage) {
    log.info("processing incoming TEXT_NOTE: [{}]", eventMessage);
    GenericEvent event = (GenericEvent) eventMessage.getEvent();

    TextNoteEvent textNoteEvent = new TextNoteEvent(
        event.getPubKey(),
        event.getTags(),
        event.getContent()
    );
    textNoteEvent.setId(event.getId());
    textNoteEvent.setCreatedAt(event.getCreatedAt());
    textNoteEvent.setSignature(event.getSignature());

    Long id = redisCache.saveEventEntity(event);

    if (event.getKind() == Kind.TAKE_INTENT.getValue()) {
      notifierService.nostrEventHandler(new AddNostrEvent<>(tradeEventEntityService.getById(id)));
    }else {
      notifierService.nostrEventHandler(new AddNostrEvent<>(event));
    }
  }
}
