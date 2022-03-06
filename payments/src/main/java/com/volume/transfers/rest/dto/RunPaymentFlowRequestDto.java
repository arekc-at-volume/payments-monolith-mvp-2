package com.volume.transfers.rest.dto;

import com.volume.shared.domain.types.DeviceId;
import com.volume.shared.domain.types.InstitutionId;
import com.volume.shared.domain.types.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class RunPaymentFlowRequestDto {
    private final DeviceId deviceId;

    // external references
    private final UserId shopperId;
    private final UserId merchantId;

    // transfer details
    private final BigDecimal amount;
    private final String currency;
    private final String description;
    private final String reference;

    // transfer details : payer
    private final InstitutionId institutionId;
}
