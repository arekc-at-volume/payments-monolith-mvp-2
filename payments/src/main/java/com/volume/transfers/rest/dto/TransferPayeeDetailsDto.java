package com.volume.transfers.rest.dto;

import com.volume.users.rest.dto.AccountIdentificationDto;
import com.volume.users.rest.dto.MerchantPayeeDetailsDto;
import com.volume.users.rest.dto.PostalAddressDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TransferPayeeDetailsDto {
    private final String accountHolderName;
    private final PostalAddressDto postalAddress;
    private final List<AccountIdentificationDto> accountIdentificationDto;

    public static TransferPayeeDetailsDto forTest() {
        return new TransferPayeeDetailsDto(
                "some merchant",
                PostalAddressDto.forTest(),
                List.of(
                        AccountIdentificationDto.testAccountNumber(),
                        AccountIdentificationDto.testSortCode()
                )
        );
    }

    public static TransferPayeeDetailsDto from(MerchantPayeeDetailsDto merchantPayeeDetails) {
        return new TransferPayeeDetailsDto(
                merchantPayeeDetails.getAccountHolderName(),
                merchantPayeeDetails.getPostalAddress(),
                merchantPayeeDetails.getAccountIdentificationDto()
        );
    }
}
