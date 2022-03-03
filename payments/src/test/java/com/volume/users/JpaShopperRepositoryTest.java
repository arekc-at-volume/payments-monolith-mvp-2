package com.volume.users;

import com.volume.shared.domain.types.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManagerFactory;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JpaShopperRepositoryTest {

    @Autowired
    JpaShoppersRepository shoppersRepository;
    @Autowired
    JpaUsersRepository userRepository;
    @Autowired
    EntityManagerFactory emf;

    @Test
    void saveNewInstance() {
        // arrange
        var newInstance = new ShopperAggregate(new UserId(UUID.randomUUID()), new UserId(UUID.randomUUID()));

        // act
        shoppersRepository.save(newInstance);

        // assert
        doInJPA(() -> emf, em -> {
            ShopperAggregate loadedInstance = em.find(ShopperAggregate.class, newInstance.getId());

            assertThat(loadedInstance).isNotNull();
            assertThat(loadedInstance).isEqualTo(newInstance);
            assertThat(loadedInstance.getVersion()).isEqualTo(0L);
        });
    }

    @Test
    void shopperAggregateCanBeLoadedViaShopperRepository() {
        // arrange
        var newInstance = new ShopperAggregate(new UserId(UUID.randomUUID()), new UserId(UUID.randomUUID()));
        doInJPA(() -> emf, em -> {
            em.persist(newInstance);
        });

        Optional<ShopperAggregate> loadedInstance = shoppersRepository.findById(newInstance.getId());

        assertThat(loadedInstance).isPresent();
        assertThat(loadedInstance.get()).isEqualTo(newInstance);
    }

    @Test
    void shopperAggregateCanBeLoadedViaUsersRepository() {
        // arrange
        var newInstance = new ShopperAggregate(new UserId(UUID.randomUUID()), new UserId(UUID.randomUUID()));
        doInJPA(() -> emf, em -> {
            em.persist(newInstance);
        });

        Optional<UserEntity> loadedInstance = userRepository.findById(newInstance.getId());

        assertThat(loadedInstance).isPresent();
        assertThat(loadedInstance.get()).isEqualTo(newInstance);
    }

}
