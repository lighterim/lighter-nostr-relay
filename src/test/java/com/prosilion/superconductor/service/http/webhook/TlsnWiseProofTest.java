package com.prosilion.superconductor.service.http.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TlsnWiseProofTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final TlsnVerifierService service = new TlsnVerifierService();

    @Test
    void eveningShapeParsesAmountAsDecimalAndConfirmTsInSeconds() throws Exception {
        ObjectNode root = webhook("wise.com");
        body(root, "\"paymentReference\":\"384600\"");
        body(root, "\"id\":2396019333");
        body(root, "\"refundRecipientId\":null");
        body(root, "\"targetAmount\":500.0");
        body(root, "\"targetCurrency\":\"SGD\"");
        body(root, "\"state\":\"OUTGOING_PAYMENT_SENT\"");
        body(root, "\"actor\":\"SENDER\"");
        body(root, "\"stateHistory\":[{\"state\":\"OUTGOING_PAYMENT_SENT\",\"date\":1727780000000}]");

        var results = root.get("results");
        assertEquals("384600", TlsnVerifierService.findTargetFieldValue(mapper, results, "paymentReference"));
        assertEquals("2396019333", TlsnVerifierService.findTargetFieldValue(mapper, results, "id"));
        assertNull(TlsnVerifierService.findTargetFieldValue(mapper, results, "refundRecipientId"));
        String amount = TlsnVerifierService.findTargetFieldValue(mapper, results, "targetAmount");
        assertEquals(0, TlsnVerifierService.parseWiseAmount(amount).compareTo(new BigDecimal("500")));
        assertEquals("1727780000", TlsnVerifierService.getConfirmTimestamp(mapper, results));
    }

    @Test
    void decimalStringsStayMajorUnits() {
        assertEquals(0, TlsnVerifierService.parseWiseAmount("100").compareTo(new BigDecimal("100")));
        assertEquals(0, TlsnVerifierService.parseWiseAmount("100.0").compareTo(new BigDecimal("100")));
        assertEquals(0, TlsnVerifierService.parseWiseAmount("100.00").compareTo(new BigDecimal("100")));
        assertEquals(0, TlsnVerifierService.parseWiseAmount("500.0").compareTo(new BigDecimal("500")));
    }

    @Test
    void literalNullIsNotARefund() {
        assertFalse(TlsnVerifierService.isWiseRefund(null));
        assertFalse(TlsnVerifierService.isWiseRefund(""));
        assertFalse(TlsnVerifierService.isWiseRefund("null"));
        assertTrue(TlsnVerifierService.isWiseRefund("99"));
    }

    @Test
    void unknownServerDoesNotSign() {
        assertNull(service.verifyTlsnProof("{\"server_name\":\"api.monzo.com\",\"results\":[]}"));
    }

    @Test
    void mismatchedStateRefundOrZeroAmountDoesNotSign() throws Exception {
        assertNull(service.verifyTlsnProof(wisePayload("SENDER", "FUNDS_CONVERTED", "500.0", "null")));
        assertNull(service.verifyTlsnProof(wisePayload("RECIPIENT", "OUTGOING_PAYMENT_SENT", "500.0", "null")));
        assertNull(service.verifyTlsnProof(wisePayload("SENDER", "OUTGOING_PAYMENT_SENT", "0", "null")));
        assertNull(service.verifyTlsnProof(wisePayload("SENDER", "OUTGOING_PAYMENT_SENT", "500.0", "99")));
    }

    private String wisePayload(String actor, String state, String amount, String refund) throws Exception {
        ObjectNode root = webhook("wise.com");
        body(root, "\"paymentReference\":\"384600\"");
        body(root, "\"id\":2396019333");
        body(root, "\"actor\":\"" + actor + "\"");
        body(root, "\"state\":\"" + state + "\"");
        body(root, "\"targetAmount\":" + amount);
        body(root, "\"targetCurrency\":\"SGD\"");
        body(root, "\"refundRecipientId\":" + refund);
        return mapper.writeValueAsString(root);
    }

    private ObjectNode webhook(String serverName) {
        ObjectNode root = mapper.createObjectNode();
        root.put("server_name", serverName);
        root.putArray("results");
        return root;
    }

    private void body(ObjectNode root, String fragment) {
        ObjectNode row = ((ArrayNode) root.get("results")).addObject();
        row.put("type", "RECV");
        row.put("part", "BODY");
        row.put("value", fragment);
    }
}
