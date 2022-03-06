package com.volume.transfers.rest.dto;

import com.volume.shared.domain.types.InstitutionId;
import com.volume.shared.domain.types.TransferId;
import com.volume.shared.domain.types.TransferIdempotencyId;
import com.volume.shared.domain.types.UserId;
import com.volume.yapily.YapilyApplicationUserId;
import com.volume.yapily.YapilyUserId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class TransferDto {

    private final TransferId id;
    private final UserId shopperId;
    private final UserId merchantId;
    private final YapilyApplicationUserId yapilyApplicationUserId;
    private final YapilyUserId yapilyUserId;
    private final BigDecimal amount;
    private final String currency;
    private final String description;
    private final String reference;
    private final TransferIdempotencyId idempotencyId;
    private final InstitutionId institutionId;
    private TransferPayeeDetailsDto transferPayeeDetails;

    public TransferDto(
            TransferId id,
            UserId shopperId,
            UserId merchantId,
            YapilyApplicationUserId yapilyApplicationUserId, YapilyUserId yapilyUserId,
            BigDecimal amount, String currency, String description, String reference, TransferIdempotencyId idempotencyId,
            InstitutionId institutionId,
            TransferPayeeDetailsDto transferPayeeDetails) {
        this.id = id;
        this.shopperId = shopperId;
        this.merchantId = merchantId;
        this.yapilyApplicationUserId = yapilyApplicationUserId;
        this.yapilyUserId = yapilyUserId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.reference = reference;
        this.idempotencyId = idempotencyId;
        this.institutionId = institutionId;
        this.transferPayeeDetails = transferPayeeDetails;
    }
}
