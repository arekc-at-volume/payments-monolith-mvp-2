package com.volume.users.rest.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MerchantPayeeDetailsDto {
    private final String accountHolderName;
    private final PostalAddressDto postalAddress;
    private final List<AccountIdentificationDto> accountIdentification;

    public static MerchantPayeeDetailsDto forTest() {
        return new MerchantPayeeDetailsDto(
                "Big Boss",
                PostalAddressDto.forTest(),
                List.of(
                        AccountIdentificationDto.testAccountNumber(),
                        AccountIdentificationDto.testSortCode()
                )
        );
    }
}
