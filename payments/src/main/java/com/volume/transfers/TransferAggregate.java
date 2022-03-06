package com.volume.transfers;

import com.volume.shared.domain.types.InstitutionId;
import com.volume.shared.domain.types.TransferId;
import com.volume.shared.domain.types.TransferIdempotencyId;
import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedAggregateRepository;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedAggregateRoot;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedEntity;
import com.volume.users.AccountIdentificationVO;
import com.volume.users.AuthenticatedUser;
import com.volume.users.PostalAddressVO;
import com.volume.yapily.YapilyInstitutionId;
import lombok.Getter;
import lombok.Value;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

enum TransferType {
    DOMESTIC_PAYMENT,
    DOMESTIC_VARIABLE_RECURRING_PAYMENT
}

interface JpaTransferAggregateRepository extends BaseKeyedVersionedAggregateRepository<TransferAggregate, TransferId> {
}

@Value
class CreateTransferCommand {
    // external references
    private UserId shopperId;
    private UserId merchantId;

    // transfer details
    private BigDecimal amount;
    private String currency;
    private String description;
    private String reference;

    // transfer details : payer
    private InstitutionId institutionId;

    // transfer details : payee
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

@Entity
@Getter
public class TransferAggregate extends BaseKeyedVersionedAggregateRoot<TransferId> {
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
    @OneToOne(mappedBy = "transfer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private TransferPayeeDetailsEntity payee;

    // other
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserId updatedBy;

    protected TransferAggregate() {
        super();
    }

    public TransferAggregate(
            TransferId id,
            UserId shopperId, UserId merchantId,
            BigDecimal amount, String currency, String description, String reference,
            TransferIdempotencyId idempotencyId,
            InstitutionId institutionId,
            LocalDateTime createAt,
            UserId createdBy
    ) {
        super(id);
        this.shopperId = shopperId;
        this.merchantId = merchantId;
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
                command.getAmount(),
                command.getCurrency(),
                command.getDescription(),
                command.getReference(),
                TransferIdempotencyId.Companion.random(),
                command.getInstitutionId(),
                LocalDateTime.now(),
                callingUser.getUserId()
        );

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

    public static TransferAggregate forTest() {
        var transfer = new TransferAggregate(
                TransferId.Companion.random(),
                UserId.Companion.random(),
                UserId.Companion.random(),
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
