package com.prosilion.superconductor.repository.join.classified;

import com.prosilion.superconductor.entity.join.classified.EventEntityPaymentTagEntity;
import com.prosilion.superconductor.repository.join.EventEntityAbstractTagEntityRepository;
import org.springframework.stereotype.Repository;

import static nostr.event.NIP77Event.PAYMENT_TAG_CODE;

@Repository
public interface EventEntityPaymentTagEntityRepository<T extends EventEntityPaymentTagEntity> extends EventEntityAbstractTagEntityRepository<T> {

    default String getCode() {
        return PAYMENT_TAG_CODE;
    }
}
