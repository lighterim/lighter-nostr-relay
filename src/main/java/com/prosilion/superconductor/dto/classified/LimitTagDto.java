package com.prosilion.superconductor.dto.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.classified.LimitTagEntity;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.tag.LimitTag;

@Getter
public class LimitTagDto implements AbstractTagDto {

    private final LimitTag limitTag;

    public LimitTagDto(@NonNull LimitTag limitTag){
        this.limitTag = limitTag;
    }

    @Override
    public String getCode() {
        return limitTag.getCode();
    }

    @Override
    public AbstractTagEntity convertDtoToEntity() {
        return new LimitTagEntity(limitTag);
    }
}
