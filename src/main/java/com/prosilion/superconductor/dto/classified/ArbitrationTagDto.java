package com.prosilion.superconductor.dto.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.standard.ArbitrationTagEntity;
import com.prosilion.superconductor.entity.standard.TlsnProofTagEntity;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.tag.ArbitrationTag;
import nostr.event.tag.TlsnProofTag;

@Getter
public class ArbitrationTagDto implements AbstractTagDto {

    private final ArbitrationTag tag;

    public ArbitrationTagDto(@NonNull ArbitrationTag t){
        this.tag = t;
    }

    @Override
    public String getCode() {
        return tag.getCode();
    }

    @Override
    public AbstractTagEntity convertDtoToEntity() {
        return new ArbitrationTagEntity(tag);
    }
}
