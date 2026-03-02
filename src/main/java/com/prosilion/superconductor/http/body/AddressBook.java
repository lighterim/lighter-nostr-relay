package com.prosilion.superconductor.http.body;

import lombok.Data;

import java.math.BigInteger;

@Data
public class AddressBook {

    private String name;

    private String address;

    private String nftId;

    private BigInteger chainId;

    private String createdBy;

    private String pubkey;
}
