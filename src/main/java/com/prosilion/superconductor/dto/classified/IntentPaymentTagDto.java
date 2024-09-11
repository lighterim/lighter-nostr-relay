package com.prosilion.superconductor.dto.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.standard.IntentPaymentTagEntity;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.tag.PaymentTag;

@Getter
public class IntentPaymentTagDto implements AbstractTagDto {

    private final PaymentTag paymentTag;

    public IntentPaymentTagDto(@NonNull PaymentTag paymentTag){
        this.paymentTag = paymentTag;
    }

    @Override
    public String getCode() {
        return paymentTag.getCode();
    }

    @Override
    public AbstractTagEntity convertDtoToEntity() {
        return new IntentPaymentTagEntity(paymentTag);
    }
}
