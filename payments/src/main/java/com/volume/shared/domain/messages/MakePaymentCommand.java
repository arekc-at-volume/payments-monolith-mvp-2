package com.volume.shared.domain.messages;

import com.volume.shared.domain.types.TransferId;
import lombok.Value;

@Value
public class MakePaymentCommand {
    private final TransferId transferId;
}

