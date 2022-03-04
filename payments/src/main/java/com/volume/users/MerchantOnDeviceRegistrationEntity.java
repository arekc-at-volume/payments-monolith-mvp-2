package com.volume.users;

import com.volume.shared.domain.types.DeviceId;
import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedEntity;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity
@Getter
public class MerchantOnDeviceRegistrationEntity extends BaseKeyedVersionedEntity<UUID> {
    @ManyToOne
    @JoinColumn(name = "shopper_id", nullable = false)
    private ShopperAggregate shopper;
    private DeviceId deviceId;
    private UserId merchantId;

    protected MerchantOnDeviceRegistrationEntity() {
        super();
    }

    public MerchantOnDeviceRegistrationEntity(UUID id, DeviceId deviceId, UserId merchantId) {
        super(id);
        this.deviceId = deviceId;
        this.merchantId = merchantId;
    }

    public static MerchantOnDeviceRegistrationEntity forTest() {
        return new MerchantOnDeviceRegistrationEntity(UUID.randomUUID(), DeviceId.Companion.random(), UserId.Companion.random());
    }

    public void setShopper(ShopperAggregate shopper) {
        this.shopper = shopper;
    }
}
