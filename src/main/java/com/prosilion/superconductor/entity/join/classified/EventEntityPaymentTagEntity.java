package com.prosilion.superconductor.entity.join.classified;

import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "event-payment_tag-join")
public class EventEntityPaymentTagEntity extends EventEntityAbstractTagEntity {

    private Long paymentTagId;

    public <T extends EventEntityAbstractTagEntity> EventEntityPaymentTagEntity(Long eventId, Long paymentTagId){
        super.setEventId(eventId);
        this.paymentTagId = paymentTagId;
    }

}
