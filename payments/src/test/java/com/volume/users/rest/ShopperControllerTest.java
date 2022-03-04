package com.volume.users.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volume.shared.domain.types.DeviceId;
import com.volume.shared.domain.types.UserId;
import com.volume.users.MerchantOnDeviceRegistrationEntity;
import com.volume.users.ShopperAggregate;
import com.volume.users.persistence.JpaShoppersRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityManagerFactory;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    void givenShopperIsAlreadyRegisteredOnDeviceForMerchant_whenCreateShopperForTheSameMerchantIsCalled_201CreatedWithExistingShopperIdIsReturned() throws Exception {
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

    @Disabled("This test will become relevant when we decide that we bind shopperId to ONLY deviceId and not to deviceId AND merchantId. For the latter is implemented")
    @Test
    void givenShopperIsAlreadyRegisteredOnDeviceForMerchant_whenCreateShopperForDifferentMerchantIsCalled_201CreatedWithExistingShopperAndNewRegistrationIsReturned() throws Exception {
        // arrange
        // create shopper with one merchant registration
        var newInstance = ShopperAggregate.forTest();
        var newInstanceRegistration = newInstance.getMerchantAppRegistrations().stream().toList().get(0);
        shoppersRepository.save(newInstance);
        shoppersRepository.flush();

        // act : simulate shopper making call from the same device but new app/merchant
        var differentMerchantId = UserId.Companion.random();
        var requestDto = new CreateShopperRequestDto(
                newInstanceRegistration.getDeviceId().asString(),
                differentMerchantId.asString()
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
        assertThat(responseDto.getMerchantId()).isEqualTo(differentMerchantId.asString());

        doInJPA(() -> emf, em -> {
            ShopperAggregate shopperAggregate = em.find(ShopperAggregate.class, newInstance.getId());
            List<MerchantOnDeviceRegistrationEntity> registrations = shopperAggregate.getMerchantAppRegistrations().stream().toList();

            assertThat(shopperAggregate).isNotNull();
            assertThat(registrations.size()).isEqualTo(2);
        });
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
    void givenShopperExists_whenGetShopperById_200OkWithValidShopperDataIsReturned() throws Exception {
        // arrange
        // create shopper with one merchant registration
        var newInstance = ShopperAggregate.forTest();
        var newInstanceRegistration = newInstance.getMerchantAppRegistrations().stream().toList().get(0);
        shoppersRepository.save(newInstance);
        shoppersRepository.flush();

        // act
        String responseString = mvc.perform(
                        get("/api/shoppers/" + newInstance.getId().asString())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // assert
        ShopperDto responseDto = mapper.readValue(responseString, ShopperDto.class);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getShopperId()).isNotBlank();
        assertThat(responseDto.getCreatedAt()).isNotNull();
        assertThat(responseDto.getUpdatedAt()).isNotNull();
        assertThat(responseDto.getUpdatedBy()).isNotNull();
        assertThat(responseDto.getVersion()).isEqualTo(0);
        assertThat(responseDto.getMerchantRegistrations().size()).isEqualTo(1);
        assertThat(responseDto.getMerchantRegistrations().get(0).getMerchantId()).isEqualTo(newInstanceRegistration.getMerchantId().asString());
        assertThat(responseDto.getMerchantRegistrations().get(0).getDeviceId()).isEqualTo(newInstanceRegistration.getDeviceId().asString());
        assertThat(responseDto.getMerchantRegistrations().get(0).getVersion()).isEqualTo(0);
    }

    @Test
    void givenShopperDoesNotExist_whenGetShopperById_200OkWithValidShopperDataIsReturned() throws Exception {
        // act and assert
        String responseString = mvc.perform(
                        get("/api/shoppers/" + UserId.Companion.random().asString())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();
    }

}