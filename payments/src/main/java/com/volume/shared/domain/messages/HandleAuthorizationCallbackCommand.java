package com.volume.shared.domain.messages;

import com.volume.shared.domain.types.TransferId;
import lombok.Value;

@Value
public class HandleAuthorizationCallbackCommand {
    private final TransferId transferId;
    private final String oneTimeToken;
}
