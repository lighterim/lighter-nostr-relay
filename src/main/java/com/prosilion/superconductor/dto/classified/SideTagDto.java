package com.prosilion.superconductor.dto.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.classified.SideTagEntity;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.tag.SideTag;

@Getter
public class SideTagDto implements AbstractTagDto {

    private final SideTag sideTag;

    public SideTagDto(@NonNull SideTag sideTag){
        this.sideTag = sideTag;
    }

    @Override
    public String getCode() {
        return sideTag.getCode();
    }

    @Override
    public AbstractTagEntity convertDtoToEntity() {
        return new SideTagEntity(sideTag);
    }
}
