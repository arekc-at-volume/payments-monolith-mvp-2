package com.volume.users;

import com.volume.shared.domain.AuthenticatedUser;
import com.volume.shared.domain.messages.CreateShopperCommand;
import com.volume.shared.domain.messages.ShopperCreatedEvent;
import com.volume.shared.domain.types.DeviceId;
import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.rest.RestErrorResponse;
import com.volume.users.exceptions.ShopperNotFoundException;
import com.volume.users.persistence.JpaMerchantOnDeviceRegistrationRepository;
import com.volume.users.persistence.JpaShoppersRepository;
import com.volume.users.rest.MerchantOnDeviceRegistrationDto;
import com.volume.users.rest.ShopperDto;
import com.volume.users.rest.dto.CreateShopperRequestDto;
import com.volume.users.rest.dto.CreateShopperResponseDto;
import com.volume.yapily.YapilyApplicationUserId;
import com.volume.yapily.YapilyClient;
import com.volume.yapily.YapilyReferenceUserId;
import com.volume.yapily.YapilyUserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import yapily.sdk.ApplicationUser;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Getter
public class ShopperAggregate extends UserEntity {
    private YapilyApplicationUserId yapilyApplicationUserId;
    private YapilyReferenceUserId yapilyReferenceUserId;
    private YapilyUserId yapilyUserId;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "shopper")
    private Set<MerchantOnDeviceRegistrationEntity> merchantAppRegistrations = new HashSet<>();

    protected ShopperAggregate() {
        super();
    }

    public ShopperAggregate(UserId id, UserId updateBy, YapilyApplicationUserId yapilyApplicationUserId, YapilyReferenceUserId yapilyReferenceUserId, YapilyUserId yapilyUserId, Set<MerchantOnDeviceRegistrationEntity> merchantAppRegistrations) {
        super(id, updateBy);
        this.yapilyApplicationUserId = yapilyApplicationUserId;
        this.yapilyReferenceUserId = yapilyReferenceUserId;
        this.yapilyUserId = yapilyUserId;
        this.merchantAppRegistrations = merchantAppRegistrations;
    }

    public static ShopperAggregate create(
            AuthenticatedUser callingUser,
            CreateShopperCommand createShopperCommand,
            JpaShoppersRepository shoppersRepository,
            YapilyClient yapilyClient
    ) {
        // command processing logic
        var yapilyApplicationId = YapilyApplicationUserId.Companion.random();
        var yapilyReferenceId = YapilyReferenceUserId.Companion.random();
        ApplicationUser applicationUser = yapilyClient.createApplicationUser(yapilyApplicationId, yapilyReferenceId);

        var newShopper = new ShopperAggregate(
                UserId.Companion.random(),
                callingUser.getUserId(),
                new YapilyApplicationUserId(applicationUser.getApplicationUserId()),
                YapilyReferenceUserId.Companion.fromString(applicationUser.getReferenceId()),
                YapilyUserId.Companion.fromString(applicationUser.getUuid()),
                new HashSet<>()
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
                UserId.Companion.random(),
                YapilyApplicationUserId.Companion.random(),
                YapilyReferenceUserId.Companion.random(),
                YapilyUserId.Companion.random(),
                new HashSet<>()
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
                        registrationEntity.getDeviceId(),
                        registrationEntity.getMerchantId(),
                        registrationEntity.getVersion()
                )).collect(Collectors.toList()),
                this.getVersion()
        );
    }
}
