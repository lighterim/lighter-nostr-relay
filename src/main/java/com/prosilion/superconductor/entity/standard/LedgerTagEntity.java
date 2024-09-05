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
import nostr.event.NIP77Event;
import nostr.event.tag.LedgerTag;
import nostr.event.tag.PaymentTag;

import java.util.Objects;

import static nostr.event.NIP77Event.LEDGER_TAG_CODE;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "trade_message_ledger_tag")
public class LedgerTagEntity extends AbstractTagEntity {

    private String chain;
    private String network;
    private String txId;
    private String txUrl;

    public LedgerTagEntity(@NonNull LedgerTag tag){
        this.chain = tag.getChain();
        this.network = tag.getNetwork();
        this.txId = tag.getTxId();
        this.txUrl = tag.getTxUrl();
    }

    @Override
    public String getCode() {
        return LEDGER_TAG_CODE;
    }

    @Override
    public AbstractTagDto convertEntityToDto() {
        return new PaymentTagDto(new PaymentTag(chain, network, txId, txUrl));
    }

    @Override
    public BaseTag getAsBaseTag() {
        return new PaymentTag(chain, network, txId, txUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LedgerTagEntity that = (LedgerTagEntity) o;
        return Objects.equals(chain, that.chain) && Objects.equals(network, that.network) && Objects.equals(txId, that.txId) && Objects.equals(txUrl, that.txUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chain, network, txId, txUrl);
    }
}
