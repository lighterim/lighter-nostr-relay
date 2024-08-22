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
@Table(name = "event-token_tag-join")
public class EventEntityTokenTagEntity extends EventEntityAbstractTagEntity {

    private Long tokenTagId;

    public <T extends EventEntityAbstractTagEntity> EventEntityTokenTagEntity(Long eventId, Long tokenTagId){
        super.setEventId(eventId);
        this.tokenTagId = tokenTagId;
    }

}
