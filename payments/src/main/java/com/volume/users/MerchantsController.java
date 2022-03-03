package com.volume.users;

import com.google.common.base.Preconditions;
import com.volume.shared.domain.types.EmailAddress;
import com.volume.shared.domain.types.PhoneNumber;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedAggregateRoot;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Value
class CreateMerchantRequestDto {
    private String companyName;
    private String email;
    private String phoneNumber;

    public CreateMerchantCommand toCommand() {
        return new CreateMerchantCommand(
                this.companyName,
                EmailAddress.fromString(email),
                PhoneNumber.fromString(phoneNumber)
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

@RestController
@AllArgsConstructor
class MerchantsController {

    private final JpaMerchantsRepository merchantsRepository;

    @PostMapping(value = "/api/merchants", consumes = APPLICATION_JSON_VALUE)
    ResponseEntity createMerchant(@RequestBody CreateMerchantRequestDto requestDto) {
        MerchantAggregate merchantAggregate =
                MerchantAggregate.create(AuthenticatedUser.admin(), requestDto.toCommand(), merchantsRepository);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateMerchantResponseDto.fromAggregate(merchantAggregate));
    }

}
