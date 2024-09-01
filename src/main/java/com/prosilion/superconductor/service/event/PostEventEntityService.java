package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.dto.EventDto;
import com.prosilion.superconductor.dto.generic.ElementAttributeDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.PostIntentEventEntity;
import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import com.prosilion.superconductor.repository.PostEventEntityRepository;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import com.prosilion.superconductor.service.event.join.generic.GenericTagEntitiesService;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.GenericTag;
import nostr.event.impl.PostIntentEvent;
import nostr.event.tag.MakeTag;
import nostr.event.tag.QuoteTag;
import nostr.event.tag.TokenTag;
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
public class PostEventEntityService implements EventEntityServiceIF<PostIntentEvent> {


    private final ConcreteTagEntitiesService<
            BaseTag,
            AbstractTagEntityRepository<AbstractTagEntity>,
            AbstractTagEntity,
            EventEntityAbstractTagEntity,
            EventEntityAbstractTagEntityRepository<EventEntityAbstractTagEntity>>
            concreteTagEntitiesService;

    private final PostEventEntityRepository postEventEntityRepository;

    private final GenericTagEntitiesService genericTagEntitiesService;

    @Autowired
    public PostEventEntityService(
            ConcreteTagEntitiesService<
                    BaseTag,
                    AbstractTagEntityRepository<AbstractTagEntity>,
                    AbstractTagEntity,
                    EventEntityAbstractTagEntity,
                    EventEntityAbstractTagEntityRepository<EventEntityAbstractTagEntity>> concreteTagEntitiesService,
            GenericTagEntitiesService genericTagEntitiesService,
            PostEventEntityRepository postEventEntityRepository){
        this.concreteTagEntitiesService = concreteTagEntitiesService;
        this.genericTagEntitiesService = genericTagEntitiesService;
        this.postEventEntityRepository = postEventEntityRepository;
    }

    @Override
    public Kind getKind() {
        return Kind.POST_INTENT;
    }

    @Override
    public Long saveEventEntity(@NonNull PostIntentEvent event) {
        MakeTag side = event.getSideTag();
        QuoteTag quoteTag = event.getQuoteTag();
        TokenTag tokenTag = event.getTokenTag();

        PostIntentEventEntity savedEntity = Optional.of(postEventEntityRepository.save(EventDto.convertToEntity(event))).orElseThrow(NoResultException::new);
        concreteTagEntitiesService.saveTags(savedEntity.getId(), event.getTags());
        genericTagEntitiesService.saveGenericTags(savedEntity.getId(), event.getTags());
        return savedEntity.getId();
    }

    @Override
    public Map<Kind, Map<Long, PostIntentEvent>> getAll() {
        Map<Kind, Map<Long, PostIntentEvent>> map = postEventEntityRepository.findAll().stream()
                .map(this::populateEventEntity)
                .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                        Collectors.toMap(PostIntentEventEntity::getId, PostIntentEventEntity::convertEntityToDto)));
        return map;
    }

    private PostIntentEventEntity populateEventEntity(PostIntentEventEntity postIntentEventEntity) {
        List<BaseTag> concreteTags = concreteTagEntitiesService.getTags(postIntentEventEntity.getId())
                .stream().map(AbstractTagEntity::getAsBaseTag).toList();
        List<BaseTag> genericTags = genericTagEntitiesService.getGenericTags(postIntentEventEntity.getId())
                .stream().map(
                        genericTag ->new GenericTag(genericTag.code(), 1, genericTag.atts().stream().map(ElementAttributeDto::getElementAttribute).toList()))
                .toList().stream().map(BaseTag.class::cast).toList();
        postIntentEventEntity.setTags(Stream.concat(concreteTags.stream(), genericTags.stream()).toList());
        return postIntentEventEntity;
    }

    @Override
    public PostIntentEvent getEventById(@NonNull Long id) {
        return populateEventEntity(postEventEntityRepository.findById(id).orElseThrow(NoResultException::new)).convertEntityToDto();
    }
}
