package com.prosilion.superconductor.entity.standard;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.dto.classified.LedgerTagDto;
import com.prosilion.superconductor.dto.classified.TlsnProofTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.event.BaseTag;
import nostr.event.TradeStatus;
import nostr.event.tag.LedgerTag;
import nostr.event.tag.TlsnProofTag;

import java.util.Objects;

import static nostr.event.NIP77Event.LEDGER_TAG_CODE;
import static nostr.event.NIP77Event.TLSN_PROOF_TAG_CODE;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "trade_message_tlsn_proof_tag")
public class TlsnProofTagEntity extends AbstractTagEntity {

    private String paymentMethod;
    private String paymentId;
    private String account1;
    private String account2;
    private String account3;
    private String amount;
    private String currency;
    private String state;
    private String confirmationTs;
    private String tradeId;
    private String signature;

    public TlsnProofTagEntity(@NonNull TlsnProofTag t){
        this.paymentMethod = t.getPaymentMethod();
        this.paymentId = t.getPaymentId();
        this.account1 = t.getAccount1();
        this.account2 = t.getAccount2();
        this.account3 = t.getAccount3();
        this.amount = t.getAmount();
        this.currency = t.getCurrency();
        this.state = t.getState();
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
        .paymentMethod(paymentMethod).paymentId(paymentId).account1(account1).account2(account2).account3(account3)
                .amount(amount).currency(currency).state(state).confirmationTs(confirmationTs).tradeId(tradeId).signature(signature)
                .build());
    }

    @Override
    public BaseTag getAsBaseTag() {
        return TlsnProofTag.builder()
                .paymentMethod(paymentMethod).paymentId(paymentId).account1(account1).account2(account2).account3(account3)
                .amount(amount).currency(currency).state(state).confirmationTs(confirmationTs).tradeId(tradeId).signature(signature)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TlsnProofTagEntity that = (TlsnProofTagEntity) o;
        return Objects.equals(paymentMethod, that.paymentMethod) && Objects.equals(paymentId, that.paymentId) && Objects.equals(account1, that.account1) && Objects.equals(account2, that.account2) && Objects.equals(account3, that.account3)
                && Objects.equals(amount, that.amount) && Objects.equals(currency, that.currency) && Objects.equals(state, that.state) && Objects.equals(confirmationTs, that.confirmationTs)  && Objects.equals(tradeId, that.tradeId) && Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentMethod, paymentId, account1, account2, account3, amount, currency, state, confirmationTs, tradeId);
    }
}
