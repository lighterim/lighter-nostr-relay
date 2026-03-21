package com.prosilion.superconductor.dto.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.standard.TlsnProofTagEntity;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.tag.TlsnProofTag;

@Getter
public class TlsnProofTagDto implements AbstractTagDto {

    private final TlsnProofTag tag;

    public TlsnProofTagDto(@NonNull TlsnProofTag t){
        this.tag = t;
    }

    @Override
    public String getCode() {
        return tag.getCode();
    }

    @Override
    public AbstractTagEntity convertDtoToEntity() {
        return new TlsnProofTagEntity(tag);
    }
}
