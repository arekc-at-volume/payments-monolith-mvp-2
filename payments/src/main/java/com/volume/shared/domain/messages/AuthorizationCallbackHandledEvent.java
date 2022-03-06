package com.volume.shared.domain.messages;

import com.volume.shared.domain.types.TransferId;
import lombok.Value;

@Value
public class AuthorizationCallbackHandledEvent {
    private final TransferId transferId;
}
