package com.prosilion.superconductor.service.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import com.prosilion.superconductor.util.RestClient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
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
        this.eventFieldNames = new HashSet<>(List.of(TAKE_TAG_CODE, TOKEN_TAG_CODE, PAYMENT_TAG_CODE, QUOTE_TAG_CODE, LIMIT_TAG_CODE, EIP712_TAG_CODE, PERMIT2_TAG_CODE));
    }

    @Override
    public Kind getKind() {
        return Kind.TAKE_INTENT;
    }

    //获取实时市场价
    public void updateRealtimePrice(@NonNull TakeIntentEvent takeIntentEvent) {
        QuoteTag quoteTag = takeIntentEvent.getQuoteTag();
        if(quoteTag.getNumber().compareTo(BigDecimal.ZERO) <= 0) {
            TokenTag tokenTag = takeIntentEvent.getTokenTag();
            String symbol = tokenTag.getSymbol();
            String currency = quoteTag.getCurrency();
            RestClient restClient = new RestClient("https://spot.lighter.im");
            HttpResponse<String> response = restClient.get(String.format("/api/price/%d/%s/%s", tokenTag.getChainId(), symbol, currency)).join();
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                JsonObject spotObj = JsonParser.parseString(responseBody).getAsJsonObject();
                BigDecimal price = spotObj.get("price").getAsBigDecimal();
                String sign = spotObj.get("sign").getAsString();
                String tokenAddress = spotObj.get("token_address").getAsString();
                String timestamp = spotObj.get("timestamp").getAsString();
                String msg = String.format("%d%s%s%s", tokenTag.getChainId(), tokenAddress, timestamp, price.stripTrailingZeros());
                boolean verify = ED25519Signer.verify(msg, sign);
                if(!verify) {
                    throw new RuntimeException("Spot API price verify fail. msg: " + msg);
                }
                if(quoteTag.getSlippageBP()!=null) {
                    BigDecimal bp = BigDecimal.valueOf(quoteTag.getSlippageBP()).divide(new BigDecimal("10000"));
                    price = price.add(price.multiply(bp));
                }
                String escrowSign = getEscrowSign(takeIntentEvent, sign);
                if(!StringUtils.hasText(escrowSign)) {
                    throw new RuntimeException("Get escrow sign fail.");
                }
                quoteTag.setNumber(price);
                quoteTag.setSignature(escrowSign);
            } else {
                throw new RuntimeException("Spot API request failed with status:" + response.statusCode());
            }
        }
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

    private String getEscrowSign(TakeIntentEvent takeIntentEvent, String sign) {
        String data = getSignEscrowData(takeIntentEvent, sign);

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

    private String getSignEscrowData(TakeIntentEvent takeIntentEvent, String sign) {
        TokenTag tokenTag = takeIntentEvent.getTokenTag();
        TakeTag takeTag = takeIntentEvent.getTakeTag();
        QuoteTag quoteTag = takeIntentEvent.getQuoteTag();
        PaymentTag paymentTag = takeIntentEvent.getPaymentTag();
        EIP712Tag eip712Tag = takeIntentEvent.getEip712Tag();

        List<List<String>> tags = new ArrayList<>();

        String buyer;
        String seller;
        if (takeTag.getSide() == Side.BUY) {
            buyer = takeTag.getTakerNip05();
            seller = takeTag.getMakerNip05();
        } else {
            buyer = takeTag.getMakerNip05();
            seller = takeTag.getTakerNip05();
        }
        List<String> escrowParam = new ArrayList<>();
        escrowParam.add("escrow_param");
        escrowParam.add(String.valueOf(takeIntentEvent.getTradeId()));
        escrowParam.add(tokenTag.getAddress());
        escrowParam.add(takeTag.getVolume().toPlainString());
        escrowParam.add(quoteTag.getNumber().toPlainString());
        escrowParam.add(quoteTag.getUsdRate().toPlainString());
        escrowParam.add(takeTag.getPayer());
        escrowParam.add(seller);
        escrowParam.add(takeTag.getSellerFeeRate().toPlainString());
        escrowParam.add(paymentTag.getMethod());
        escrowParam.add(quoteTag.getCurrency());
        escrowParam.add(buyer);
        escrowParam.add(takeTag.getBuyerFeeRate().toPlainString());
        escrowParam.add(paymentTag.getAccount());
        escrowParam.add(paymentTag.getQrCode());
        escrowParam.add(paymentTag.getMemo());

        List<String> eip712Param = new ArrayList<>();
        eip712Param.add("eip712");
        eip712Param.add(eip712Tag.getDomainAppName());
        eip712Param.add(eip712Tag.getDomainVersion());
        eip712Param.add(String.valueOf(tokenTag.getChainId()));
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
}
