package com.volume.users.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volume.shared.domain.types.EmailAddress;
import com.volume.shared.domain.types.PhoneNumber;
import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.rest.RestErrorResponse;
import com.volume.users.MerchantAggregate;
import com.volume.users.rest.dto.CreateMerchantRequestDto;
import com.volume.users.rest.dto.MerchantDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
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
    @Autowired
    private ObjectMapper mapper;

    @Test
    void postMerchant_returns201CreatedWithNewMerchantId() throws Exception {
        var request = CreateMerchantRequestDto.forTest();
        mvc.perform(
                post("/api/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        ).andExpect(status().isCreated());

        doInJPA(() -> emf, em -> {
            Query query = em.createQuery("select m from MerchantAggregate m where m.companyName = :companyName");
            query.setParameter("companyName", request.getCompanyName());
            MerchantAggregate merchant = (MerchantAggregate )query.getResultList().get(0);

            assertThat(merchant.getId()).isNotNull();
            assertThat(merchant.getCreateAt()).isNotNull();
            assertThat(merchant.getUpdatedAt()).isNotNull();
            assertThat(merchant.getUpdateBy()).isNotNull();
            assertThat(merchant.getCompanyName()).isEqualTo(request.getCompanyName());
            assertThat(merchant.getPhoneNumber()).isEqualTo(request.getPhoneNumber());
            assertThat(merchant.getEmailAddress()).isEqualTo(request.getEmail());
        });
    }

    @Test
    void whenMerchantExists_getMerchantById_returns200OkWithAllMerchantDetails() throws Exception {
        var existingMerchant = MerchantAggregate.randomForTest().build();
        var existingMerchantDto = existingMerchant.toDto();
        doInJPA(() -> emf, em -> {
            em.persist(existingMerchant);
        });

        String responseString = mvc.perform(
                        get("/api/merchants/" + existingMerchant.getId().asString())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MerchantDto responseDto = mapper.readValue(responseString, MerchantDto.class);

        assertThat(existingMerchantDto).isEqualTo(responseDto);
    }

    @Test
    void whenMerchantDoesNotExist_getMerchantById_returns404NotFoundWithProperMessage() throws Exception {
        var merchantId = UserId.Companion.random().asString();
        String responseString = mvc.perform(
                        get("/api/merchants/" + merchantId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        RestErrorResponse restErrorResponse = mapper.readValue(responseString, RestErrorResponse.class);

        assertThat(restErrorResponse).isNotNull();
        assertThat(restErrorResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restErrorResponse.getMessage()).isEqualTo(format("Merchant with id=%s does not exist", merchantId));
        assertThat(restErrorResponse.getTraceId()).isNotBlank();
    }

}