package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.dto.EventDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.ProfileEntity;
import com.prosilion.superconductor.entity.join.ProfileEntityAbstractTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import com.prosilion.superconductor.repository.ProfileEntityRepository;
import com.prosilion.superconductor.repository.join.ProfileEntityAbstractTagEntityRepository;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.MetadataEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProfileEntityService implements EventEntityServiceIF<MetadataEvent> {

    private final ProfileConcreteTagEntitiesService<
            BaseTag,
            AbstractTagEntityRepository<AbstractTagEntity>,
            AbstractTagEntity,
            ProfileEntityAbstractTagEntity,
            ProfileEntityAbstractTagEntityRepository<ProfileEntityAbstractTagEntity>>
            concreteTagEntitiesService;

    private final ProfileEntityRepository profileEntityRepository;

//    private final GenericTagEntitiesService genericTagEntitiesService;

    private final Set<String> eventFieldNames;

    @Autowired
    public ProfileEntityService(
            ProfileConcreteTagEntitiesService<
                    BaseTag,
                    AbstractTagEntityRepository<AbstractTagEntity>,
                    AbstractTagEntity,
                    ProfileEntityAbstractTagEntity,
                    ProfileEntityAbstractTagEntityRepository<ProfileEntityAbstractTagEntity>> concreteTagEntitiesService,
            /*GenericTagEntitiesService genericTagEntitiesService,*/
            ProfileEntityRepository eventEntityRepository) {
        this.concreteTagEntitiesService = concreteTagEntitiesService;
//        this.genericTagEntitiesService = genericTagEntitiesService;
        this.profileEntityRepository = eventEntityRepository;
        this.eventFieldNames = new HashSet<>(List.of());
    }

    @Override
    public Kind getKind() {
        return Kind.SET_METADATA;
    }

    @Override
    public Long saveEventEntity(@NonNull MetadataEvent event) {
        ProfileEntity savedEntity = Optional.of(profileEntityRepository.save(EventDto.convertToEntity(event))).orElseThrow(NoResultException::new);
        // remove key tag from INTENT event fields.
        List<BaseTag> tags = event.getTags().stream().filter(t -> !eventFieldNames.contains(t.getCode())).toList();
        concreteTagEntitiesService.saveTags(savedEntity.getId(), tags);
//        genericTagEntitiesService.saveGenericTags(savedEntity.getId(), tags);
        return savedEntity.getId();
    }

    @Override
    public Map<Kind, Map<Long, MetadataEvent>> getAll() {
        Map<Kind, Map<Long, MetadataEvent>> map = profileEntityRepository.findAll().stream()
                .map(this::populateEventEntity)
                .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                        Collectors.toMap(ProfileEntity::getId, ProfileEntity::convertEntityToDto)));
        return map;
    }

    private ProfileEntity populateEventEntity(ProfileEntity postProfileEntity) {
        List<BaseTag> concreteTags = concreteTagEntitiesService.getTags(postProfileEntity.getId())
                .stream().map(AbstractTagEntity::getAsBaseTag).toList();

//        List<BaseTag> genericTags = genericTagEntitiesService.getGenericTags(postProfileEntity.getId())
//                .stream().map(
//                        genericTag -> new GenericTag(genericTag.code(), postProfileEntity.getNip(), genericTag.atts().stream().map(ElementAttributeDto::getElementAttribute).toList()))
//                .toList().stream().map(BaseTag.class::cast).toList();
//        postProfileEntity.setTags(Stream.concat(concreteTags.stream(), genericTags.stream()).toList());
        postProfileEntity.setTags(concreteTags);
        return postProfileEntity;
    }

    @Override
    public MetadataEvent getEventById(@NonNull Long id) {
        return populateEventEntity(profileEntityRepository.findById(id).orElseThrow(NoResultException::new)).convertEntityToDto();
    }

    @Override
    public MetadataEvent getEventByEventIdString(@NonNull String eventIdString) {
        return populateEventEntity(profileEntityRepository.findByEventIdString(eventIdString).orElseThrow(NoResultException::new)).convertEntityToDto();
    }

    public MetadataEvent getEventByNip05(@NonNull String nip05) {
        return populateEventEntity(profileEntityRepository.findByNip05(nip05).orElseThrow(NoResultException::new)).convertEntityToDto();
    }
}
