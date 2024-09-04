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
@Table(name = "intent_make_tag_join")
public class EventEntityMakeTagEntity extends EventEntityAbstractTagEntity {

    private Long makeTagId;

    public <T extends EventEntityAbstractTagEntity> EventEntityMakeTagEntity(Long eventId, Long sideTagId){
        super.setEventId(eventId);
        this.makeTagId = sideTagId;
    }

}
