package com.prosilion.superconductor.service.request;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.prosilion.superconductor.entity.Subscriber;
import com.prosilion.superconductor.entity.join.subscriber.SubscriberFilter;
import com.prosilion.superconductor.service.request.pubsub.TerminatedSocket;
import com.prosilion.superconductor.util.EmptyFiltersException;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.GenericTagQuery;
import nostr.base.PublicKey;
import nostr.event.Kind;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.query.CompositionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;


@Slf4j
@Service
public class CachedSubscriberService extends AbstractSubscriberService {

    private final Map<Long, List<Combo>> subscriberSessionHashComboMap = new ConcurrentHashMap<>();

//    private final Map<Integer, Set<Long>> kindIndex = new ConcurrentHashMap<>();

//    private final Map<String, Set<Long>> authorIndex = new ConcurrentHashMap<>();

    private final Map<String, Set<Long>> sessionToSub = new ConcurrentHashMap<>();


    @Autowired
    public CachedSubscriberService(ApplicationEventPublisher publisher) {
        super(publisher);
    }

    @Override
    public Long save(@NonNull Subscriber subscriber, @NonNull List<Filters> filtersList) throws EmptyFiltersException {

        long subscriberSessionHash = subscriber.getSubscriberSessionHash();
        sessionToSub.computeIfAbsent(subscriber.getSessionId(), k -> new CopyOnWriteArraySet<>()).add(subscriberSessionHash);
        for (Filters f : filtersList) {
//            if (f.getKinds() != null) {
//                f.getKinds().forEach(kind ->
//                        kindIndex.computeIfAbsent(kind.getValue(), k -> new CopyOnWriteArraySet<>()).add(subscriberSessionHash));
//            }
//
//            if (f.getAuthors() != null) {
//                f.getAuthors().forEach(pubkey ->
//                        authorIndex.computeIfAbsent(pubkey.toString(), k -> new CopyOnWriteArraySet<>()).add(subscriberSessionHash));
//            }
            Combo combo = new Combo(
                    subscriber,
                    new SubscriberFilter(
                            subscriberSessionHash,
                            f.getSince(),
                            f.getUntil(),
                            f.getLimit()
                    ),
                    f
            );
            subscriberSessionHashComboMap.computeIfAbsent(subscriberSessionHash, k->new ArrayList<>(List.of(combo))).add(combo);
        }
        return subscriberSessionHash;
    }

    @Override
    public Map<Long, List<Filters>> getAllFiltersOfAllSubscribers() {
        Map<Long, List<Filters>> map = new HashMap<>();
        subscriberSessionHashComboMap.forEach((k, v) -> map.put(k, v.stream().map(Combo::getFilters).toList()));
        return map;
    }

    public Set<Long> findMatchingSubscribers(GenericEvent event) {
        Set<Long> candidates = new HashSet<>();

//        Set<Long> kindMatches = kindIndex.get(event.getKind());
//        if (kindMatches != null) candidates.addAll(kindMatches);
//
//        // 2. 从 Author 索引中获取候选人
//        Set<Long> authorMatches = authorIndex.get(event.getPubKey().toString());
//        if (authorMatches != null) candidates = Sets.intersection(candidates, authorMatches);
//
//        // 3. 二次精细化校验 (因为 Filter 可能包含其他复杂条件如 since/until)
        Set<Long> result = new HashSet<>();
//        for (Long key : candidates) {
//            List<Filters> filters = subscriberSessionHashComboMap.get(key).stream().map(Combo::getFilters).toList();
//            if (filters.stream().allMatch(f -> matchFilter(f, event))) {
//                result.add(key);
//            }
//        }
        return result;
    }

    private boolean matchFilter(Filters f, GenericEvent event) {
        return true;
    }

    //  @Cacheable("subscriber")
    @Override
    public Subscriber get(@NonNull Long subscriberSessionHash) {
        return subscriberSessionHashComboMap.get(subscriberSessionHash).getFirst().getSubscriber();
    }

    @Override
    public List<Filters> getFiltersList(@NonNull Long subscriberSessionHash) {
        return subscriberSessionHashComboMap.get(subscriberSessionHash).stream().map(Combo::getFilters).toList();
    }

    @EventListener
    public void terminateSocket(@NonNull TerminatedSocket terminatedSocket) {
        removeSubscriberBySessionId(terminatedSocket.sessionId());
    }

