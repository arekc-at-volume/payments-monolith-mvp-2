package com.volume.transfers;

import com.volume.shared.domain.messages.DomainEvent;
import com.volume.shared.domain.messages.MerchantCreatedEvent;
import com.volume.shared.domain.types.*;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedAggregateRepository;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedAggregateRoot;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedEntity;
import com.volume.shared.infrastructure.rest.dto.DtoUtilities;
import com.volume.users.*;
import com.volume.users.exceptions.MerchantNotFoundException;
import com.volume.users.exceptions.ShopperNotFoundException;
import com.volume.users.exceptions.TransferNotFoundException;
import com.volume.users.persistence.JpaMerchantsRepository;
import com.volume.users.persistence.JpaShoppersRepository;
import com.volume.users.rest.dto.AccountIdentificationDto;
import com.volume.users.rest.dto.CreateMerchantResponseDto;
import com.volume.users.rest.dto.MerchantPayeeDetailsDto;
import com.volume.users.rest.dto.PostalAddressDto;
import com.volume.yapily.YapilyApplicationUserId;
import com.volume.yapily.YapilyClient;
import com.volume.yapily.YapilyInstitutionId;
import com.volume.yapily.YapilyUserId;
import lombok.*;
import org.springframework.stereotype.Service;
import yapily.sdk.PaymentAuthorisationRequestResponse;
import yapily.sdk.PaymentRequest;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

enum TransferType {
    DOMESTIC_PAYMENT,
    DOMESTIC_VARIABLE_RECURRING_PAYMENT
}

interface JpaTransferAggregateRepository extends BaseKeyedVersionedAggregateRepository<TransferAggregate, TransferId> {
}

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
class TransferPayeeDetailsDto {
    private final String accountHolderName;
    private final PostalAddressDto postalAddress;
    private final List<AccountIdentificationDto> accountIdentificationDto;

    public static TransferPayeeDetailsDto forTest() {
        return new TransferPayeeDetailsDto(
                "some merchant",
                PostalAddressDto.forTest(),
                List.of(
                        AccountIdentificationDto.testAccountNumber(),
                        AccountIdentificationDto.testSortCode()
                )
        );
    }

    public static TransferPayeeDetailsDto from(MerchantPayeeDetailsDto merchantPayeeDetails) {
        return new TransferPayeeDetailsDto(
                merchantPayeeDetails.getAccountHolderName(),
                merchantPayeeDetails.getPostalAddress(),
                merchantPayeeDetails.getAccountIdentificationDto()
        );
    }
}


@Value
class CreateTransferCommand {
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
    private final TransferPayeeDetailsDto transferPayeeDetails;
}

@Value
class TransferCreatedEvent {
    private TransferId transferId;

    // external references
    private UserId shopperId;
    private UserId merchantId;

    // transfer details
    private BigDecimal amount;
    private String currency;
    private String description;
    private String reference;
    private TransferIdempotencyId idempotencyId;

    // transfer details : payer
    private InstitutionId institutionId;

    // transfer details : payee

    // other
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserId updatedBy;
}

@Entity
@Getter
class TransferPayeeDetailsEntity extends BaseKeyedVersionedEntity<TransferId> {
    private String accountHolderName;
    @Embedded
    private PostalAddressVO postalAddress;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "account_id_1_type")),
            @AttributeOverride(name = "number", column = @Column(name = "account_id_1_number")),
    })
    private AccountIdentificationVO accountIdentification1;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "account_id_2_type")),
            @AttributeOverride(name = "number", column = @Column(name = "account_id_2_number")),
    })
    private AccountIdentificationVO accountIdentification2;
    @OneToOne
    private TransferAggregate transfer;

    protected TransferPayeeDetailsEntity() {
        super();
    }

    public TransferPayeeDetailsEntity(TransferId id, String accountHolderName, PostalAddressVO postalAddress, AccountIdentificationVO accountIdentification1, AccountIdentificationVO accountIdentification2) {
        super(id);
        this.accountHolderName = accountHolderName;
        this.postalAddress = postalAddress;
        this.accountIdentification1 = accountIdentification1;
        this.accountIdentification2 = accountIdentification2;
    }

    public static TransferPayeeDetailsEntity forTest(TransferId transferId) {
        return new TransferPayeeDetailsEntity(
                transferId,
                "test account holder name",
                PostalAddressVO.testDomesticPaymentUKPayeeAddress(),
                AccountIdentificationVO.testAccountNumber(),
                AccountIdentificationVO.testSortCode()
        );
    }

    public void setTransfer(TransferAggregate transferAggregate) {
        this.transfer = transferAggregate;
    }
}

@Value
class GeneratePaymentAuthorizationUrlCommand {
    private final TransferId transferId;
}

@Value
class PaymentAuthorizationUrlCreatedEvent implements DomainEvent {
    private final TransferId transferId;
    private final String authorizationUrl;
    private final String qrCodeUrl;
}

@Value
class CreateTransferRequestDto {
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

@Value
class CreateTransferResponseDto {
    private final TransferId transferId;

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

