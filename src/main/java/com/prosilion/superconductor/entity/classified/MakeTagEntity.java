package com.prosilion.superconductor.entity.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.dto.classified.MakeTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.event.BaseTag;
import nostr.event.NIP77Event;
import nostr.event.Side;
import nostr.event.tag.MakeTag;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "make_tag")
public class MakeTagEntity extends AbstractTagEntity {

    private String side;
    private String nip05;
    private String pubkey;

    public MakeTagEntity(@NonNull MakeTag sideTag){
        this.side = sideTag.getSide().getSide();
        this.nip05 = sideTag.getMakerNip05();
        this.pubkey = sideTag.getMakerPubkey();
    }

    @Override
    public String getCode() {
        return NIP77Event.MAKE_TAG_CODE;
    }

    @Override
    public AbstractTagDto convertEntityToDto() {
        return new MakeTagDto(new MakeTag(Side.valueOf(side.toUpperCase()), nip05, pubkey));
    }

    @Override
    public BaseTag getAsBaseTag() {
        return new MakeTag(Side.valueOf(side.toUpperCase()), nip05, pubkey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MakeTagEntity that = (MakeTagEntity) o;
        return Objects.equals(side, that.side);
    }

    @Override
    public int hashCode() {
        return Objects.hash(side);
    }
}
