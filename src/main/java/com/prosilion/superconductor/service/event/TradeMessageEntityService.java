package com.prosilion.superconductor.service.event;


import com.prosilion.superconductor.dto.EventDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.TradeMessageEntity;
import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import com.prosilion.superconductor.repository.TradeMessageEntityRepository;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.TradeMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nostr.event.NIP77Event.*;
import static nostr.event.NIP77Event.QUOTE_TAG_CODE;

@Slf4j
@Service
public class TradeMessageEntityService implements EventEntityServiceIF<TradeMessageEvent> {

    private final TradeMessageEntityRepository tradeMessageEntityRepository;
    private final ConcreteTagEntitiesService<
            BaseTag,
            AbstractTagEntityRepository<AbstractTagEntity>,
            AbstractTagEntity,
            EventEntityAbstractTagEntity,
            EventEntityAbstractTagEntityRepository<EventEntityAbstractTagEntity>>
            concreteTagEntitiesService;
    private final Set<String> eventFieldNames;

    @Autowired
    public TradeMessageEntityService(
            ConcreteTagEntitiesService<
                    BaseTag,
                    AbstractTagEntityRepository<AbstractTagEntity>,
                    AbstractTagEntity,
                    EventEntityAbstractTagEntity,
                    EventEntityAbstractTagEntityRepository<EventEntityAbstractTagEntity>> concreteTagEntitiesService,
            TradeMessageEntityRepository tradeMessageEntityRepository) {
        this.concreteTagEntitiesService = concreteTagEntitiesService;
        this.tradeMessageEntityRepository = tradeMessageEntityRepository;
        this.eventFieldNames = new HashSet<>(List.of(CREATED_BY_TAG_CODE));
    }

    @Override
    public Kind getKind() {
        return Kind.TRADE_MESSAGE;
    }

    public Long saveEventEntity(@NonNull TradeMessageEvent event) {
        TradeMessageEntity savedEntity = Optional.of(tradeMessageEntityRepository.save(EventDto.convertToEntity(event))).orElseThrow(NoResultException::new);
        // remove key tag from INTENT event fields.
        List<BaseTag> tags = event.getTags().stream().filter(t -> !eventFieldNames.contains(t.getCode())).toList();
        concreteTagEntitiesService.saveTags(savedEntity.getId(), tags);
        return savedEntity.getId();
    }

    private TradeMessageEntity populateTradeMessageEntity(TradeMessageEntity tradeMessageEntity) {
        List<BaseTag> concreteTags = concreteTagEntitiesService.getTags(tradeMessageEntity.getId())
                .stream().map(AbstractTagEntity::getAsBaseTag).toList();
        tradeMessageEntity.setTags(concreteTags);
        return tradeMessageEntity;
    }

    public Map<Kind, Map<Long, TradeMessageEvent>> getAll() {
        return tradeMessageEntityRepository.findAll().stream()
                .map(this::populateTradeMessageEntity)
                .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                        Collectors.toMap(TradeMessageEntity::getId, TradeMessageEntity::convertEntityToDto)));
    }

    @Override
    public TradeMessageEvent getEventById(@NonNull Long id) {
        return populateTradeMessageEntity(tradeMessageEntityRepository.findById(id).orElseThrow(NoResultException::new)).convertEntityToDto();
    }

    @Override
    public TradeMessageEvent getEventByEventIdString(@NonNull String eventIdString) {
        return populateTradeMessageEntity(tradeMessageEntityRepository.findByEventIdString(eventIdString).orElseThrow(NoResultException::new)).convertEntityToDto();
    }
}
