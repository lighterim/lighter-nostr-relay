package com.prosilion.superconductor.service.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import com.prosilion.superconductor.util.ED25519Signer;
import com.prosilion.superconductor.util.EIP712Signer;
import com.prosilion.superconductor.util.RestClient;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.Side;
import nostr.event.TradeStatus;
import nostr.event.impl.GenericTag;
import nostr.event.impl.TakeIntentEvent;
import nostr.event.tag.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.http.HttpResponse;
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

    public static final int PRICE_DECIMALS = 18;

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
        String buyer;
        String seller;

        if (takeTag.getSide() == Side.BUY) {
            buyer = takeTag.getTakerNip05();
            seller = takeTag.getMakerNip05();
        } else {
            buyer = takeTag.getMakerNip05();
            seller = takeTag.getTakerNip05();
        }
        return new EscrowTag(takeIntentEvent.getTradeId(),
                takeIntentEvent.getTokenTag().getAddress(),
                takeTag.getVolume(),
                quoteTag.getNumber(),
                quoteTag.getUsdRate(),
                permit2Tag.getPayer(),
                seller,
                takeTag.getSellerFeeRate(),
                EIP712Signer.keccak256(paymentTag.getMethod()),
                EIP712Signer.keccak256(quoteTag.getCurrency()),
                EIP712Signer.keccak256(paymentTag.getAccount() + paymentTag.getQrCode() + paymentTag.getMemo()),
                buyer,
                takeTag.getBuyerFeeRate(),
                getEscrowSign(takeIntentEvent, seller, buyer));
    }
    private String getEscrowSign(TakeIntentEvent takeIntentEvent, String seller, String buyer) {
        String data = getSignEscrowData(takeIntentEvent, seller, buyer);

        RestClient restClient = new RestClient("https://api.lighter.im");
        HttpResponse<String> response = restClient.post("/signature/escrow", data).join();
        if (response.statusCode() == 200) {
            String responseBody = response.body();
            JsonObject spotObj = JsonParser.parseString(responseBody).getAsJsonObject();
            int code = spotObj.get("code").getAsInt();
            if(code!=0) {
                return null;
            }
            return spotObj.get("data").getAsString();
        }
        return null;
    }

    private String getSignEscrowData(TakeIntentEvent takeIntentEvent, String seller, String buyer) {
        TokenTag tokenTag = takeIntentEvent.getTokenTag();
        TakeTag takeTag = takeIntentEvent.getTakeTag();
        QuoteTag quoteTag = takeIntentEvent.getQuoteTag();
        PaymentTag paymentTag = takeIntentEvent.getPaymentTag();
        EIP712Tag eip712Tag = takeIntentEvent.getEip712Tag();

        String chainId = String.valueOf(tokenTag.getChainId());
        int tokenDecimals = tokenConfig.getDecimals(chainId, tokenTag.getSymbol());
        if(tokenDecimals==0) {
            log.warn("takeIntentEvent:{}, token: {},{}, decimals:0", takeIntentEvent.getId(), tokenTag.getSymbol(), tokenTag.getAddress());
        }

        List<List<String>> tags = new ArrayList<>();

        List<String> escrowParam = new ArrayList<>();
        escrowParam.add("escrow_param");
        escrowParam.add(String.valueOf(takeIntentEvent.getTradeId()));
        escrowParam.add(tokenTag.getAddress());
        escrowParam.add(takeTag.getVolume().multiply(BigDecimal.TEN.pow(tokenDecimals)).stripTrailingZeros().toPlainString());
        escrowParam.add(quoteTag.getNumber().multiply(BigDecimal.TEN.pow(PRICE_DECIMALS)).stripTrailingZeros().toPlainString());
        escrowParam.add(quoteTag.getUsdRate().multiply(BigDecimal.TEN.pow(PRICE_DECIMALS)).stripTrailingZeros().toPlainString());
        escrowParam.add(takeTag.getPayer());
        escrowParam.add(seller);
        escrowParam.add(takeTag.getSellerFeeRate().stripTrailingZeros().toPlainString());
        escrowParam.add(paymentTag.getMethod());
        escrowParam.add(quoteTag.getCurrency());
        escrowParam.add(buyer);
        escrowParam.add(takeTag.getBuyerFeeRate().stripTrailingZeros().toPlainString());
        escrowParam.add(paymentTag.getAccount());
        escrowParam.add(paymentTag.getQrCode());
        escrowParam.add(paymentTag.getMemo());

        List<String> eip712Param = new ArrayList<>();
        eip712Param.add("eip712");
        eip712Param.add(eip712Tag.getDomainAppName());
        eip712Param.add(eip712Tag.getDomainVersion());
        eip712Param.add(chainId);
        eip712Param.add(eip712Tag.getContractAddress());

        tags.add(escrowParam);
        tags.add(eip712Param);

        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("tags", tags);

        Gson gson = new GsonBuilder().create();
        return gson.toJson(jsonData);
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
        return takeEventEntityRepository.findAll().stream()
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

    @Transactional
    public void updateTradeStatus(long tradeId, TradeStatus tradeStatus) {
        Optional<TakeIntentEventEntity> opt  = takeEventEntityRepository.findById(tradeId);
        if(opt.isEmpty()){
            log.warn("tradeId: {}, entity not exists!: tradeStatus:{}", tradeId, tradeStatus);
            return;
        }
        TakeIntentEventEntity entity = opt.get();
        entity.setStatus(tradeStatus.getValue());
        entityManager.merge(entity);
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
}
