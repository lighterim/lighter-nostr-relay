package com.prosilion.superconductor.http.body;

import lombok.Data;
import nostr.event.Role;
import nostr.event.Side;

import java.math.BigInteger;

@Data
public class Rate {

    private String nftId;

    private BigInteger chainId;

    private Side side;

    private Role role;
}
