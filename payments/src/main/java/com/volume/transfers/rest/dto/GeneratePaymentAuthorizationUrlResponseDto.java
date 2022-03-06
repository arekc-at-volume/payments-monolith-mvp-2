package com.volume.transfers.rest.dto;

import com.volume.shared.domain.messages.PaymentAuthorizationUrlCreatedEvent;
import com.volume.shared.domain.types.TransferId;
import com.volume.shared.infrastructure.rest.dto.DtoUtilities;
import com.volume.transfers.TransferAggregate;
import lombok.Value;

@Value
public class GeneratePaymentAuthorizationUrlResponseDto {
    private final TransferId transferId;
    private final String authorizationUrl;
    private final String qrCodeUrl;

    public static GeneratePaymentAuthorizationUrlResponseDto fromAggregate(TransferAggregate transferAggregateAfter) {
        var event = DtoUtilities.getEvent(transferAggregateAfter, PaymentAuthorizationUrlCreatedEvent.class);
        return new GeneratePaymentAuthorizationUrlResponseDto(
                event.getTransferId(),
                event.getAuthorizationUrl(),
                event.getQrCodeUrl()
        );
    }
}
