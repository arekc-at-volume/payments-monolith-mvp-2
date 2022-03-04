package com.volume.shared.domain.messages;

import com.volume.shared.domain.types.UserId;
import lombok.Value;

@Value
public class MerchantCreatedEvent {
    private final UserId merchantId;
}
