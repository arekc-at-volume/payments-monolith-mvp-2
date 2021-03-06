package com.volume.transfers.rest.dto;

import com.volume.shared.domain.messages.GeneratePaymentAuthorizationUrlCommand;
import com.volume.shared.domain.types.TransferId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class GeneratePaymentAuthorizationUrlRequestDto {
    private final TransferId transferId;

    public GeneratePaymentAuthorizationUrlCommand toCommand() {
        return new GeneratePaymentAuthorizationUrlCommand(
                this.transferId
        );
    }
}
