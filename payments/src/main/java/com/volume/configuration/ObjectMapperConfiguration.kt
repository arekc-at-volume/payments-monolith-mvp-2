package com.volume.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary


/**
 * TODO: work on that kotlin module because for now it does not work and Kotlin data classes are not deserialized properly.
 * It works when called directly but RestController somehow does not use this configuration. Something is missing.
 */
@Configuration
class ObjectMapperConfiguration {

    @Bean
    @Primary
    fun objectMapper() : ObjectMapper {
        val kotlinModule: KotlinModule = KotlinModule.Builder()
            .strictNullChecks(true)
            .build()
        val objectMapper: ObjectMapper = JsonMapper.builder()
            .addModule(kotlinModule)
            .build()
        return objectMapper;
    }

}