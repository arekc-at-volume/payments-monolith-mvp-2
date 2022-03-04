package com.volume.shared.infrastructure.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.http.HttpStatus

/**
 * How to configure light serialization test?
 * It is either with
 *  @JsonTest
 *
 * or with
 *
 * @ExtendWith(SpringExtension::class)
 * @Import(ObjectMapperConfiguration::class)
 */
@JsonTest
//@ExtendWith(SpringExtension::class)
//@Import(ObjectMapperConfiguration::class)
class RestErrorResponseTest(@Autowired val mapper: ObjectMapper) {
    // This test checks if with additional ObjectMapper configuration for kotlin this type will serialize/deserialize properly
    @Test
    fun serializationTest() {
        val instance = RestErrorResponse(HttpStatus.CREATED, "some message", "some-trace-it");
        val serialized = mapper.writeValueAsString(instance);
        val deserialized = mapper.readValue<RestErrorResponse>(serialized)

        assertThat(deserialized).isEqualTo(instance);
    }
}