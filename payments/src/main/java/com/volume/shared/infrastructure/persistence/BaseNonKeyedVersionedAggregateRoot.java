package com.volume.shared.infrastructure.persistence;

import lombok.ToString;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.*;

@MappedSuperclass
@ToString(callSuper = true)
public class BaseNonKeyedVersionedAggregateRoot extends BaseNonKeyedVersionedEntity {
    private final @Transient List<Object> domainEvents = new ArrayList<>();

    protected BaseNonKeyedVersionedAggregateRoot() {
        super();
    }

    protected void registerEvent(Object event) {
        domainEvents.add(Objects.requireNonNull(event));
    }

    @AfterDomainEventPublication
    protected void clearDomainEvents() {
        this.domainEvents.clear();
    }

    @DomainEvents
    protected Collection<Object> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}
