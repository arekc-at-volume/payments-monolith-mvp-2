package com.volume.users.persistence;

import com.volume.shared.domain.types.DeviceId;
import com.volume.shared.domain.types.UserId;
import com.volume.users.MerchantOnDeviceRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaMerchantOnDeviceRegistrationRepository extends JpaRepository<MerchantOnDeviceRegistrationEntity, UUID> {
    // TODO: rework so that in this case we do not read ShopperAggregate eagerly
    Optional<MerchantOnDeviceRegistrationEntity> findByDeviceIdAndMerchantId(DeviceId deviceId, UserId merchantId);
}
