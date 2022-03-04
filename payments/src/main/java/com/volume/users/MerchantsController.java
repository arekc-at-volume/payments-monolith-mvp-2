package com.volume.users;

import com.google.common.base.Preconditions;
import com.volume.shared.domain.messages.CreateMerchantCommand;
import com.volume.shared.domain.messages.MerchantCreatedEvent;
import com.volume.shared.domain.types.EmailAddress;
import com.volume.shared.domain.types.PhoneNumber;
import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedAggregateRoot;
import com.volume.shared.infrastructure.rest.RestErrorResponse;
import com.volume.users.exceptions.MerchantNotFoundException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Random;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
class CreateMerchantRequestDto {
    private final String companyName;
    private final String email;
    private final String phoneNumber;
    private final MerchantPayeeDetailsDto merchantPayeeDetails;

    public CreateMerchantCommand toCommand() {
        return new CreateMerchantCommand(
                this.companyName,
                EmailAddress.fromString(email),
                PhoneNumber.fromString(phoneNumber),
                this.merchantPayeeDetails
        );
    }

    public static CreateMerchantRequestDto forTest() {
        return new CreateMerchantRequestDto(
                "company ABC" + new Random().nextInt(),
                EmailAddress.fromString("boss@company.com").toString(),
                PhoneNumber.fromString("123456789").toString(),
                MerchantPayeeDetailsDto.forTest()
        );
    }
}

enum AccountIdentificationType {
    SORT_CODE,
    ACCOUNT_NUMBER,
    IBAN,
    BBAN,
    BIC,
    PAN,
    MASKED_PAN,
    MSISDN,
    BSB,
    NCC,
    ABA,
    ABA_WIRE,
    ABA_ACH,
    EMAIL,
    ROLL_NUMBER;
}

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
class AccountIdentificationDto {
    private final AccountIdentificationType type;
    private final String number;

    public static AccountIdentificationDto testAccountNumber() {
        return new AccountIdentificationDto(AccountIdentificationType.ACCOUNT_NUMBER, "12345678");
    }

    public static AccountIdentificationDto testSortCode() {
        return new AccountIdentificationDto(AccountIdentificationType.SORT_CODE, "123456");
    }

    public AccountIdentificationVO toDomain() {
        return new AccountIdentificationVO(this.type, this.number);
    }
}

enum AddressType {
    BUSINESS,
    CORRESPONDENCE,
    DELIVERY_TO,
    MAIL_TO,
    PO_BOX,
    POSTAL,
    RESIDENTIAL,
    STATEMENT,
    UNKNOWN
}

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
class PostalAddressDto {
    private final String addressLine;
    private final AddressType addressType;
    private final String buildingNumber;
    private final String country;
    private final String county;
    private final String department;
    private final String postCode;
    private final String streetName;
    private final String subDepartment;
    private final String townName;

    public PostalAddressVO toDomain() {
        return new PostalAddressVO(
                this.addressLine,
                this.addressType,
                this.buildingNumber,
                this.country,
                this.county,
                this.department,
                this.postCode,
                this.streetName,
                this.subDepartment,
                this.townName
        );
    }

    /**
     * Generates enough data for UK payee address
     *
     * @return
     */
    public static PostalAddressDto forTest() {
        return new PostalAddressDto(
                null,
                AddressType.POSTAL, // TODO: no idea if that is correct
                null,
                "United Kingdom",
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}

@Value
class CreateMerchantResponseDto {
    private String merchantId;

    public static CreateMerchantResponseDto fromAggregate(MerchantAggregate aggregate) {
        MerchantCreatedEvent event = getEvent(aggregate, MerchantCreatedEvent.class);
        return fromEvent(event);
    }

    private static CreateMerchantResponseDto fromEvent(MerchantCreatedEvent event) {
        return new CreateMerchantResponseDto(event.getMerchantId().getValue().toString());
    }

    private static <EventT> EventT getEvent(BaseKeyedVersionedAggregateRoot aggregate, Class<EventT> type) {
        verifyOneEventInResultOfType(aggregate, MerchantCreatedEvent.class);
        return type.cast(aggregate.resultEvents().get(0));
    }

    private static <EventT> void verifyOneEventInResultOfType(BaseKeyedVersionedAggregateRoot aggregate, Class<EventT> type) {
        Preconditions.checkState(
                aggregate.resultEvents().size() == 1,
                format("Expected one event of type in MerchantAggregate but found", type.getName(), aggregate.resultEvents().size())
        );
    }
}

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
class MerchantDto {
    private final String merchantId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String updatedBy;
    private final String companyName;
    private final String emailAddress;
    private final String phoneNumber;

    static MerchantDto fromAggregate(MerchantAggregate aggregate) {
        return aggregate.toDto();
    }
}

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

