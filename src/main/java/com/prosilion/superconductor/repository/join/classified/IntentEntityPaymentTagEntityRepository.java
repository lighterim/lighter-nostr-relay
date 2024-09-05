package com.prosilion.superconductor.repository.join.classified;

import com.prosilion.superconductor.entity.join.classified.IntentEntityPaymentTagEntity;
import com.prosilion.superconductor.repository.join.IntentEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

import static nostr.event.NIP77Event.PAYMENT_TAG_CODE;

@Repository
public interface IntentEntityPaymentTagEntityRepository<T extends IntentEntityPaymentTagEntity> extends IntentEntityAbstractTagEntityRepository<T> {

    default String getCode() {
        return PAYMENT_TAG_CODE;
    }
}
