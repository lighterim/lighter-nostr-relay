package com.prosilion.superconductor.http.body;

import lombok.Data;

@Data
public class Trades {

    private Long nftId;

    private String chainId;

    private String pubKey;

    private Long id;

}
