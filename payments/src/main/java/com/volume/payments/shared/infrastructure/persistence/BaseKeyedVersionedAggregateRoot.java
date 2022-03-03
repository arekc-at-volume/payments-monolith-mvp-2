package com.volume.payments.shared.infrastructure.persistence;

import lombok.ToString;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.*;

@MappedSuperclass
@ToString(callSuper = true)
public class BaseKeyedVersionedAggregateRoot<PK extends Serializable> extends BaseKeyedVersionedEntity<PK> {
    private final @Transient List<Object> domainEvents = new ArrayList<>();
    private final @Transient List<Object> resultEvents = new ArrayList<>();

    protected BaseKeyedVersionedAggregateRoot() {
        super();
    }

    protected BaseKeyedVersionedAggregateRoot(PK id) {
        super(id);
    }

    protected void registerEvent(Object event) {
        domainEvents.add(Objects.requireNonNull(event));
        resultEvents.add(Objects.requireNonNull(event));
    }

    @AfterDomainEventPublication
    protected void clearDomainEvents() {
        this.domainEvents.clear();
    }

    @DomainEvents
    protected Collection<Object> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }


    public void clearResultEvents() {
        this.resultEvents.clear();
    }

    public List<Object> resultEvents() {
        return Collections.unmodifiableList(resultEvents);
    }
}

