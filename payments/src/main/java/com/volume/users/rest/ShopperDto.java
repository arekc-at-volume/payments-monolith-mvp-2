package com.volume.users.rest;

import com.volume.users.ShopperAggregate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ShopperDto {
    private final String shopperId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String updatedBy;
    private final List<MerchantOnDeviceRegistrationDto> merchantRegistrations;
    private final Long version;

    public static ShopperDto fromAggregate(ShopperAggregate shopperAggregate) {
        return shopperAggregate.toDto();
    }
}
