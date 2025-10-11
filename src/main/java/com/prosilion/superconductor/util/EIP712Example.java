package com.prosilion.superconductor.util;

import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

public class EIP712Example {

    public static void main(String[] args) {
        try {
            // 生成测试密钥对
            String privateKey = "199b1a34d5cd548314842f6996456bb2a930c3763193dbe900192f993321ea43"; // 例如: "abc123..."
            String expectedAddress = "0xD58382f295f5c98BAeB525FAbb7FEBcCc62bc63B"; // 例如: "0x04..."


            String dataJson = "{\"domain\":{\"name\":\"MainnetUserTxn\",\"version\":\"1\",\"chainId\":11155111,\"verifyingContract\":\"0xecd4fff6ea02f004143f9cf0b9546964800b16aa\"},\"message\":{\"timestamp\":\"1760355495\",\"price\":\"10\"},\"primaryType\":\"PriceParams\",\"types\":{\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}],\"PriceParams\":[{\"name\":\"timestamp\",\"type\":\"string\"},{\"name\":\"price\",\"type\":\"uint256\"}]}}";

            // 签名
            Sign.SignatureData signature = EIP712Signer.signMessage(
                    privateKey, dataJson
            );

            System.out.println("\n=== 签名结果 ===");
            System.out.println("R: " + Numeric.toHexString(signature.getR()));
            System.out.println("S: " + Numeric.toHexString(signature.getS()));
            System.out.println("V: " + (signature.getV()[0] & 0xFF));

            // 确保R和S都是32字节（64个十六进制字符）
            byte[] r = signature.getR();
            byte[] s = signature.getS();
            byte[] v = signature.getV();

            // 如果需要，可以填充前导零
            String rHex = Numeric.toHexString(r).substring(2); // 去掉0x前缀
            String sHex = Numeric.toHexString(s).substring(2);
            String vHex = String.format("%02x", v[0] & 0xFF);

            // 拼接完整的签名
            String fullSignature = "0x" + rHex + sHex + vHex;
            System.out.println("完整签名: " + fullSignature);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}