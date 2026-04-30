package com.prosilion.superconductor.service.event;

import com.prosilion.superconductor.dto.EventDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.IntentEventEntity;
import com.prosilion.superconductor.entity.join.IntentEntityAbstractTagEntity;
import com.prosilion.superconductor.repository.AbstractTagEntityRepository;
import com.prosilion.superconductor.repository.PostEventEntityRepository;
import com.prosilion.superconductor.repository.join.IntentEntityAbstractTagEntityRepository;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseTag;
import nostr.event.IntentType;
import nostr.event.Kind;
import nostr.event.Side;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.PostIntentEvent;
import nostr.event.impl.TakeIntentEvent;
import nostr.event.tag.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static nostr.event.NIP77Event.*;

@Slf4j
@Service
public class IntentEntityService implements EventEntityServiceIF<PostIntentEvent> {

    private final IntentConcreteTagEntitiesService<
            BaseTag,
            AbstractTagEntityRepository<AbstractTagEntity>,
            AbstractTagEntity,
            IntentEntityAbstractTagEntity,
            IntentEntityAbstractTagEntityRepository<IntentEntityAbstractTagEntity>>
            concreteTagEntitiesService;

    private final PostEventEntityRepository postEventEntityRepository;

//    private final GenericTagEntitiesService genericTagEntitiesService;

    private final Set<String> eventFieldNames;

    @Autowired
    public IntentEntityService(
            IntentConcreteTagEntitiesService<
                    BaseTag,
                    AbstractTagEntityRepository<AbstractTagEntity>,
                    AbstractTagEntity,
                    IntentEntityAbstractTagEntity,
                    IntentEntityAbstractTagEntityRepository<IntentEntityAbstractTagEntity>> concreteTagEntitiesService,
            /*GenericTagEntitiesService genericTagEntitiesService,*/
            PostEventEntityRepository postEventEntityRepository) {
        this.concreteTagEntitiesService = concreteTagEntitiesService;
//        this.genericTagEntitiesService = genericTagEntitiesService;
        this.postEventEntityRepository = postEventEntityRepository;
        this.eventFieldNames = new HashSet<>(List.of(MAKE_TAG_CODE, TOKEN_TAG_CODE, LIMIT_TAG_CODE, QUOTE_TAG_CODE, PERMIT2_TAG_CODE, EIP712_TAG_CODE,PAYMENT_TAG_CODE));
    }

    @Override
    public Kind getKind() {
        return Kind.POST_INTENT;
    }

    @Override
    public Long saveEventEntity(@NonNull PostIntentEvent event) {
        IntentEventEntity savedEntity = Optional.of(postEventEntityRepository.save(EventDto.convertToEntity(event))).orElseThrow(NoResultException::new);
        // remove key tag from INTENT event fields.
        List<BaseTag> tags = event.getTags().stream().filter(t -> !eventFieldNames.contains(t.getCode())).toList();
        concreteTagEntitiesService.saveTags(savedEntity.getId(), tags);
//        genericTagEntitiesService.saveGenericTags(savedEntity.getId(), tags);
        return savedEntity.getId();
    }

