package com.volume.users.rest.dto;

import com.volume.shared.domain.types.EmailAddress;
import com.volume.shared.domain.types.PhoneNumber;
import com.volume.shared.domain.types.UserId;
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
    private final UserId merchantId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final UserId updatedBy;
    private final String companyName;
    private final EmailAddress emailAddress;
    private final PhoneNumber phoneNumber;

    public static MerchantDto fromAggregate(MerchantAggregate aggregate) {
        return aggregate.toDto();
    }
}
