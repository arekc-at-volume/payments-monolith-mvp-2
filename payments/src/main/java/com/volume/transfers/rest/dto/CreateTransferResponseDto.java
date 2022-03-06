package com.volume.transfers.rest.dto;

import com.volume.shared.domain.messages.TransferCreatedEvent;
import com.volume.shared.domain.types.InstitutionId;
import com.volume.shared.domain.types.TransferId;
import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.rest.dto.DtoUtilities;
import com.volume.transfers.TransferAggregate;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class CreateTransferResponseDto {
    private final TransferId transferId;

    // external references
    private final UserId shopperId;
    private final UserId merchantId;

    // transfer details
    private final BigDecimal amount;
    private final String currency;
    private final String description;
    private final String reference;

    // transfer details : payer
    private final InstitutionId institutionId;

    // transfer details : payee


    public static CreateTransferResponseDto fromAggregate(TransferAggregate aggregate) {
        var event = DtoUtilities.getEvent(aggregate, TransferCreatedEvent.class);
        return new CreateTransferResponseDto(
                event.getTransferId(),
                event.getShopperId(),
                event.getMerchantId(),
                event.getAmount(),
                event.getCurrency(),
                event.getDescription(),
                event.getReference(),
                event.getInstitutionId()
        );
    }

}
