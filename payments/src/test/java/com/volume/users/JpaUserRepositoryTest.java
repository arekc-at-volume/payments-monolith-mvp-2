package com.volume.users;

import com.volume.shared.domain.types.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManagerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JpaUserRepositoryTest {

    @Autowired
    JpaUsersRepository usersRepository;
    @Autowired
    EntityManagerFactory emf;

    @Test
    void usersRepositoryIsAbleToReturnAllUsers() {
        var shopper = new ShopperAggregate(UserId.random(), UserId.random());
        var merchant = new MerchantAggregate(UserId.random(), UserId.random(), "secret");
        doInJPA(() -> emf, em -> {
            em.persist(shopper);
            em.persist(merchant);
        });

        List<UserEntity> users = usersRepository.findAll();

        assertThat(users.size()).isEqualTo(2);
        assertThat(users).contains(shopper, merchant);
    }


}