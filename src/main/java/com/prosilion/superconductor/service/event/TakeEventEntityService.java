package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.dto.EventTagPlugin;
import com.prosilion.superconductor.dto.generic.ElementAttributeDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.EventEntity;
import com.prosilion.superconductor.entity.TakeIntentEventEntity;
import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import com.prosilion.superconductor.entity.standard.EventTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import com.prosilion.superconductor.repository.EventEntityRepository;
import com.prosilion.superconductor.repository.TakeEventEntityRepository;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import com.prosilion.superconductor.service.event.join.generic.GenericTagEntitiesService;
import com.prosilion.superconductor.util.TagUtil;
import jakarta.persistence.NoResultException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.ElementAttribute;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.Side;
import nostr.event.impl.GenericTag;
import nostr.event.impl.TakeIntentEvent;
import nostr.event.impl.TradeMessageEvent;
import nostr.event.tag.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class TakeEventEntityService implements EventEntityServiceIF<TakeIntentEvent> {

    private final TakeEventEntityRepository takeEventEntityRepository;

//    private final EventEntityRepository eventEntityRepository;

//    private final EventTagPlugin eventTagPlugin;

    private final ConcreteTagEntitiesService<
            BaseTag,
            AbstractTagEntityRepository<AbstractTagEntity>,
            AbstractTagEntity,
            EventEntityAbstractTagEntity,
            EventEntityAbstractTagEntityRepository<EventEntityAbstractTagEntity>>
            concreteTagEntitiesService;
    private final GenericTagEntitiesService genericTagEntitiesService;
    @Autowired
    public TakeEventEntityService(
            ConcreteTagEntitiesService<
                    BaseTag,
                    AbstractTagEntityRepository<AbstractTagEntity>,
                    AbstractTagEntity,
                    EventEntityAbstractTagEntity,
                    EventEntityAbstractTagEntityRepository<EventEntityAbstractTagEntity>> concreteTagEntitiesService,
            GenericTagEntitiesService genericTagEntitiesService,
            TakeEventEntityRepository takeEventEntityRepository) {
        this.concreteTagEntitiesService = concreteTagEntitiesService;
        this.genericTagEntitiesService = genericTagEntitiesService;
        this.takeEventEntityRepository = takeEventEntityRepository;
    }

    @Override
    public Kind getKind() {
        return Kind.TAKE_INTENT;
    }

    public Long saveEventEntity(@NonNull TakeIntentEvent event) {

//        BaseTag eventTag = tagList.stream().filter(it->it.getCode().equals("e")).findFirst().orElseThrow();

        TakeTag takeTag = event.getTakeTag();
        TokenTag tokenTag = event.getTokenTag();
        PaymentTag paymentTag = event.getPaymentTag();
        QuoteTag quoteTag = event.getQuoteTag();
        BigDecimal volume = takeTag.getVolume();

//        String takerUserId = TagUtil.getGenericTagAttributeValue(tagList, "user_id", 0);
//        EventTagEntity entity = eventTagPlugin.convertDtoToEntity((EventTag)eventTag);
//        String eventIdString = entity.getEventIdString();

//        EventEntity intentEvent = eventEntityRepository.findByEventIdString(eventIdString).orElseThrow();
//        this.populateEventEntity(intentEvent);

//        List<BaseTag> intentEventTags = intentEvent.getTags();
//        String intentCreatorPubKey = intentEvent.getPubKey();
//        SideTag sideTag = (SideTag) intentEventTags.stream().filter(it->it.getCode().equals("side")).findFirst().orElseThrow();
//        QuoteTag quoteTag = (QuoteTag) intentEventTags.stream().filter(it->it.getCode().equals("quote")).findFirst().orElseThrow();
//        PaymentTag paymentTag = (PaymentTag) intentEventTags.stream().filter(it->it.getCode().equals("payment")).findFirst().orElseThrow();
//        GenericTag userIdTag = (GenericTag) intentEventTags.stream().filter(it->it.getCode().equals("user_id")).findFirst().orElseThrow();
//        TokenTag tokenTag = (TokenTag) intentEventTags.stream().filter(it->it.getCode().equals("token")).findFirst().orElseThrow();
//        String intentCreatorUserId = userIdTag.getAttributes().get(0).getValue().toString();
        String buyerId;
        String buyerPubKey;
        String sellerId;
        String sellerPubKey;

        if (takeTag.getSide() == Side.BUY) {
            buyerId = takeTag.getTakerNip05();
            buyerPubKey = takeTag.getTakerPubkey();
            sellerId = takeTag.getMakerNip05();
            sellerPubKey = takeTag.getMakerPubkey();
        } else {
            buyerId = takeTag.getMakerNip05();
            buyerPubKey = takeTag.getMakerPubkey();
            sellerId = takeTag.getTakerNip05();
            sellerPubKey = takeTag.getTakerPubkey();
        }


        TakeIntentEventEntity eventToSave = new TakeIntentEventEntity(
                event.getKind(),
                event.getId(),
                takeTag.getSide().getSide(),
                takeTag.getIntentEventId(),
                volume,
                buyerId,
                buyerPubKey,
                sellerId,
                sellerPubKey,
                tokenTag.getAddress(), tokenTag.getSymbol(), tokenTag.getChain(),
                tokenTag.getNetwork(), quoteTag.getNumber(),  quoteTag.getCurrency(),
                quoteTag.getUsdRate(), paymentTag.getMethod(), paymentTag.getAccount(),
                paymentTag.getQrCode(), paymentTag.getMemo(), event.getSignature().getPubKey().toHexString(),
                event.getCreatedAt()
        );
        takeEventEntityRepository.save(eventToSave);
//        concreteTagEntitiesService.saveTags(eventToSave.getId(), event.getTags());
//        genericTagEntitiesService.saveGenericTags(eventToSave.getId(), event.getTags());
        return eventToSave.getId();
    }

    private @NotNull TakeIntentEventEntity populateEventEntity(TakeIntentEventEntity eventEntity) {
//        List<BaseTag> concreteTags = concreteTagEntitiesService.getTags(
//                        eventEntity.getId()).stream()
//                .map(AbstractTagEntity::getAsBaseTag).toList();
//
//        List<BaseTag> genericTags = genericTagEntitiesService.getGenericTags(
//                        eventEntity.getId()).stream()
//                .map(genericTag ->
//                        new GenericTag(genericTag.code(), 1, genericTag.atts().stream()
//                                .map(ElementAttributeDto::getElementAttribute).toList())).toList().stream()
//                .map(BaseTag.class::cast).toList();
//
//        eventEntity.setTags(Stream.concat(concreteTags.stream(), genericTags.stream()).toList());
        return eventEntity;
    }

    public Map<Kind, Map<Long, TakeIntentEvent>> getAll() {
        return takeEventEntityRepository.findAll().stream()
                .map(this::populateEventEntity)
                .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                        Collectors.toMap(TakeIntentEventEntity::getId, TakeIntentEventEntity::convertEntityToDto)));
    }

    @Override
    public TakeIntentEvent getEventById(@NonNull Long id) {
        return populateEventEntity(takeEventEntityRepository.findById(id).orElseThrow(NoResultException::new)).convertEntityToDto();
    }
}
