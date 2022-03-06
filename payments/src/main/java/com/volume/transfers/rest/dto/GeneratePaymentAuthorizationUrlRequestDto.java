package com.volume.transfers.rest.dto;

import com.volume.shared.domain.messages.GeneratePaymentAuthorizationUrlCommand;
import com.volume.shared.domain.types.TransferId;
import lombok.Value;

@Value
public class GeneratePaymentAuthorizationUrlRequestDto {
    private final TransferId transferId;

    public GeneratePaymentAuthorizationUrlCommand toCommand() {
        return new GeneratePaymentAuthorizationUrlCommand(
                this.transferId
        );
    }
}
