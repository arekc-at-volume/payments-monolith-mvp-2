package com.volume.transfers.rest.dto;

import com.volume.shared.domain.messages.AuthorizationCallbackHandledEvent;
import com.volume.shared.domain.types.TransferId;
import com.volume.shared.infrastructure.rest.dto.DtoUtilities;
import com.volume.transfers.TransferAggregate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class HandleAuthorizationCallbackResponseDto {
    private final TransferId transferId;

    public static HandleAuthorizationCallbackResponseDto fromAggregate(TransferAggregate aggregate) {
        var event = DtoUtilities.getEvent(aggregate, AuthorizationCallbackHandledEvent.class);
        return new HandleAuthorizationCallbackResponseDto(
            event.getTransferId()
        );
    }
}
