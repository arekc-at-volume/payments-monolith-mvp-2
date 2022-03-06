package com.volume.shared.domain.messages;

import com.volume.shared.domain.types.TransferId;
import com.volume.shared.infrastructure.rest.dto.DtoUtilities;
import com.volume.transfers.TransferAggregate;
import com.volume.transfers.rest.dto.MakePaymentResponseDto;
import lombok.Value;

@Value
public class PaymentMadeEvent {
    private final TransferId transferId;

    public static MakePaymentResponseDto fromAggregate(TransferAggregate aggregate) {
        var event = DtoUtilities.getEvent(aggregate, PaymentMadeEvent.class);
        return new MakePaymentResponseDto(event.getTransferId());
    }
}
