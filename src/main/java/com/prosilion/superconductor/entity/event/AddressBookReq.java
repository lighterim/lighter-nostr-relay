package com.prosilion.superconductor.entity.event;

import lombok.Data;

import java.math.BigInteger;

@Data
public class AddressBookReq {

    private String name;

    private String address;

    private String nftId;

    private BigInteger chainId;

    private String createdBy;

    private String pubkey;
}
