package com.volume.users.rest.dto;

import com.volume.shared.domain.messages.MerchantCreatedEvent;
import com.volume.shared.infrastructure.rest.dto.DtoUtilities;
import com.volume.users.MerchantAggregate;
import lombok.Value;

import static com.volume.shared.infrastructure.rest.dto.DtoUtilities.getEvent;
import static java.lang.String.format;

@Value
public class CreateMerchantResponseDto {
    private String merchantId;

    public static CreateMerchantResponseDto fromAggregate(MerchantAggregate aggregate) {
        return DtoUtilities.createResultFromEvent(
                aggregate,
                MerchantCreatedEvent.class,
                event -> new CreateMerchantResponseDto(event.getMerchantId().getValue().toString())
        );
    }

}
