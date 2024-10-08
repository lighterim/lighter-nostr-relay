package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.dto.EventDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.IntentEventEntity;
import com.prosilion.superconductor.entity.join.IntentEntityAbstractTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import com.prosilion.superconductor.repository.PostEventEntityRepository;
import com.prosilion.superconductor.repository.join.IntentEntityAbstractTagEntityRepository;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.PostIntentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static nostr.event.NIP77Event.*;

@Slf4j
@Service
public class IntentEntityService implements EventEntityServiceIF<PostIntentEvent> {

    private final IntentConcreteTagEntitiesService<
            BaseTag,
            AbstractTagEntityRepository<AbstractTagEntity>,
            AbstractTagEntity,
            IntentEntityAbstractTagEntity,
            IntentEntityAbstractTagEntityRepository<IntentEntityAbstractTagEntity>>
            concreteTagEntitiesService;

    private final PostEventEntityRepository postEventEntityRepository;

//    private final GenericTagEntitiesService genericTagEntitiesService;

    private final Set<String> eventFieldNames;

    @Autowired
    public IntentEntityService(
            IntentConcreteTagEntitiesService<
                    BaseTag,
                    AbstractTagEntityRepository<AbstractTagEntity>,
                    AbstractTagEntity,
                    IntentEntityAbstractTagEntity,
                    IntentEntityAbstractTagEntityRepository<IntentEntityAbstractTagEntity>> concreteTagEntitiesService,
            /*GenericTagEntitiesService genericTagEntitiesService,*/
            PostEventEntityRepository postEventEntityRepository) {
        this.concreteTagEntitiesService = concreteTagEntitiesService;
//        this.genericTagEntitiesService = genericTagEntitiesService;
        this.postEventEntityRepository = postEventEntityRepository;
        this.eventFieldNames = new HashSet<>(List.of(MAKE_TAG_CODE, TOKEN_TAG_CODE, LIMIT_TAG_CODE, QUOTE_TAG_CODE));
    }

    @Override
    public Kind getKind() {
        return Kind.POST_INTENT;
    }

    @Override
    public Long saveEventEntity(@NonNull PostIntentEvent event) {
        IntentEventEntity savedEntity = Optional.of(postEventEntityRepository.save(EventDto.convertToEntity(event))).orElseThrow(NoResultException::new);
        // remove key tag from INTENT event fields.
        List<BaseTag> tags = event.getTags().stream().filter(t -> !eventFieldNames.contains(t.getCode())).toList();
        concreteTagEntitiesService.saveTags(savedEntity.getId(), tags);
//        genericTagEntitiesService.saveGenericTags(savedEntity.getId(), tags);
        return savedEntity.getId();
    }

    @Override
    public Map<Kind, Map<Long, PostIntentEvent>> getAll() {
        Map<Kind, Map<Long, PostIntentEvent>> map = postEventEntityRepository.findAll().stream()
                .map(this::populateEventEntity)
                .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                        Collectors.toMap(IntentEventEntity::getId, IntentEventEntity::convertEntityToDto)));
        return map;
    }

    private IntentEventEntity populateEventEntity(IntentEventEntity postIntentEventEntity) {
        List<BaseTag> concreteTags = concreteTagEntitiesService.getTags(postIntentEventEntity.getId())
                .stream().map(AbstractTagEntity::getAsBaseTag).toList();

//        List<BaseTag> genericTags = genericTagEntitiesService.getGenericTags(postIntentEventEntity.getId())
//                .stream().map(
//                        genericTag -> new GenericTag(genericTag.code(), postIntentEventEntity.getNip(), genericTag.atts().stream().map(ElementAttributeDto::getElementAttribute).toList()))
//                .toList().stream().map(BaseTag.class::cast).toList();
//        postIntentEventEntity.setTags(Stream.concat(concreteTags.stream(), genericTags.stream()).toList());
        postIntentEventEntity.setTags(concreteTags);
        return postIntentEventEntity;
    }

    @Override
    public PostIntentEvent getEventById(@NonNull Long id) {
        return populateEventEntity(postEventEntityRepository.findById(id).orElseThrow(NoResultException::new)).convertEntityToDto();
    }

    @Override
    public PostIntentEvent getEventByEventIdString(@NonNull String eventIdString) {
        return populateEventEntity(postEventEntityRepository.findByEventIdString(eventIdString).orElseThrow(NoResultException::new)).convertEntityToDto();
    }
}
