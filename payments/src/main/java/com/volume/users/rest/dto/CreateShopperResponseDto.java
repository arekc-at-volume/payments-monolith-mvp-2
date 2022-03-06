package com.volume.users.rest.dto;

import com.volume.shared.domain.messages.ShopperCreatedEvent;
import com.volume.shared.domain.types.DeviceId;
import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.rest.dto.DtoUtilities;
import com.volume.users.ShopperAggregate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CreateShopperResponseDto {
    private final UserId shopperId;
    private final DeviceId deviceId;
    private final UserId merchantId;

    public static CreateShopperResponseDto fromAggregate(ShopperAggregate aggregate) {
        return DtoUtilities.createResultFromEvent(
                aggregate,
                ShopperCreatedEvent.class,
                event -> new CreateShopperResponseDto(
                        event.getShopperId(),
                        event.getDeviceId(),
                        event.getMerchantId()
                )
        );
    }
}
