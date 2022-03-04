package com.volume.users.rest.dtos;

import com.volume.users.AddressType;
import com.volume.users.PostalAddressVO;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PostalAddressDto {
    private final String addressLine;
    private final AddressType addressType;
    private final String buildingNumber;
    private final String country;
    private final String county;
    private final String department;
    private final String postCode;
    private final String streetName;
    private final String subDepartment;
    private final String townName;

    public PostalAddressVO toDomain() {
        return new PostalAddressVO(
                this.addressLine,
                this.addressType,
                this.buildingNumber,
                this.country,
                this.county,
                this.department,
                this.postCode,
                this.streetName,
                this.subDepartment,
                this.townName
        );
    }

    /**
     * Generates enough data for UK payee address
     *
     * @return
     */
    public static PostalAddressDto forTest() {
        return new PostalAddressDto(
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
