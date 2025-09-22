package com.prosilion.superconductor.dto.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.standard.ProfilePaymentTagEntity;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.tag.PaymentTag;

@Getter
public class ProfilePaymentTagDto implements AbstractTagDto {

    private final PaymentTag paymentTag;

    public ProfilePaymentTagDto(@NonNull PaymentTag paymentTag) {
        this.paymentTag = paymentTag;
    }

    @Override
    public String getCode() {
        return paymentTag.getCode();
    }

    @Override
    public AbstractTagEntity convertDtoToEntity() {
        return new ProfilePaymentTagEntity(paymentTag);
    }
}
