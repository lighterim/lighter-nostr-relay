package com.prosilion.superconductor.entity;

import com.prosilion.superconductor.repository.AccountMapEntityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AccountMapEntityService {

    private final AccountMapEntityRepository accountMapEntityRepository;

    @Autowired
    public AccountMapEntityService(AccountMapEntityRepository accountMapEntityRepository){
        this.accountMapEntityRepository = accountMapEntityRepository;
    }

    public Long save(AccountMapEntity accountMapEntity){
        return accountMapEntityRepository.save(accountMapEntity).getId();
    }

    public AccountMapEntity findById(Long id){
        return accountMapEntityRepository.findById(id).orElse(null);
    }

    public AccountMapEntity findByDomainAndAccountName(String domain, String accountName){
        return accountMapEntityRepository.findByDomainAndAccountName(domain, accountName).orElse(null);
    }

    public AccountMapEntity findByDomainAndAccountNumber(String doman, String accountNumber){
        return accountMapEntityRepository.findByDomainAndAccountNumber( doman, accountNumber).orElse(null);
    }
}
