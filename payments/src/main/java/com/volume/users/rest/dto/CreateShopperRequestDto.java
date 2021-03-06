package com.volume.users.rest.dto;

import com.volume.shared.domain.messages.CreateShopperCommand;
import com.volume.shared.domain.types.DeviceId;
import com.volume.shared.domain.types.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CreateShopperRequestDto {
    private final DeviceId deviceId;
    private final UserId merchantId;

    public CreateShopperCommand toCommand() {
        return new CreateShopperCommand(deviceId, merchantId);
    }
}
