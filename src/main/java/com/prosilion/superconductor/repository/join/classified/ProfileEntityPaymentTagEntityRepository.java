package com.prosilion.superconductor.repository.join.classified;

import com.prosilion.superconductor.entity.join.classified.IntentEntityPaymentTagEntity;
import com.prosilion.superconductor.entity.join.classified.ProfileEntityPaymentTagEntity;
import com.prosilion.superconductor.repository.join.IntentEntityAbstractTagEntityRepository;
import com.prosilion.superconductor.repository.join.ProfileEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

import static nostr.event.NIP77Event.PAYMENT_TAG_CODE;

@Repository
public interface ProfileEntityPaymentTagEntityRepository<T extends ProfileEntityPaymentTagEntity> extends ProfileEntityAbstractTagEntityRepository<T> {

    default String getCode() {
        return PAYMENT_TAG_CODE;
    }
}
