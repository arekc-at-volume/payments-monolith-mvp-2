package com.volume.users.rest;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MerchantOnDeviceRegistrationDto {
    private final String deviceId;
    private final String merchantId;
    private final Long version;
}
