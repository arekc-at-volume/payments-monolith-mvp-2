package com.volume.users.rest;

import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.rest.RestErrorResponse;
import com.volume.users.*;
import com.volume.users.exceptions.MerchantNotFoundException;
import com.volume.users.persistence.JpaMerchantsRepository;
import com.volume.users.rest.dtos.CreateMerchantRequestDto;
import com.volume.users.rest.dtos.CreateMerchantResponseDto;
import com.volume.users.rest.dtos.MerchantDto;
import lombok.AllArgsConstructor;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@AllArgsConstructor
class MerchantsController {

    private final JpaMerchantsRepository merchantsRepository;
    private final Tracer tracer;

    @PostMapping(value = "/api/merchants", consumes = APPLICATION_JSON_VALUE)
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

