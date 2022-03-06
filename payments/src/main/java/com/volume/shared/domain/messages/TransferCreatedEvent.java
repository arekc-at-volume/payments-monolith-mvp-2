package com.volume.shared.domain.messages;

import com.volume.shared.domain.types.InstitutionId;
import com.volume.shared.domain.types.TransferId;
import com.volume.shared.domain.types.TransferIdempotencyId;
import com.volume.shared.domain.types.UserId;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class TransferCreatedEvent {
    private TransferId transferId;

    // external references
    private UserId shopperId;
    private UserId merchantId;

    // transfer details
    private BigDecimal amount;
    private String currency;
    private String description;
    private String reference;
    private TransferIdempotencyId idempotencyId;

    // transfer details : payer
    private InstitutionId institutionId;

    // transfer details : payee

    // other
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserId updatedBy;
}
