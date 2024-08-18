package com.prosilion.superconductor.service.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.superconductor.service.event.EventService;
import com.prosilion.superconductor.service.okresponse.ClientOkResponseService;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.message.EventMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventMessageService<T extends EventMessage> implements MessageService<T> {
  @Getter
  public final String command = "EVENT";
  private final EventService eventService;
  private final ClientOkResponseService okResponseService;

  @Autowired
  public EventMessageService(EventService eventService, ClientOkResponseService okResponseService) {
    this.eventService = eventService;
    this.okResponseService = okResponseService;
  }

  public void processIncoming(@NotNull EventMessage eventMessage, @NonNull String sessionId) {
    log.info("EVENT message NIP: {}", eventMessage.getNip());
    /**
     * EVENT message type: GenericEvent(
     * id=089d8a8f790bf03a5efd3157a4ecc3152fc0e2c83f243978784d8fe31a267bcd,
     * pubKey=cccd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984,
     * createdAt=1723894554811,
     * kind=30402,
     * tags=[
     * GenericTag(code=title, nip=null, attributes=[ElementAttribute(name=param0, value=Custom Title2, nip=null)]),
     * GenericTag(code=side, nip=null, attributes=[ElementAttribute(name=param0, value=sell, nip=null)]),
     * GenericTag(code=user_id, nip=null, attributes=[ElementAttribute(name=param0, value=test.lighter.im, nip=null)]),
     * GenericTag(code=published_at, nip=null, attributes=[ElementAttribute(name=param0, value=1723894554811, nip=null)]),
     * GenericTag(code=symbol, nip=null, attributes=[ElementAttribute(name=param0, value=xrd, nip=null)]),
     * GenericTag(code=token_addr, nip=null, attributes=[ElementAttribute(name=param0, value=resource_tdx_2_1tknxxxxxxxxxradxrdxxxxxxxxx009923554798xxxxxxxxxtfd2jc, nip=null)]),
     * PriceTag(number=271, currency=CNY, frequency=1),
     * GenericTag(code=currency, nip=null, attributes=[ElementAttribute(name=param0, value=CNY, nip=null)]),
     * GenericTag(code=amount, nip=null, attributes=[ElementAttribute(name=param0, value=1000, nip=null)]),
     * GenericTag(code=payment_method, nip=null, attributes=[ElementAttribute(name=param0, value=cash, nip=null)])
     * ],
     * content=Custom Content2, signature=86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546, _serializedEvent=null, nip=null, attributes=[])
     */
    log.info("EVENT message type: {}", eventMessage.getEvent());
    try {
      eventService.processIncomingEvent(eventMessage);
      okResponseService.processOkClientResponse(sessionId, eventMessage);
    } catch (JsonProcessingException e) {
      log.info("FAILED event message: {}", e.getMessage());
    }
  }
}