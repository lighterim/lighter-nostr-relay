package com.prosilion.superconductor.dto.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.classified.MakeTagEntity;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.tag.MakeTag;

@Getter
public class MakeTagDto implements AbstractTagDto {

    private final MakeTag sideTag;

    public MakeTagDto(@NonNull MakeTag sideTag){
        this.sideTag = sideTag;
    }

    @Override
    public String getCode() {
        return sideTag.getCode();
    }

    @Override
    public AbstractTagEntity convertDtoToEntity() {
        return new MakeTagEntity(sideTag);
    }
}
