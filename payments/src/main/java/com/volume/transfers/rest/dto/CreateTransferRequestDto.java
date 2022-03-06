package com.volume.transfers.rest.dto;

import com.volume.shared.domain.messages.CreateTransferCommand;
import com.volume.shared.domain.types.InstitutionId;
import com.volume.shared.domain.types.UserId;
import com.volume.yapily.YapilyApplicationUserId;
import com.volume.yapily.YapilyInstitutionId;
import com.volume.yapily.YapilyUserId;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class CreateTransferRequestDto {
    // external references
    private final UserId shopperId;
    private final UserId merchantId;

    // transfer details
    private final BigDecimal amount;
    private final String currency;
    private final String description;
    private final String reference;

    // transfer details : payer
    private final InstitutionId institutionId;

    public static CreateTransferRequestDto forTest(UserId existingShopper, UserId existingMerchant) {
        return new CreateTransferRequestDto(
                existingShopper,
                existingMerchant,
                BigDecimal.valueOf(10.00),
                "GBP",
                "test transfer description",
                "test transfer reference",
                InstitutionId.Companion.fromYapilyInstitution(YapilyInstitutionId.Companion.modeloSandbox())
        );
    }

    // transfer details : payee

    public CreateTransferCommand toCommand(
            YapilyApplicationUserId yapilyApplicationUserId,
            YapilyUserId yapilyUserId,
            TransferPayeeDetailsDto transferPayeeDetailsDto
    ) {
        return new CreateTransferCommand(
                this.shopperId,
                this.merchantId,
                yapilyApplicationUserId,
                yapilyUserId,
                this.amount,
                this.currency,
                this.description,
                this.reference,
                this.institutionId,
                transferPayeeDetailsDto
        );
    }
}
