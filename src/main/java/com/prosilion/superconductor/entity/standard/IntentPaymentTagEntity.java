package com.prosilion.superconductor.entity.standard;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.dto.classified.IntentPaymentTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.util.NostrSigner;
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
public class IntentPaymentTagEntity extends AbstractTagEntity {

    private String method;
    private String account;
    private String qrCode;
    private String memo;

    public IntentPaymentTagEntity(@NonNull PaymentTag paymentTag){
        this.method = paymentTag.getMethod();
        this.account = NostrSigner.encrypt(paymentTag.getAccount());
        this.qrCode = NostrSigner.encrypt(paymentTag.getQrCode());
        this.memo = NostrSigner.encrypt(paymentTag.getMemo());
    }

    @Override
    public String getCode() {
        return "payment";
    }

    @Override
    public AbstractTagDto convertEntityToDto() {
        return new IntentPaymentTagDto(new PaymentTag(
                method,
                NostrSigner.decrypt(account),
                NostrSigner.decrypt(qrCode),
                NostrSigner.decrypt(memo)));
    }

    @Override
    public BaseTag getAsBaseTag() {
        return new PaymentTag(
                method,
                NostrSigner.decrypt(account),
                NostrSigner.decrypt(qrCode),
                NostrSigner.decrypt(memo));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntentPaymentTagEntity that = (IntentPaymentTagEntity) o;
        return Objects.equals(method, that.method) && Objects.equals(account, that.account) && Objects.equals(qrCode, that.qrCode) && Objects.equals(memo, that.memo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, account, qrCode, memo);
    }
}
