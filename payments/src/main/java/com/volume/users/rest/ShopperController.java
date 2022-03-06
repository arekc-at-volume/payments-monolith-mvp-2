package com.volume.users.rest;

import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.rest.RestErrorResponse;
import com.volume.users.ShopperApplicationService;
import com.volume.users.exceptions.ShopperNotFoundException;
import com.volume.users.persistence.JpaShoppersRepository;
import com.volume.users.rest.dto.CreateShopperRequestDto;
import com.volume.users.rest.dto.CreateShopperResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@AllArgsConstructor
class ShopperController {

    private final ShopperApplicationService shopperApplicationService;
    private final Tracer tracer;

    @PostMapping(value = "/api/shoppers", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<CreateShopperResponseDto> createOrRetrieveExistingShopper(@RequestBody CreateShopperRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(shopperApplicationService.createShopper(requestDto));
    }

    @GetMapping(value = "/api/shoppers/{shopperId}", produces = APPLICATION_JSON_VALUE)
    ResponseEntity<ShopperDto> findShopperById(@PathVariable("shopperId") String shopperIdParam) {
        var shopperId = UserId.Companion.fromString(shopperIdParam);
        return
                shopperApplicationService.findShopperById(shopperId)
                        .map(shopperDto -> ResponseEntity.status(HttpStatus.OK).body(shopperDto))
                        .orElseThrow(() -> new ShopperNotFoundException(shopperId));
    }

    @ExceptionHandler(ShopperNotFoundException.class)
    ResponseEntity<RestErrorResponse> handle(ShopperNotFoundException exception) {
        return RestErrorResponse.fromException(HttpStatus.NOT_FOUND, exception, tracer);
    }

}