    // transfer details : payee


    public static CreateTransferResponseDto fromAggregate(TransferAggregate aggregate) {
        var event = DtoUtilities.getEvent(aggregate, TransferCreatedEvent.class);
        return new CreateTransferResponseDto(
                event.getTransferId(),
                event.getShopperId(),
                event.getMerchantId(),
                event.getAmount(),
                event.getCurrency(),
                event.getDescription(),
                event.getReference(),
                event.getInstitutionId()
        );
    }

}

@Service
@AllArgsConstructor
class TransferAggregateService {

    private final JpaTransferAggregateRepository transferRepository;
    private final JpaShoppersRepository shopperRepository;
    private final JpaMerchantsRepository merchantRepository;
    private final YapilyClient yapilyClient;

    /**
     * We should discuss how we want to have such constraints handled. I think it should be within aggregate command
     * handlers until it uses types/repositories/services from within same bounded context.
     * <p>
     * In this case we should build separate shopper repository in transfer context based on shopper events. (TODO)
     * The same should happen to Merchant payee details (TODO)
     */
    public CreateTransferResponseDto createNewTransfer(AuthenticatedUser callingUser, CreateTransferRequestDto requestDto) {
        // constraint 1 : shopper must exist
        ShopperAggregate shopperAggregate = shopperRepository.findById(requestDto.getShopperId())
                .orElseThrow(() -> new ShopperNotFoundException(requestDto.getShopperId()));

        // constraint 2 : merchant must exist
        MerchantAggregate merchantAggregate = merchantRepository.findById(requestDto.getMerchantId())
                .orElseThrow(() -> new MerchantNotFoundException(requestDto.getMerchantId()));

        // constraint 3 : merchant must have payee details properly configured
        // TODO

        TransferPayeeDetailsDto payeeDetailsDto = TransferPayeeDetailsDto.from(merchantAggregate.getMerchantPayeeDetails().toDto());

        TransferAggregate transferAggregate = TransferAggregate.create(
                callingUser,
                requestDto.toCommand(
                        shopperAggregate.getYapilyApplicationUserId(),
                        shopperAggregate.getYapilyUserId(),
                        payeeDetailsDto
                ),
                transferRepository
        );

        return CreateTransferResponseDto.fromAggregate(transferAggregate);
    }

    public GeneratePaymentAuthorizationUrlResponseDto generateAuthorizationUrl(AuthenticatedUser callingUser, GeneratePaymentAuthorizationUrlRequestDto requestDto) {
        // constraint 1: transfer must exist and be in status = TODO
        TransferAggregate transferAggregateBefore = transferRepository.findById(requestDto.getTransferId())
                .orElseThrow(() -> new TransferNotFoundException(requestDto.getTransferId()));
        // TODO: validate transfer status

        GeneratePaymentAuthorizationUrlCommand generatePaymentAuthorizationUrlCommand = requestDto.toCommand();
        TransferAggregate transferAggregateAfter =
                transferAggregateBefore.handle(generatePaymentAuthorizationUrlCommand, transferRepository, yapilyClient);

        return GeneratePaymentAuthorizationUrlResponseDto.fromAggregate(transferAggregateAfter);
    }


}

@Value
class GeneratePaymentAuthorizationUrlRequestDto {
    private final TransferId transferId;

    public GeneratePaymentAuthorizationUrlCommand toCommand() {
        return new GeneratePaymentAuthorizationUrlCommand(
                this.transferId
        );
    }
}

@Value
class GeneratePaymentAuthorizationUrlResponseDto {
    private final TransferId transferId;
    private final String authorizationUrl;
    private final String qrCodeUrl;

    public static GeneratePaymentAuthorizationUrlResponseDto fromAggregate(TransferAggregate transferAggregateAfter) {
        var event = DtoUtilities.getEvent(transferAggregateAfter, PaymentAuthorizationUrlCreatedEvent.class);
        return new GeneratePaymentAuthorizationUrlResponseDto(
                event.getTransferId(),
                event.getAuthorizationUrl(),
                event.getQrCodeUrl()
        );
    }
}

@Entity
@Getter
public class TransferAggregate extends BaseKeyedVersionedAggregateRoot<TransferId> {
    // external references
    private UserId shopperId;
    private UserId merchantId;
    private YapilyApplicationUserId yapilyApplicationUserId;
    private YapilyUserId yapilyUserId;

    // transfer details
    private BigDecimal amount;
    private String currency;
    private String description;
    private String reference;
    private TransferIdempotencyId idempotencyId;

    // transfer details : payer
    private InstitutionId institutionId;

