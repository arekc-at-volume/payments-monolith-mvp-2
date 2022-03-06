package com.volume.shared.domain.messages;

import com.volume.shared.domain.types.TransferId;
import lombok.Value;

@Value
public class PaymentAuthorizationUrlCreatedEvent implements DomainEvent {
    private final TransferId transferId;
    private final String authorizationUrl;
    private final String qrCodeUrl;
}
