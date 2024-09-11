package com.prosilion.superconductor.dto.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.entity.standard.ProfileRelaysTagEntity;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.tag.RelaysTag;

@Getter
public class ProfileRelaysTagDto implements AbstractTagDto {
    private final RelaysTag relaysTag;

    public ProfileRelaysTagDto(@NonNull RelaysTag relaysTag) {
        this.relaysTag = relaysTag;
    }

    @Override
    public String getCode() {
        return relaysTag.getCode();
    }

    @Override
    public ProfileRelaysTagEntity convertDtoToEntity() {
        return new ProfileRelaysTagEntity(relaysTag);
    }
}
