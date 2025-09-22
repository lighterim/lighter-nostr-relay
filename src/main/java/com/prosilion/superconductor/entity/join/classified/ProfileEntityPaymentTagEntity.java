package com.prosilion.superconductor.entity.join.classified;

import com.prosilion.superconductor.entity.join.IntentEntityAbstractTagEntity;
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
@Table(name = "profile_payment_tag_join")
public class ProfileEntityPaymentTagEntity extends ProfileEntityAbstractTagEntity {

    private Long paymentTagId;

    public <T extends IntentEntityAbstractTagEntity> ProfileEntityPaymentTagEntity(Long profileId, Long paymentTagId) {
        super.setProfileId(profileId);
        this.paymentTagId = paymentTagId;
    }

}
