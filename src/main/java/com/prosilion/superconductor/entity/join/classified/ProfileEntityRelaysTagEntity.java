package com.prosilion.superconductor.entity.join.classified;

import com.prosilion.superconductor.entity.join.ProfileEntityAbstractTagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "profile_relays_tag_join")
public class ProfileEntityRelaysTagEntity extends ProfileEntityAbstractTagEntity {
    private Long relaysId;

    public <T extends ProfileEntityAbstractTagEntity> ProfileEntityRelaysTagEntity(Long profileId, Long relaysId) {
        super.setProfileId(profileId);
        this.relaysId = relaysId;
    }
}