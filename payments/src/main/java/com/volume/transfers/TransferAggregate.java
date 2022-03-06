package com.volume.transfers;

import com.volume.shared.domain.messages.*;
import com.volume.shared.domain.types.*;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedAggregateRoot;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedEntity;
import com.volume.transfers.persistence.JpaTransferAggregateRepository;
import com.volume.transfers.rest.dto.*;
import com.volume.users.*;
import com.volume.users.exceptions.MerchantNotFoundException;
import com.volume.users.exceptions.ShopperNotFoundException;
import com.volume.users.exceptions.TransferNotFoundException;
import com.volume.users.persistence.JpaMerchantsRepository;
import com.volume.users.persistence.JpaShoppersRepository;
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
