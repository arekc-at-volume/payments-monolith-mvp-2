package com.volume.users;

import com.volume.shared.infrastructure.persistence.ValueObject;
import com.volume.yapily.YapilyAccountIdentification;
import com.volume.yapily.YapilyAccountIdentificationAccountNumber;
import com.volume.yapily.YapilyAccountIdentificationSortCode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.Embeddable;

import static java.lang.String.format;

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

    public YapilyAccountIdentification toYapily() {
        switch (type) {
            case ACCOUNT_NUMBER:
                return new YapilyAccountIdentificationAccountNumber(number);
            case SORT_CODE:
                return new YapilyAccountIdentificationSortCode(number);
            default:
                throw new IllegalStateException(format("AccountIdentification type %s not supported", this.type));
        }

    }
}
