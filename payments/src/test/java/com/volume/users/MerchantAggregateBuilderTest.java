package com.volume.users;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantAggregateBuilderTest {

    @Test
    void builder() {
        MerchantAggregate aggregate = MerchantAggregate.randomForTest().build();

        assertThat(aggregate).isNotNull();
        assertThat(aggregate.getId()).isNotNull();
        assertThat(aggregate.getCompanyName()).isNotBlank();
        assertThat(aggregate.getEmailAddress()).isNotNull();
        assertThat(aggregate.getPhoneNumber()).isNotNull();
        assertThat(aggregate.getMerchantPayeeDetails().getAccountHolderName()).isNotBlank();
        assertThat(aggregate.getMerchantPayeeDetails().getPostalAddress()).isNotNull();
        assertThat(aggregate.getMerchantPayeeDetails().getAccountIdentification1()).isNotNull();
        assertThat(aggregate.getMerchantPayeeDetails().getAccountIdentification2()).isNotNull();
    }

}