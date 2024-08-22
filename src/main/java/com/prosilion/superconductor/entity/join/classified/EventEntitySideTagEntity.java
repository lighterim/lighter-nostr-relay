package com.prosilion.superconductor.entity.join.classified;

import com.prosilion.superconductor.entity.join.EventEntityAbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "event-side_tag-join")
public class EventEntitySideTagEntity extends EventEntityAbstractTagEntity {

    private Long sideTagId;

    public <T extends EventEntityAbstractTagEntity> EventEntitySideTagEntity(Long eventId, Long sideTagId){
        super.setEventId(eventId);
        this.sideTagId = sideTagId;
    }

}
