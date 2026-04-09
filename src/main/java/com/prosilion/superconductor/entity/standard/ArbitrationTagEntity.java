package com.prosilion.superconductor.entity.standard;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.dto.classified.ArbitrationTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.event.BaseTag;
import nostr.event.tag.ArbitrationTag;

import java.util.Objects;

import static nostr.event.NIP77Event.ARBITRATION_TAG_CODE;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "trade_message_arbitration_tag")
public class ArbitrationTagEntity extends AbstractTagEntity {

    private String arbitrator;
    private Integer buyerThresholdBp;
    private Integer nonce;
    private String resolutionTs;
    private String signature;

    public ArbitrationTagEntity(@NonNull ArbitrationTag t){
        this.arbitrator = t.getArbitrator();
        this.buyerThresholdBp = t.getBuyerThresholdBp();
        this.nonce = t.getNonce();
        this.resolutionTs = t.getResolutionTs();
        this.signature = t.getSignature();
    }

    @Override
    public String getCode() {
        return ARBITRATION_TAG_CODE;
    }

    @Override
    public AbstractTagDto convertEntityToDto() {
        return new ArbitrationTagDto(ArbitrationTag.builder()
        .arbitrator(arbitrator).buyerThresholdBp(buyerThresholdBp).nonce(nonce).resolutionTs(resolutionTs).signature(signature)
                .build());
    }

    @Override
    public BaseTag getAsBaseTag() {
        return ArbitrationTag.builder()
                .arbitrator(arbitrator).buyerThresholdBp(buyerThresholdBp).nonce(nonce).resolutionTs(resolutionTs).signature(signature)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArbitrationTagEntity that = (ArbitrationTagEntity) o;
        return Objects.equals(arbitrator, that.arbitrator) && Objects.equals(buyerThresholdBp, that.buyerThresholdBp)
                && Objects.equals(nonce, that.nonce) && Objects.equals(resolutionTs, that.resolutionTs)
                && Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbitrator, buyerThresholdBp, nonce, resolutionTs, signature);
    }
}
