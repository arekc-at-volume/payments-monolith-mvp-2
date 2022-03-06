package com.volume.transfers.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volume.transfers.rest.dto.CreateTransferRequestDto;
import com.volume.transfers.rest.dto.CreateTransferResponseDto;
import com.volume.users.MerchantAggregate;
import com.volume.users.ShopperAggregate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityManagerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransfersControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    EntityManagerFactory emf;
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
    void createTransfer() throws Exception {
        var requestDto = CreateTransferRequestDto.forTest(shopperAggregate.getId(), merchantAggregate.getId());
        String responseAsString = mvc.perform(
                        post("/api/transfers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        var responseDto = mapper.readValue(responseAsString, CreateTransferResponseDto.class);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getTransferId()).isNotNull();
        assertThat(responseDto.getAmount()).isEqualTo(requestDto.getAmount());
        assertThat(responseDto.getCurrency()).isEqualTo(requestDto.getCurrency());
        assertThat(responseDto.getShopperId()).isEqualTo(requestDto.getShopperId());
        assertThat(responseDto.getMerchantId()).isEqualTo(requestDto.getMerchantId());
        assertThat(responseDto.getDescription()).isEqualTo(requestDto.getDescription());
        assertThat(responseDto.getReference()).isEqualTo(requestDto.getReference());
    }

}