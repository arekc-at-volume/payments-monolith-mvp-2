package com.volume.transfers.rest.dto;

import com.volume.shared.domain.messages.HandleAuthorizationCallbackCommand;
import com.volume.shared.domain.types.TransferId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class HandleAuthorizationCallbackRequestDto {
    private final TransferId transferId;
    private final String oneTimeToken;

    public HandleAuthorizationCallbackCommand toCommand() {
        return new HandleAuthorizationCallbackCommand(
                this.transferId,
                this.oneTimeToken
        );
    }
}
