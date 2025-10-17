package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.dto.EventDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.AccountMessageEntity;
import com.prosilion.superconductor.entity.RemarkMessageEntity;
import com.prosilion.superconductor.entity.join.IntentEntityAbstractTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import com.prosilion.superconductor.repository.AccountEventEntityRepository;
import com.prosilion.superconductor.repository.RemarkEventEntityRepository;
import com.prosilion.superconductor.repository.join.IntentEntityAbstractTagEntityRepository;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.AccountIntentEvent;
import nostr.event.impl.RemarkIntentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static nostr.event.NIP77Event.ACCOUNT_TAG_CODE;
import static nostr.event.NIP77Event.REMARK_TAG_CODE;

@Slf4j
@Service
public class RemarkMessageEntityService implements EventEntityServiceIF<RemarkIntentEvent> {

    private final IntentConcreteTagEntitiesService<
            BaseTag,
            AbstractTagEntityRepository<AbstractTagEntity>,
            AbstractTagEntity,
            IntentEntityAbstractTagEntity,
            IntentEntityAbstractTagEntityRepository<IntentEntityAbstractTagEntity>>
            concreteTagEntitiesService;

    private final RemarkEventEntityRepository remarkEventEntityRepository;

//    private final GenericTagEntitiesService genericTagEntitiesService;

    private final Set<String> eventFieldNames;

    @Autowired
    public RemarkMessageEntityService(
            IntentConcreteTagEntitiesService<
                    BaseTag,
                    AbstractTagEntityRepository<AbstractTagEntity>,
                    AbstractTagEntity,
                    IntentEntityAbstractTagEntity,
                    IntentEntityAbstractTagEntityRepository<IntentEntityAbstractTagEntity>> concreteTagEntitiesService,
            /*GenericTagEntitiesService genericTagEntitiesService,*/
            RemarkEventEntityRepository remarkEventEntityRepository) {
        this.concreteTagEntitiesService = concreteTagEntitiesService;
//        this.genericTagEntitiesService = genericTagEntitiesService;
        this.remarkEventEntityRepository = remarkEventEntityRepository;
        this.eventFieldNames = new HashSet<>(List.of(REMARK_TAG_CODE));
    }

    @Override
    public Kind getKind() {
        return Kind.REMARK_INTENT;
    }

    @Override
    public Long saveEventEntity(@NonNull RemarkIntentEvent event) {
        RemarkMessageEntity savedEntity = Optional.of(remarkEventEntityRepository.save(EventDto.convertToEntity(event))).orElseThrow(NoResultException::new);
        // remove key tag from INTENT event fields.
        List<BaseTag> tags = event.getTags().stream().filter(t -> !eventFieldNames.contains(t.getCode())).toList();
        concreteTagEntitiesService.saveTags(savedEntity.getId(), tags);
//        genericTagEntitiesService.saveGenericTags(savedEntity.getId(), tags);
        return savedEntity.getId();
    }

    @Override
    public Map<Kind, Map<Long, RemarkIntentEvent>> getAll() {
        return remarkEventEntityRepository.findAll().stream()
                .map(this::populateEventEntity)
                .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                        Collectors.toMap(RemarkMessageEntity::getId, RemarkMessageEntity::convertEntityToDto)));
    }

    private RemarkMessageEntity populateEventEntity(RemarkMessageEntity remarkMessageEntity) {
        List<BaseTag> concreteTags = concreteTagEntitiesService.getTags(remarkMessageEntity.getId())
                .stream().map(AbstractTagEntity::getAsBaseTag).toList();

//        List<BaseTag> genericTags = genericTagEntitiesService.getGenericTags(postIntentEventEntity.getId())
//                .stream().map(
//                        genericTag -> new GenericTag(genericTag.code(), postIntentEventEntity.getNip(), genericTag.atts().stream().map(ElementAttributeDto::getElementAttribute).toList()))
//                .toList().stream().map(BaseTag.class::cast).toList();
//        postIntentEventEntity.setTags(Stream.concat(concreteTags.stream(), genericTags.stream()).toList());
        remarkMessageEntity.setTags(concreteTags);
        return remarkMessageEntity;
    }

    @Override
    public RemarkIntentEvent getEventById(@NonNull Long id) {
        return populateEventEntity(remarkEventEntityRepository.findById(id).orElseThrow(NoResultException::new)).convertEntityToDto();
    }

    @Override
    public RemarkIntentEvent getEventByEventIdString(@NonNull String eventIdString) {
        return populateEventEntity(remarkEventEntityRepository.findByEventIdString(eventIdString).orElseThrow(NoResultException::new)).convertEntityToDto();
    }

}
