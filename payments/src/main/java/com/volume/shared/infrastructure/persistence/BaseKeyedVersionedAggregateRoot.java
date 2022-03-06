package com.volume.shared.infrastructure.persistence;

import com.volume.yapily.YapilyClient;
import lombok.ToString;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

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

    // TODO: These are to be moved somewhere

    @Transactional
    public <Command, Aggregate, AggregateRepository extends BaseKeyedVersionedAggregateRepository> Aggregate runOnAggregate(Command command, Function<Command, Aggregate> handler, AggregateRepository repository) {
        var aggregate = handler.apply(command);

        // REMEMBER: save always AFTER register
        repository.save(aggregate);
        return aggregate;
    }

    @Transactional
    public <Command, Aggregate, AggregateRepository extends BaseKeyedVersionedAggregateRepository> Aggregate runOnAggregate(Command command, BiFunction<Command, YapilyClient, Aggregate> handler, AggregateRepository repository, YapilyClient yapilyClient) {
        var aggregate = handler.apply(command, yapilyClient);

        // REMEMBER: save always AFTER register
        repository.save(aggregate);
        return aggregate;
    }
}

