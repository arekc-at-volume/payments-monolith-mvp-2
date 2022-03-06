package com.volume.transfers;

import com.volume.transfers.persistence.JpaTransferAggregateRepository;
import com.volume.transfers.rest.dto.CreateTransferRequestDto;
import com.volume.transfers.rest.dto.CreateTransferResponseDto;
import com.volume.transfers.rest.dto.GeneratePaymentAuthorizationUrlRequestDto;
import com.volume.shared.domain.AuthenticatedUser;
import com.volume.users.MerchantAggregate;
import com.volume.users.ShopperAggregate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManagerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferAggregateTest {

    @Autowired
    JpaTransferAggregateRepository transferRepository;
    @Autowired TransferAggregateService transferService;
    @Autowired EntityManagerFactory emf;
    private ShopperAggregate shopperAggregate;
    private MerchantAggregate merchantAggregate;

    @BeforeAll
    void setup() {
        shopperAggregate = ShopperAggregate.forTest();
        merchantAggregate = MerchantAggregate.randomForTest().build();
        doInJPA(() -> emf, em -> { em.persist(shopperAggregate); });
        doInJPA(() -> emf, em -> { em.persist(merchantAggregate); });
    }

    @Test
    void createNewTransfer() {
        // act
        var requestDto = CreateTransferRequestDto.forTest(shopperAggregate.getId(), merchantAggregate.getId());
        CreateTransferResponseDto newTransfer = transferService.createNewTransfer(AuthenticatedUser.merchant(), requestDto);

        // assert
        doInJPA(() -> emf, em -> {
            TransferAggregate transferAggregate = em.find(TransferAggregate.class, newTransfer.getTransferId());
            assertThat(transferAggregate).isNotNull();
            assertThat(transferAggregate.getTransferStatus()).isEqualTo(TransferStatus.CREATED);
            assertThat(transferAggregate.getShopperId()).isEqualTo(newTransfer.getShopperId());
            assertThat(transferAggregate.getMerchantId()).isEqualTo(newTransfer.getMerchantId());
            assertThat(transferAggregate.getPayee()).isNotNull();
        });
    }

    @Test
    void generateAuthorizationUrl() {
        // arrange
        CreateTransferResponseDto newTransfer;
        {
            var requestDto = CreateTransferRequestDto.forTest(shopperAggregate.getId(), merchantAggregate.getId());
            newTransfer = transferService.createNewTransfer(AuthenticatedUser.merchant(), requestDto);
        }

        // act
        var requestDto = new GeneratePaymentAuthorizationUrlRequestDto(newTransfer.getTransferId());
        var responseDto = transferService.generateAuthorizationUrl(AuthenticatedUser.merchant(), requestDto);

        // assert

        assertThat(responseDto.getTransferId()).isEqualTo(newTransfer.getTransferId());
        assertThat(responseDto.getAuthorizationUrl()).isNotBlank();
        assertThat(responseDto.getQrCodeUrl()).isNotBlank();

        doInJPA(() -> emf, em -> {
            // TODO  verify that aggregate was updated properly
        });
    }

}