    // transfer details : payee
    @OneToOne(mappedBy = "transfer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private TransferPayeeDetailsEntity payee;

    // consent details:
    private LocalDateTime consentRequestedAt;
    private InstitutionConsentId institutionConsentId;

    // other
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserId updatedBy;

    protected TransferAggregate() {
        super();
    }

    public TransferAggregate(
            TransferId id,
            UserId shopperId, UserId merchantId, YapilyApplicationUserId yapilyApplicationUserId, YapilyUserId yapilyUserId,
            BigDecimal amount, String currency, String description, String reference,
            TransferIdempotencyId idempotencyId,
            InstitutionId institutionId,
            LocalDateTime createAt,
            UserId createdBy
    ) {
        super(id);
        this.shopperId = shopperId;
        this.merchantId = merchantId;
        this.yapilyApplicationUserId = yapilyApplicationUserId;
        this.yapilyUserId = yapilyUserId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.reference = reference;
        this.idempotencyId = idempotencyId;
        this.institutionId = institutionId;
        this.createdAt = createAt;
        this.updatedAt = createAt;
        this.updatedBy = createdBy;
    }

    public void setTransferPayeeDetails(TransferPayeeDetailsEntity payee) {
        this.payee = payee;
        payee.setTransfer(this);
    }

    public static TransferAggregate create(AuthenticatedUser callingUser, CreateTransferCommand command, JpaTransferAggregateRepository repository) {
        var newTransfer = new TransferAggregate(
                TransferId.Companion.random(),
                command.getShopperId(),
                command.getMerchantId(),
                command.getYapilyApplicationUserId(),
                command.getYapilyUserId(),
                command.getAmount(),
                command.getCurrency(),
                command.getDescription(),
                command.getReference(),
                TransferIdempotencyId.Companion.random(),
                command.getInstitutionId(),
                LocalDateTime.now(),
                callingUser.getUserId()
        );

        var transferPayeeDetailsEntity = new TransferPayeeDetailsEntity(
                newTransfer.getId(),
                command.getTransferPayeeDetails().getAccountHolderName(),
                command.getTransferPayeeDetails().getPostalAddress().toDomain(),
                command.getTransferPayeeDetails().getAccountIdentificationDto().get(0).toDomain(),
                command.getTransferPayeeDetails().getAccountIdentificationDto().get(1).toDomain()
        );

        newTransfer.setTransferPayeeDetails(transferPayeeDetailsEntity);

        newTransfer.registerEvent(
                new TransferCreatedEvent(
                        newTransfer.getId(),
                        newTransfer.getMerchantId(),
                        newTransfer.getMerchantId(),
                        newTransfer.getAmount(),
                        newTransfer.getCurrency(),
                        newTransfer.getDescription(),
                        newTransfer.getReference(),
                        newTransfer.getIdempotencyId(),
                        newTransfer.getInstitutionId(),
                        newTransfer.getCreatedAt(),
                        newTransfer.getUpdatedAt(),
                        newTransfer.getUpdatedBy()
                )
        );

        repository.save(newTransfer);

        return newTransfer;
    }

    public TransferAggregate handle(GeneratePaymentAuthorizationUrlCommand command, JpaTransferAggregateRepository repository, YapilyClient yapilyClient) {

        PaymentRequest paymentRequest = yapilyClient.createPaymentRequest(
                this.amount,
                this.currency,
                this.payee.getAccountHolderName(),
                this.idempotencyId.toYapily(),
                this.description,
                List.of(this.payee.getAccountIdentification1().toYapily(), this.payee.getAccountIdentification2().toYapily())
        );

        var consentRequestedAt = LocalDateTime.now();
        PaymentAuthorisationRequestResponse paymentAuthorisationRequestResponse = yapilyClient.generateAuthorizationUrl(
                this.yapilyApplicationUserId,
                this.institutionId.toYapily(),
                paymentRequest,
                "https://localhost:8080/api/callback/"
        );

        // update aggregate stat
        this.consentRequestedAt = consentRequestedAt;
        this.institutionConsentId = new InstitutionConsentId(paymentAuthorisationRequestResponse.getInstitutionConsentId());

        // generate events
        this.registerEvent(
                new PaymentAuthorizationUrlCreatedEvent(
                        this.getId(),
                        paymentAuthorisationRequestResponse.getAuthorisationUrl(),
                        paymentAuthorisationRequestResponse.getQrCodeUrl()
                )
        );

        return this;
    }

    public static TransferAggregate forTest() {
        var transfer = new TransferAggregate(
                TransferId.Companion.random(),
                UserId.Companion.random(),
                UserId.Companion.random(),
                YapilyApplicationUserId.Companion.random(),
                YapilyUserId.Companion.random(),
                BigDecimal.valueOf(10.00).setScale(2),
                "GBP",
                "test transfer description",
                "test transfer reference",
                TransferIdempotencyId.Companion.random(),
                InstitutionId.Companion.fromYapilyInstitution(YapilyInstitutionId.Companion.modeloSandbox()),
                LocalDateTime.now(),
                UserId.Companion.random()
        );

        var transferPayeeDetails = TransferPayeeDetailsEntity.forTest(transfer.getId());
        transfer.setTransferPayeeDetails(transferPayeeDetails);

        return transfer;
    }

    @PrePersist
    @PreUpdate
    void fixAmountPrecision() {
        // TODO: research topic from business and technical side
        this.amount.setScale(2);
    }
}
