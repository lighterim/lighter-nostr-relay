package com.prosilion.superconductor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="account_map", indexes = {
        @Index(name="IX_ACCOUNT_MAP_UK_DOMAIN_ACCOUNT_NAME", columnList ="domain, accountName", unique = true ),
        @Index(name="IX_ACCOUNT_MAP_UK_DOMAIN_ACCOUNT_NUMBER", columnList ="domain, accountNumber", unique = true ),
        @Index(name="IX_ACCOUNT_MAP_PUBKEY", columnList = "pubkey"),
        @Index(name="IX_ACCOUNT_MAP_NFT_ID_CHAIN_ID", columnList = "nftId, chainId")
})
public class AccountMapEntity {
    @Id
    private Long id;
    private String domain;
    private String accountName;
    private String accountNumber;
    private String pubkey;
    private Integer chainId;
    private Integer nftId;
    private Long createAt;
}
