package com.volume.shared.infrastructure.persistence;

import com.volume.PaymentsApplication;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

@Entity
class SimpleKeyedVersionedEntity extends BaseKeyedVersionedEntity<UUID> {
    private String name;
    private Integer age;

    protected SimpleKeyedVersionedEntity() {}

    public SimpleKeyedVersionedEntity(UUID id, String name, Integer age) {
        super(id);
        this.name = name;
        this.age = age;
    }
}

interface SimpleKeyedVersionedEntityRepository extends BaseKeyedVersionedEntityRepository<SimpleKeyedVersionedEntity, UUID> {}

@ContextConfiguration(classes = PaymentsApplication.class)
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
// does not require @DirtiesContext because it works on its own type and repository
class BaseKeyedVersionedEntityTest {

    @Autowired
    SimpleKeyedVersionedEntityRepository repository;
    @Autowired
    EntityManagerFactory emf;

    @Test
    void saveNewInstance() {
        // arrange
        var newInstance = new SimpleKeyedVersionedEntity(UUID.randomUUID(), "Arek", 45);

        // act
        repository.save(newInstance);

        // assert
        doInJPA(() -> emf, em -> {
           em.find(SimpleKeyedVersionedEntity.class, newInstance.getId());
        });
    }

    @Test
    void loadExistingInstance() {
        // arrange
        var newInstance = new SimpleKeyedVersionedEntity(UUID.randomUUID(), "Arek", 45);
        repository.save(newInstance);

        // act
        Optional<SimpleKeyedVersionedEntity> loadedInstance = repository.findById(newInstance.getId());

        // assert
        assertThat(loadedInstance).isPresent();
        assertThat(loadedInstance.get()).isEqualTo(newInstance);
    }

    @Test
    void whenPrimaryKeyIsNullValidExceptionIsThrown() {
        assertThatThrownBy(() -> {
            var newInstance = new SimpleKeyedVersionedEntity(null, "Arek", 45);
        })
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Primary key of an entity of type com.volume.shared.infrastructure.persistence.SimpleKeyedVersionedEntity cannot be null");

    }
}


