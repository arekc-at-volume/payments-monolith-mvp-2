package com.volume.shared.domain.types

import com.volume.shared.infrastructure.persistence.ValueObject
import java.io.Serializable
import java.util.*

data class UserId(val value: UUID) : ValueObject, Serializable {
    init {
        // validate here
        // TODO: add validator
    }
    companion object {
        fun random(): UserId {
            return UserId(UUID.randomUUID())
        }
        fun fromString(value: String) : UserId {
            return UserId(UUID.fromString(value));
        }
    }

    override fun asString(): String {
        return this.value.toString()
    }
}

data class PhoneNumber(val value: String) : ValueObject, Serializable {
    init {
        // validate here
        // TODO: add validator
    }
    companion object {
        fun testRandom() : PhoneNumber {
            //return PhoneNumber(Faker.instance().phoneNumber().cellPhone());
            return PhoneNumber("123456789")
        }

        @JvmStatic
        fun fromString(value: String): PhoneNumber {
            return PhoneNumber(value);
        }
    }

    override fun asString(): String {
        return this.value.toString()
    }
}

data class EmailAddress(val value: String) : ValueObject, Serializable {
    init {
        // TODO: add validator
    }
    companion object {
        fun testRandom() : EmailAddress {
            //return EmailAddress(Faker.instance().internet().emailAddress())
            return EmailAddress("test_user@test.com")
        }

        @JvmStatic
        fun fromString(value: String): EmailAddress {
            return EmailAddress(value)
        }
    }

    override fun asString(): String {
        return this.value.toString();
    }
}