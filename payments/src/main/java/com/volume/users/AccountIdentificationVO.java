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
}
