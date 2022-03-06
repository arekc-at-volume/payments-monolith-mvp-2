package com.volume.transfers;

import com.volume.shared.domain.AuthenticatedUser;
import com.volume.shared.domain.messages.*;
import com.volume.shared.domain.types.*;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedAggregateRoot;
import com.volume.transfers.persistence.JpaTransferAggregateRepository;
import com.volume.transfers.rest.dto.TransferDto;
import com.volume.yapily.YapilyApplicationUserId;
import com.volume.yapily.YapilyClient;
import com.volume.yapily.YapilyInstitutionId;
import com.volume.yapily.YapilyUserId;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yapily.sdk.ApiResponseOfPaymentResponse;
import yapily.sdk.Consent;
import yapily.sdk.PaymentAuthorisationRequestResponse;
import yapily.sdk.PaymentRequest;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
public class TransferAggregate extends BaseKeyedVersionedAggregateRoot<TransferId> {
    private TransferStatus transferStatus;

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
    @Column(length = 2000) // TODO : validate what really should be the length of consentToken
    private String consentToken;

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
        this.transferStatus = TransferStatus.CREATED;
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
                        newTransfer.getTransferStatus(),
                        newTransfer.getShopperId(),
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

    public TransferAggregate handle(MakePaymentCommand command, YapilyClient yapilyClient) {
        var statusUpdated = new TransactionStatusUpdater();
        statusUpdated.updateTransferStatus(this, TransferStatus.STARTING_PAYMENT);

        try {
            ApiResponseOfPaymentResponse apiResponseOfPaymentResponse = yapilyClient.makePayment(this.consentToken, createPaymentRequest(yapilyClient));
            statusUpdated.updateTransferStatus(this, TransferStatus.PAYMENT_SUCCEEDED);
        } catch (Exception exception) {
            statusUpdated.updateTransferStatus(this, TransferStatus.PAYMENT_FAILED);
        }

        registerEvent(
                new PaymentMadeEvent(this.getId())
        );

        return this;
    }

    static class TransactionStatusUpdater {
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        protected void updateTransferStatus(TransferAggregate aggregate, TransferStatus status) {
            aggregate.transferStatus = status;
        }
    }

    public TransferAggregate handle(GeneratePaymentAuthorizationUrlCommand command, YapilyClient yapilyClient) {
        // update status
        var statusUpdater = new TransactionStatusUpdater();
        statusUpdater.updateTransferStatus(this, TransferStatus.REQUESTING_AUTHORIZATION_URL);

        var consentRequestedAt = LocalDateTime.now();
        PaymentAuthorisationRequestResponse paymentAuthorisationRequestResponse = null;
        try {
            paymentAuthorisationRequestResponse = requestAuthorizationUrl(command, yapilyClient);
        } catch (Exception exception) {
            // TODO: go through this really carefully. Right now its not enough
            statusUpdater.updateTransferStatus(this, TransferStatus.AUTHORIZATION_URL_REQUEST_FAILED);
        }

        // update aggregate stat
        this.consentRequestedAt = consentRequestedAt;
        this.institutionConsentId = new InstitutionConsentId(paymentAuthorisationRequestResponse.getInstitutionConsentId());

        // after everything is done, let's update status again
        statusUpdater.updateTransferStatus(this, TransferStatus.AUTHORIZATION_URL_REQUEST_SUCCEEDED);

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

    private PaymentRequest createPaymentRequest(YapilyClient yapilyClient) {
        return yapilyClient.createPaymentRequest(
                this.amount,
                this.currency,
                this.payee.getAccountHolderName(),
                this.idempotencyId.toYapily(),
                this.description,
                List.of(this.payee.getAccountIdentification1().toYapily(), this.payee.getAccountIdentification2().toYapily()),
                false // TODO: add discovery of refund feature in a particular institution
        );
    }

    @NotNull
    private PaymentAuthorisationRequestResponse requestAuthorizationUrl(GeneratePaymentAuthorizationUrlCommand command, YapilyClient yapilyClient) {
        PaymentRequest paymentRequest = createPaymentRequest(yapilyClient);
        PaymentAuthorisationRequestResponse paymentAuthorisationRequestResponse = yapilyClient.generateAuthorizationUrl(
                this.yapilyApplicationUserId,
                this.institutionId.toYapily(),
                paymentRequest,
                true,
                "http://localhost:8080/api/callback/?transferId=" + command.getTransferId().asString()
        );
        return paymentAuthorisationRequestResponse;
    }

    public TransferAggregate handle(HandleAuthorizationCallbackCommand command, YapilyClient yapilyClient) {
        var statusUpdater = new TransactionStatusUpdater();
        // TODO: improve this simplified status handling. Error is not handled here
        statusUpdater.updateTransferStatus(this, TransferStatus.AUTHORIZATION_SUCCEEDED);

        Consent consent = yapilyClient.exchangeOneTimeToken(command.getOneTimeToken());
        // TODO: add error handling
        // TODO: here is way more to do. I haven't fully analyzed response from yapily.
        this.consentToken = consent.getConsentToken();

        this.registerEvent(
                new AuthorizationCallbackHandledEvent(
                        this.getId()
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

    public TransferDto toDto() {
        return new TransferDto(
                this.getId(),
                this.getTransferStatus(),
                this.getShopperId(),
                this.getMerchantId(),
                this.getYapilyApplicationUserId(),
                this.getYapilyUserId(),
                this.getAmount(),
                this.getCurrency(),
                this.getDescription(),
                this.getReference(),
                this.getIdempotencyId(),
                this.getInstitutionId(),
                this.getPayee().toDto(),
                this.getConsentRequestedAt(),
                this.getConsentToken()
        );
    }
}
