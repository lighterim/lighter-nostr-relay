package com.prosilion.superconductor.repository;

import com.prosilion.superconductor.entity.AccountMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountMapEntityRepository extends JpaRepository<AccountMapEntity, Long> {

    Optional<AccountMapEntity> findByDomainAndAccountName(String domain, String accountName);

    Optional<AccountMapEntity> findByDomainAndAccountNumber(String domain, String accountNumber);
}
