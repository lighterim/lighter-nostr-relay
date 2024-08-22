package com.prosilion.superconductor.entity.classified;

import com.prosilion.superconductor.dto.AbstractTagDto;
import com.prosilion.superconductor.dto.classified.SideTagDto;
import com.prosilion.superconductor.entity.AbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.event.BaseTag;
import nostr.event.tag.SideTag;

import java.math.BigDecimal;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "side_tag")
public class SideTagEntity extends AbstractTagEntity {

    private String side;

    public SideTagEntity(@NonNull SideTag sideTag){
        this.side = sideTag.getSide();
    }

    @Override
    public String getCode() {
        return "side";
    }

    @Override
    public AbstractTagDto convertEntityToDto() {
        return new SideTagDto(new SideTag(side));
    }

    @Override
    public BaseTag getAsBaseTag() {
        return new SideTag(side);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SideTagEntity that = (SideTagEntity) o;
        return Objects.equals(side, that.side);
    }

    @Override
    public int hashCode() {
        return Objects.hash(side);
    }
}
