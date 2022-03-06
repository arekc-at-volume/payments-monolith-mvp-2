package com.volume.users.rest.dto;

import com.volume.shared.domain.messages.MerchantCreatedEvent;
import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.rest.dto.DtoUtilities;
import com.volume.users.MerchantAggregate;
import lombok.Value;

import static com.volume.shared.infrastructure.rest.dto.DtoUtilities.getEvent;
import static java.lang.String.format;

@Value
public class CreateMerchantResponseDto {
    // TODO: Add all created merchant's details here
    private UserId merchantId;

    public static CreateMerchantResponseDto fromAggregate(MerchantAggregate aggregate) {
        return DtoUtilities.createResultFromEvent(
                aggregate,
                MerchantCreatedEvent.class,
                event -> new CreateMerchantResponseDto(event.getMerchantId())
        );
    }

}

