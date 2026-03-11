package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.config.TokenConfig;
import com.prosilion.superconductor.dto.EventDto;
import com.prosilion.superconductor.dto.generic.ElementAttributeDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.TakeIntentEventEntity;
import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import com.prosilion.superconductor.repository.TakeEventEntityRepository;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import com.prosilion.superconductor.service.event.join.generic.GenericTagEntitiesService;
import com.prosilion.superconductor.util.EIP712Signer;
import com.prosilion.superconductor.util.RestClient;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.TradeStatus;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.impl.TakeIntentEvent;
import nostr.event.tag.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nostr.event.NIP77Event.*;

@Slf4j
@Service
public class TradeEntityService implements EventEntityServiceIF<TakeIntentEvent> {

    @PersistenceContext
    private EntityManager entityManager;
    private final TakeEventEntityRepository takeEventEntityRepository;

    @Autowired
    RestClient restClient;

    private static final Collection<String> includedStatusList = Arrays.asList(
            TradeStatus.TakeEvent.getValue(),
            TradeStatus.CreateEscrowEvent.getValue(),
            TradeStatus.BuyerPaidEvent.getValue(),
            TradeStatus.SellerRequestCancelEvent.getValue(),
            TradeStatus.BuyerCancelEvent.getValue(),
            TradeStatus.BuyerDisputedEvent.getValue(),
            TradeStatus.SellerDisputedEvent.getValue()
    );

    private final ConcreteTagEntitiesService<
            BaseTag,
            AbstractTagEntityRepository<AbstractTagEntity>,
            AbstractTagEntity,
            EventEntityAbstractTagEntity,
            EventEntityAbstractTagEntityRepository<EventEntityAbstractTagEntity>>
            concreteTagEntitiesService;
    private final GenericTagEntitiesService genericTagEntitiesService;

    private final Set<String> eventFieldNames;

    @Value("${take.event.default.content:be leaved with empty.}")
    private String defaultContent;

    @Resource
    private TokenConfig tokenConfig;

    @Autowired
    public TradeEntityService(
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
        this.eventFieldNames = new HashSet<>(List.of(TAKE_TAG_CODE, TOKEN_TAG_CODE, PAYMENT_TAG_CODE, QUOTE_TAG_CODE, LIMIT_TAG_CODE, EIP712_TAG_CODE, PERMIT2_TAG_CODE, ESCROW_TAG_CODE));
    }

    @Override
    public Kind getKind() {
        return Kind.TAKE_INTENT;
    }

    public Long saveEventEntity(@NonNull TakeIntentEvent event) {
        if(!StringUtils.hasText(event.getContent()) && StringUtils.hasText(defaultContent)){
            event.setContent(defaultContent);
        }
        TakeIntentEventEntity savedEntity = Optional.of(takeEventEntityRepository.save(EventDto.convertToEntity(event))).orElseThrow(NoResultException::new);
        // remove key tag from INTENT event fields.
        List<BaseTag> tags = event.getTags().stream().filter(t -> !eventFieldNames.contains(t.getCode())).toList();
        concreteTagEntitiesService.saveTags(savedEntity.getId(), tags);
        genericTagEntitiesService.saveGenericTags(savedEntity.getId(), tags);
        return savedEntity.getId();
    }

    public EscrowTag getEscrowTag(TakeIntentEvent takeIntentEvent) {
        Permit2Tag permit2Tag = takeIntentEvent.getPermit2Tag();
        TakeTag takeTag = takeIntentEvent.getTakeTag();
        QuoteTag quoteTag = takeIntentEvent.getQuoteTag();
        PaymentTag paymentTag = takeIntentEvent.getPaymentTag();
        TokenTag tokenTag = takeIntentEvent.getTokenTag();
        EIP712Tag eip712Tag = takeIntentEvent.getEip712Tag();
        return EIP712Signer.getSignedEscrowTag(restClient, tokenTag, takeTag, quoteTag, permit2Tag, paymentTag, eip712Tag,
                tokenConfig, takeIntentEvent.getTradeId());
//        String buyer;
//        String seller;
//        //the permit2Tag maybe is null when a buyer take bulk sell intent.
//        String payer = permit2Tag == null ? takeTag.getPayer() : permit2Tag.getPayer();
//
//        if (takeTag.getSide() == Side.BUY) {
//            buyer = takeTag.getTakerNip05();
//            seller = takeTag.getMakerNip05();
//        } else {
//            buyer = takeTag.getMakerNip05();
//            seller = takeTag.getTakerNip05();
//        }
//        return new EscrowTag(takeIntentEvent.getTradeId(),
//                takeIntentEvent.getTokenTag().getAddress(),
//                takeTag.getVolume(),
//                quoteTag.getNumber(),
//                quoteTag.getUsdRate(),
//                payer,
//                seller,
//                takeTag.getSellerFeeRate(),
//                EIP712Signer.keccak256(paymentTag.getMethod()),
//                EIP712Signer.keccak256(quoteTag.getCurrency()),
//                EIP712Signer.keccak256(paymentTag.getAccount() + paymentTag.getQrCode() + paymentTag.getMemo()),
//                buyer,
//                takeTag.getBuyerFeeRate(),
//                getEscrowSign(takeIntentEvent, seller, buyer, tokenConfig));
    }


