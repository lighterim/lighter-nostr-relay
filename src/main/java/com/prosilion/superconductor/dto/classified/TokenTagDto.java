package com.prosilion.superconductor.dto.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import com.prosilion.superconductor.entity.standard.TokenTagEntity;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.tag.TokenTag;

@Getter
public class TokenTagDto implements AbstractTagDto {

    private final TokenTag tokenTag;

    public TokenTagDto(@NonNull TokenTag tokenTag){
        this.tokenTag = tokenTag;
    }

    @Override
    public String getCode() {
        return tokenTag.getCode();
    }

    @Override
    public AbstractTagEntity convertDtoToEntity() {
        return new TokenTagEntity(tokenTag);
    }
}
