package com.volume.shared.domain.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.volume.shared.infrastructure.persistence.ValueObject
import java.io.Serializable

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