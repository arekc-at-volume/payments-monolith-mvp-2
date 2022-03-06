package com.volume.shared.domain.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.volume.shared.infrastructure.persistence.ValueObject
import java.io.Serializable
import java.util.*

data class UserId(private val value: UUID) : ValueObject, Serializable {
    init {
        // validate here
        // TODO: add validator
    }

    companion object {
        fun random(): UserId {
            return UserId(UUID.randomUUID())
        }

        @JsonCreator
        fun fromString(value: String): UserId {
            return UserId(UUID.fromString(value));
        }
    }

    @JsonValue
    override fun asString(): String {
        return this.value.toString()
    }
}

data class InstitutionConsentId(private val value: String) : ValueObject, Serializable {

    companion object {
        @JsonCreator
        fun fromString(value: String): InstitutionId = InstitutionId(value)

        fun testRandom(): InstitutionConsentId = InstitutionConsentId(UUID.randomUUID().toString())
    }

    override fun asString(): String = value

}