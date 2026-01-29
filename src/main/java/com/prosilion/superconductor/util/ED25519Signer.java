package com.prosilion.superconductor.util;

import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import java.util.HexFormat;
import org.bouncycastle.crypto.Signer;

public class ED25519Signer {

    public final static String publicKeyHex = "f8141e0fbd570817ff8e417a10027f1368ef4993edee3c9d2fdf4074aac78fa0";

    public static boolean verify(String message, String signatureHex) {
        try {
            // 将十六进制字符串转换为字节数组
            byte[] publicKeyBytes = HexFormat.of().parseHex(publicKeyHex);
            byte[] signatureBytes = HexFormat.of().parseHex(signatureHex);
            byte[] messageBytes = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);

            // 创建Ed25519公钥参数
            Ed25519PublicKeyParameters publicKey = new Ed25519PublicKeyParameters(publicKeyBytes, 0);

            // 创建签名验证器
            Signer verifier = new Ed25519Signer();
            verifier.init(false, publicKey);
            verifier.update(messageBytes, 0, messageBytes.length);

            // 验证签名
            return verifier.verifySignature(signatureBytes);

        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        String message = "111551110x1c7d4b196cb0c7b01d743fbc6116a902379c723817696584331.001520";
        String signature = "4b8f5ca71c0ccbae83bfa720bcfbeb5de65a319608c28891de4768eacaa24607831019307b9862c52a190b07c6e0bf5e3f1251cc2d097c9d4087ace74be8ad09";
        System.out.println(verify(message,signature));
    }

}
