package com.prosilion.superconductor.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.IntentType;
import nostr.event.NIP77Event;
import nostr.event.Side;
import nostr.event.impl.PostIntentEvent;
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
     */
    public static boolean verifySignature(NIP77Event event, SignerType signerType) {
        String structuredDataJson;
        EIP712Tag eip712Tag;
        String signature;
        if(signerType.equals(SignerType.POST_EVENT)) {
            PostIntentEvent postIntentEvent = (PostIntentEvent)event;
            eip712Tag = postIntentEvent.getEip712Tag();
            signature = postIntentEvent.getSideTag().getIntentType() == IntentType.SIGNATURE_SELL ? postIntentEvent.getPermit2Tag().getSignature() :eip712Tag.getSign();
            structuredDataJson = createPostStructuredDataJson(postIntentEvent);
        } else {
            return false;
        }
        try {
            String expectedAddress = eip712Tag.getWalletAddress();
            log.info("event-id:{}, structDataJson1:{}, event1:{}", event.getId(), structuredDataJson, event);
            StructuredDataEncoder encoder = new StructuredDataEncoder(structuredDataJson);
            byte[] messageHash = encoder.hashStructuredData();
            log.info("event-id:{}, structDataJson:{}, hash:{}, event:{}", event.getId(), structuredDataJson, org.web3j.utils.Numeric.toHexString(messageHash), event);
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

    private static String createPostStructuredDataJson(PostIntentEvent event) {
        IntentType intentType = event.getSideTag().getIntentType();
        switch (intentType) {
            case BUYER_INTENT, BULK_SELL -> {
                return getBuyerIntentStructuredData(event);
            }
            case SIGNATURE_SELL -> {
                return getSignatureSellStructuredData(event);
            }
        }
        return null;
    }

    private static String getSignatureSellStructuredData(PostIntentEvent event) {
        Map<String, Object> structuredData = new LinkedHashMap<>();
        // 1. 定义所有类型（包括嵌套结构）
        Map<String, List<Map<String, String>>> types = new LinkedHashMap<>();

        EIP712Tag eip712Tag = event.getEip712Tag();
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
        domainMap.put("name", eip712Tag.getDomainAppName());
        domainMap.put("chainId", tokenTag.getChainId());
        domainMap.put("verifyingContract", eip712Tag.getContractAddress());
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

    private static String getBuyerIntentStructuredData(PostIntentEvent event) {
        Map<String, Object> structuredData = new LinkedHashMap<>();
        // 1. 定义所有类型（包括嵌套结构）
        Map<String, List<Map<String, String>>> types = new LinkedHashMap<>();

        EIP712Tag eip712Tag = event.getEip712Tag();
        TokenTag tokenTag = event.getTokenTag();
        LimitTag limitTag = event.getLimitTag();
        QuoteTag quoteTag = event.getQuoteTag();
        PaymentTag paymentTag = event.getPaymentTags().get(0);

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


    public static void main(String[] args){
        validateSignatureSell();
    }

    private static void validateSignatureSell(){
        PublicKey pk = new PublicKey("b1fa4af2eec41f798352271d3e01c52457789e01586ab37b4f50bb74601569db");
        List<BaseTag> tags = List.of(
            new EIP712Tag("0xece12A4E213990bC00fe3BCf89E1Df5dF37D663e", "", "Permit2", "0x000000000022D473030F116dDEE9F6B43aC78BA3", ""),
            //["token","WETH","ethereum",11155111,"Sepolia","0xfFf9976782d46CC05630D1f6eBAb18b2324d6B14","1761237799",1000000000000000000]
            new TokenTag("USDC", "ethereum", "sepolia", "0xfFf9976782d46CC05630D1f6eBAb18b2324d6B14", new BigDecimal(1000000000000000000L), BigInteger.valueOf(11155111), "1761237799"),
            // ["quote","3.221E+21","USD","1E+18",""]
            new QuoteTag(new BigDecimal("3221000000000000000000"), "USD", new BigDecimal("1000000000000000000"), "1761237799", ""),
            new MakeTag(Side.SELL, "", pk.toString(), IntentType.SIGNATURE_SELL),
            new Permit2Tag("3", "0x6989c0044eeab23d55490b38b9d2a131a70748d022a177e1c44bb334fca516813e851e04a9db4a24de93785749b606e281a8bc228936907f25447da1150622be1c", "0x37Fb3a55652042d41Ff1e089C03Ff1fE45093CF4", "0x1e4d58c5a97ab35c614a90ab04acc78711729f18"),
            new LimitTag(BigDecimal.valueOf(100000000000000000L), BigDecimal.valueOf(1000000000000000000L)), new PaymentTag("wechat", "oren", "wxp://f2f0cFGOsdaOtU3SQfpyBcl_0u0UCU9AIIVaTEmmVDgvN-Q", "wechat")
        );
        PostIntentEvent e = new PostIntentEvent(pk, tags, "ccc");
        System.out.println(verifySignature(e, SignerType.POST_EVENT));
    }
}