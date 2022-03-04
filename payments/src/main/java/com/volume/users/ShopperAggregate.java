package com.volume.users;

import com.volume.shared.domain.messages.CreateShopperCommand;
import com.volume.shared.domain.messages.ShopperCreatedEvent;
import com.volume.shared.domain.types.DeviceId;
import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedEntity;
import com.volume.users.persistence.JpaShoppersRepository;
import com.volume.users.rest.MerchantOnDeviceRegistrationDto;
import com.volume.users.rest.ShopperDto;
import lombok.Getter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Getter
public class ShopperAggregate extends UserEntity {
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "shopper")
    private Set<MerchantOnDeviceRegistrationEntity> merchantAppRegistrations = new HashSet<>();

    protected ShopperAggregate() {
        super();
    }

    public ShopperAggregate(UserId id, UserId updateBy) {
        super(id, updateBy);
    }

    public static ShopperAggregate create(
            AuthenticatedUser callingUser,
            CreateShopperCommand createShopperCommand,
            JpaShoppersRepository shoppersRepository
    ) {
        // command processing logic
        var newShopper = new ShopperAggregate(
                UserId.Companion.random(),
                callingUser.getUserId()
        );
        var newMerchantOnDeviceRegistration = new MerchantOnDeviceRegistrationEntity(
                UUID.randomUUID(),
                createShopperCommand.getDeviceId(),
                createShopperCommand.getMerchantId()
        );
        newShopper.addMerchantAppRegistration(newMerchantOnDeviceRegistration);

        // registering events
        newShopper.registerEvent(new ShopperCreatedEvent(
                newShopper.getId(),
                createShopperCommand.getDeviceId(),
                createShopperCommand.getMerchantId()
        ));

        // saving aggregate
        shoppersRepository.save(newShopper);

        return newShopper;
    }

    public void addMerchantAppRegistration(MerchantOnDeviceRegistrationEntity appRegistration) {
        // TODO: make some checks, validations here
        this.merchantAppRegistrations.add(appRegistration);
        appRegistration.setShopper(this);
    }

    public static ShopperAggregate forTest() {
        var aggregate = new ShopperAggregate(
                UserId.Companion.random(),
                UserId.Companion.random()
        );

        var testMerchantAppRegistration = MerchantOnDeviceRegistrationEntity.forTest();
        aggregate.addMerchantAppRegistration(testMerchantAppRegistration);
        return aggregate;
    }

    public ShopperDto toDto() {
        return new ShopperDto(
            this.getId().asString(),
            this.getCreateAt(),
            this.getUpdatedAt(),
            this.getUpdateBy().asString(),
            this.merchantAppRegistrations.stream().map(registrationEntity -> new MerchantOnDeviceRegistrationDto(
                    registrationEntity.getDeviceId().asString(),
                    registrationEntity.getMerchantId().asString(),
                    registrationEntity.getVersion()
            )).collect(Collectors.toList()),
            this.getVersion()
        );
    }
}
