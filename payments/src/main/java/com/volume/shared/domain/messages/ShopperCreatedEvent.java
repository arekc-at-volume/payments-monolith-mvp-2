package com.volume.shared.domain.messages;

import com.volume.shared.domain.types.DeviceId;
import com.volume.shared.domain.types.UserId;
import lombok.Value;

import java.io.Serializable;

/**
 * Shopper was created on deviceId for merchantId.
 * IMPORTANT:
 * It is important to know that it closes given shopper within his device. Same person for same merchant on a different device will be a different shopper.
 */
@Value
public class ShopperCreatedEvent implements DomainEvent, Serializable {
    private final UserId shopperId;
    private final DeviceId deviceId;
    private final UserId merchantId;
}
