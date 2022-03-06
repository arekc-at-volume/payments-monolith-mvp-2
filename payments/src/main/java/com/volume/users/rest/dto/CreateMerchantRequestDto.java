package com.volume.users.rest.dto;

import com.volume.shared.domain.messages.CreateMerchantCommand;
import com.volume.shared.domain.types.EmailAddress;
import com.volume.shared.domain.types.PhoneNumber;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.Random;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CreateMerchantRequestDto {
    private final String companyName;
    private final EmailAddress email;
    private final PhoneNumber phoneNumber;
    private final MerchantPayeeDetailsDto merchantPayeeDetails;

    public CreateMerchantCommand toCommand() {
        return new CreateMerchantCommand(
                this.companyName,
                this.email,
                this.phoneNumber,
                this.merchantPayeeDetails
        );
    }

    public static CreateMerchantRequestDto forTest() {
        return new CreateMerchantRequestDto(
                "company ABC" + new Random().nextInt(),
                EmailAddress.fromString("boss@company.com"),
                PhoneNumber.fromString("123456789"),
                MerchantPayeeDetailsDto.forTest()
        );
    }
}
