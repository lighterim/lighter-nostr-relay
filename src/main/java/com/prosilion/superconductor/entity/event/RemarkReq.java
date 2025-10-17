package com.prosilion.superconductor.entity.event;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.math.BigInteger;
import java.util.List;

@Data
public class RemarkReq {

    private String name;

    private String address;

    private String nftId;

    private BigInteger chainId;

    private String createdBy;

    private String pubkey;
}
