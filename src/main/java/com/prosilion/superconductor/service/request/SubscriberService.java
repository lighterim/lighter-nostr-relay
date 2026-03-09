package com.prosilion.superconductor.service.request;

import com.prosilion.superconductor.entity.Subscriber;
import com.prosilion.superconductor.util.EmptyFiltersException;
import com.prosilion.superconductor.util.NoExistingUserException;
import lombok.NonNull;
import nostr.event.impl.Filters;

import java.util.List;
import java.util.Map;

public interface SubscriberService {
    /**
     * persist subscriber membership
     * &lt; subscriberId, List &lt; Fitlers &gt;&gt;
     * @param subscriber
     * @param filtersList
     * @return
     * @throws EmptyFiltersException
     */
    Long save(@NonNull Subscriber subscriber, @NonNull List<Filters> filtersList) throws EmptyFiltersException;


  List<Long> removeSubscriberBySessionId(@NonNull String sessionId);

  Long removeSubscriberBySubscriberId(@NonNull String subscriberId) throws NoExistingUserException;

  List<Filters> getFiltersList(@NonNull Long subscriberId);

  Map<Long, List<Filters>> getAllFiltersOfAllSubscribers();

  Subscriber get(@NonNull Long subscriberHash);
}
