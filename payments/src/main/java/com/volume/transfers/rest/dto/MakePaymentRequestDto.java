package com.volume.transfers.rest.dto;

import com.volume.shared.domain.messages.MakePaymentCommand;
import com.volume.shared.domain.types.TransferId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MakePaymentRequestDto {
    private final TransferId transferId;

    public MakePaymentCommand toCommand() {
        return new MakePaymentCommand(this.getTransferId());
    }
}
