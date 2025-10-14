package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.dto.EventDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.AccountMessageEntity;
import com.prosilion.superconductor.entity.IntentEventEntity;
import com.prosilion.superconductor.entity.TakeIntentEventEntity;
import com.prosilion.superconductor.entity.join.IntentEntityAbstractTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import com.prosilion.superconductor.repository.AccountEventEntityRepository;
import com.prosilion.superconductor.repository.PostEventEntityRepository;
import com.prosilion.superconductor.repository.join.IntentEntityAbstractTagEntityRepository;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.AccountIntentEvent;
import nostr.event.impl.PostIntentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static nostr.event.NIP77Event.*;

@Slf4j
@Service
public class AccountMessageEntityService implements EventEntityServiceIF<AccountIntentEvent> {

    private final IntentConcreteTagEntitiesService<
            BaseTag,
            AbstractTagEntityRepository<AbstractTagEntity>,
            AbstractTagEntity,
            IntentEntityAbstractTagEntity,
            IntentEntityAbstractTagEntityRepository<IntentEntityAbstractTagEntity>>
            concreteTagEntitiesService;

    private final AccountEventEntityRepository accountEventEntityRepository;

//    private final GenericTagEntitiesService genericTagEntitiesService;

    private final Set<String> eventFieldNames;

    @Autowired
    public AccountMessageEntityService(
            IntentConcreteTagEntitiesService<
                    BaseTag,
                    AbstractTagEntityRepository<AbstractTagEntity>,
                    AbstractTagEntity,
                    IntentEntityAbstractTagEntity,
                    IntentEntityAbstractTagEntityRepository<IntentEntityAbstractTagEntity>> concreteTagEntitiesService,
            /*GenericTagEntitiesService genericTagEntitiesService,*/
            AccountEventEntityRepository accountEventEntityRepository) {
        this.concreteTagEntitiesService = concreteTagEntitiesService;
//        this.genericTagEntitiesService = genericTagEntitiesService;
        this.accountEventEntityRepository = accountEventEntityRepository;
        this.eventFieldNames = new HashSet<>(List.of(ACCOUNT_TAG_CODE));
    }

    @Override
    public Kind getKind() {
        return Kind.ACCOUNT_INTENT;
    }

    @Override
    public Long saveEventEntity(@NonNull AccountIntentEvent event) {
        AccountMessageEntity savedEntity = Optional.of(accountEventEntityRepository.save(EventDto.convertToEntity(event))).orElseThrow(NoResultException::new);
        // remove key tag from INTENT event fields.
        List<BaseTag> tags = event.getTags().stream().filter(t -> !eventFieldNames.contains(t.getCode())).toList();
        concreteTagEntitiesService.saveTags(savedEntity.getId(), tags);
//        genericTagEntitiesService.saveGenericTags(savedEntity.getId(), tags);
        return savedEntity.getId();
    }

    @Override
    public Map<Kind, Map<Long, AccountIntentEvent>> getAll() {
        return accountEventEntityRepository.findAll().stream()
                .map(this::populateEventEntity)
                .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                        Collectors.toMap(AccountMessageEntity::getId, AccountMessageEntity::convertEntityToDto)));
    }

    private AccountMessageEntity populateEventEntity(AccountMessageEntity accountMessageEntity) {
        List<BaseTag> concreteTags = concreteTagEntitiesService.getTags(accountMessageEntity.getId())
                .stream().map(AbstractTagEntity::getAsBaseTag).toList();

//        List<BaseTag> genericTags = genericTagEntitiesService.getGenericTags(postIntentEventEntity.getId())
//                .stream().map(
//                        genericTag -> new GenericTag(genericTag.code(), postIntentEventEntity.getNip(), genericTag.atts().stream().map(ElementAttributeDto::getElementAttribute).toList()))
//                .toList().stream().map(BaseTag.class::cast).toList();
//        postIntentEventEntity.setTags(Stream.concat(concreteTags.stream(), genericTags.stream()).toList());
        accountMessageEntity.setTags(concreteTags);
        return accountMessageEntity;
    }

    @Override
    public AccountIntentEvent getEventById(@NonNull Long id) {
        return populateEventEntity(accountEventEntityRepository.findById(id).orElseThrow(NoResultException::new)).convertEntityToDto();
    }

    @Override
    public AccountIntentEvent getEventByEventIdString(@NonNull String eventIdString) {
        return populateEventEntity(accountEventEntityRepository.findByEventIdString(eventIdString).orElseThrow(NoResultException::new)).convertEntityToDto();
    }

}
