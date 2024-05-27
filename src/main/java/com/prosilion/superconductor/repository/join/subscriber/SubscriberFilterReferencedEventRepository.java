package com.prosilion.superconductor.repository.join.subscriber;

import com.prosilion.superconductor.entity.join.subscriber.SubscriberFilterReferencedEvent;
import nostr.event.impl.GenericEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriberFilterReferencedEventRepository extends JpaRepository<SubscriberFilterReferencedEvent, Long> {
  void deleteAllByFilterIdIn(List<Long> filterId);

  default void save(Long filterId, GenericEvent referencedEvent) {
    this.save(new SubscriberFilterReferencedEvent(filterId, referencedEvent.getId()));
  }
}