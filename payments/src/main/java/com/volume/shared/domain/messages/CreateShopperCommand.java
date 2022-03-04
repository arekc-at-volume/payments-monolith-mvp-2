package com.volume.shared.domain.messages;

import com.volume.shared.domain.types.DeviceId;
import com.volume.shared.domain.types.UserId;
import lombok.Value;

@Value
public class CreateShopperCommand {
    private final DeviceId deviceId;
    private final UserId merchantId;
}

