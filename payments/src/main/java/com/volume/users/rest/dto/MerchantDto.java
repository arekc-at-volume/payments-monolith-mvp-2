package com.volume.users.rest.dto;

import com.volume.users.MerchantAggregate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MerchantDto {
    private final String merchantId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String updatedBy;
    private final String companyName;
    private final String emailAddress;
    private final String phoneNumber;

    public static MerchantDto fromAggregate(MerchantAggregate aggregate) {
        return aggregate.toDto();
    }
}
