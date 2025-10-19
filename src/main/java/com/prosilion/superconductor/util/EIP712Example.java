package com.prosilion.superconductor.util;

import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

public class EIP712Example {

    public static void main(String[] args) {
        try {
            // 生成测试密钥对
            String privateKey = "199b1a34d5cd548314842f6996456bb2a930c3763193dbe900192f993321ea43"; // 例如: "abc123..."
            String expectedAddress = "0xD58382f295f5c98BAeB525FAbb7FEBcCc62bc63B"; // 例如: "0x04..."


            String dataJson = "{\"domain\":{\"name\":\"MainnetUserTxn\",\"version\":\"1\",\"chainId\":11155111,\"verifyingContract\":\"0xecd4fff6ea02f004143f9cf0b9546964800b16aa\"},\"message\":{\"token\":\"0x1c7d4b196cb0c7b01d743fbc6116a902379c7238\",\"range\":{\"min\":\"1000000\",\"max\":\"100000000\"},\"expiryTime\":\"1760355495\",\"currency\":\"0xc4ae21aac0c6549d71dd96035b7e0bdb6c79ebdba8891b666115bc976d16a29e\",\"paymentMethod\":\"0xcac9daea62d7b89d75ac73af4ee14dcf25721012ae82b568c2ea5c808eaa04ff\",\"payeeDetails\":\"0x6a9692f0895ad75b54be50c6d31f240bf7ca223ed73a5edde22aff95851dfdff\",\"price\":\"1000000000000000000\",\"usdRate\":71900000},\"primaryType\":\"IntentParams\",\"types\":{\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}],\"IntentParams\":[{\"name\":\"token\",\"type\":\"address\"},{\"name\":\"range\",\"type\":\"Range\"},{\"name\":\"expiryTime\",\"type\":\"uint64\"},{\"name\":\"currency\",\"type\":\"bytes32\"},{\"name\":\"paymentMethod\",\"type\":\"bytes32\"},{\"name\":\"payeeDetails\",\"type\":\"bytes32\"},{\"name\":\"usdRate\",\"type\":\"uint256\"},{\"name\":\"price\",\"type\":\"uint256\"}],\"Range\":[{\"name\":\"min\",\"type\":\"uint256\"},{\"name\":\"max\",\"type\":\"uint256\"}]}}";

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