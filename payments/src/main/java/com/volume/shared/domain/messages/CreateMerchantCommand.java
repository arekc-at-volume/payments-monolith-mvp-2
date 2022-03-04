package com.volume.shared.domain.messages;

import com.volume.shared.domain.types.EmailAddress;
import com.volume.shared.domain.types.PhoneNumber;
import com.volume.users.rest.dtos.MerchantPayeeDetailsDto;
import lombok.Value;

import java.io.Serializable;

@Value
public class CreateMerchantCommand implements Serializable {
    private final String companyName;
    private final EmailAddress emailAddress;
    private final PhoneNumber phoneNumber;
    private final MerchantPayeeDetailsDto merchantPayeeDetailsDto;
}