    @Override
    public List<Long> removeSubscriberBySessionId(@NonNull String sessionId) {
        Set<Long> subHashSet = sessionToSub.remove(sessionId);
        if(subHashSet != null) {
            subHashSet.forEach(hash -> {
                List<Combo> list =  subscriberSessionHashComboMap.remove(hash);
                log.info("remove subscriber by session id " + sessionId + " from hash " + hash + "combo: " + list);
//                kindIndex.values().forEach(set -> set.remove(hash));
//                authorIndex.values().forEach(set -> set.remove(hash));
            });
            return new ArrayList<>(subHashSet);
        }
        return new ArrayList<>();
    }

    @Override
    public Long removeSubscriberBySubscriberId(@NonNull String subscriberId, @NonNull String sessionId) {
        log.info("removeSubscriberBySubscriberId " + subscriberId);
        Long hash = new Subscriber(subscriberId, sessionId, true).getSubscriberSessionHash();
        List<Combo> list =  subscriberSessionHashComboMap.remove(hash);
        log.info("removeSubscriberBySubscriberId " + sessionId + " from hash " + hash + "combo: " + list);
        return hash;
    }

    @Getter
    private static class Combo {
        private final Subscriber subscriber;
        private final SubscriberFilter subscriberFilter;
        private final Filters filters;

        public Combo(Subscriber subscriber, SubscriberFilter subscriberFilter, Filters filters) throws EmptyFiltersException {
            this.subscriber = subscriber;
            if (!checkMinimallyPopulatedFilters(subscriberFilter, filters))
                throw new EmptyFiltersException(String.format("invalid: empty filters encountered for subscriber [%s]", subscriber.getSubscriberId()));
            this.subscriberFilter = subscriberFilter;
            this.filters = filters;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Combo combo = (Combo) o;
            return Objects.equals(subscriber, combo.subscriber) && Objects.equals(subscriberFilter, combo.subscriberFilter) && Objects.equals(filters, combo.filters);
        }

        @Override
        public int hashCode() {
            return Objects.hash(subscriber, subscriberFilter, filters);
        }

        @Override
        public String toString() {
            return "Combo{" +
                    "subscriber=" + subscriber +
                    ", subscriberFilter=" + subscriberFilter +
                    ", filters=" + filters +
                    '}';
        }

        private static boolean checkMinimallyPopulatedFilters(SubscriberFilter subscriberFilter, Filters filters) {
            return Objects.nonNull(subscriberFilter.getSince())
                    || Objects.nonNull(subscriberFilter.getUntil())
                    || Objects.nonNull(subscriberFilter.getLimit())
                    || hasValidField(filters.getEvents(), getEventPredicate())
                    || hasValidField(filters.getAuthors(), getPubKeyPredicate())
                    || hasValidField(filters.getKinds(), getKindPredicate())
                    || hasValidField(filters.getReferencedEvents(), getEventPredicate())
                    || hasValidField(filters.getReferencePubKeys(), getPubKeyPredicate())
                    || hasValidGenericTagQuery(filters.getGenericTagQuery())
                    || hasValidGenericTagQueryList(filters.getGenericTagQueryList())
                    || hasValidCompositionQuery(filters.getCompositionQuery());
        }

        private static boolean hasValidCompositionQuery(CompositionQuery compositionQuery) {
            return Objects.nonNull(compositionQuery)
                    && compositionQuery.getKind() != null
                    && (
                            compositionQuery.getKind() == Kind.POST_INTENT
                                    || (
                                            !compositionQuery.getAnyMatchList().isEmpty()
                                                    && compositionQuery.getAnyMatchList().stream().anyMatch(Combo::hasValidGenericTagQuery)
                            )
            );
        }

        private static <T> boolean hasValidField(List<T> filtersField, Predicate<T> fieldPredicate) {
            return Objects.nonNull(filtersField)
                    && !filtersField.isEmpty()
                    && filtersField.stream().anyMatch(fieldPredicate);
        }

        private static Predicate<PublicKey> getPubKeyPredicate() {
            return pubKey -> !pubKey.toString().isBlank();
        }

        private static Predicate<GenericEvent> getEventPredicate() {
            return event -> !event.getId().isBlank();
        }

        private static Predicate<Kind> getKindPredicate() {
            return kind -> !kind.toString().isBlank();
        }

        private static boolean hasValidGenericTagQuery(GenericTagQuery genericTagQuery) {
            return Objects.nonNull(genericTagQuery)
                    && Objects.nonNull(genericTagQuery.getTagName())
                    && !genericTagQuery.getTagName().isBlank();
        }

        private static boolean hasValidGenericTagQueryList(List<GenericTagQuery> list) {
            return Objects.nonNull(list) && !list.isEmpty() && list.stream().anyMatch(Combo::hasValidGenericTagQuery);
        }
    }
}
