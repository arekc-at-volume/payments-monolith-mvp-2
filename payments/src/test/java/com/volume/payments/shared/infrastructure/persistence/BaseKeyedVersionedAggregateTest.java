package com.volume.payments.shared.infrastructure.persistence;

import com.volume.payments.PaymentsApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

@Entity
class SimpleKeyedVersionedAggregate extends BaseKeyedVersionedAggregateRoot<UUID> {
    private String name;
    private Integer age;

    protected SimpleKeyedVersionedAggregate() {}

    public SimpleKeyedVersionedAggregate(UUID id, String name, Integer age) {
        super(id);
        this.name = name;
        this.age = age;
    }
}

interface SimpleKeyedVersionedAggregateRepository extends BaseKeyedVersionedAggregateRepository<SimpleKeyedVersionedAggregate, UUID> {}

@ContextConfiguration(classes = PaymentsApplication.class)
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class BaseKeyedVersionedAggregateTest {

    @Autowired
    SimpleKeyedVersionedAggregateRepository repository;
    @Autowired
    EntityManagerFactory emf;

    @Test
    void saveNewInstance() {
        // arrange
        var newInstance = new SimpleKeyedVersionedAggregate(UUID.randomUUID(), "Arek", 45);

        // act
        repository.save(newInstance);

        // assert
        doInJPA(() -> emf, em -> {
            em.find(SimpleKeyedVersionedAggregate.class, newInstance.getId());
        });
    }

    @Test
    void loadExistingInstance() {
        // arrange
        var newInstance = new SimpleKeyedVersionedAggregate(UUID.randomUUID(), "Arek", 45);
        repository.save(newInstance);

        // act
        Optional<SimpleKeyedVersionedAggregate> loadedInstance = repository.findById(newInstance.getId());

        // assert
        assertThat(loadedInstance).isPresent();
        assertThat(loadedInstance.get()).isEqualTo(newInstance);
    }
}
