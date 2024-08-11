package com.prosilion.superconductor.service.request;

import com.prosilion.superconductor.entity.Subscriber;
import com.prosilion.superconductor.service.AbstractSubscriberService;
import com.prosilion.superconductor.service.NotifierService;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import nostr.event.impl.GenericEvent;
import nostr.event.message.ReqMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReqService<T extends ReqMessage, U extends GenericEvent> {
  private final AbstractSubscriberService abstractSubscriberService;
  private final NotifierService<U> notifierService;

  @Autowired
  public ReqService(AbstractSubscriberService abstractSubscriberService, NotifierService<U> notifierService) {
    this.abstractSubscriberService = abstractSubscriberService;
    this.notifierService = notifierService;
  }

  public void processIncoming(@NotNull T reqMessage, @NonNull String sessionId) {
    notifierService.subscriptionEventHandler(
        abstractSubscriberService.save(
            new Subscriber(
                reqMessage.getSubscriptionId(),
                reqMessage.getSubscriptionId(),
                sessionId,
                true),
            reqMessage.getFiltersList()
        ));
  }
}
