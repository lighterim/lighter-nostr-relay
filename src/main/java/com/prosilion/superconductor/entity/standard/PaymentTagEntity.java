package com.prosilion.superconductor.entity.standard;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.dto.classified.PaymentTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.event.BaseTag;
import nostr.event.tag.PaymentTag;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "intent_payment_tag")
public class PaymentTagEntity extends AbstractTagEntity {

    private String method;
    private String account;
    private String qrCode;
    private String memo;

    public PaymentTagEntity(@NonNull PaymentTag paymentTag){
        this.method = paymentTag.getMethod();
        this.account = paymentTag.getAccount();
        this.qrCode = paymentTag.getQrCode();
        this.memo = paymentTag.getMemo();
    }

    @Override
    public String getCode() {
        return "payment";
    }

    @Override
    public AbstractTagDto convertEntityToDto() {
        return new PaymentTagDto(new PaymentTag(method, account, qrCode, memo));
    }

    @Override
    public BaseTag getAsBaseTag() {
        return new PaymentTag(method, account, qrCode, memo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentTagEntity that = (PaymentTagEntity) o;
        return Objects.equals(method, that.method) && Objects.equals(account, that.account) && Objects.equals(qrCode, that.qrCode) && Objects.equals(memo, that.memo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, account, qrCode, memo);
    }
}
