package com.prosilion.superconductor.service.request;

import com.prosilion.superconductor.service.request.pubsub.AddNostrEvent;
import com.prosilion.superconductor.service.request.pubsub.EoseNotice;
import com.prosilion.superconductor.service.request.pubsub.FireNostrEvent;
import com.prosilion.superconductor.util.EmptyFiltersException;
import com.prosilion.superconductor.util.FilterMatcher;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriberNotifierService<T extends GenericEvent> {
    private final AbstractSubscriberService abstractSubscriberService;
    private final FilterMatcher filterMatcher;

    @Autowired
    public SubscriberNotifierService(AbstractSubscriberService abstractSubscriberService, FilterMatcher filterMatcher) {
        this.abstractSubscriberService = abstractSubscriberService;
        this.filterMatcher = filterMatcher;
    }

    /**
     * 一个新事件发生时，事件处理器。
     * @param addNostrEvent
     */
    protected void nostrEventHandler(@NonNull AddNostrEvent<T> addNostrEvent) {
        abstractSubscriberService.getAllFiltersOfAllSubscribers().forEach((subscriberSessionHash, filtersList) ->
                eventHandler(subscriberSessionHash, addNostrEvent));
    }

    /**
     * 新构建的订阅者，事件处理器。
     *
     * @param subscriberSessionHash
     * @param addNostrEvent
     */
    protected void newSubscriptionHandler(@NonNull Long subscriberSessionHash, @NonNull AddNostrEvent<T> addNostrEvent) {
        eventHandler(subscriberSessionHash, addNostrEvent);
    }

    protected void newSubscriptionHandler(@NonNull String sessionId, @NonNull String subscriberid, @NonNull List<Filters> filters) throws EmptyFiltersException {
        abstractSubscriberService.save(sessionId, subscriberid, filters);
    }

    private void eventHandler(@NonNull Long subscriberSessionHash, @NonNull AddNostrEvent<T> addNostrEvent) {
        broadcastMatch(addNostrEvent, subscriberSessionHash);
    }

    private void broadcastMatch(AddNostrEvent<T> addNostrEvent, Long subscriberSessionHash) {
        // abstractSubscriberService.getFiltersList(subscriberSessionHash)---> 接收者的过滤条件集合List<Filters>
        abstractSubscriberService.getFiltersList(subscriberSessionHash).forEach(
                // 接收者过滤器:filters,
                filters -> filterMatcher.intersectFilterMatches(filters, (AddNostrEvent<GenericEvent>) addNostrEvent).forEach(
                        event -> broadcastToClients(
                                (FireNostrEvent<T>) new FireNostrEvent<>(subscriberSessionHash,
                                        abstractSubscriberService.get(subscriberSessionHash).getSubscriberId(), event.event()
                                )
                        )
                )
        );
    }

    @SneakyThrows
    private void broadcastToClients(@NonNull FireNostrEvent<T> fireNostrEvent) {
        abstractSubscriberService.broadcastToClients(fireNostrEvent);
    }

    @SneakyThrows
    protected void broadcastEose(@NonNull Long subscriberSessionHash) {
        abstractSubscriberService.broadcastToClients(new EoseNotice(
                subscriberSessionHash,
                abstractSubscriberService.get(subscriberSessionHash).getSubscriberId()));
    }
}