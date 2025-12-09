package com.prosilion.superconductor.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.IntentType;
import nostr.event.NIP77Event;
import nostr.event.Side;
import nostr.event.impl.PostIntentEvent;
import nostr.event.impl.TakeIntentEvent;
import nostr.event.tag.*;
import org.web3j.crypto.*;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
@Slf4j
public class EIP712Signer {

    private static final Gson gson = new GsonBuilder().create();

    /**
     * 使用私钥进行 EIP-712 签名
     */
    public static Sign.SignatureData signMessage(
            String privateKeyHex,
            String structuredDataJson
    ) throws Exception {
        StructuredDataEncoder encoder = new StructuredDataEncoder(structuredDataJson);
        byte[] messageHash = encoder.hashStructuredData();
        ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(privateKeyHex));
        return Sign.signMessage(messageHash, keyPair, false);
    }

    public static String keccak256(String msg) {
        byte[] hash = Hash.sha3(msg.getBytes(StandardCharsets.UTF_8));
        return org.web3j.utils.Numeric.toHexString(hash);
    }

    /**
     * 验证签名
     * 1. bulk_sell: permit2 & eip712
     * 2. signature_sell: permit2(witness: intentParams)
     * 3. buyer_intent: eip712(IntentParam)
     */
    public static boolean verifySignature(NIP77Event event, SignerType signerType) {
        if(signerType.equals(SignerType.POST_EVENT)) {
            PostIntentEvent postIntentEvent = (PostIntentEvent)event;
            IntentType intentType = postIntentEvent.getSideTag().getIntentType();
            return switch(intentType) {
                case BULK_SELL -> verifyBulkSellIntent(postIntentEvent);
                case SIGNATURE_SELL -> verifySignatureSell(postIntentEvent);
                case BUYER_INTENT -> verifyBuyerIntent(postIntentEvent);
            };
//            if(intentType.equals(IntentType.BULK_SELL)) {
//                boolean verifyPermit2 = verifyPermit2(postIntentEvent);
//                if(!verifyPermit2) {
//                    return false;
//                }
//            }
//            eip712Tag = postIntentEvent.getEip712Tag();
//            signature = eip712Tag.getSign();
//            structuredDataJson = createPostStructuredDataJson(postIntentEvent, false);
        } else if(signerType.equals(SignerType.TAKE_EVENT)){
            TakeIntentEvent takeIntentEvent = (TakeIntentEvent)event;
            return verifySellerTakeIntent(takeIntentEvent);
        } else {
            return false;
        }
//        try {
//            String expectedAddress = eip712Tag.getWalletAddress();
//            log.info("eip712-event-id:{}, structDataJson1:{}, event1:{}", event.getId(), structuredDataJson, event);
//            StructuredDataEncoder encoder = new StructuredDataEncoder(structuredDataJson);
//            byte[] messageHash = encoder.hashStructuredData();
//            log.info("eip712-event-id:{}, structDataJson:{}, hash:{}, event:{}", event.getId(), structuredDataJson, org.web3j.utils.Numeric.toHexString(messageHash), event);
//            return verifySignature(messageHash, signature, expectedAddress);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    /**
     * buyer intent verify: intent
     * @param postIntentEvent
     * @return
     */
    private static boolean verifyBuyerIntent(PostIntentEvent postIntentEvent) {
        EIP712Tag eip712Tag = postIntentEvent.getEip712Tag();
        String signature = eip712Tag.getSign();
        String expectedAddress = eip712Tag.getWalletAddress();
        String json = getIntentStructuredData(postIntentEvent);
        return verifyEip712Signature(json, signature, expectedAddress, postIntentEvent);
    }

    private static boolean verifySellerTakeIntent(TakeIntentEvent takeIntentEvent) {
        Permit2Tag permit2Tag = takeIntentEvent.getPermit2Tag();
        String signature = permit2Tag.getSignature();
        String expectedAddress = permit2Tag.getWalletAddress();
        String json = getSignatureSellTakeStructuredData(takeIntentEvent);
        return verifyEip712Signature(json, signature, expectedAddress, takeIntentEvent);
    }

    /**
     * signature sell verify: permit2 & intent(witness)
     * @param postIntentEvent
     * @return
     */
    private static boolean verifySignatureSell(PostIntentEvent postIntentEvent) {
        Permit2Tag permit2Tag = postIntentEvent.getPermit2Tag();
        String signature = permit2Tag.getSignature();
        String json = getSignatureSellStructuredData(postIntentEvent);
        String expectedAddress = permit2Tag.getWalletAddress();
        return verifyEip712Signature(json, signature, expectedAddress, postIntentEvent);
    }

    /**
     * bulk sell:
     * 1. permit2Tag
     * 2. eip712Tag
     * @param event
     * @return
     */
    private static boolean verifyBulkSellIntent(PostIntentEvent event){
        Permit2Tag permit2Tag = event.getPermit2Tag();
        String permit2Signature = permit2Tag.getSignature();
        String permit2Json = getBulkSellPermit2StructuredData(event);
        String expectedPermit2Address = permit2Tag.getWalletAddress();
        if(!verifyEip712Signature(permit2Json, permit2Signature, expectedPermit2Address, event)){
            return false;
        }

        EIP712Tag eip712Tag = event.getEip712Tag();
        String intentSig = eip712Tag.getSign();
        String intentJson = getIntentStructuredData(event);
        String expectedIntentAddress = eip712Tag.getWalletAddress();
        return verifyEip712Signature(intentJson, intentSig, expectedIntentAddress, event);
    }

//    private static boolean verifyPermit2(PostIntentEvent postIntentEvent) {
//        Permit2Tag permit2Tag = postIntentEvent.getPermit2Tag();
//        String signature = permit2Tag.getSignature();
//        String structuredDataJson = createPostStructuredDataJson(postIntentEvent, true);
//        try {
//            String expectedAddress = permit2Tag.getWalletAddress();
//            log.info("permit2-event-id:{}, structDataJson1:{}, event1:{}", postIntentEvent.getId(), structuredDataJson, postIntentEvent);
//            StructuredDataEncoder encoder = new StructuredDataEncoder(structuredDataJson);
//            byte[] messageHash = encoder.hashStructuredData();
//            log.info("permit2-event-id:{}, structDataJson:{}, hash:{}, event:{}", postIntentEvent.getId(), structuredDataJson, org.web3j.utils.Numeric.toHexString(messageHash), postIntentEvent);
//            return verifySignature(messageHash, signature, expectedAddress);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private static boolean verifyEip712Signature(String structuredDataJson, String signature, String expectedAddress, NIP77Event event) {
        try {
            log.info("permit2-event-id:{}, structDataJson1:{}, event1:{}", event.getId(), structuredDataJson, event);
            StructuredDataEncoder encoder = new StructuredDataEncoder(structuredDataJson);
            byte[] messageHash = encoder.hashStructuredData();
            log.info("permit2-event-id:{}, structDataJson:{}, hash:{}, event:{}", event.getId(), structuredDataJson, org.web3j.utils.Numeric.toHexString(messageHash), event);
            return verifySignature(messageHash, signature, expectedAddress);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean verifySignature(byte[] messageHash, String signature, String expectedAddress) {
        Sign.SignatureData signatureData = parseHexSignature(signature);
        for (int recoveryId = 0; recoveryId < 4; recoveryId++) {
            try {
                BigInteger publicKey = Sign.recoverFromSignature(
                        (byte) recoveryId,
                        new ECDSASignature(
                                new BigInteger(1, signatureData.getR()),
                                new BigInteger(1, signatureData.getS())
                        ),
                        messageHash
                );
                if (publicKey != null && expectedAddress.equalsIgnoreCase("0x" + Keys.getAddress(publicKey))) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * 解析十六进制签名为 SignatureData 对象
     */
    private static Sign.SignatureData parseHexSignature(String hexSignature) {
        byte[] signatureBytes = Numeric.hexStringToByteArray(hexSignature);

        if (signatureBytes.length != 65) {
            throw new IllegalArgumentException("签名必须为65字节");
        }

        // 分割 R(32字节) + S(32字节) + V(1字节)
        byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
        byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);
        byte v = signatureBytes[64];

        // 调整 V 值（如果需要）
        if (v < 27) {
            v += 27;
        }

        return new Sign.SignatureData(v, r, s);
    }

    private static java.util.Map<String, String> createType(String name, String type) {
        java.util.Map<String, String> typeMap = new java.util.LinkedHashMap<>();
        typeMap.put("name", name);
        typeMap.put("type", type);
        return typeMap;
    }

//    private static String createPostStructuredDataJson(PostIntentEvent event, boolean isPermit2) {
//        IntentType intentType = event.getSideTag().getIntentType();
//        if(intentType.equals(IntentType.BULK_SELL) && isPermit2) {
//            return getBulkSellPermit2StructuredData(event);
//        }
//        switch (intentType) {
//            case BUYER_INTENT, BULK_SELL -> {
//                return getIntentStructuredData(event);
//            }
//            case SIGNATURE_SELL -> {
//                return getSignatureSellStructuredData(event);
//            }
//        }
//        return null;
//    }

    /**
     * bulk sell Permit2结构化数据
     * {
     *   "domain": {
     *     "name": "Permit2",
     *     "chainId": 11155111,
     *     "verifyingContract": "0x000000000022d473030f116ddee9f6b43ac78ba3"
     *   },
     *   "message": {
     *     "details": {
     *       "token": "0x1c7D4B196Cb0C7B01d743Fbc6116a902379C7238",
     *       "amount": "1",
     *       "expiration": "1764829826",
     *       "nonce": "11155111"
     *     },
     *     "spender": "0x53104d304898b00609dfad6c159513a430f80da6",
     *     "sigDeadline": "1764829826"
     *   },
     *   "primaryType": "PermitSingle",
     *   "types": {
     *     "EIP712Domain": [
     *       {
     *         "name": "name",
     *         "type": "string"
     *       },
     *       {
     *         "name": "chainId",
     *         "type": "uint256"
     *       },
     *       {
     *         "name": "verifyingContract",
     *         "type": "address"
     *       }
     *     ],
     *     "PermitSingle": [
     *       {
     *         "name": "details",
     *         "type": "PermitDetails"
     *       },
     *       {
     *         "name": "spender",
     *         "type": "address"
     *       },
     *       {
     *         "name": "sigDeadline",
     *         "type": "uint256"
     *       }
     *     ],
     *     "PermitDetails": [
     *       {
     *         "name": "token",
     *         "type": "address"
     *       },
     *       {
     *         "name": "amount",
     *         "type": "uint160"
     *       },
     *       {
     *         "name": "expiration",
     *         "type": "uint48"
     *       },
     *       {
     *         "name": "nonce",
     *         "type": "uint48"
     *       }
     *     ]
     *   }
     * }
     * @param event
     * @return
     */
    private static String getBulkSellPermit2StructuredData(PostIntentEvent event) {
        Map<String, Object> structuredData = new LinkedHashMap<>();
        // 1. 定义所有类型（包括嵌套结构）
        Map<String, List<Map<String, String>>> types = new LinkedHashMap<>();
        Permit2Tag permit2Tag = event.getPermit2Tag();
        TokenTag tokenTag = event.getTokenTag();

        List<Map<String, String>> domainType = createNoVDomainTypes();
        types.put("EIP712Domain", domainType);

        List<Map<String, String>> permitDetailsType = getPermitDetailsType();
        types.put("PermitDetails", permitDetailsType);

        List<Map<String, String>> permitSingleType = getPermitSingleType();
        types.put("PermitSingle", permitSingleType);

        Map<String, Object> domainMap = new LinkedHashMap<>();
        domainMap.put("name", permit2Tag.getDomainAppName());
        domainMap.put("chainId", tokenTag.getChainId());
        domainMap.put("verifyingContract", permit2Tag.getContractAddress());
        structuredData.put("domain", domainMap);

        Map<String, Object> permitDetailsMap = new LinkedHashMap<>();
        permitDetailsMap.put("token", tokenTag.getAddress());
        permitDetailsMap.put("amount", tokenTag.getAmount().toPlainString());
        permitDetailsMap.put("expiration", tokenTag.getExpiryTime());
        permitDetailsMap.put("nonce", permit2Tag.getNonce());

        Map<String, Object> messageMap = new LinkedHashMap<>();
        messageMap.put("details", permitDetailsMap);
        messageMap.put("spender", permit2Tag.getSpender());
        messageMap.put("sigDeadline", tokenTag.getExpiryTime());

        structuredData.put("message", messageMap);
        structuredData.put("primaryType", "PermitSingle");
        structuredData.put("types", types);

        return gson.toJson(structuredData);
    }

    /**
     * PermitTransferFrom(
     *  TokenPermissions permitted,
     *  address spender,
     *  uint256 nonce,
     *  uint256 deadline
     *  )TokenPermissions(
     *      address token,
     *      uint256 amount
     *   )
     * @param event
     * @return
     */
    private static String getSignatureSellTakeStructuredData(TakeIntentEvent event) {
        Map<String, Object> structuredData = new LinkedHashMap<>();
        // 1. 定义所有类型（包括嵌套结构）
        Map<String, List<Map<String, String>>> types = new LinkedHashMap<>();

        TokenTag tokenTag = event.getTokenTag();
        LimitTag limitTag = event.getLimitTag();
        Permit2Tag permit2Tag = event.getPermit2Tag();
        TakeTag takeTag = event.getTakeTag();

        // EIP712Domain 类型定义
        List<Map<String, String>> domainType = createNoVDomainTypes();
        types.put("EIP712Domain", domainType);

        List<Map<String, String>> tokenPermissionsType = new ArrayList<>();
        tokenPermissionsType.add(createType("token", "address"));
        tokenPermissionsType.add(createType("amount", "uint256"));

        List<Map<String, String>> permitTransferFromType = new ArrayList<>();
        permitTransferFromType.add(createType("permitted", "TokenPermissions"));
        permitTransferFromType.add(createType("spender", "address"));
        permitTransferFromType.add(createType("nonce", "uint256"));
        permitTransferFromType.add(createType("deadline", "uint256"));

        types.put("TokenPermissions", tokenPermissionsType);
        types.put("PermitTransferFrom", permitTransferFromType);

        // 3. 域数据
        Map<String, Object> domainMap = new LinkedHashMap<>();
        domainMap.put("name", permit2Tag.getDomainAppName());
        domainMap.put("chainId", tokenTag.getChainId());
        domainMap.put("verifyingContract", permit2Tag.getContractAddress());
        structuredData.put("domain", domainMap);

        Map<String, Object> tokenPermissionsMap = new LinkedHashMap<>();
        tokenPermissionsMap.put("token", tokenTag.getAddress());
        tokenPermissionsMap.put("amount", takeTag.getVolume().toPlainString());

        Map<String, Object> permitTransferFromMap = new LinkedHashMap<>();
        permitTransferFromMap.put("permitted", tokenPermissionsMap);
        permitTransferFromMap.put("spender", permit2Tag.getSpender());
        permitTransferFromMap.put("nonce", permit2Tag.getNonce());
        permitTransferFromMap.put("deadline", tokenTag.getExpiryTime());

        structuredData.put("message", permitTransferFromMap);
        structuredData.put("primaryType", "PermitWitnessTransferFrom");
        structuredData.put("types", types);

        return gson.toJson(structuredData);
    }

    private static String getSignatureSellStructuredData(PostIntentEvent event) {
        Map<String, Object> structuredData = new LinkedHashMap<>();
        // 1. 定义所有类型（包括嵌套结构）
        Map<String, List<Map<String, String>>> types = new LinkedHashMap<>();

        TokenTag tokenTag = event.getTokenTag();
        LimitTag limitTag = event.getLimitTag();
        Permit2Tag permit2Tag = event.getPermit2Tag();
        QuoteTag quoteTag = event.getQuoteTag();
        PaymentTag paymentTag = event.getPaymentTags().get(0);

        // EIP712Domain 类型定义
        List<Map<String, String>> domainType = createNoVDomainTypes();
        types.put("EIP712Domain", domainType);

        // IntentRange 类型定义
        List<Map<String, String>> rangeType = getRangeType();

        // IntentParams 类型定义 - 包含对 IntentRange 的引用
        List<Map<String, String>> intentParamsType = getIntentParamsType();

        List<Map<String, String>> tokenPermissionsType = new ArrayList<>();
        tokenPermissionsType.add(createType("token", "address"));
        tokenPermissionsType.add(createType("amount", "uint256"));

        List<Map<String, String>> permitWitnessTransferFromType = new ArrayList<>();
        permitWitnessTransferFromType.add(createType("permitted", "TokenPermissions"));
        permitWitnessTransferFromType.add(createType("spender", "address"));
        permitWitnessTransferFromType.add(createType("nonce", "uint256"));
        permitWitnessTransferFromType.add(createType("deadline", "uint256"));
        permitWitnessTransferFromType.add(createType("witness", "IntentParams"));

        types.put("IntentParams", intentParamsType);
        types.put("TokenPermissions", tokenPermissionsType);
        types.put("PermitWitnessTransferFrom", permitWitnessTransferFromType);
        types.put("Range", rangeType);

        // 3. 域数据
        Map<String, Object> domainMap = new LinkedHashMap<>();
        domainMap.put("name", permit2Tag.getDomainAppName());
        domainMap.put("chainId", tokenTag.getChainId());
        domainMap.put("verifyingContract", permit2Tag.getContractAddress());
        structuredData.put("domain", domainMap);

        Map<String, Object> rangeMap = new LinkedHashMap<>();
        rangeMap.put("min", limitTag.getLowLimit().toPlainString());
        rangeMap.put("max", limitTag.getUpLimit().toPlainString());

        Map<String, Object> tokenPermissionsMap = new LinkedHashMap<>();
        tokenPermissionsMap.put("token", tokenTag.getAddress());
        tokenPermissionsMap.put("amount", tokenTag.getAmount().toPlainString());

        Map<String, Object> intentParamsMap = new LinkedHashMap<>();
        intentParamsMap.put("token", tokenTag.getAddress());
        intentParamsMap.put("range", rangeMap);
        intentParamsMap.put("expiryTime", tokenTag.getExpiryTime());
        intentParamsMap.put("currency", keccak256(quoteTag.getCurrency()));
        intentParamsMap.put("paymentMethod", keccak256(paymentTag.getMethod()));
        intentParamsMap.put("payeeDetails", keccak256(paymentTag.getAccount() + paymentTag.getQrCode() + paymentTag.getMemo()));
        intentParamsMap.put("price", quoteTag.getNumber().toPlainString());

        Map<String, Object> permitWitnessTransferFromMap = new LinkedHashMap<>();
        permitWitnessTransferFromMap.put("permitted", tokenPermissionsMap);
        permitWitnessTransferFromMap.put("spender", permit2Tag.getSpender());
        permitWitnessTransferFromMap.put("nonce", permit2Tag.getNonce());
        permitWitnessTransferFromMap.put("deadline", tokenTag.getExpiryTime());
        permitWitnessTransferFromMap.put("witness", intentParamsMap);

        structuredData.put("message", permitWitnessTransferFromMap);
        structuredData.put("primaryType", "PermitWitnessTransferFrom");
        structuredData.put("types", types);

        return gson.toJson(structuredData);
    }

    private static String getIntentStructuredData(PostIntentEvent postIntentEvent) {
        Map<String, Object> structuredData = new LinkedHashMap<>();
        // 1. 定义所有类型（包括嵌套结构）
        Map<String, List<Map<String, String>>> types = new LinkedHashMap<>();

        EIP712Tag eip712Tag = postIntentEvent.getEip712Tag();
        TokenTag tokenTag = postIntentEvent.getTokenTag();
        LimitTag limitTag = postIntentEvent.getLimitTag();
        QuoteTag quoteTag = postIntentEvent.getQuoteTag();
        PaymentTag paymentTag = postIntentEvent.getPaymentTags().get(0);

        // EIP712Domain 类型定义
        List<Map<String, String>> domainType = createDomainTypes();
        types.put("EIP712Domain", domainType);

        // IntentRange 类型定义
        List<Map<String, String>> rangeType = getRangeType();

        // IntentParams 类型定义 - 包含对 IntentRange 的引用
        List<Map<String, String>> intentParamsType = getIntentParamsType();

        types.put("IntentParams", intentParamsType);
        types.put("Range", rangeType);

        // 3. 域数据
        Map<String, Object> domainMap = new LinkedHashMap<>();
        domainMap.put("name", eip712Tag.getDomainAppName());
        domainMap.put("version", eip712Tag.getDomainVersion());
        domainMap.put("chainId", tokenTag.getChainId());
        domainMap.put("verifyingContract", eip712Tag.getContractAddress());
        structuredData.put("domain", domainMap);

        Map<String, Object> rangeMap = new LinkedHashMap<>();
        rangeMap.put("min", limitTag.getLowLimit().toPlainString());
        rangeMap.put("max", limitTag.getUpLimit().toPlainString());

        // 4. 消息数据
        Map<String, Object> messageMap = new LinkedHashMap<>();
        messageMap.put("token", tokenTag.getAddress());
        messageMap.put("range", rangeMap);
        messageMap.put("expiryTime", tokenTag.getExpiryTime());
        messageMap.put("currency", keccak256(quoteTag.getCurrency()));
        messageMap.put("paymentMethod", keccak256(paymentTag.getMethod()));
        messageMap.put("payeeDetails", keccak256(paymentTag.getAccount() + paymentTag.getQrCode() + paymentTag.getMemo()));
        messageMap.put("price", quoteTag.getNumber().toPlainString());

        structuredData.put("message", messageMap);
        structuredData.put("primaryType", "IntentParams");
        structuredData.put("types", types);

        return gson.toJson(structuredData);
    }

    private static List<Map<String, String>> createDomainTypes() {
        List<Map<String, String>> domainType = new ArrayList<>();
        domainType.add(createType("name", "string"));
        domainType.add(createType("version", "string"));
        domainType.add(createType("chainId", "uint256"));
        domainType.add(createType("verifyingContract", "address"));
        return domainType;
    }

    private static List<Map<String, String>> createNoVDomainTypes() {
        List<Map<String, String>> domainType = new ArrayList<>();
        domainType.add(createType("name", "string"));
        domainType.add(createType("chainId", "uint256"));
        domainType.add(createType("verifyingContract", "address"));
        return domainType;
    }

    private static List<Map<String, String>> getPermitSingleType() {
        List<Map<String, String>> permitSingleType = new ArrayList<>();
        permitSingleType.add(createType("details", "PermitDetails"));
        permitSingleType.add(createType("spender", "address"));
        permitSingleType.add(createType("sigDeadline", "uint256"));
        return permitSingleType;
    }

    private static List<Map<String, String>> getPermitDetailsType() {
        List<Map<String, String>> permitDetailsType = new ArrayList<>();
        permitDetailsType.add(createType("token", "address"));
        permitDetailsType.add(createType("amount", "uint160"));
        permitDetailsType.add(createType("expiration", "uint48"));
        permitDetailsType.add(createType("nonce", "uint48"));
        return permitDetailsType;
    }

    private static List<Map<String, String>> getRangeType() {
        List<Map<String, String>> rangeType = new ArrayList<>();
        rangeType.add(createType("min", "uint256"));
        rangeType.add(createType("max", "uint256"));
        return rangeType;
    }

    private static List<Map<String, String>> getIntentParamsType() {
        List<Map<String, String>> intentParamsType = new ArrayList<>();
        intentParamsType.add(createType("token", "address"));
        intentParamsType.add(createType("range", "Range"));
        intentParamsType.add(createType("expiryTime", "uint64"));
        intentParamsType.add(createType("currency", "bytes32"));
        intentParamsType.add(createType("paymentMethod", "bytes32"));
        intentParamsType.add(createType("payeeDetails", "bytes32"));
        intentParamsType.add(createType("price", "uint256"));
        return intentParamsType;
    }

    private static List<Map<String, String>> getEscrowParamsType() {
        /**
         * {
         *   "EIP712Domain": [
         *     {
         *       "name": "name",
         *       "type": "string"
         *     },
         *     {
         *       "name": "version",
         *       "type": "string"
         *     },
         *     {
         *       "name": "chainId",
         *       "type": "uint256"
         *     },
         *     {
         *       "name": "verifyingContract",
         *       "type": "address"
         *     }
         *   ],
         *   "EscrowParams": [
         *     {
         *       "name": "id",
         *       "type": "uint256"
         *     },
         *     {
         *       "name": "token",
         *       "type": "address"
         *     },
         *     {
         *       "name": "volume",
         *       "type": "uint256"
         *     },
         *     {
         *       "name": "price",
         *       "type": "uint256"
         *     },
         *     {
         *       "name": "usdRate",
         *       "type": "uint256"
         *     },
         *     {
         *       "name": "payer",
         *       "type": "address"
         *     },
         *     {
         *       "name": "seller",
         *       "type": "address"
         *     },
         *     {
         *       "name": "sellerFeeRate",
         *       "type": "uint256"
         *     },
         *     {
         *       "name": "paymentMethod",
         *       "type": "bytes32"
         *     },
         *     {
         *       "name": "currency",
         *       "type": "bytes32"
         *     }{
         *       "name": "payeeDetails",
         *       "type": "bytes32"
         *     },
         *     {
         *       "name": "buyer",
         *       "type": "address"
         *     },
         *     {
         *       "name": "buyerFeeRate",
         *       "type": "uint256"
         *     }
         *   ]
         * }
         * }
         */
        List<Map<String, String>> intentParamsType = new ArrayList<>();
        intentParamsType.add(createType("id", "uint256"));
        intentParamsType.add(createType("token", "address"));
        intentParamsType.add(createType("volume", "uint256"));
        intentParamsType.add(createType("price", "uint256"));
        intentParamsType.add(createType("usdRate", "uint256"));
        intentParamsType.add(createType("payer", "address"));
        intentParamsType.add(createType("seller", "address"));
        intentParamsType.add(createType("sellerFeeRate","uint256"));
        intentParamsType.add(createType("paymentMethod", "bytes32"));
        intentParamsType.add(createType("currency", "bytes32"));
        intentParamsType.add(createType("payeeDetails", "bytes32"));
        intentParamsType.add(createType("buyer", "address"));
        intentParamsType.add(createType("buyerFeeRate", "uint256"));
        return intentParamsType;
    }


    public static void main(String[] args){
        validateSignatureSell();
        System.out.println("validateEscrowParams="+validateEscrowParams());

        PostIntentEvent event = new PostIntentEvent();
        TokenTag tokenTag = new TokenTag("USDC", "11155111", "sepolia", "0x1c7D4B196Cb0C7B01d743Fbc6116a902379C7238", BigDecimal.TEN, new BigInteger("11155111"), "1764829826", BigDecimal.TEN);
        LimitTag limitTag = new LimitTag(BigDecimal.valueOf(1000000L), BigDecimal.valueOf(1000000L));
        Permit2Tag permit2Tag = new Permit2Tag("11155111", "0x5a41235a9127cd6a85e3ee3afcb41e26d50b0c020d2dc8c29ebfd294300bf0b815a555484204fff2996276fac6a4e5a485465a4fc893370a6f652b790ee19fef1b", "0xD58382f295f5c98BAeB525FAbb7FEBcCc62bc63B", "0x53104d304898b00609dfad6c159513a430f80da6", "0x000000000022d473030f116ddee9f6b43ac78ba3", "0x000000000022d473030f116ddee9f6b43ac78ba3", "Permit2");
        EIP712Tag eip712Tag = new EIP712Tag("0xD58382f295f5c98BAeB525FAbb7FEBcCc62bc63B", "0xd5379dca1bf8c1d204121374b3f8d8fbf7c6605e", "MainUserTxn", "1", "0x5a41235a9127cd6a85e3ee3afcb41e26d50b0c020d2dc8c29ebfd294300bf0b815a555484204fff2996276fac6a4e5a485465a4fc893370a6f652b790ee19fef1b");
        event.setEip712Tag(eip712Tag);
        event.setPermit2Tag(permit2Tag);
        event.setTokenTag(tokenTag);
        event.setLimitTag(limitTag);
        event.setPaymentTags(List.of(new PaymentTag("wechat", "dust", "wxp://f2f0in9xnsA4G_eXWBRORK63ixD6bMQcP11eKGFz1VS4Kf0", "memo")));
        event.setQuoteTag(new QuoteTag(new BigDecimal("1000000000000000000"), "USD", new BigDecimal("1000000000000000000"), new BigInteger("1761237799"), "", 0));
        System.out.println(getIntentStructuredData(event));
        System.out.println(getBulkSellPermit2StructuredData(event));
    }

    private static boolean validateEscrowParams(){
        String domainName = "MainnetUserTxn";
        String domainVersion = "1";
        int chainId = 11155111;
        String verifyContract = "0xd5379dca1bf8c1d204121374b3f8d8fbf7c6605e";
        int tradeId = 1;
        String tokenAddr = "0x1c7D4B196Cb0C7B01d743Fbc6116a902379C7238";
        BigDecimal volume = new BigDecimal("1000000");
        BigDecimal price = new BigDecimal("1000000000000000000");
        BigDecimal usdRate = new BigDecimal("0");
        String seller = "0x58344547f5C5eDbc8a15Ba2584089b30A04ca0E3";
        String payer = "0x58344547f5C5eDbc8a15Ba2584089b30A04ca0E3";
        String sellerFeeRate = "0";
        String paymentMethod = "wechat";
        String currency = "USD";
        String buyer = "0x58344547f5C5eDbc8a15Ba2584089b30A04ca0E3";
        String buyerFeeRate = "0";
        String account = "dust";
        String qrCode = "wxp://f2f0in9xnsA4G_eXWBRORK63ixD6bMQcP11eKGFz1VS4Kf0";
        String memo = "memo";
//        String signature = "0x18e58d3647bcf7f1d627fbd3659e9517227f1169f5b3140851a9c63b7cd681617519064c83cddf17bf6c773f71096d25966d76b224f0e64019fa5a7eb4da536f1b";
        String signature = "0x217dc98fe27bb191e7378fe8f6260bc7e61b311b231cab2b54f1ad45e9a2f619590372a0a67b899ad0e0b8a04faa24f904cf5a7a84afddf87e8c736f5aabbb371b";
//        String expectedAddress = "0x58344547f5C5eDbc8a15Ba2584089b30A04ca0E3";
        String expectedAddress = "0xd58382f295f5c98baeb525fabb7febccc62bc63b";
        String structuredDataJson = getEscrowParamsEip712Struct(domainName,
                domainVersion,
                chainId,
                verifyContract,
                tradeId,
                tokenAddr,
                volume,
                price,
                usdRate,
                seller,
                payer,
                sellerFeeRate,
                paymentMethod,
                currency,
                buyer,
                buyerFeeRate,
                account,
                qrCode,
                memo);
        StructuredDataEncoder encoder = null;
        try {
            encoder = new StructuredDataEncoder(structuredDataJson);
            byte[] messageHash = encoder.hashStructuredData();
            log.info("event-id:{}, structDataJson:{}, hash:{}", tradeId, structuredDataJson, org.web3j.utils.Numeric.toHexString(messageHash));
            return verifySignature(messageHash, signature, expectedAddress);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void validateSignatureSell(){
        PublicKey pk = new PublicKey("107a920c39760225f1b2494121093ded75dae5363321d456f8922227f0539607");
        List<BaseTag> tags = List.of(
            new EIP712Tag("0xD58382f295f5c98BAeB525FAbb7FEBcCc62bc63B", "0xd5379dca1bf8c1d204121374b3f8d8fbf7c6605e", "MainUserTxn", "1", "0x5a41235a9127cd6a85e3ee3afcb41e26d50b0c020d2dc8c29ebfd294300bf0b815a555484204fff2996276fac6a4e5a485465a4fc893370a6f652b790ee19fef1b"),
            //["token","WETH","ethereum",11155111,"Sepolia","0xfFf9976782d46CC05630D1f6eBAb18b2324d6B14","1761237799",1000000000000000000]
            new TokenTag("USDT", "ethereum", "sepolia", "0xaA8E23Fb1079EA71e0a56F48a2aA51851D8433D0", new BigDecimal(1000000L), BigInteger.valueOf(11155111), "1761904920", new BigDecimal(5000L)),
            // ["quote","3.221E+21","USD","1E+18",""]
            new QuoteTag(new BigDecimal("1000000000000000000"), "USD", new BigDecimal("1000000000000000000"), new BigInteger("1761237799"), "", 0),
            new MakeTag(Side.SELL, "", pk.toString(), IntentType.SIGNATURE_SELL),
            new Permit2Tag("270178257646664", "0x5a41235a9127cd6a85e3ee3afcb41e26d50b0c020d2dc8c29ebfd294300bf0b815a555484204fff2996276fac6a4e5a485465a4fc893370a6f652b790ee19fef1b", "0xD58382f295f5c98BAeB525FAbb7FEBcCc62bc63B", "0x1e4d58c5a97ab35c614a90ab04acc78711729f18", "0xD58382f295f5c98BAeB525FAbb7FEBcCc62bc63B", "0x000000000022d473030f116ddee9f6b43ac78ba3", "Permit2"),
            new LimitTag(BigDecimal.valueOf(1000000L), BigDecimal.valueOf(1000000L)),
            new PaymentTag("wechat", "dust", "wxp://f2f0in9xnsA4G_eXWBRORK63ixD6bMQcP11eKGFz1VS4Kf0", "memo")
        );
        PostIntentEvent e = new PostIntentEvent(pk, tags, "ccc");
        System.out.println(verifySignature(e, SignerType.POST_EVENT));
    }

    private static String getEscrowParamsEip712Struct(
            String domainName,
            String domainVersion,
            int chainId,
            String verifyContract,
            int tradeId,
            String tokenAddr,
            BigDecimal volume,
            BigDecimal price,
            BigDecimal usdRate,
            String seller,
            String payer,
            String sellerFeeRate,
            String paymentMethod,
            String currency,
            String buyer,
            String buyerFeeRate,
            String account,
            String qrCode,
            String memo

    ){
        Map<String, Object> structuredData = new LinkedHashMap<>();
        // 1. 定义所有类型（包括嵌套结构）
        Map<String, List<Map<String, String>>> types = new LinkedHashMap<>();



        // EIP712Domain 类型定义
        List<Map<String, String>> domainType = createDomainTypes();
        types.put("EIP712Domain", domainType);

        // IntentParams 类型定义 - 包含对 IntentRange 的引用
        List<Map<String, String>> escrowParamsType = getEscrowParamsType();

        types.put("EscrowParams", escrowParamsType);
        // 3. 域数据
        Map<String, Object> domainMap = new LinkedHashMap<>();
        domainMap.put("name", domainName);
        domainMap.put("version", domainVersion);
        domainMap.put("chainId", chainId);
        domainMap.put("verifyingContract", verifyContract);
        structuredData.put("domain", domainMap);

        /**
         * message['id'] = tag[1]
         *             message['token'] = tag[2]
         *             message['volume'] = tag[3]
         *             message['price'] = tag[4]
         *             message['usdRate'] = tag[5]
         *             message['payer'] = tag[6]
         *             message['seller'] = tag[7]
         *             message['sellerFeeRate'] = tag[8]
         *             message['paymentMethod'] = tag[9]
         *             message['currency'] = tag[10]
         *             message['buyer'] = tag[11]
         *             message['buyerFeeRate'] = tag[12]
         *
         *             account = tag[13]
         *             qr_code = tag[14]
         *             memo = tag[15]
         *             message['payeeDetails'] = account + qr_code + memo
         */
        // 4. 消息数据
        Map<String, Object> messageMap = new LinkedHashMap<>();
        messageMap.put("id", tradeId);
        messageMap.put("token", tokenAddr);
        messageMap.put("volume", volume.toPlainString());
        messageMap.put("price", price.toPlainString());
        messageMap.put("usdRate", usdRate.toPlainString());
        messageMap.put("payer", payer);
        messageMap.put("seller", seller);
        messageMap.put("sellerFeeRate", sellerFeeRate);
        messageMap.put("paymentMethod", keccak256(paymentMethod));
        messageMap.put("currency", keccak256(currency));
        messageMap.put("buyer", buyer);
        messageMap.put("buyerFeeRate", buyerFeeRate);
        messageMap.put("payeeDetails", keccak256(account + qrCode + memo));

        structuredData.put("message", messageMap);
        structuredData.put("primaryType", "EscrowParams");
        structuredData.put("types", types);

        return gson.toJson(structuredData);
    }
}