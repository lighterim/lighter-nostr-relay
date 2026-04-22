package com.prosilion.superconductor.entity.standard;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.dto.classified.TlsnProofTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.event.BaseTag;
import nostr.event.tag.TlsnProofTag;

import java.util.Objects;

import static nostr.event.NIP77Event.TLSN_PROOF_TAG_CODE;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "trade_message_tlsn_proof_tag", indexes={
        @Index(name = "IX_TRADE_MESSAGE_TLSN_PROOF_TAG_TRADE_ID", columnList = "tradeId")
})
public class TlsnProofTagEntity extends AbstractTagEntity {

    private String paymentMethod;
    private String paymentId;
    private String payeeDetails;

    private String amount;
    private String currency;
    private String confirmationTs;
    private String tradeId;
    private String signature;
    private int status = 0;

    public TlsnProofTagEntity(@NonNull TlsnProofTag t){
        this.paymentMethod = t.getPaymentMethod();
        this.paymentId = t.getPaymentId();
        this.payeeDetails = t.getPayeeDetails();
        this.amount = t.getAmount();
        this.currency = t.getCurrency();
        this.confirmationTs = t.getConfirmationTs();
        this.tradeId = t.getTradeId();
        this.signature = t.getSignature();
    }

    @Override
    public String getCode() {
        return TLSN_PROOF_TAG_CODE;
    }

    @Override
    public AbstractTagDto convertEntityToDto() {
        return new TlsnProofTagDto(TlsnProofTag.builder()
        .paymentMethod(paymentMethod).paymentId(paymentId).payeeDetails(payeeDetails)
                .amount(amount).currency(currency).confirmationTs(confirmationTs).tradeId(tradeId).signature(signature)
                .build());
    }

    @Override
    public BaseTag getAsBaseTag() {
        return TlsnProofTag.builder()
                .paymentMethod(paymentMethod).paymentId(paymentId).payeeDetails(payeeDetails)
                .amount(amount).currency(currency).confirmationTs(confirmationTs).tradeId(tradeId).signature(signature)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TlsnProofTagEntity that = (TlsnProofTagEntity) o;
        return Objects.equals(paymentMethod, that.paymentMethod) && Objects.equals(paymentId, that.paymentId)
                && Objects.equals(payeeDetails, that.payeeDetails) && Objects.equals(amount, that.amount)
                && Objects.equals(currency, that.currency) && Objects.equals(confirmationTs, that.confirmationTs)
                && Objects.equals(tradeId, that.tradeId) && Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentMethod, paymentId, payeeDetails, amount, currency, confirmationTs, tradeId);
    }
}
