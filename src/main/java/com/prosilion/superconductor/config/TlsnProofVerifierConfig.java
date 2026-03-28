package com.prosilion.superconductor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "verifier")
public class TlsnProofVerifierConfig {

    /**
     * 外层 Key 是 payment.method(如: "wise")
     * 内层 Key 是 Chain ID (如 "11155111")
     */
    private Map<String, Map<String, Eip712Domain>> methods;

    @Data
    public static class Eip712Domain {
        private String address;
        private String name;
        private String version;
    }

    public Eip712Domain getEip712Domain(BigInteger chainId, String paymentMethod) {
        String method = paymentMethod.toLowerCase();
        String chain = chainId.toString().toLowerCase();
        if (methods != null && methods.containsKey(method) && methods.get(method).containsKey(chain)) {
            return methods.get(method).get(chain);
        }
        return null;
    }

}