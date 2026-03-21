package com.prosilion.superconductor.service.http.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prosilion.superconductor.config.TokenConfig;
import com.prosilion.superconductor.entity.AccountMapEntity;
import com.prosilion.superconductor.entity.AccountMapEntityService;
import com.prosilion.superconductor.service.event.RedisCache;
import com.prosilion.superconductor.util.EIP712Signer;
import com.prosilion.superconductor.util.ErrorCode;
import com.prosilion.superconductor.util.RestClient;
import jakarta.annotation.Resource;
import nostr.event.BaseMessage;
import nostr.event.Kind;
import nostr.event.TradeStatus;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.TakeIntentEvent;
import nostr.event.tag.PaymentTag;
import nostr.event.tag.QuoteTag;
import nostr.event.tag.TakeTag;
import nostr.event.tag.TlsnProofTag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TlsnVerifierService {

    @Resource
    private RedisCache<GenericEvent> redisCache;
//    @Resource
//    private TradeEntityService  tradeEntityService;
    @Resource
    private AccountMapEntityService accountMapEntityService;

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
            if (!root.isArray() &&  root.hasNonNull("server_name")) {
                if (root.get("server_name").asText().equals(WISE)) {
                    String strTradeId = getStringFieldValue(root, "paymentReference");
                    String paymentId = getStringFieldValue(root, "id");
                    String targetRecipientId = getStringFieldValue(root, "targetRecipientId");
                    String refundRecipientId = getStringFieldValue(root, "refundRecipientId");
                    String targetAmount = getStringFieldValue(root, "targetAmount");
                    String targetCurrency = getStringFieldValue(root, "targetCurrency");
                    String state = getStringFieldValue(root, "state");
                    String userId = getStringFieldValue(root, "userId");
                    String profileId = getStringFieldValue(root, "profileId");
                    String confirmTimestamp = getConfirmTimestamp(root.get("stateHistory"));

                    return verifyWiseTlsnProof(strTradeId, paymentId, targetRecipientId, refundRecipientId, targetAmount, targetCurrency, state, userId, profileId, confirmTimestamp);
                }
                return null;

            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * [ { "state": "WAITING_FOR_PAYMENT", "date": 1754204822000 }, { "state": "INCOMING_PAYMENT_IN_PROGRESS", "date": 1754204827000 }, { "state": "FUNDS_RECEIVED", "date": 1754204828000 }, { "state": "FUNDS_CONVERTED", "date": 1754204830000 }, { "state": "OUTGOING_PAYMENT_SENT", "date": 1754204831000 } ]
     * @param stateHistoryArray
     * @return
     */
    private String getConfirmTimestamp(JsonNode stateHistoryArray) {
        if(stateHistoryArray ==null || !stateHistoryArray.isArray()) {
            return null;
        }
        for (JsonNode node : stateHistoryArray) {
            if (node.hasNonNull("state") && WISE_STATE_OUTGOING_PAYMENT_SENT.equals(node.get("state").asText())) {
                String millis =  node.get("date").asText();
                if(millis.length() > 11){
                    return String.valueOf(Long.parseLong(millis) / 1000);
                }
            }
        }
        return null;
    }

    private String getStringFieldValue(JsonNode root, String fieldName) {
        return root.hasNonNull(fieldName) ? root.get(fieldName).asText() : null;
    }




    /**
     * {
     *     "id": 1656869131,
     *     "userId": 67537976,
     *     "profileId": 50765188,
     *     "actor": "SENDER",
     *     "quoteId": "dfe481cf-a613-4a3d-91f5-91a331bcdd4c",
     *     "state": "OUTGOING_PAYMENT_SENT",
     *     "stateHistory": [
     *         {
     *             "state": "WAITING_FOR_PAYMENT",
     *             "date": 1754204822000
     *         },
     *         {
     *             "state": "INCOMING_PAYMENT_IN_PROGRESS",
     *             "date": 1754204827000
     *         },
     *         {
     *             "state": "FUNDS_RECEIVED",
     *             "date": 1754204828000
     *         },
     *         {
     *             "state": "FUNDS_CONVERTED",
     *             "date": 1754204830000
     *         },
     *         {
     *             "state": "OUTGOING_PAYMENT_SENT",
     *             "date": 1754204831000
     *         }
     *     ],
     *     "sourceAmount": 10.0,
     *     "invoiceAmount": 10.0,
     *     "sourceCurrency": "GBP",
     *     "targetAmount": 10.0,
     *     "targetCurrency": "GBP",
     *     "feeAmount": 0.00,
     *     "discountAmount": 0,
     *     "targetRecipientId": 31997068,
     *     "refundRecipientId": null,
     *     "issues": [],
     *     "paymentReference": "",
     *     "fixedRate": 1.0,
     *     "feeDetails": {
     *         "transferwise": 0.0,
     *         "payIn": 0,
     *         "partner": 0
     *     },
     *     "availableReceipts": [
     *         "CONFIRMATION"
     *     ],
     *     "externalReferences": []
     * }
     * @param strTradeId --> .paymentReference
     * @param paymentId --> .id
     * @param targetRecipientId --> .targetRecipientId
     * @param refundRecipientId --> .refundRecipientId
     * @param targetAmount --> .targetAmount
     * @param targetCurrency --> .targetCurrency
     * @param state --> .state
     * @param userId -->.userId
     * @param profileId -->.profileId
     * @param confirmTimestamp --> .stateHistory[-1].OUTGOING_PAYMENT_SENT
     * @return
     */
    private BaseMessage verifyWiseTlsnProof(String strTradeId, String paymentId, String targetRecipientId, String refundRecipientId,
                                            String targetAmount,  String targetCurrency, String state, String userId, String profileId,
                                            String confirmTimestamp) {
        ErrorCode result = validateWiseTlsnProof(strTradeId, paymentId, targetRecipientId, refundRecipientId, targetAmount, targetCurrency, state, confirmTimestamp);
        if(result != ErrorCode.SUCCESS) {
            return null;
        }

        long tradeId = Long.parseLong(strTradeId);
        TakeIntentEvent e = (TakeIntentEvent) redisCache.getEventEntityById(Kind.TAKE_INTENT, tradeId);
        TlsnProofTag tlsnProof = TlsnProofTag.builder()
                .paymentId(paymentId).tradeId(strTradeId).account1(e.getPaymentTag().getAccount()).amount(targetAmount)
                .currency(targetCurrency).confirmationTs(StringUtils.isNotBlank(confirmTimestamp)?confirmTimestamp:"")
                .build();
        return EIP712Signer.getTlsnProofEvent(restClient, e.getTokenTag(), e.getTakeTag(), e.getQuoteTag(), e.getPermit2Tag(), e.getPaymentTag(), e.getEip712Tag(), tlsnProof, tokenConfig, tradeId);
    }

    private ErrorCode validateWiseTlsnProof(String strTradeId, String paymentId, String targetRecipientId, String refundRecipientId, String targetAmount, String targetCurrency, String state, String confirmTimestamp) {
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
        if(tradeEvent == null || (tradeEvent.getTradeTag().getStatus() != TradeStatus.CreateEscrowEvent && tradeEvent.getTradeTag().getStatus() != TradeStatus.BuyerPaidEvent)){
            return ErrorCode.WISE_VERIFIER_TRADE_NOT_FOUND_OR_STATUS_ERROR;
        }
        long targetTimestamp = 0L;
        if(StringUtils.isNotBlank(confirmTimestamp)){
            targetTimestamp = Long.parseLong(confirmTimestamp);
        }
        if(targetTimestamp > 0 && tradeEvent.getCreatedAt() >= targetTimestamp){
            return ErrorCode.WISE_VERIFIER_PAYMENT_BEFORE_TRADE;
        }
        PaymentTag paymentTag = tradeEvent.getPaymentTag();
        QuoteTag quoteTag = tradeEvent.getQuoteTag();
        TakeTag takeTag = tradeEvent.getTakeTag();
        if(!quoteTag.getCurrency().equals(targetCurrency)){
            return ErrorCode.WISE_VERIFIER_PAYMENT_CURRENCY_INCORRECT;
        }
        BigDecimal amount = quoteTag.getNumber().multiply(takeTag.getVolume());
        if(amount.compareTo(transferAmount) < 0){
            return ErrorCode.WISE_VERIFIER_PAYMENT_INSUFFICIENT;
        }

        AccountMapEntity accountMapEntity = accountMapEntityService.findByDomainAndAccountNumber(WISE, targetRecipientId);
        if(accountMapEntity == null){
            return ErrorCode.WISE_VERIFIER_RECIPIENT_NOT_FOUND;
        }
        String payeeAcct = paymentTag.getAccount();
        if(payeeAcct == null || StringUtils.isBlank(accountMapEntity.getAccountName()) || payeeAcct.equalsIgnoreCase(accountMapEntity.getAccountName())){
            return ErrorCode.WISE_VERIFIER_RECIPIENT_NOT_MATCH;
        }
        return ErrorCode.SUCCESS;
    }

    private boolean alreadyVerify(String server, String paymentId) {
        return false;
    }


    final static String WISE = "wise.com";
    final static String WISE_STATE_OUTGOING_PAYMENT_SENT = "OUTGOING_PAYMENT_SENT";
}
