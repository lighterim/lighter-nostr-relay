package com.prosilion.superconductor.entity.join.classified;

import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import com.prosilion.superconductor.entity.join.IntentEntityAbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "intent_payment_tag_join")
public class IntentEntityPaymentTagEntity extends IntentEntityAbstractTagEntity {

    private Long paymentTagId;

    public <T extends EventEntityAbstractTagEntity> IntentEntityPaymentTagEntity(Long intentId, Long paymentTagId) {
        super.setIntentId(intentId);
        this.paymentTagId = paymentTagId;
    }

}
