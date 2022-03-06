package com.volume.transfers.rest.dto;

import com.volume.shared.domain.types.TransferId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class RunPaymentFlowResponseDto {
    private final TransferId transferId;
    private final String authorizationUrl;
    private final String qrCodeUrl;
}
