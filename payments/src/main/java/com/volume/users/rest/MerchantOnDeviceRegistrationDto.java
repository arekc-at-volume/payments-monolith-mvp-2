package com.volume.users.rest;

import com.volume.shared.domain.types.DeviceId;
import com.volume.shared.domain.types.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MerchantOnDeviceRegistrationDto {
    private final DeviceId deviceId;
    private final UserId merchantId;
    private final Long version;
}
