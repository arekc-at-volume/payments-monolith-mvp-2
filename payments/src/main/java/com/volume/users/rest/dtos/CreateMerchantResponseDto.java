package com.volume.users.rest.dtos;

import com.google.common.base.Preconditions;
import com.volume.shared.domain.messages.MerchantCreatedEvent;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedAggregateRoot;
import com.volume.users.MerchantAggregate;
import lombok.Value;

import static java.lang.String.format;

@Value
public class CreateMerchantResponseDto {
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
