package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.entity.EventEntity;
import com.prosilion.superconductor.entity.TradeMessageEntity;
import com.prosilion.superconductor.repository.TradeMessageEntityRepository;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.ElementAttribute;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.GenericTag;
import nostr.event.impl.TradeMessageEvent;
import nostr.event.tag.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TradeMessageEntityService implements EventEntityServiceIF<TradeMessageEvent> {

    private final TradeMessageEntityRepository tradeMessageEntityRepository;


    @Autowired
    public TradeMessageEntityService(TradeMessageEntityRepository tradeMessageEntityRepository) {
        this.tradeMessageEntityRepository = tradeMessageEntityRepository;
    }

    @Override
    public Kind getKind() {
        return Kind.TRADE_MESSAGE;
    }

    public Long saveEventEntity(@NonNull TradeMessageEvent event) {

        CreatedByTag createdByTag = event.getCreatedByTag();
        TradeMessageEntity entityToSave = new TradeMessageEntity(
                event.getKind(),
                event.getId(),
                createdByTag.getTakeIntentEventId(),
                createdByTag.getNip05(),
                createdByTag.getPubkey(),
                event.getContent(),
                event.getSignature().getPubKey().toHexString(),
                event.getCreatedAt()
        );
        tradeMessageEntityRepository.save(entityToSave);

        return entityToSave.getId();
    }

    private @NotNull TradeMessageEntity populateTradeMessageEntity(TradeMessageEntity tradeMessageEntity){
//        EventEntity eventEntity = new EventEntity();
//        eventEntity.setEventIdString(tradeMessageEntity.getEventIdString());
//        eventEntity.setNip(77);
//        eventEntity.setContent(tradeMessageEntity.getContent());
//        eventEntity.setKind(30079);
//        eventEntity.setSignature(tradeMessageEntity.getSignature());
//        eventEntity.setCreatedAt(tradeMessageEntity.getCreateAt());
//        eventEntity.setPubKey(tradeMessageEntity.getPubKey());
//
//        List<BaseTag> tags = new ArrayList<>();
//        EventTag eventTag = new EventTag();
//        eventTag.setIdEvent(tradeMessageEntity.getTakeIntentEventId());
//        tags.add(eventTag);
//
//        GenericTag buyerId = new GenericTag("buyer_id");
//        buyerId.addAttribute(new ElementAttribute("param0", tradeMessageEntity.getUserId(), null));
//
//        tags.add(buyerId);
//
//        eventEntity.setTags(tags);

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
}
