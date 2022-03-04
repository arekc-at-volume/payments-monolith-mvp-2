package com.volume.shared.infrastructure.rest.dto;

import com.google.common.base.Preconditions;
import com.volume.shared.domain.messages.MerchantCreatedEvent;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedAggregateRoot;

import java.util.function.Function;

import static java.lang.String.format;

public class DtoUtilities {

    public static <EventT, DtoResultT> DtoResultT createResultFromEvent(
            BaseKeyedVersionedAggregateRoot aggregate,
            Class<EventT> eventType,
            Function<EventT, DtoResultT> dtoFactory
    ) {
        EventT event = getEvent(aggregate, eventType);
        return dtoFactory.apply(event);
    }

    /**
     * Gets an event from an aggregate.
     * For now this is simplified version making an assumptions that:
     * - there is an event in an aggregate
     * - there is only one event in an aggregate
     * - event is of required type
     *
     * In the future it will need to be made a little bit smarter.
     */
    public static <EventT> EventT getEvent(BaseKeyedVersionedAggregateRoot aggregate, Class<EventT> type) {
        verifyOneEventInResultOfType(aggregate, MerchantCreatedEvent.class);
        return type.cast(aggregate.resultEvents().get(0));
    }

    public static <EventT> void verifyOneEventInResultOfType(BaseKeyedVersionedAggregateRoot aggregate, Class<EventT> type) {
        Preconditions.checkState(
                aggregate.resultEvents().size() == 1,
                format("Expected one event of type in MerchantAggregate but found", type.getName(), aggregate.resultEvents().size())
        );
    }
}
