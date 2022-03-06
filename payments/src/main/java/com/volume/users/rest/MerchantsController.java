package com.volume.users.rest;

import com.volume.shared.domain.messages.CreateShopperCommand;
import com.volume.shared.domain.messages.ShopperCreatedEvent;
import com.volume.shared.domain.types.DeviceId;
import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.rest.RestErrorResponse;
import com.volume.shared.infrastructure.rest.dto.DtoUtilities;
import com.volume.users.AuthenticatedUser;
import com.volume.users.MerchantAggregate;
import com.volume.users.ShopperAggregate;
import com.volume.users.exceptions.MerchantNotFoundException;
import com.volume.users.exceptions.ShopperNotFoundException;
import com.volume.users.persistence.JpaMerchantOnDeviceRegistrationRepository;
import com.volume.users.persistence.JpaMerchantsRepository;
import com.volume.users.persistence.JpaShoppersRepository;
import com.volume.users.rest.dto.CreateMerchantRequestDto;
import com.volume.users.rest.dto.CreateMerchantResponseDto;
import com.volume.users.rest.dto.MerchantDto;
import com.volume.yapily.YapilyClient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@AllArgsConstructor
class MerchantsController {

    private final JpaMerchantsRepository merchantsRepository;
    private final Tracer tracer;

    @PostMapping(value = "/api/merchants", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity createMerchant(@RequestBody CreateMerchantRequestDto requestDto) {
        MerchantAggregate merchantAggregate =
                MerchantAggregate.create(AuthenticatedUser.admin(), requestDto.toCommand(), merchantsRepository);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateMerchantResponseDto.fromAggregate(merchantAggregate));
    }

    @GetMapping("/api/merchants/{merchantId}")
    ResponseEntity<MerchantDto> findMerchant(@PathVariable("merchantId") String userId) throws MerchantNotFoundException {
        UserId merchantId = UserId.Companion.fromString(userId);
        return merchantsRepository.findById(merchantId)
                .map(aggregate -> ResponseEntity.status(HttpStatus.OK).body(MerchantDto.fromAggregate(aggregate)))
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));
    }

    /**
     * I'll start with exception handling from the most granular going up if I see a benefit.
     */
    @ExceptionHandler(MerchantNotFoundException.class)
    ResponseEntity<RestErrorResponse> handle(MerchantNotFoundException exception) {
        return RestErrorResponse.fromException(HttpStatus.NOT_FOUND, exception, tracer);
    }

}


@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
class CreateShopperRequestDto {
    private final String deviceId;
    private final String merchantId;

    public CreateShopperCommand toCommand() {
        return new CreateShopperCommand(
                DeviceId.Companion.fromString(deviceId),
                UserId.Companion.fromString(merchantId)
        );
    }
}

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
class CreateShopperResponseDto {
    private final String shopperId;
    private final String deviceId;
    private final String merchantId;

    public static CreateShopperResponseDto fromAggregate(ShopperAggregate aggregate) {
        return DtoUtilities.createResultFromEvent(
                aggregate,
                ShopperCreatedEvent.class,
                event -> new CreateShopperResponseDto(
                        event.getShopperId().asString(),
                        event.getDeviceId().asString(),
                        event.getMerchantId().asString()
                )
        );
    }
}


@RestController
@AllArgsConstructor
class ShopperController {

    private final JpaShoppersRepository shoppersRepository;
    private final JpaMerchantOnDeviceRegistrationRepository merchantOnDeviceRegistrationRepository;
    private final YapilyClient yapilyClient;
    private final Tracer tracer;

    @PostMapping(value = "/api/shoppers", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<CreateShopperResponseDto> createOrRetrieveExistingShopper(@RequestBody CreateShopperRequestDto requestDto) {

        // case 1: shopper already is created for deviceId/merchantId but for some reason user lost that data on the device
        // so first let's check if shopper for given merchantId and deviceId already exists
        var deviceId = DeviceId.Companion.fromString(requestDto.getDeviceId());
        var merchantId = UserId.Companion.fromString(requestDto.getMerchantId());

        Optional<UserId> existingShopperId = tryRetrievingExistingShopperId(deviceId, merchantId);
        if (existingShopperId.isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            new CreateShopperResponseDto(
                                    existingShopperId.get().asString(),
                                    deviceId.asString(),
                                    merchantId.asString()
                            )
                    );
        }

        // TODO: here, maybe, we need to add case for same deviceId->shopperId but different merchantId

        // create shopper
        CreateShopperCommand createShopperCommand = requestDto.toCommand();
        ShopperAggregate shopperAggregate = ShopperAggregate.create(AuthenticatedUser.merchant(), createShopperCommand, shoppersRepository, yapilyClient);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateShopperResponseDto.fromAggregate(shopperAggregate));
    }

    @GetMapping(value = "/api/shoppers/{shopperId}", produces = APPLICATION_JSON_VALUE)
    ResponseEntity<ShopperDto> findShopperById(@PathVariable("shopperId") String shopperIdParam) {
        var shopperId = UserId.Companion.fromString(shopperIdParam);
        return
                shoppersRepository.findById(shopperId)
                        .map(aggregate -> ResponseEntity.status(HttpStatus.OK).body(ShopperDto.fromAggregate(aggregate)))
                        .orElseThrow(() -> new ShopperNotFoundException(shopperId));
    }

    private Optional<UserId> tryRetrievingExistingShopperId(DeviceId deviceId, UserId merchantId) {
        return
                merchantOnDeviceRegistrationRepository.findByDeviceIdAndMerchantId(deviceId, merchantId)
                        .map(registration -> Optional.of(registration.getShopper().getId()))
                        .orElse(Optional.empty());
    }

    @ExceptionHandler(ShopperNotFoundException.class)
    ResponseEntity<RestErrorResponse> handle(ShopperNotFoundException exception) {
        return RestErrorResponse.fromException(HttpStatus.NOT_FOUND, exception, tracer);
    }
}

