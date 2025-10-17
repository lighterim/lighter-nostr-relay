package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.dto.EventDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.AddressBookMessageEntity;
import com.prosilion.superconductor.entity.join.IntentEntityAbstractTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import com.prosilion.superconductor.repository.AddressBookEventEntityRepository;
import com.prosilion.superconductor.repository.join.IntentEntityAbstractTagEntityRepository;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.AddressBookIntentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static nostr.event.NIP77Event.ADDRESS_BOOK_TAG_CODE;

@Slf4j
@Service
public class AddressBookMessageEntityService implements EventEntityServiceIF<AddressBookIntentEvent> {

    private final IntentConcreteTagEntitiesService<
            BaseTag,
            AbstractTagEntityRepository<AbstractTagEntity>,
            AbstractTagEntity,
            IntentEntityAbstractTagEntity,
            IntentEntityAbstractTagEntityRepository<IntentEntityAbstractTagEntity>>
            concreteTagEntitiesService;

    private final AddressBookEventEntityRepository addressBookEventEntityRepository;

//    private final GenericTagEntitiesService genericTagEntitiesService;

    private final Set<String> eventFieldNames;

    @Autowired
    public AddressBookMessageEntityService(
            IntentConcreteTagEntitiesService<
                    BaseTag,
                    AbstractTagEntityRepository<AbstractTagEntity>,
                    AbstractTagEntity,
                    IntentEntityAbstractTagEntity,
                    IntentEntityAbstractTagEntityRepository<IntentEntityAbstractTagEntity>> concreteTagEntitiesService,
            /*GenericTagEntitiesService genericTagEntitiesService,*/
            AddressBookEventEntityRepository addressBookEventEntityRepository) {
        this.concreteTagEntitiesService = concreteTagEntitiesService;
//        this.genericTagEntitiesService = genericTagEntitiesService;
        this.addressBookEventEntityRepository = addressBookEventEntityRepository;
        this.eventFieldNames = new HashSet<>(List.of(ADDRESS_BOOK_TAG_CODE));
    }

    @Override
    public Kind getKind() {
        return Kind.ADDRESS_BOOK_INTENT;
    }

    @Override
    public Long saveEventEntity(@NonNull AddressBookIntentEvent event) {
        AddressBookMessageEntity savedEntity = Optional.of(addressBookEventEntityRepository.save(EventDto.convertToEntity(event))).orElseThrow(NoResultException::new);
        // remove key tag from INTENT event fields.
        List<BaseTag> tags = event.getTags().stream().filter(t -> !eventFieldNames.contains(t.getCode())).toList();
        concreteTagEntitiesService.saveTags(savedEntity.getId(), tags);
//        genericTagEntitiesService.saveGenericTags(savedEntity.getId(), tags);
        return savedEntity.getId();
    }

    @Override
    public Map<Kind, Map<Long, AddressBookIntentEvent>> getAll() {
        return addressBookEventEntityRepository.findAll().stream()
                .map(this::populateEventEntity)
                .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                        Collectors.toMap(AddressBookMessageEntity::getId, AddressBookMessageEntity::convertEntityToDto)));
    }

    private AddressBookMessageEntity populateEventEntity(AddressBookMessageEntity addressBookMessageEntity) {
        List<BaseTag> concreteTags = concreteTagEntitiesService.getTags(addressBookMessageEntity.getId())
                .stream().map(AbstractTagEntity::getAsBaseTag).toList();

//        List<BaseTag> genericTags = genericTagEntitiesService.getGenericTags(postIntentEventEntity.getId())
//                .stream().map(
//                        genericTag -> new GenericTag(genericTag.code(), postIntentEventEntity.getNip(), genericTag.atts().stream().map(ElementAttributeDto::getElementAttribute).toList()))
//                .toList().stream().map(BaseTag.class::cast).toList();
//        postIntentEventEntity.setTags(Stream.concat(concreteTags.stream(), genericTags.stream()).toList());
        addressBookMessageEntity.setTags(concreteTags);
        return addressBookMessageEntity;
    }

    @Override
    public AddressBookIntentEvent getEventById(@NonNull Long id) {
        return populateEventEntity(addressBookEventEntityRepository.findById(id).orElseThrow(NoResultException::new)).convertEntityToDto();
    }

    @Override
    public AddressBookIntentEvent getEventByEventIdString(@NonNull String eventIdString) {
        return populateEventEntity(addressBookEventEntityRepository.findByEventIdString(eventIdString).orElseThrow(NoResultException::new)).convertEntityToDto();
    }

}