    private @NotNull TakeIntentEventEntity populateEventEntity(TakeIntentEventEntity eventEntity) {
        List<BaseTag> concreteTags = concreteTagEntitiesService.getTags(
                        eventEntity.getId()).stream()
                .map(AbstractTagEntity::getAsBaseTag).toList();
        List<BaseTag> genericTags = genericTagEntitiesService.getGenericTags(
                        eventEntity.getId()).stream()
                .map(genericTag ->
                        new GenericTag(genericTag.code(), eventEntity.getNip(), genericTag.atts().stream()
                                .map(ElementAttributeDto::getElementAttribute).toList())).toList().stream()
                .map(BaseTag.class::cast).toList();
        eventEntity.setTags(Stream.concat(concreteTags.stream(), genericTags.stream()).toList());
        return eventEntity;
    }

    public Map<Kind, Map<Long, TakeIntentEvent>> getAll() {
        return takeEventEntityRepository.findByStatusIn(includedStatusList).stream()
                .map(this::populateEventEntity)
                .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                        Collectors.toMap(TakeIntentEventEntity::getId, TakeIntentEventEntity::convertEntityToDto)));
    }

    @Override
    public TakeIntentEvent getEventById(@NonNull Long id) {
        return populateEventEntity(takeEventEntityRepository.findById(id).orElseThrow(NoResultException::new)).convertEntityToDto();
    }

    @Override
    public TakeIntentEvent getEventByEventIdString(@NonNull String eventIdString) {
        return populateEventEntity(takeEventEntityRepository.findByEventIdString(eventIdString).orElseThrow(NoResultException::new)).convertEntityToDto();
    }

    public TakeIntentEvent getTakeIntentEventByEntity(TakeIntentEventEntity entity) {
        return populateEventEntity(entity).convertEntityToDto();
    }

    public TakeIntentEventEntity getTakeIntentEventEntityById(long tradeId) {
        return takeEventEntityRepository.findById(tradeId).orElseThrow(NoResultException::new);
    }

    @Transactional
    public void updateTradeStatus(TakeIntentEventEntity takeIntentEventEntity, TradeStatus tradeStatus) {
        if(takeIntentEventEntity!=null) {
            takeIntentEventEntity.setStatus(tradeStatus.getValue());
            entityManager.merge(takeIntentEventEntity);
        }
    }

    @Transactional
    public void updateTradeStatusAndEscrowHash(TakeIntentEventEntity takeIntentEventEntity, TradeStatus tradeStatus, String escrowHash) {
        if(takeIntentEventEntity!=null) {
            takeIntentEventEntity.setEscrowHash(escrowHash);
            takeIntentEventEntity.setStatus(tradeStatus.getValue());
            entityManager.merge(takeIntentEventEntity);
        }
    }

    @Transactional
    public void updateEscrowSign(long tradeId, String sign) {
        Optional<TakeIntentEventEntity> opt  = takeEventEntityRepository.findById(tradeId);
        if(opt.isEmpty()){
            log.warn("updateEscrowSign tradeId: {}, entity not exists!", tradeId);
            return;
        }
        TakeIntentEventEntity entity = opt.get();
        entity.setEscrowSignature(sign);
        entityManager.merge(entity);
    }

    public Map<Kind, Map<Long, TakeIntentEvent>> getAllByReq(Filters filter) {
        Specification<TakeIntentEventEntity> spec = Specification.where(null);

        PublicKey publicKey = CollectionUtils.isEmpty(filter.getAuthors()) ? null : filter.getAuthors().getFirst();
        if(publicKey!=null) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.equal(root.get("buyerPubKey"), publicKey.toHexString()),
                            cb.equal(root.get("sellerPubKey"), publicKey.toHexString())
                    ));
        }

        return takeEventEntityRepository.findAll(spec).stream()
                .map(this::populateEventEntity)
                .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                        Collectors.toMap(TakeIntentEventEntity::getId, TakeIntentEventEntity::convertEntityToDto)));
    }
}
