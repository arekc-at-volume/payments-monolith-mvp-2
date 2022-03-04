package com.volume.users.rest.dtos;

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
    private final String email;
    private final String phoneNumber;
    private final MerchantPayeeDetailsDto merchantPayeeDetails;

    public CreateMerchantCommand toCommand() {
        return new CreateMerchantCommand(
                this.companyName,
                EmailAddress.fromString(email),
                PhoneNumber.fromString(phoneNumber),
                this.merchantPayeeDetails
        );
    }

    public static CreateMerchantRequestDto forTest() {
        return new CreateMerchantRequestDto(
                "company ABC" + new Random().nextInt(),
                EmailAddress.fromString("boss@company.com").toString(),
                PhoneNumber.fromString("123456789").toString(),
                MerchantPayeeDetailsDto.forTest()
        );
    }
}
