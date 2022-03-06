package com.volume.users;

import com.volume.shared.domain.AuthenticatedUser;
import com.volume.shared.domain.messages.CreateShopperCommand;
import com.volume.shared.domain.types.DeviceId;
import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.rest.RestErrorResponse;
import com.volume.users.exceptions.ShopperNotFoundException;
import com.volume.users.persistence.JpaMerchantOnDeviceRegistrationRepository;
import com.volume.users.persistence.JpaShoppersRepository;
import com.volume.users.rest.ShopperDto;
import com.volume.users.rest.dto.CreateShopperRequestDto;
import com.volume.users.rest.dto.CreateShopperResponseDto;
import com.volume.yapily.YapilyClient;
import lombok.AllArgsConstructor;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ShopperApplicationService {

    private final JpaShoppersRepository shoppersRepository;
    private final JpaMerchantOnDeviceRegistrationRepository merchantOnDeviceRegistrationRepository;
    private final YapilyClient yapilyClient;
    private final Tracer tracer;

    public CreateShopperResponseDto createShopper(CreateShopperRequestDto requestDto) {
        // case 1: shopper already is created for deviceId/merchantId but for some reason user lost that data on the device
        // so first let's check if shopper for given merchantId and deviceId already exists

        Optional<UserId> existingShopperId = tryRetrievingExistingShopperId(requestDto.getDeviceId(), requestDto.getMerchantId());
        if (existingShopperId.isPresent()) {
            return
                    new CreateShopperResponseDto(
                            existingShopperId.get(),
                            requestDto.getDeviceId(),
                            requestDto.getMerchantId()
                    );
        }

        // TODO: here, maybe, we need to add case for same deviceId->shopperId but different merchantId

        // create shopper
        CreateShopperCommand createShopperCommand = requestDto.toCommand();
        ShopperAggregate shopperAggregate = ShopperAggregate.create(AuthenticatedUser.merchant(), createShopperCommand, shoppersRepository, yapilyClient);

        return CreateShopperResponseDto.fromAggregate(shopperAggregate);
    }

    public Optional<ShopperDto> findShopperById(UserId shopperId) {
        return shoppersRepository.findById(shopperId)
                        .map(aggregate -> ShopperDto.fromAggregate(aggregate));
    }

    private Optional<UserId> tryRetrievingExistingShopperId(DeviceId deviceId, UserId merchantId) {
        return
                merchantOnDeviceRegistrationRepository.findByDeviceIdAndMerchantId(deviceId, merchantId)
                        .map(registration -> Optional.of(registration.getShopper().getId()))
                        .orElse(Optional.empty());
    }

}
