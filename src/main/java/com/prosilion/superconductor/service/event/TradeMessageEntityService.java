package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.dto.EventTagPlugin;
import com.prosilion.superconductor.dto.generic.ElementAttributeDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.EventEntity;
import com.prosilion.superconductor.entity.TradeEventEntity;
import com.prosilion.superconductor.entity.TradeMessageEntity;
import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import com.prosilion.superconductor.entity.standard.EventTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import com.prosilion.superconductor.repository.EventEntityRepository;
import com.prosilion.superconductor.repository.TradeEventEntityRepository;
import com.prosilion.superconductor.repository.TradeMessageEntityRepository;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import com.prosilion.superconductor.service.event.join.generic.GenericTagEntitiesService;
import com.prosilion.superconductor.util.TagUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.ElementAttribute;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.tag.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class TradeMessageEntityService<T extends GenericEvent> {

    private final TradeEventEntityRepository tradeEventEntityRepository;

    private final TradeMessageEntityRepository tradeMessageEntityRepository;
    private final EventTagPlugin eventTagPlugin;

    @Autowired
    public TradeMessageEntityService(TradeEventEntityRepository tradeEventEntityRepository, TradeMessageEntityRepository tradeMessageEntityRepository, EventTagPlugin eventTagPlugin) {
        this.tradeEventEntityRepository = tradeEventEntityRepository;
        this.tradeMessageEntityRepository = tradeMessageEntityRepository;
        this.eventTagPlugin = eventTagPlugin;
    }

    protected Long saveEventEntity(@NonNull GenericEvent event) {
        List<BaseTag> tagList = event.getTags();
        BaseTag eventTag = tagList.stream().filter(it->it.getCode().equals("e")).findFirst().orElseThrow();
        EventTagEntity entity = eventTagPlugin.convertDtoToEntity((EventTag)eventTag);
        String eventIdString = entity.getEventIdString();
        String userId = TagUtil.getGenericTagAttributeValue(tagList, "user_id", 0);
        String pubKey = event.getPubKey().toString();
        Long createAt = event.getCreatedAt();
        String content = event.getContent();
        String signature = event.getSignature().toString();

        Optional<TradeEventEntity> optionalTradeEventEntity = tradeEventEntityRepository.findByEventIdString(eventIdString);
        if (optionalTradeEventEntity.isEmpty())
            throw new RuntimeException();
        TradeEventEntity tradeEventEntity = optionalTradeEventEntity.get();
        // Neither buyer nor seller
        if (pubKey == null || (!pubKey.equals(tradeEventEntity.getBuyerPubKey()) && !pubKey.equals(tradeEventEntity.getSellerPubKey()))) {
            throw new RuntimeException();
        }

        TradeMessageEntity entityToSave = new TradeMessageEntity(
                event.getId(),
                eventIdString,
                userId,
                pubKey,
                content,
                signature,
                createAt
        );

        tradeMessageEntityRepository.save(entityToSave);

        return entityToSave.getId();
    }

    private @NotNull EventEntity populateTradeMessageEntity(TradeMessageEntity tradeMessageEntity){
        EventEntity eventEntity = new EventEntity();
        eventEntity.setEventIdString(tradeMessageEntity.getEventIdString());
        eventEntity.setNip(77);
        eventEntity.setContent(tradeMessageEntity.getContent());
        eventEntity.setKind(30079);
        eventEntity.setSignature(tradeMessageEntity.getSignature());
        eventEntity.setCreatedAt(tradeMessageEntity.getCreateAt());
        eventEntity.setPubKey(tradeMessageEntity.getPubKey());

        List<BaseTag> tags = new ArrayList<>();
        EventTag eventTag = new EventTag();
        eventTag.setIdEvent(tradeMessageEntity.getTradeEventId());
        tags.add(eventTag);

        GenericTag buyerId = new GenericTag("buyer_id");
        buyerId.addAttribute(new ElementAttribute("param0", tradeMessageEntity.getUserId(), null));

        tags.add(buyerId);

        eventEntity.setTags(tags);

        return eventEntity;
    }

    public Map<Kind, Map<Long, T>> getAll() {
        return tradeMessageEntityRepository.findAll().stream()
                .map(this::populateTradeMessageEntity)
                .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                        Collectors.toMap(EventEntity::getId, EventEntity::convertEntityToDto)));
    }
}
