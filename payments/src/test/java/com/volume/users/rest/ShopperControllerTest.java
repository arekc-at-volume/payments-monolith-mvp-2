package com.volume.users.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volume.shared.domain.types.DeviceId;
import com.volume.shared.domain.types.UserId;
import com.volume.users.ShopperAggregate;
import com.volume.users.persistence.JpaShoppersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManagerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ShopperControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JpaShoppersRepository shoppersRepository;
    @Autowired
    EntityManagerFactory emf;
    @Autowired
    ObjectMapper mapper;

    @Test
    void givenShopperIsAlreadyRegisteredOnDeviceForMerchant_whenCreateShopperCalled_201CreatedWithExistingShopperIdIsReturned() throws Exception {
        // arrange
        // create shopper with one merchant registration
        var newInstance = ShopperAggregate.forTest();
        var newInstanceRegistration = newInstance.getMerchantAppRegistrations().stream().toList().get(0);
        shoppersRepository.save(newInstance);
        shoppersRepository.flush();

        // act
        var requestDto = new CreateShopperRequestDto(
                newInstanceRegistration.getDeviceId().asString(),
                newInstanceRegistration.getMerchantId().asString()
        );
        String responseString = mvc.perform(
                        post("/api/shoppers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // assert
        CreateShopperResponseDto responseDto = mapper.readValue(responseString, CreateShopperResponseDto.class);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getShopperId()).isNotBlank();
        assertThat(responseDto.getDeviceId()).isEqualTo(newInstanceRegistration.getDeviceId().asString());
        assertThat(responseDto.getMerchantId()).isEqualTo(newInstanceRegistration.getMerchantId().asString());
    }

    @Test
    void givenShopperIsNotRegisteredOnThisDevice_whenCreateShopperCalled_201CreatedWithExistingShopperIdIsReturned() throws Exception {
        // arrange
        var deviceId = DeviceId.Companion.random();
        var merchantId = UserId.Companion.random();

        // act
        var requestDto = new CreateShopperRequestDto(deviceId.asString(), merchantId.asString());
        String responseString = mvc.perform(
                        post("/api/shoppers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // assert
        CreateShopperResponseDto responseDto = mapper.readValue(responseString, CreateShopperResponseDto.class);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getShopperId()).isNotBlank();
        assertThat(responseDto.getDeviceId()).isEqualTo(deviceId.asString());
        assertThat(responseDto.getMerchantId()).isEqualTo(merchantId.asString());
    }

    @Test
    void givenShopperExists_whenGetShopperById_200OkWithValidShopperDataIsReturned() {

    }

    @Test
    void givenShopperDoesNotExist_whenGetShopperById_200OkWithValidShopperDataIsReturned() {

    }


}