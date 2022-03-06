package com.volume.transfers;

import com.volume.transfers.persistence.JpaTransferAggregateRepository;
import com.volume.users.AccountIdentificationVO;
import com.volume.users.PostalAddressVO;
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
class TransferAggregateRepositoryTest {

    @Autowired
    JpaTransferAggregateRepository transferRepository;
    @Autowired
    EntityManagerFactory emf;

    @Test
    void saveNewInstance() {

        // arrange
        var newInstance = TransferAggregate.forTest();

        // act
        transferRepository.save(newInstance);

        doInJPA(() -> emf, em -> {
            TransferAggregate transferAggregate = em.find(TransferAggregate.class, newInstance.getId());

            // assert
            assertThat(transferAggregate.getId()).isNotNull();
            assertThat(transferAggregate.getShopperId()).isEqualTo(newInstance.getShopperId());
            assertThat(transferAggregate.getMerchantId()).isEqualTo(newInstance.getMerchantId());
            assertThat(transferAggregate.getYapilyApplicationUserId()).isEqualTo(newInstance.getYapilyApplicationUserId());
            assertThat(transferAggregate.getYapilyUserId()).isEqualTo(newInstance.getYapilyUserId());
            assertThat(transferAggregate.getAmount()).isEqualTo(newInstance.getAmount());
            assertThat(transferAggregate.getCurrency()).isEqualTo(newInstance.getCurrency());
            assertThat(transferAggregate.getDescription()).isEqualTo(newInstance.getDescription());
            assertThat(transferAggregate.getReference()).isEqualTo(newInstance.getReference());
            assertThat(transferAggregate.getIdempotencyId()).isNotNull();
            assertThat(transferAggregate.getInstitutionId()).isEqualTo(newInstance.getInstitutionId());
            // TODO: solve problem of time precision stored in database
//            assertThat(transferAggregate.getCreatedAt()).isEqualTo(newInstance.getCreatedAt());
//            assertThat(transferAggregate.getUpdatedAt()).isEqualTo(newInstance.getUpdatedAt());
            assertThat(transferAggregate.getUpdatedBy()).isEqualTo(newInstance.getUpdatedBy());
            assertThat(transferAggregate.getPayee().getTransfer().getId()).isEqualTo(transferAggregate.getId());
            assertThat(transferAggregate.getPayee().getPostalAddress()).isEqualTo(PostalAddressVO.testDomesticPaymentUKPayeeAddress());
            assertThat(transferAggregate.getPayee().getAccountIdentification1()).isEqualTo(AccountIdentificationVO.testAccountNumber());
            assertThat(transferAggregate.getPayee().getAccountIdentification2()).isEqualTo(AccountIdentificationVO.testSortCode());
        });
    }

    @Test
    void loadExistingInstance() {
        // arrange
        var newInstance = TransferAggregate.forTest();
        doInJPA(() -> emf, em -> {
            em.persist(newInstance);
        });

        Optional<TransferAggregate> loadedInstance = transferRepository.findById(newInstance.getId());

        // assert
        assertThat(loadedInstance).isPresent();
        assertThat(loadedInstance.get().getId()).isNotNull();
        assertThat(loadedInstance.get().getShopperId()).isEqualTo(newInstance.getShopperId());
        assertThat(loadedInstance.get().getMerchantId()).isEqualTo(newInstance.getMerchantId());
        assertThat(loadedInstance.get().getAmount()).isEqualTo(newInstance.getAmount());
        assertThat(loadedInstance.get().getCurrency()).isEqualTo(newInstance.getCurrency());
        assertThat(loadedInstance.get().getDescription()).isEqualTo(newInstance.getDescription());
        assertThat(loadedInstance.get().getReference()).isEqualTo(newInstance.getReference());
        assertThat(loadedInstance.get().getIdempotencyId()).isNotNull();
        assertThat(loadedInstance.get().getInstitutionId()).isEqualTo(newInstance.getInstitutionId());
        // TODO: solve problem of time precision stored in database
//            assertThat(transferAggregate.getCreatedAt()).isEqualTo(newInstance.getCreatedAt());
//            assertThat(transferAggregate.getUpdatedAt()).isEqualTo(newInstance.getUpdatedAt());
        assertThat(loadedInstance.get().getUpdatedBy()).isEqualTo(newInstance.getUpdatedBy());
        assertThat(loadedInstance.get().getUpdatedBy()).isEqualTo(newInstance.getUpdatedBy());
        assertThat(loadedInstance.get().getPayee().getTransfer().getId()).isEqualTo(loadedInstance.get().getId());
        assertThat(loadedInstance.get().getPayee().getPostalAddress()).isEqualTo(PostalAddressVO.testDomesticPaymentUKPayeeAddress());
        assertThat(loadedInstance.get().getPayee().getAccountIdentification1()).isEqualTo(AccountIdentificationVO.testAccountNumber());
        assertThat(loadedInstance.get().getPayee().getAccountIdentification2()).isEqualTo(AccountIdentificationVO.testSortCode());
    }

}