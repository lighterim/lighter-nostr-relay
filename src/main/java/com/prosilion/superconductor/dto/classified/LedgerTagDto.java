package com.prosilion.superconductor.dto.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.standard.LedgerTagEntity;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.tag.LedgerTag;

@Getter
public class LedgerTagDto implements AbstractTagDto {

    private final LedgerTag ledgerTag;

    public LedgerTagDto(@NonNull LedgerTag ledgerTag){
        this.ledgerTag = ledgerTag;
    }

    @Override
    public String getCode() {
        return ledgerTag.getCode();
    }

    @Override
    public AbstractTagEntity convertDtoToEntity() {
        return new LedgerTagEntity(ledgerTag);
    }
}
