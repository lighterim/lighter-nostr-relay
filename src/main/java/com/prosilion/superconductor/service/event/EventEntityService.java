package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.dto.EventDto;
import com.prosilion.superconductor.dto.generic.ElementAttributeDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.EventEntity;
import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import com.prosilion.superconductor.repository.EventEntityRepository;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import com.prosilion.superconductor.service.event.join.generic.GenericTagEntitiesService;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class EventEntityService<T extends GenericEvent> implements EventEntityServiceIF<GenericEvent> {
  private final ConcreteTagEntitiesService<
      BaseTag,
      AbstractTagEntityRepository<AbstractTagEntity>,
      AbstractTagEntity,
      EventEntityAbstractTagEntity,
      EventEntityAbstractTagEntityRepository<EventEntityAbstractTagEntity>>
      concreteTagEntitiesService;
  private final GenericTagEntitiesService genericTagEntitiesService;
  private final EventEntityRepository eventEntityRepository;
//  private final TakeEventEntityService<? extends GenericEvent> tradeEventEntityService;
//  private final TradeMessageEntityService<? extends GenericEvent> tradeMessageEntityService;

  @Autowired
  public EventEntityService(
      ConcreteTagEntitiesService<
          BaseTag,
          AbstractTagEntityRepository<AbstractTagEntity>,
          AbstractTagEntity,
          EventEntityAbstractTagEntity,
          EventEntityAbstractTagEntityRepository<EventEntityAbstractTagEntity>>
          concreteTagEntitiesService,
      GenericTagEntitiesService genericTagEntitiesService,
      EventEntityRepository eventEntityRepository/*,
      TakeEventEntityService<? extends GenericEvent> tradeEventEntityService,
      TradeMessageEntityService<? extends GenericEvent> tradeMessageEntityService*/) {

    this.concreteTagEntitiesService = concreteTagEntitiesService;
    this.genericTagEntitiesService = genericTagEntitiesService;
    this.eventEntityRepository = eventEntityRepository;
//    this.tradeEventEntityService = tradeEventEntityService;
//    this.tradeMessageEntityService = tradeMessageEntityService;
  }

  @Override
  public Kind getKind() {
    return Kind.TEXT_NOTE;
  }

  public Long saveEventEntity(@NonNull GenericEvent event) {
//    Integer kind = event.getKind();
//    if (kind == 30078) {
//      return tradeEventEntityService.saveEventEntity(event);
//    } else if(kind == 30079){
//      return tradeMessageEntityService.saveEventEntity(event);
//    }
    EventDto eventToSave = new EventDto(
        event.getPubKey(),
        event.getId(),
        Kind.valueOf(event.getKind()),
        event.getNip(),
        event.getCreatedAt(),
        event.getSignature(),
        event.getTags(),
        event.getContent()
    );

    EventEntity savedEntity = Optional.of(eventEntityRepository.save(eventToSave.convertDtoToEntity())).orElseThrow(NoResultException::new);
    concreteTagEntitiesService.saveTags(savedEntity.getId(), event.getTags());
    genericTagEntitiesService.saveGenericTags(savedEntity.getId(), event.getTags());
    return savedEntity.getId();
  }

  public Map<Kind, Map<Long, GenericEvent>> getAll() {
    Map<Kind, Map<Long,GenericEvent>> map = eventEntityRepository.findAll().stream()
            .map(this::populateEventEntity)
            .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                    Collectors.toMap(EventEntity::getId, EventEntity::convertEntityToDto)));

//    Map<Long, T> tradeEventMap = (Map<Long, T>) tradeEventEntityService.getAll().get(Kind.valueOf(30078));
//    Map<Long, T> tradeMessageMap = (Map<Long, T>) tradeMessageEntityService.getAll().get(Kind.valueOf(30079));
//    if (tradeEventMap != null)
//      map.put(Kind.valueOf(30078), tradeEventMap);
//
//    if (tradeMessageMap != null)
//      map.put(Kind.valueOf(30079), tradeMessageMap);

    return map;
  }

  public GenericEvent getEventById(@NonNull Long id) {
    return populateEventEntity(
        getById(id)
            .orElseThrow())
        .convertEntityToDto();
  }

  private @NotNull Optional<EventEntity> getById(Long id) {
    return eventEntityRepository.findById(id);
  }

  private @NotNull EventEntity populateEventEntity(EventEntity eventEntity) {
    List<BaseTag> concreteTags = concreteTagEntitiesService.getTags(
            eventEntity.getId()).stream()
        .map(AbstractTagEntity::getAsBaseTag).toList();

    List<BaseTag> genericTags = genericTagEntitiesService.getGenericTags(
            eventEntity.getId()).stream()
        .map(genericTag ->
            new GenericTag(genericTag.code(), 1, genericTag.atts().stream()
                .map(ElementAttributeDto::getElementAttribute).toList())).toList().stream()
        .map(BaseTag.class::cast).toList();

    eventEntity.setTags(Stream.concat(concreteTags.stream(), genericTags.stream()).toList());
    return eventEntity;
  }
}