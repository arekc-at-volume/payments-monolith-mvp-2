package com.volume.users;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManagerFactory;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class JpaMerchantRepositoryTest {

    @Autowired
    JpaMerchantsRepository merchantRepository;
    @Autowired
    JpaUsersRepository userRepository;
    @Autowired
    EntityManagerFactory emf;

    @Test
    void saveNewInstance() {
        // arrange
        var newInstance = MerchantAggregate.randomForTest().build();
        // act
        merchantRepository.save(newInstance);

        // assert
        doInJPA(() -> emf, em -> {
            MerchantAggregate loadedInstance = em.find(MerchantAggregate.class, newInstance.getId());

            assertThat(loadedInstance).isNotNull();
            assertThat(loadedInstance).isEqualTo(newInstance);
            assertThat(loadedInstance.getVersion()).isEqualTo(0L);
        });
    }

    @Test
    void merchantAggregateCanBeLoadedViaMerchantRepository() {
        // arrange
        var newInstance = MerchantAggregate.randomForTest().build();
        doInJPA(() -> emf, em -> {
            em.persist(newInstance);
        });

        Optional<MerchantAggregate> loadedInstance = merchantRepository.findById(newInstance.getId());

        assertThat(loadedInstance).isPresent();
        assertThat(loadedInstance.get()).isEqualTo(newInstance);
    }

    @Test
    void merchantAggregateCanBeLoadedViaUsersRepository() {
        // arrange
        var newInstance = MerchantAggregate.randomForTest().build();
        doInJPA(() -> emf, em -> {
            em.persist(newInstance);
        });

        Optional<UserEntity> loadedInstance = userRepository.findById(newInstance.getId());

        assertThat(loadedInstance).isPresent();
        assertThat(loadedInstance.get()).isEqualTo(newInstance);
    }

    @Test
    void updateCausesVersionToBeIncremented() {
        // arrange
        var newInstance = MerchantAggregate.randomForTest()
                .clientSecret("client-secret")
                .build();
        doInJPA(() -> emf, em -> {
            em.persist(newInstance);
        });

        // act
        doInJPA(() -> emf, em -> {
            MerchantAggregate loadedInstance = em.find(MerchantAggregate.class, newInstance.getId());
            loadedInstance.changeSecret("new-secret");
        });

        // assert
        Optional<MerchantAggregate> loadedInstanceAfterUpdate = merchantRepository.findById(newInstance.getId());

        // make sure original instance has original data
        assertThat(newInstance.getClientSecret()).isEqualTo("client-secret");
        // check updated instance
        assertThat(loadedInstanceAfterUpdate).isPresent();
        assertThat(loadedInstanceAfterUpdate.get().getClientSecret()).isEqualTo("new-secret");
        assertThat(loadedInstanceAfterUpdate.get().getVersion()).isEqualTo(1L);
        assertThat(loadedInstanceAfterUpdate.get().getEmailAddress()).isEqualTo(newInstance.getEmailAddress());
        assertThat(loadedInstanceAfterUpdate.get().getPhoneNumber()).isEqualTo(newInstance.getPhoneNumber());
    }

}

