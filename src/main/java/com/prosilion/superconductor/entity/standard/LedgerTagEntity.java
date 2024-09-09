package com.prosilion.superconductor.entity.standard;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.dto.classified.LedgerTagDto;
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
import nostr.event.TradeStatus;
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
    private String tradeStatus;

    public LedgerTagEntity(@NonNull LedgerTag tag){
        this.chain = tag.getChain();
        this.network = tag.getNetwork();
        this.txId = tag.getTxId();
        this.txUrl = tag.getTxUrl();
        this.tradeStatus = tag.getTradeStatus().name();
    }

    @Override
    public String getCode() {
        return LEDGER_TAG_CODE;
    }

    @Override
    public AbstractTagDto convertEntityToDto() {
        return new LedgerTagDto(new LedgerTag(chain, network, txId, txUrl, TradeStatus.valueOf(tradeStatus)));
    }

    @Override
    public BaseTag getAsBaseTag() {
        return new LedgerTag(chain, network, txId, txUrl, TradeStatus.valueOf(tradeStatus));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LedgerTagEntity that = (LedgerTagEntity) o;
        return Objects.equals(chain, that.chain) && Objects.equals(network, that.network) && Objects.equals(txId, that.txId) && Objects.equals(txUrl, that.txUrl) && Objects.equals(tradeStatus, that.tradeStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chain, network, txId, txUrl, tradeStatus);
    }
}
