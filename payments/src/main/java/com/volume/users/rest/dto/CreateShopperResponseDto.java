package com.volume.users.rest.dto;

import com.volume.shared.domain.messages.ShopperCreatedEvent;
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
    private final String shopperId;
    private final String deviceId;
    private final String merchantId;

    public static CreateShopperResponseDto fromAggregate(ShopperAggregate aggregate) {
        return DtoUtilities.createResultFromEvent(
                aggregate,
                ShopperCreatedEvent.class,
                event -> new CreateShopperResponseDto(
                        event.getShopperId().asString(),
                        event.getDeviceId().asString(),
                        event.getMerchantId().asString()
                )
        );
    }
}
