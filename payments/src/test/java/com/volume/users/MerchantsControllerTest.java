package com.volume.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityManagerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class MerchantsControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    EntityManagerFactory emf;
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void postMerchant_returns201CreatedWithNewMerchantId() throws Exception {
        var request = CreateMerchantRequestDto.forTest();
        mvc.perform(
                post("/api/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        ).andExpect(status().isCreated());
    }

    @Test
    void getMerchantById_returns200OkWithAllMerchantDetails() throws Exception {
        var existingMerchant = MerchantAggregate.randomForTest().build();
        var existingMerchantDto = existingMerchant.toDto();
        doInJPA(() -> emf, em -> {
            em.persist(existingMerchant);
        });

        String responseString = mvc.perform(
                        get("/api/merchants/" + existingMerchant.getId().getValue().toString())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MerchantDto responseDto = mapper.readValue(responseString, MerchantDto.class);

        assertThat(existingMerchantDto).isEqualTo(responseDto);
    }

}