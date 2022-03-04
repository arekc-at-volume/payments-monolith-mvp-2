package com.volume.users;

import com.volume.shared.domain.messages.CreateMerchantCommand;
import com.volume.shared.domain.messages.MerchantCreatedEvent;
import com.volume.shared.domain.types.EmailAddress;
import com.volume.shared.domain.types.PhoneNumber;
import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedEntity;
import com.volume.shared.infrastructure.persistence.ValueObject;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Embeddable
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


@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Embeddable
class AccountIdentificationVO implements ValueObject {
    private AccountIdentificationType type;
    private String number;

    protected AccountIdentificationVO() {
    }

    @Override
    public String asString() {
        return this.toString();
    }
}

/**
 * TODO: Ultimately I don't think primary key will be UserId. I leave if like that for now.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class MerchantPayeeDetailsEntity extends BaseKeyedVersionedEntity<UserId> {
    @OneToOne
    private MerchantAggregate merchant;
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

    public MerchantPayeeDetailsEntity(
            UserId id,
            MerchantAggregate merchant,
            String accountHolderName,
            PostalAddressVO postalAddress,
            AccountIdentificationVO accountIdentification1,
            AccountIdentificationVO accountIdentification2
    ) {
        super(id);
        this.merchant = merchant;
        this.accountHolderName = accountHolderName;
        this.postalAddress = postalAddress;
        this.accountIdentification1 = accountIdentification1;
        this.accountIdentification2 = accountIdentification2;
    }

    public void setMerchant(MerchantAggregate merchant) {
        this.merchant = merchant;
    }
}

@Entity
@Getter
class MerchantAggregate extends UserEntity {
    private String clientSecret;
    private EmailAddress emailAddress;
    private PhoneNumber phoneNumber;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "merchant")
    private MerchantPayeeDetailsEntity merchantPayeeDetails;

    protected MerchantAggregate() {
        super();
    }

    protected MerchantAggregate(
            UserId id, UserId updatedBy, String clientSecret, EmailAddress emailAddress, PhoneNumber phoneNumber,
            MerchantPayeeDetailsEntity merchantPayeeDetails) {
        super(id, updatedBy);
        this.clientSecret = clientSecret;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.merchantPayeeDetails = merchantPayeeDetails;
    }

    private void setMerchantPayeeDetails(MerchantPayeeDetailsEntity merchantPayeeDetails) {
        this.merchantPayeeDetails = merchantPayeeDetails;
        merchantPayeeDetails.setMerchant(this);
    }

    /**
     * TODO: We do not have transactionality on either static method or constructor. We need to create some transactional wrapper
     * for such occasions and always run aggregate creation wrapped in it.
     */
    public static MerchantAggregate create(AuthenticatedUser callingUser, CreateMerchantCommand command, JpaMerchantsRepository repository) {
        // command processing logic
        var newMerchant = new MerchantAggregate(
                UserId.Companion.random(),
                callingUser.getUserId(),
                command.getCompanyName(),
                command.getEmailAddress(),
                command.getPhoneNumber(),
                null // TODO: make it somehow better
        );
        var merchantPayeeDetails = new MerchantPayeeDetailsEntity(
                newMerchant.getId(),
                newMerchant,
                command.getMerchantPayeeDetailsDto().getAccountHolderName(),
                command.getMerchantPayeeDetailsDto().getPostalAddress().toDomain(),
                command.getMerchantPayeeDetailsDto().getAccountIdentificationDto()[0].toDomain(),
                command.getMerchantPayeeDetailsDto().getAccountIdentificationDto()[1].toDomain()
        );
        newMerchant.setMerchantPayeeDetails(merchantPayeeDetails);

        // emitting events
        newMerchant.registerEvent(new MerchantCreatedEvent(newMerchant.getId()));

        // saving aggregate (causes events to be published synchronously, in the same transaction)
        repository.save(newMerchant);

        return newMerchant;
    }


    public void changeSecret(String secret) {
        this.clientSecret = secret;
    }

    public static MerchantAggregateBuilder randomForTest() {
        var newMerchantId = UserId.Companion.random();
        var merchantPayeeDetailsDto = MerchantPayeeDetailsDto.forTest();
        return
                MerchantAggregate.builder()
                        .id(newMerchantId)
                        .updatedBy(UserId.Companion.random())
                        .clientSecret("test-secret")
                        .emailAddress(EmailAddress.Companion.testRandom())
                        .phoneNumber(PhoneNumber.Companion.testRandom())
                        .merchantPayeeDetails(new MerchantPayeeDetailsEntity(
                                        newMerchantId,
                                        null, // TODO: for this is just a hack. Fix it.
                                        merchantPayeeDetailsDto.getAccountHolderName(),
                                        merchantPayeeDetailsDto.getPostalAddress().toDomain(),
                                        merchantPayeeDetailsDto.getAccountIdentificationDto()[0].toDomain(),
                                        merchantPayeeDetailsDto.getAccountIdentificationDto()[1].toDomain()
                                )
                        );
    }

    public static MerchantAggregateBuilder builder() {
        return new MerchantAggregateBuilder();
    }

    public static class MerchantAggregateBuilder {
        private UserId userId;
        private UserId updatedBy;
        private String clientSecret;
        private EmailAddress emailAddress;
        private PhoneNumber phoneNumber;
        private MerchantPayeeDetailsEntity merchantPayeeDetails;

        MerchantAggregateBuilder id(UserId id) {
            this.userId = id;
            return this;
        }

        MerchantAggregateBuilder updatedBy(UserId updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        MerchantAggregateBuilder clientSecret(String value) {
            this.clientSecret = value;
            return this;
        }

        MerchantAggregateBuilder emailAddress(EmailAddress value) {
            this.emailAddress = value;
            return this;
        }

        MerchantAggregateBuilder phoneNumber(PhoneNumber value) {
            this.phoneNumber = value;
            return this;
        }

        MerchantAggregateBuilder merchantPayeeDetails(MerchantPayeeDetailsEntity value) {
            this.merchantPayeeDetails = value;
            return this;
        }

        MerchantAggregate build() {
            var aggregate = new MerchantAggregate(
                    this.userId,
                    this.updatedBy,
                    this.clientSecret,
                    this.emailAddress,
                    this.phoneNumber,
                    this.merchantPayeeDetails
            );
            aggregate.setMerchantPayeeDetails(merchantPayeeDetails);
            return aggregate;
        }
    }

    MerchantDto toDto() {
        return new MerchantDto(
                this.getId().asString(),
                null, // TODO: add this field to aggregate
                null, // TODO: add this field to aggregate
                null, // TODO: add this field to aggregate
                null, // TODO: add this field to aggregate
                this.emailAddress.asString(),
                this.phoneNumber.asString()
        );
    }


}



