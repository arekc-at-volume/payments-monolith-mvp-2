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
class PostalAddressVO implements ValueObject {
    private String addressLine;
    private AddressType addressType;
    private String buildingNumber;
    private String country;
    private String county;
    private String department;
    private String postCode;
    private String streetName;
    private String subDepartment;
    private String townName;

    protected PostalAddressVO() {
    }

    @Override
    public String asString() {
        return this.toString();
    }

    public static PostalAddressVO testDomesticPaymentUKPayeeAddress() {
        return new PostalAddressVO(
                null,
                AddressType.POSTAL, // TODO: no idea if that is correct
                null,
                "United Kingdom",
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
