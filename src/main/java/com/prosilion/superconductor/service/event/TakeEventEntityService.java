package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.dto.EventDto;
import com.prosilion.superconductor.dto.EventTagPlugin;
import com.prosilion.superconductor.dto.generic.ElementAttributeDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.EventEntity;
import com.prosilion.superconductor.entity.TakeIntentEventEntity;
import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import com.prosilion.superconductor.entity.standard.EventTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import com.prosilion.superconductor.repository.EventEntityRepository;
import com.prosilion.superconductor.repository.TakeEventEntityRepository;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import com.prosilion.superconductor.service.event.join.generic.GenericTagEntitiesService;
import com.prosilion.superconductor.util.TagUtil;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.ElementAttribute;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.Side;
import nostr.event.impl.GenericTag;
import nostr.event.impl.TakeIntentEvent;
import nostr.event.impl.TradeMessageEvent;
import nostr.event.tag.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nostr.event.NIP77Event.*;
import static nostr.event.NIP77Event.QUOTE_TAG_CODE;

@Slf4j
@Service
public class TakeEventEntityService implements EventEntityServiceIF<TakeIntentEvent> {

    private final TakeEventEntityRepository takeEventEntityRepository;

    private final ConcreteTagEntitiesService<
            BaseTag,
            AbstractTagEntityRepository<AbstractTagEntity>,
            AbstractTagEntity,
            EventEntityAbstractTagEntity,
            EventEntityAbstractTagEntityRepository<EventEntityAbstractTagEntity>>
            concreteTagEntitiesService;
    private final GenericTagEntitiesService genericTagEntitiesService;

    private final Set<String> eventFieldNames;
    @Autowired
    public TakeEventEntityService(
            ConcreteTagEntitiesService<
                    BaseTag,
                    AbstractTagEntityRepository<AbstractTagEntity>,
                    AbstractTagEntity,
                    EventEntityAbstractTagEntity,
                    EventEntityAbstractTagEntityRepository<EventEntityAbstractTagEntity>> concreteTagEntitiesService,
            GenericTagEntitiesService genericTagEntitiesService,
            TakeEventEntityRepository takeEventEntityRepository) {
        this.concreteTagEntitiesService = concreteTagEntitiesService;
        this.genericTagEntitiesService = genericTagEntitiesService;
        this.takeEventEntityRepository = takeEventEntityRepository;
        this.eventFieldNames = new HashSet<>(List.of(TAKE_TAG_CODE, TOKEN_TAG_CODE, PAYMENT_TAG_CODE, QUOTE_TAG_CODE));
    }

    @Override
    public Kind getKind() {
        return Kind.TAKE_INTENT;
    }

    public Long saveEventEntity(@NonNull TakeIntentEvent event) {
        TakeIntentEventEntity savedEntity = Optional.of(takeEventEntityRepository.save(EventDto.convertToEntity(event))).orElseThrow(NoResultException::new);
        // remove key tag from INTENT event fields.
        List<BaseTag> tags = event.getTags().stream().filter(t -> !eventFieldNames.contains(t.getCode())).toList();
        concreteTagEntitiesService.saveTags(savedEntity.getId(), event.getTags());
        genericTagEntitiesService.saveGenericTags(savedEntity.getId(), event.getTags());
        return savedEntity.getId();
    }

    private @NotNull TakeIntentEventEntity populateEventEntity(TakeIntentEventEntity eventEntity) {
        List<BaseTag> concreteTags = concreteTagEntitiesService.getTags(
                        eventEntity.getId()).stream()
                .map(AbstractTagEntity::getAsBaseTag).toList();
        List<BaseTag> genericTags = genericTagEntitiesService.getGenericTags(
                        eventEntity.getId()).stream()
                .map(genericTag ->
                        new GenericTag(genericTag.code(), eventEntity.getNip(), genericTag.atts().stream()
                                .map(ElementAttributeDto::getElementAttribute).toList())).toList().stream()
                .map(BaseTag.class::cast).toList();
        eventEntity.setTags(Stream.concat(concreteTags.stream(), genericTags.stream()).toList());
        return eventEntity;
    }

    public Map<Kind, Map<Long, TakeIntentEvent>> getAll() {
        return takeEventEntityRepository.findAll().stream()
                .map(this::populateEventEntity)
                .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                        Collectors.toMap(TakeIntentEventEntity::getId, TakeIntentEventEntity::convertEntityToDto)));
    }

    @Override
    public TakeIntentEvent getEventById(@NonNull Long id) {
        return populateEventEntity(takeEventEntityRepository.findById(id).orElseThrow(NoResultException::new)).convertEntityToDto();
    }
}
