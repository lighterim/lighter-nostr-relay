package com.prosilion.superconductor.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import nostr.event.NIP77Event;
import nostr.event.impl.PostIntentEvent;
import nostr.event.impl.TakeIntentEvent;
import nostr.event.tag.*;
import org.web3j.crypto.*;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
            signature = eip712Tag.getSign();
            structuredDataJson = createPostStructuredDataJson(postIntentEvent);
        } else {
            return false;
        }
        try {
            String expectedAddress = eip712Tag.getWalletAddress();
            StructuredDataEncoder encoder = new StructuredDataEncoder(structuredDataJson);
            byte[] messageHash = encoder.hashStructuredData();
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
        Map<String, Object> structuredData = new LinkedHashMap<>();

        // 1. 定义所有类型（包括嵌套结构）
        Map<String, List<Map<String, String>>> types = new LinkedHashMap<>();

        // EIP712Domain 类型定义
        List<Map<String, String>> domainType = createDomainTypes();
        types.put("EIP712Domain", domainType);

        // IntentRange 类型定义
        List<Map<String, String>> rangeType = new ArrayList<>();
        rangeType.add(createType("min", "uint256"));
        rangeType.add(createType("max", "uint256"));

        // IntentParams 类型定义 - 包含对 IntentRange 的引用
        List<Map<String, String>> paramsType = new ArrayList<>();
        paramsType.add(createType("token", "address"));
        paramsType.add(createType("range", "Range"));
        paramsType.add(createType("expiryTime", "uint64"));
        paramsType.add(createType("currency", "bytes32"));
        paramsType.add(createType("paymentMethod", "bytes32"));
        paramsType.add(createType("payeeDetails", "bytes32"));
        paramsType.add(createType("usdRate", "uint256"));
        paramsType.add(createType("price", "uint256"));

        types.put("IntentParams", paramsType);
        types.put("Range", rangeType);

        EIP712Tag eip712Tag = event.getEip712Tag();
        TokenTag tokenTag = event.getTokenTag();
        LimitTag limitTag = event.getLimitTag();
        QuoteTag quoteTag = event.getQuoteTag();
        PaymentTag paymentTag = event.getPaymentTags().get(0);
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
        messageMap.put("usdRate", quoteTag.getUsdRate().toPlainString());

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
}