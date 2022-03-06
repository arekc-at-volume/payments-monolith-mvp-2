package com.volume.transfers.rest.dto;

import com.volume.shared.domain.types.TransferId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MakePaymentResponseDto {
    private final TransferId transferId;
}
