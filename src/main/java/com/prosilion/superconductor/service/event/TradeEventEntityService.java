package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.dto.EventTagPlugin;
import com.prosilion.superconductor.dto.generic.ElementAttributeDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.EventEntity;
import com.prosilion.superconductor.entity.TradeEventEntity;
import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import com.prosilion.superconductor.entity.standard.EventTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import com.prosilion.superconductor.repository.EventEntityRepository;
import com.prosilion.superconductor.repository.TradeEventEntityRepository;
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
import nostr.event.impl.TakeIntentEvent;
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
public class TradeEventEntityService<T extends GenericEvent> {

    private final TradeEventEntityRepository tradeEventEntityRepository;

    private final EventEntityRepository eventEntityRepository;

    private final EventTagPlugin eventTagPlugin;

    private final ConcreteTagEntitiesService<
            BaseTag,
            AbstractTagEntityRepository<AbstractTagEntity>,
            AbstractTagEntity,
            EventEntityAbstractTagEntity,
            EventEntityAbstractTagEntityRepository<EventEntityAbstractTagEntity>>
            concreteTagEntitiesService;
    private final GenericTagEntitiesService genericTagEntitiesService;
    @Autowired
    public TradeEventEntityService(TradeEventEntityRepository tradeEventEntityRepository,
                                   EventTagPlugin eventTagPlugin,
                                   EventEntityRepository eventEntityRepository,
                                   ConcreteTagEntitiesService<
                                           BaseTag,
                                           AbstractTagEntityRepository<AbstractTagEntity>,
                                           AbstractTagEntity,
                                           EventEntityAbstractTagEntity,
                                           EventEntityAbstractTagEntityRepository<EventEntityAbstractTagEntity>>
                                               concreteTagEntitiesService,
                                   GenericTagEntitiesService genericTagEntitiesService) {
        this.tradeEventEntityRepository = tradeEventEntityRepository;
        this.eventTagPlugin = eventTagPlugin;
        this.eventEntityRepository = eventEntityRepository;
        this.concreteTagEntitiesService = concreteTagEntitiesService;
        this.genericTagEntitiesService = genericTagEntitiesService;
    }

    protected Long saveEventEntity(@NonNull GenericEvent event) {
        List<BaseTag> tagList = event.getTags();

//        SideTag takeIntentSideTag = (SideTag) tagList.stream().filter(it -> it.getCode().equals("side")).findFirst().orElseThrow();
//        String takeIntentSide = takeIntentSideTag.getSide();

        BaseTag eventTag = tagList.stream().filter(it->it.getCode().equals("e")).findFirst().orElseThrow();
        BigDecimal volume = new BigDecimal(Objects.requireNonNull(TagUtil.getGenericTagAttributeValue(tagList, "volume", 0)));
        String takerUserId = TagUtil.getGenericTagAttributeValue(tagList, "user_id", 0);
        EventTagEntity entity = eventTagPlugin.convertDtoToEntity((EventTag)eventTag);
        String eventIdString = entity.getEventIdString();

        EventEntity intentEvent = eventEntityRepository.findByEventIdString(eventIdString).orElseThrow();
        this.populateEventEntity(intentEvent);

        List<BaseTag> intentEventTags = intentEvent.getTags();
        String intentCreatorPubKey = intentEvent.getPubKey();
        SideTag sideTag = (SideTag) intentEventTags.stream().filter(it->it.getCode().equals("side")).findFirst().orElseThrow();
        QuoteTag quoteTag = (QuoteTag) intentEventTags.stream().filter(it->it.getCode().equals("quote")).findFirst().orElseThrow();
        PaymentTag paymentTag = (PaymentTag) intentEventTags.stream().filter(it->it.getCode().equals("payment")).findFirst().orElseThrow();
        GenericTag userIdTag = (GenericTag) intentEventTags.stream().filter(it->it.getCode().equals("user_id")).findFirst().orElseThrow();
        TokenTag tokenTag = (TokenTag) intentEventTags.stream().filter(it->it.getCode().equals("token")).findFirst().orElseThrow();
        String intentCreatorUserId = userIdTag.getAttributes().get(0).getValue().toString();
        String buyerId;
        String buyerPubKey;
        String sellerId;
        String sellerPubKey;
        String taker;
        if (sideTag.getSide().equals("buy")) {
            taker = "SELL";
            buyerId = intentCreatorUserId;
            buyerPubKey = intentCreatorPubKey;
            sellerId = takerUserId;
            sellerPubKey = event.getPubKey().toHexString();
        } else {
            taker = "BUY";
            buyerId = takerUserId;
            buyerPubKey = event.getPubKey().toHexString();
            sellerId = intentCreatorUserId;
            sellerPubKey = intentCreatorPubKey;
        }


        TradeEventEntity eventToSave = new TradeEventEntity(
                event.getId(),
                intentEvent.getEventIdString(),
                buyerId, buyerPubKey, sellerId, sellerPubKey,
                tokenTag.getAddress(), tokenTag.getSymbol(),
                volume, quoteTag.getUsdRate(), quoteTag.getCurrency(), taker,
                paymentTag.getMethod(), paymentTag.getAccount(), paymentTag.getQrCode(), paymentTag.getMemo(),
                event.getSignature().toString(), event.getCreatedAt()
        );

        tradeEventEntityRepository.save(eventToSave);

        return eventToSave.getId();
    }

