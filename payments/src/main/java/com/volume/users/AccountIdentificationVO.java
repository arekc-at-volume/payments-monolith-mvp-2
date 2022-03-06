package com.volume.users;

import com.volume.shared.infrastructure.persistence.ValueObject;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.Embeddable;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Embeddable
public
class AccountIdentificationVO implements ValueObject {
    private AccountIdentificationType type;
    private String number;

    protected AccountIdentificationVO() {
    }

    @Override
    public String asString() {
        return this.toString();
    }

    public static AccountIdentificationVO testAccountNumber() {
        return new AccountIdentificationVO(AccountIdentificationType.ACCOUNT_NUMBER, "12345678");
    }

    public static AccountIdentificationVO testSortCode() {
        return new AccountIdentificationVO(AccountIdentificationType.SORT_CODE, "123456");
    }
}
