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
}
