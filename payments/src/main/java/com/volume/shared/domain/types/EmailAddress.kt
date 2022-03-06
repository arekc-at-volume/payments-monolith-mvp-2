package com.volume.shared.domain.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.volume.shared.infrastructure.persistence.ValueObject
import lombok.Value
import java.io.Serializable
import java.util.*

data class EmailAddress(private val value: String) : ValueObject, Serializable {
    init {
        // TODO: add validator
    }
    companion object {
        fun testRandom() : EmailAddress {
            //return EmailAddress(Faker.instance().internet().emailAddress())
            return EmailAddress("test_user@test.com")
        }

        @JvmStatic
        @JsonCreator
        fun fromString(value: String): EmailAddress {
            return EmailAddress(value)
        }
    }

    override fun asString(): String {
        return this.value.toString();
    }
}

data class TransferId(private val value: UUID) : ValueObject, Serializable {

    companion object {
        @JsonCreator
        fun fromString(value: String): TransferId = TransferId(UUID.fromString(value))

        fun random(): TransferId = TransferId(UUID.randomUUID())
    }

    override fun asString(): String = value.toString()

}