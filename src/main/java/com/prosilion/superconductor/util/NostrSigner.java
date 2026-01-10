package com.prosilion.superconductor.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import org.bitcoinj.base.Bech32;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class NostrSigner {

    private static final String NOSTR_PRI_KEY = "nsec1vddt4v6g974ya7m3h0vjzckpyeh7nnllpxpx7l3f3k3yc93qdkssadqq95";

    private static final Gson gson = new GsonBuilder().create();
    private static final X9ECParameters CURVE = CustomNamedCurves.getByName("secp256k1");
    private static final BigInteger SECP256K1_N = CURVE.getN();

    public static void main(String[] args) {
        JsonArray tags = new JsonArray();
        JsonArray relayTag = new JsonArray();
        relayTag.add("relays");
        relayTag.add("wss://nostr-relay.lighter.im");
        tags.add(relayTag);

        String eventId = NostrSigner.getEventId("",
                System.currentTimeMillis() / 1000, 30027,
        tags, "test");
        System.out.println(eventId);
        System.out.println(NostrSigner.getEventSig(eventId));
    }

    public static String getEventSig(String eventIdHex) {
        try {
            byte[] privKeyBytes = Bech32.decodeBytes(NOSTR_PRI_KEY, "nsec", Bech32.Encoding.BECH32);
            byte[] eventId = Hex.decode(eventIdHex);
            byte[] sigBytes = signBIP340(eventId, privKeyBytes);

            return Hex.toHexString(sigBytes);
        } catch (Exception e) {
            throw new RuntimeException("Nostr 签名计算失败: " + e.getMessage(), e);
        }
    }

    public static String getEventId(String publicKey, long createdAt, int kind,
                                    JsonArray tags, String content) {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(0);
        jsonArray.add(publicKey);
        jsonArray.add(createdAt);
        jsonArray.add(kind);
        jsonArray.add(tags);
        jsonArray.add(content);
        String dataStr = gson.toJson(jsonArray);
        return sha256(dataStr);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 手动实现 BIP-340 Schnorr 签名 (Nostr 标准)
     */
    private static byte[] signBIP340(byte[] msg, byte[] privKey) {
        BigInteger d0 = new BigInteger(1, privKey);

        // 1. 计算公钥 P = d'*G
        ECPoint P = CURVE.getG().multiply(d0).normalize();

        // 2. 如果 P 的 y 坐标是奇数，私钥取反 (BIP-340 核心步骤)
        BigInteger d = P.getYCoord().testBitZero() ? SECP256K1_N.subtract(d0) : d0;
        byte[] pubKeyX = P.getAffineXCoord().getEncoded();

        // 3. 生成确定性 Nonce k (这里为了安全和一致性使用 BIP-340 推荐逻辑)
        // 简化处理：使用消息和私钥生成辅助随机性
        byte[] kBytes = taggedHash("BIP340/nonce", concat(privKey, pubKeyX, msg));
        BigInteger k0 = new BigInteger(1, kBytes).mod(SECP256K1_N);

        // 4. 计算 R = k'*G
        ECPoint R = CURVE.getG().multiply(k0).normalize();

        // 5. 如果 R 的 y 坐标是奇数，k 取反
        BigInteger k = R.getYCoord().testBitZero() ? SECP256K1_N.subtract(k0) : k0;
        byte[] rX = R.getAffineXCoord().getEncoded();

        // 6. 计算挑战值 e = Hash("BIP340/challenge" || R.x || P.x || msg)
        byte[] eBytes = taggedHash("BIP340/challenge", concat(rX, pubKeyX, msg));
        BigInteger e = new BigInteger(1, eBytes).mod(SECP256K1_N);

        // 7. 计算 s = (k + e*d) mod n
        BigInteger s = k.add(e.multiply(d)).mod(SECP256K1_N);

        // 8. 返回 64 字节: R.x (32字节) + s (32字节)
        byte[] sig = new byte[64];
        System.arraycopy(rX, 0, sig, 0, 32);
        byte[] sBytes = bigIntegerTo32Bytes(s);
        System.arraycopy(sBytes, 0, sig, 32, 32);

        return sig;
    }

    // --- 辅助工具方法 ---

    private static byte[] taggedHash(String tag, byte[] msg) {
        SHA256Digest digest = new SHA256Digest();
        byte[] tagHash = new byte[32];
        byte[] tagBytes = tag.getBytes(StandardCharsets.UTF_8);
        digest.update(tagBytes, 0, tagBytes.length);
        digest.doFinal(tagHash, 0);

        byte[] result = new byte[32];
        digest.update(tagHash, 0, 32);
        digest.update(tagHash, 0, 32);
        digest.update(msg, 0, msg.length);
        digest.doFinal(result, 0);
        return result;
    }

    private static byte[] concat(byte[]... arrays) {
        int len = 0;
        for (byte[] a : arrays) len += a.length;
        byte[] res = new byte[len];
        int pos = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, res, pos, a.length);
            pos += a.length;
        }
        return res;
    }

    private static byte[] bigIntegerTo32Bytes(BigInteger b) {
        byte[] res = new byte[32];
        byte[] src = b.toByteArray();
        if (src.length > 32) {
            System.arraycopy(src, src.length - 32, res, 0, 32);
        } else {
            System.arraycopy(src, 0, res, 32 - src.length, src.length);
        }
        return res;
    }
}
