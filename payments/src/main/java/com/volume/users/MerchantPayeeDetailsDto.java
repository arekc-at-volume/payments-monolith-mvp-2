package com.volume.users;

import lombok.Value;

@Value
public class MerchantPayeeDetailsDto {
    private final String accountHolderName;
    private final PostalAddressDto postalAddress;
    private final AccountIdentificationDto[] accountIdentificationDto;

    public static MerchantPayeeDetailsDto forTest() {
        return new MerchantPayeeDetailsDto(
                "Big Boss",
                PostalAddressDto.forTest(),
                new AccountIdentificationDto[]{
                        AccountIdentificationDto.testAccountNumber(),
                        AccountIdentificationDto.testSortCode(),
                }
        );
    }
}
