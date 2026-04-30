package com.prosilion.superconductor.service.http.webhook;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prosilion.superconductor.config.TlsnProofVerifierConfig;
import com.prosilion.superconductor.config.TokenConfig;
import com.prosilion.superconductor.entity.AccountMapEntity;
import com.prosilion.superconductor.entity.AccountMapEntityService;
import com.prosilion.superconductor.service.event.RedisCache;
import com.prosilion.superconductor.util.EIP712Signer;
import com.prosilion.superconductor.util.ErrorCode;
import com.prosilion.superconductor.util.RestClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import nostr.event.Kind;
import nostr.event.TradeStatus;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.TakeIntentEvent;
import nostr.event.tag.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class TlsnVerifierService {

    @Resource
    private RedisCache<GenericEvent> redisCache;
//    @Resource
//    private TradeEntityService  tradeEntityService;
    @Resource
    private AccountMapEntityService accountMapEntityService;

    @Resource
    private TlsnProofVerifierConfig tlsnProofVerifierConfig;

    @Resource
    RestClient restClient;

    @Resource
    TokenConfig tokenConfig;


    private final ObjectMapper mapper = new ObjectMapper();

    public TlsnVerifierService() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public BaseMessage verifyTlsnProof(String payload) {
        try {
            JsonNode root = mapper.readTree(payload);
            if (!root.isArray() && root.hasNonNull("server_name")) {
                if (root.get("server_name").asText().equals(WISE)) {
                    JsonNode results = root.get("results");
                    String strTradeId = findTargetFieldValue(mapper, results, "paymentReference");
                    String paymentId = findTargetFieldValue(mapper, results, "id");
                    String targetRecipientId = findTargetFieldValue(mapper, results, "targetRecipientId");
                    String refundRecipientId = findTargetFieldValue(mapper, results, "refundRecipientId");
                    String targetAmount = findTargetFieldValue(mapper, results, "targetAmount");
                    String targetCurrency = findTargetFieldValue(mapper, results, "targetCurrency");
                    String state = findTargetFieldValue(mapper, results, "state");
                    String userId = findTargetFieldValue(mapper, results, "userId");
                    String actor = findTargetFieldValue(mapper, results, "actor");
                    String profileId = findTargetFieldValue(mapper, results, "profileId");
                    String confirmTimestamp = getConfirmTimestamp(mapper, results);
                    ErrorCode result = validateWiseTlsnProof(actor, strTradeId, paymentId, targetRecipientId, refundRecipientId, targetAmount, targetCurrency, state, confirmTimestamp);
                    if(result != ErrorCode.SUCCESS) {
                        log.warn("verifyWiseTlsnProof fail: {}, {}", result.getCode(), result.getMessage());
                        return null;
                    }

                    return getTlsnProofEvent(strTradeId, paymentId, targetAmount, targetCurrency, confirmTimestamp);
                }
            }
        }
        catch (Exception ex){
            log.error("verifyTlsnProof fail: {}", ex.getMessage());
        }
        return null;
    }

    /**
     * \"stateHistory\":[{\"state\":\"WAITING_FOR_PAYMENT\",\"date\":1775813802000},{\"state\":\"INCOMING_PAYMENT_IN_PROGRESS\",\"date\":1775813821000},{\"state\":\"FUNDS_RECEIVED\",\"date\":1775813821000},{\"state\":\"FUNDS_CONVERTED\",\"date\":1775813824000},{\"state\":\"OUTGOING_PAYMENT_SENT\",\"date\":1775813831000}]"
     *
     * @param results
     * @return
     * @Param stateHistory
     */
    public static String getConfirmTimestamp(ObjectMapper mapper, JsonNode results) {
        if (results == null || !results.isArray()) {
            return null;
        }

        String searchPattern = "\"stateHistory\"";

        // 1. 查找包含 stateHistory 的原始字符串片段
        String fragment = StreamSupport.stream(results.spliterator(), false)
                .filter(node -> "RECV".equals(node.path("type").asText())
                        && "BODY".equals(node.path("part").asText()))
                .map(node -> node.path("value").asText())
                .filter(value -> value.startsWith(searchPattern))
                .findFirst()
                .orElse(null);

        if (fragment == null) {
            return null;
        }

        try {
            // 2. 补全为 JSON 对象并解析
            String wrappedJson = "{" + fragment + "}";
            JsonNode root = mapper.readTree(wrappedJson);
            JsonNode historyArray = root.get("stateHistory");

            if (historyArray == null || !historyArray.isArray()) {
                return null;
            }

            // 3. 遍历历史状态寻找目标状态
            for (JsonNode entry : historyArray) {
                String state = entry.path("state").asText();
                if (WISE_STATE_OUTGOING_PAYMENT_SENT.equals(state)) {
                    JsonNode dateNode = entry.get("date");
                    if (dateNode == null || dateNode.isNull()) {
                        continue;
                    }

                    return convertToSeconds(dateNode.asLong());
                }
            }

        } catch (Exception e) {
            log.error("Failed to parse stateHistory fragment. Field: {}, Fragment: {}, Error: {}",
                    "stateHistory", fragment, e.getMessage());
        }

        return null;
    }

    /**
     * 统一处理时间戳，确保返回的是秒（10位）
     */
    private static String convertToSeconds(long timestamp) {
        // 如果大于 9999999999L (2286年)，基本可以判定是毫秒级时间戳
        if (timestamp > 9999999999L) {
            return String.valueOf(timestamp / 1000);
        }
        return String.valueOf(timestamp);
    }


    /**
     * results:[{
     *             "type": "RECV",
     *             "part": "BODY",
     *             "value": "\"paymentReference\":\"\""
     *         },
     *         {
     *             "type": "RECV",
     *             "part": "BODY",
     *             "value": "\"id\":2069785438"
     *         },
     *         {
     *             "type": "RECV",
     *             "part": "BODY",
     *             "value": "\"targetRecipientId\":879448014"
     *         },
     *         {
     *             "type": "RECV",
     *             "part": "BODY",
     *             "value": "\"refundRecipientId\":null"
     *         }....]
     * @param mapper
     * @param resultsNode
     * @param fieldName
     * @return
     */
    public static String findTargetFieldValue(ObjectMapper mapper, JsonNode resultsNode, String fieldName) {
        if (resultsNode == null || !resultsNode.isArray()) {
            return null;
        }

        String searchPattern = "\"" + fieldName + "\"";

        return StreamSupport.stream(resultsNode.spliterator(), false)
                // 组合过滤条件，减少流的操作步骤
                .filter(node -> "RECV".equals(node.path("type").asText())
                        && "BODY".equals(node.path("part").asText()))
                .map(node -> node.path("value").asText())
                // 修复 Bug：正确拼接变量
                // 使用 startsWith 提高匹配精确度
                .filter(value -> value.startsWith(searchPattern))
                .findFirst()
                .map(value -> parseValueFragment(mapper, value, fieldName))
                .orElse(null);
    }

    public static String parseValueFragment(ObjectMapper mapper, String fragment, String targetFieldName) {
        try {
            // 补全为 JSON 对象
            String wrappedJson = "{" + fragment + "}";
            JsonNode root = mapper.readTree(wrappedJson);

            JsonNode targetNode = root.get(targetFieldName);
            return (targetNode != null && !targetNode.isNull()) ? targetNode.asText() : null;
        } catch (Exception e) {
            log.error("Failed to parse JSON fragment: [{}], field: [{}], error: {}",
                    fragment, targetFieldName, e.getMessage());
            return null;
        }
    }

    /**
     * @param strTradeId --> .paymentReference
     * @param paymentId --> .id
     * @param targetAmount --> .targetAmount
     * @param targetCurrency --> .targetCurrency
     * @param confirmTimestamp --> .stateHistory[-1].OUTGOING_PAYMENT_SENT
     * @return
     */
    private BaseMessage getTlsnProofEvent(String strTradeId, String paymentId,
                                            String targetAmount,  String targetCurrency,
                                            String confirmTimestamp) {

        long tradeId = Long.parseLong(strTradeId);
        TakeIntentEvent e = (TakeIntentEvent) redisCache.getEventEntityById(Kind.TAKE_INTENT, tradeId);
        PaymentTag paymentTag = e.getPaymentTag();
        TlsnProofTag tlsnProof = TlsnProofTag.builder()
                .paymentId(paymentId).tradeId(strTradeId).amount(targetAmount)
                .currency(targetCurrency).confirmationTs(StringUtils.isNotBlank(confirmTimestamp)?confirmTimestamp:"")
                .paymentMethod(paymentTag.getMethod()).build();
        TokenTag tokenTag = e.getTokenTag();
        TlsnProofVerifierConfig.Eip712Domain eip712Domain = tlsnProofVerifierConfig.getEip712Domain(tokenTag.getChainId(), paymentTag.getMethod());
        EIP712Tag tlsnProofVerifierEip = EIP712Tag.builder()
                .contractAddress(eip712Domain.getAddress()).domainVersion(eip712Domain.getVersion()).domainAppName(eip712Domain.getName())
                .build();
        return EIP712Signer.getTlsnProofEvent(
                restClient, tokenTag, e.getTakeTag(), e.getQuoteTag(), e.getPermit2Tag(), paymentTag,
                tlsnProofVerifierEip, tlsnProof, tokenConfig, tradeId
        );
    }

    private ErrorCode validateWiseTlsnProof(String actor, String strTradeId, String paymentId, String targetRecipientId,
                                            String refundRecipientId, String targetAmount, String targetCurrency,
                                            String state, String confirmTimestamp) {
        if(!WISE_SENDER_ACTOR.equalsIgnoreCase(actor)){
            return ErrorCode.WISE_VERIFIER_ACTOR_INCORRECT;
        }
        if(!WISE_STATE_OUTGOING_PAYMENT_SENT.equals(state)){
            return ErrorCode.WISE_VERIFIER_PAYMENT_FAIL;
        }
        BigDecimal transferAmount = new BigDecimal(targetAmount);
        if(transferAmount.compareTo(BigDecimal.ZERO) <= 0 || StringUtils.isNotBlank(refundRecipientId)){
            return ErrorCode.WISE_VERIFIER_PAYMENT_ZERO_OR_REFUND;
        }
        if(alreadyVerify(WISE, paymentId)){
            return ErrorCode.WISE_VERIFIER_DOUBLE_SPENT;
        }
        if(StringUtils.isBlank(strTradeId)){
            return ErrorCode.WISE_VERIFIER_REFERENCE_NONE;
        }
        long tradeId = Long.parseLong(strTradeId);
        TakeIntentEvent tradeEvent = (TakeIntentEvent) redisCache.getEventEntityById(Kind.TAKE_INTENT, tradeId);
        // 有效状态：TradeStatus.CreateEscrowEvent 或 BuyerPaidEvent，表示escrow已建立或买家标记已付款(防御性)
        if(tradeEvent == null || (tradeEvent.getTradeTag().getStatus() != TradeStatus.CreateEscrowEvent && tradeEvent.getTradeTag().getStatus() != TradeStatus.BuyerPaidEvent)){
            return ErrorCode.WISE_VERIFIER_TRADE_NOT_FOUND_OR_STATUS_ERROR;
        }
        if(!WISE_PAYMENT_METHOD.equalsIgnoreCase(tradeEvent.getPaymentTag().getMethod())){
            return ErrorCode.WISE_VERIFIER_PAYMENT_METHOD_NOT_MATCHE;
        }
        long targetTimestamp = 0L;
        if(StringUtils.isNotBlank(confirmTimestamp)){
            targetTimestamp = Long.parseLong(confirmTimestamp);
        }
        if(targetTimestamp > 0 && targetTimestamp < tradeEvent.getCreatedAt()){
            log.warn("targetTimestamp, createdAt: {}, {}", targetTimestamp, tradeEvent.getCreatedAt());
            return ErrorCode.WISE_VERIFIER_PAYMENT_BEFORE_TRADE;
        }
        PaymentTag paymentTag = tradeEvent.getPaymentTag();
        QuoteTag quoteTag = tradeEvent.getQuoteTag();
        TakeTag takeTag = tradeEvent.getTakeTag();
        if(!quoteTag.getCurrency().equals(targetCurrency)){
            return ErrorCode.WISE_VERIFIER_PAYMENT_CURRENCY_INCORRECT;
        }
        BigDecimal amount = quoteTag.getNumber().multiply(takeTag.getVolume());
        if(transferAmount.compareTo(amount) < 0){
            return ErrorCode.WISE_VERIFIER_PAYMENT_INSUFFICIENT;
        }

        AccountMapEntity accountMapEntity = accountMapEntityService.findByDomainAndAccountNumber(WISE, targetRecipientId);
        if(accountMapEntity == null){
            return ErrorCode.WISE_VERIFIER_RECIPIENT_NOT_FOUND;
        }
        String payeeAcct = paymentTag.getAccount();
        if(payeeAcct == null || StringUtils.isBlank(accountMapEntity.getAccountName()) || !payeeAcct.equalsIgnoreCase(accountMapEntity.getAccountName())){
            return ErrorCode.WISE_VERIFIER_RECIPIENT_NOT_MATCH;
        }
        return ErrorCode.SUCCESS;
    }

    /**
     * TODO: 当前支付凭证是否已经使用过。 需要在释放后更新（或记录）凭证信息。
     * @param server
     * @param paymentId
     * @return
     */
    private boolean alreadyVerify(String server, String paymentId) {
        return false;
    }

    final static String WISE_SENDER_ACTOR = "SENDER";
    public final static String WISE_PAYMENT_METHOD = "wise";
    public final static String WISE = "wise.com";
    final static String WISE_STATE_OUTGOING_PAYMENT_SENT = "OUTGOING_PAYMENT_SENT";
}
