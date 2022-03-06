package com.volume.shared.domain.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.volume.shared.infrastructure.persistence.ValueObject
import java.io.Serializable

data class PhoneNumber(private val value: String) : ValueObject, Serializable {
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
        @JsonCreator
        fun fromString(value: String): PhoneNumber {
            return PhoneNumber(value);
        }
    }

    override fun asString(): String {
        return this.value.toString()
    }
}