    private @NotNull EventEntity populateEventEntity(EventEntity eventEntity) {
        List<BaseTag> concreteTags = concreteTagEntitiesService.getTags(
                        eventEntity.getId()).stream()
                .map(AbstractTagEntity::getAsBaseTag).toList();

        List<BaseTag> genericTags = genericTagEntitiesService.getGenericTags(
                        eventEntity.getId()).stream()
                .map(genericTag ->
                        new GenericTag(genericTag.code(), 1, genericTag.atts().stream()
                                .map(ElementAttributeDto::getElementAttribute).toList())).toList().stream()
                .map(BaseTag.class::cast).toList();

        eventEntity.setTags(Stream.concat(concreteTags.stream(), genericTags.stream()).toList());
        return eventEntity;
    }

    private @NotNull EventEntity populateTradeEventEntity(TradeEventEntity tradeEventEntity){
        EventEntity eventEntity = new EventEntity();
        eventEntity.setEventIdString(tradeEventEntity.getEventIdString());
        eventEntity.setNip(77);
        eventEntity.setContent("");
        eventEntity.setKind(Kind.TAKE_INTENT.getValue());
        if (tradeEventEntity.getTaker().equals("BUY")) {
            eventEntity.setPubKey(tradeEventEntity.getBuyerPubKey());
        }else{
            eventEntity.setPubKey(tradeEventEntity.getSellerPubKey());
        }
        eventEntity.setSignature(tradeEventEntity.getSignature());
        eventEntity.setCreatedAt(tradeEventEntity.getCreateAt());

        List<BaseTag> tags = new ArrayList<>();
        EventTag eventTag = new EventTag();
        eventTag.setIdEvent(tradeEventEntity.getIntentEventIdString());
        tags.add(eventTag);

        GenericTag buyerId = new GenericTag("buyer_id");
        buyerId.addAttribute(new ElementAttribute("param0", tradeEventEntity.getBuyerId(), null));
        GenericTag buyerPubKey = new GenericTag("buyer_pub_key");
        buyerPubKey.addAttribute(new ElementAttribute("param0", tradeEventEntity.getBuyerPubKey(), null));

        GenericTag sellerId = new GenericTag("seller_id");
        sellerId.addAttribute(new ElementAttribute("param0", tradeEventEntity.getSellerId(), null));
        GenericTag sellerPubKey = new GenericTag("seller_pub_key");
        sellerPubKey.addAttribute(new ElementAttribute("param0", tradeEventEntity.getSellerPubKey(), null));

        PriceTag priceTag = new PriceTag();
        priceTag.setCurrency(tradeEventEntity.getCurrency());
        priceTag.setFrequency("1");
        priceTag.setNumber(tradeEventEntity.getVolume());

        TokenTag tokenTag = new TokenTag(tradeEventEntity.getVolume(), tradeEventEntity.getSymbol(), "", "", tradeEventEntity.getTokenAddr());
        GenericTag takerTag = new GenericTag("taker");
        takerTag.addAttribute(new ElementAttribute("param0", tradeEventEntity.getTaker(), null));

        PaymentTag paymentTag = new PaymentTag(tradeEventEntity.getPaymentMethod(), tradeEventEntity.getPaymentAccount(), tradeEventEntity.getPaymentQrCode(), tradeEventEntity.getPaymentMemo());

        tags.add(buyerId);
        tags.add(buyerPubKey);
        tags.add(sellerId);
        tags.add(sellerPubKey);
        tags.add(priceTag);
        tags.add(tokenTag);
        tags.add(takerTag);
        tags.add(paymentTag);

        eventEntity.setTags(tags);

        return eventEntity;
    }

    public Map<Kind, Map<Long, T>> getAll() {
        return tradeEventEntityRepository.findAll().stream()
                .map(this::populateTradeEventEntity)
                .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                        Collectors.toMap(EventEntity::getId, EventEntity::convertEntityToDto)));
    }

    public @NotNull GenericEvent getById(Long id){
        TradeEventEntity entity = tradeEventEntityRepository.findById(id).orElseThrow();
        return populateTradeEventEntity(entity).convertEntityToDto();
    }
}
