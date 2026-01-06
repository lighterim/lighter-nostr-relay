package com.prosilion.superconductor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "blockchain")
public class TokenConfig {

    /**
     * 外层 Key 是 Chain ID (如 "11155111")
     * 内层 Key 是 Token 符号 (如 "usdc")
     */
    private Map<String, Map<String, TokenDetail>> networks;

    public static final int PRICE_DECIMALS = 18;

    @Data
    public static class TokenDetail {
        private String address;
        private Integer decimals;
    }

    // 辅助方法：方便根据 chainId 和 symbol 获取配置
    public TokenDetail getToken(String chainId, String symbol) {
        if (networks != null && networks.containsKey(chainId)) {
            return networks.get(chainId).get(symbol.toLowerCase());
        }
        return null;
    }

    public int getDecimals(String chainId, String symbol) {
        if (networks != null && networks.containsKey(chainId)) {
            String lowerSymbol = symbol.toLowerCase();
            if (networks.get(chainId).containsKey(lowerSymbol)) {
                return networks.get(chainId).get(lowerSymbol).getDecimals();
            }
        }
        return 0;
    }
}