    @Transactional
    public PostIntentEvent updateIntentStatus(@NonNull TakeIntentEvent takeIntentEvent) {
        //TODO: 使用 sql(traded_amount -= takeTag.volume, 数据库traded_amount必须为正数), 这里 intent 先读出来，再intent.setTradedAmount, 有并发问题。
        String intentEventId = takeIntentEvent.getTakeTag().getIntentEventId();
        IntentEventEntity intent = postEventEntityRepository.findByEventIdString(intentEventId)
                .orElseThrow(() -> new RuntimeException("PostEvent not found with id: " + intentEventId));
        TakeTag takeTag = takeIntentEvent.getTakeTag();
        if(takeTag.getVisibleStatus()!=null) {
            // takerPubkey和takeIntentEvent已在外层判断及问题。
//            // when takeTag.visibleStatus != null, then Trade.status => Drop.
//            String buyerPubKey;
//            if (takeTag.getSide() == Side.BUY) {
//                buyerPubKey = takeTag.getTakerPubkey();
//            } else {
//                buyerPubKey = takeTag.getMakerPubkey();
//            }
            if(intent.getStatus()==1) {
                throw new RuntimeException("No permission to set visibility. intentEventId: " + intentEventId);
            }
            intent.setTradedAmount(intent.getTradedAmount().subtract(takeIntentEvent.getTakeTag().getVolume()));
            intent.setStatus(1);
        } else {
            IntentType intentType = intent.getIntentType();
            if (IntentType.BULK_SELL.equals(intentType) && takeIntentEvent.getTakeTag().getVolume().compareTo(takeIntentEvent.getTokenTag().getAmount()) == 1) {
                throw new RuntimeException("The quantity taken exceeds the remaining quantity. intentEventId: " + intentEventId);
            }

            if (IntentType.BUYER_INTENT.equals(intentType) || IntentType.SIGNATURE_SELL.equals(intentType)
                    || (IntentType.BULK_SELL.equals(intentType) && takeIntentEvent.getTakeTag().getVolume().compareTo(takeIntentEvent.getTokenTag().getAmount()) == 0)) {
                intent.setStatus(0);
            }
            // 使用sql: trade_amount += takeTag.volume
            intent.setTradedAmount(intent.getTradedAmount().add(takeIntentEvent.getTakeTag().getVolume()));
        }
        postEventEntityRepository.save(intent);
        return intent.convertEntityToDto();
    }

    @Override
    public Map<Kind, Map<Long, PostIntentEvent>> getAll() {
        Map<Kind, Map<Long, PostIntentEvent>> map = postEventEntityRepository.findByStatus(1).stream()
                .map(this::populateEventEntity)
                .collect(Collectors.groupingBy(eventEntity -> Kind.valueOf(eventEntity.getKind()),
                        Collectors.toMap(IntentEventEntity::getId, IntentEventEntity::convertEntityToDto)));
        return map;
    }

    private IntentEventEntity populateEventEntity(IntentEventEntity postIntentEventEntity) {
        List<BaseTag> concreteTags = concreteTagEntitiesService.getTags(postIntentEventEntity.getId())
                .stream().map(AbstractTagEntity::getAsBaseTag).toList();

//        List<BaseTag> genericTags = genericTagEntitiesService.getGenericTags(postIntentEventEntity.getId())
//                .stream().map(
//                        genericTag -> new GenericTag(genericTag.code(), postIntentEventEntity.getNip(), genericTag.atts().stream().map(ElementAttributeDto::getElementAttribute).toList()))
//                .toList().stream().map(BaseTag.class::cast).toList();
//        postIntentEventEntity.setTags(Stream.concat(concreteTags.stream(), genericTags.stream()).toList());
        postIntentEventEntity.setTags(concreteTags);
        return postIntentEventEntity;
    }

    @Override
    public PostIntentEvent getEventById(@NonNull Long id) {
        return populateEventEntity(postEventEntityRepository.findById(id).orElseThrow(NoResultException::new)).convertEntityToDto();
    }

    @Override
    public PostIntentEvent getEventByEventIdString(@NonNull String eventIdString) {
        return populateEventEntity(postEventEntityRepository.findByEventIdString(eventIdString).orElseThrow(NoResultException::new)).convertEntityToDto();
    }

    public List<? extends GenericEvent> getEventByFilters(Integer chainId, String strSide, String symbol, String currency, String paymentMethod, String createdBy) {
        IntentEventEntity probe = new IntentEventEntity();
        probe.setId(null);
        probe.setChainId(BigInteger.valueOf(chainId));
        probe.setStatus(1);

        if(strSide != null) {
            probe.setSide(strSide.toLowerCase());
        }
        if(symbol != null) {
            probe.setSymbol(symbol);
        }
        if(currency != null) {
            probe.setQuoteCurrency(currency);
        }
        if(paymentMethod != null) {
            probe.setPaymentMethod(paymentMethod);
        }
        if(createdBy != null) {
            probe.setPubkey(createdBy);
        }

        List<IntentEventEntity> list = postEventEntityRepository.findAll(Example.of(probe));
        log.info("Found {} events for probe {}", list.size(), probe);
        return list.stream()
                .map(this::populateEventEntity)
                .map((java.util.function.Function<? super IntentEventEntity, ? extends GenericEvent>) IntentEventEntity::convertEntityToDto)
                .toList();
    }
}
