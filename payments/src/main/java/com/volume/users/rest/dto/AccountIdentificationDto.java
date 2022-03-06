package com.volume.users.rest.dto;

import com.volume.users.AccountIdentificationType;
import com.volume.users.AccountIdentificationVO;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class AccountIdentificationDto {
    private final AccountIdentificationType type;
    private final String number;

    public static AccountIdentificationDto testAccountNumber() {
        return new AccountIdentificationDto(AccountIdentificationType.ACCOUNT_NUMBER, "12345678");
    }

    public static AccountIdentificationDto testSortCode() {
        return new AccountIdentificationDto(AccountIdentificationType.SORT_CODE, "123456");
    }

    public AccountIdentificationVO toDomain() {
        return new AccountIdentificationVO(this.type, this.number);
    }

    public static AccountIdentificationDto fromDomain(AccountIdentificationVO domain) {
        return new AccountIdentificationDto(domain.getType(), domain.getNumber());
    }
}
