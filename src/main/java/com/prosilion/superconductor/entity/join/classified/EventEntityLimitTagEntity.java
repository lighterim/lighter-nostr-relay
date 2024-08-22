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
@Table(name = "event-limit_tag-join")
public class EventEntityLimitTagEntity extends EventEntityAbstractTagEntity {

    private Long limitTagId;

    public <T extends EventEntityAbstractTagEntity> EventEntityLimitTagEntity(Long eventId, Long limitTagId){
        super.setEventId(eventId);
        this.limitTagId = limitTagId;
    }

}
