package com.volume.users.rest;

import com.volume.shared.domain.AuthenticatedUser;
import com.volume.shared.domain.messages.CreateShopperCommand;
import com.volume.shared.domain.types.DeviceId;
import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.rest.RestErrorResponse;
import com.volume.users.ShopperAggregate;
import com.volume.users.exceptions.ShopperNotFoundException;
import com.volume.users.persistence.JpaMerchantOnDeviceRegistrationRepository;
import com.volume.users.persistence.JpaShoppersRepository;
import com.volume.users.rest.dto.CreateShopperRequestDto;
import com.volume.users.rest.dto.CreateShopperResponseDto;
import com.volume.yapily.YapilyClient;
import lombok.AllArgsConstructor;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
