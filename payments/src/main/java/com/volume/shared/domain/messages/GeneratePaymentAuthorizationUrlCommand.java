package com.volume.shared.domain.messages;

import com.volume.shared.domain.types.TransferId;
import lombok.Value;

@Value
public class GeneratePaymentAuthorizationUrlCommand {
    private final TransferId transferId;
}
