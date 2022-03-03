package com.volume.users;

import com.volume.shared.domain.types.EmailAddress;
import com.volume.shared.domain.types.PhoneNumber;
import com.volume.shared.domain.types.UserId;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import javax.persistence.Entity;

import java.io.Serializable;

@Value
class CreateMerchantCommand implements Serializable {
    private final String companyName;
    private final EmailAddress emailAddress;
    private final PhoneNumber phoneNumber;
}

@Value
class MerchantCreatedEvent {
    private final UserId merchantId;
}

@Entity
@Getter
class MerchantAggregate extends UserEntity {
    private String clientSecret;
    private EmailAddress emailAddress;
    private PhoneNumber phoneNumber;

    protected MerchantAggregate() {
        super();
    }

    @Builder
    protected MerchantAggregate(UserId id, UserId updateBy, String clientSecret, EmailAddress emailAddress, PhoneNumber phoneNumber) {
        super(id, updateBy);
        this.clientSecret = clientSecret;
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
            command.getPhoneNumber()
        );

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
        return
                MerchantAggregate.builder()
                        .id(UserId.Companion.random())
                        .updateBy(UserId.Companion.random())
                        .clientSecret("test-secret")
                        .emailAddress(EmailAddress.Companion.testRandom())
                        .phoneNumber(PhoneNumber.Companion.testRandom());
    }


}



