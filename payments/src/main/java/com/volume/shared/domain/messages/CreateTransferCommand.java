package com.volume.shared.domain.messages;

import com.volume.shared.domain.types.InstitutionId;
import com.volume.shared.domain.types.UserId;
import com.volume.transfers.rest.dto.TransferPayeeDetailsDto;
import com.volume.yapily.YapilyApplicationUserId;
import com.volume.yapily.YapilyUserId;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class CreateTransferCommand {
    // external references
    private final UserId shopperId;
    private final UserId merchantId;
    private final YapilyApplicationUserId yapilyApplicationUserId;
    private final YapilyUserId yapilyUserId;

    // transfer details
    private final BigDecimal amount;
    private final String currency;
    private final String description;
    private final String reference;

    // transfer details : payer
    private final InstitutionId institutionId;

    // transfer details : payee
    // TODO: I need to find a better way to create Command/Event parts, other than DTOs
    private final TransferPayeeDetailsDto transferPayeeDetails;
}
