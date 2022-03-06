package com.volume.shared.infrastructure.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.volume.shared.domain.types.UserId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import java.util.*


//interface ValueObject {
//    @JsonValue
//    fun asString(): String
//}

//data class UserId(private val value: UUID) : ValueObject {
//    companion object {
//        @JsonCreator
//        fun fromString(value: String): UserId = UserId(UUID.fromString(value))
//    }
//
//    override fun asString(): String = value.toString()
//}

//data class UserId(private val value: UUID) : ValueObject {
//    companion object {
//        @JsonCreator
//        fun fromString(value: String): UserId {
//            return UserId(UUID.fromString(value));
//        }
//    }
//
//    @JsonValue
//    override fun asString(): String {
//        return this.value.toString()
//    }
//}

data class User(val id: UserId, val name: String) {
}

@JsonTest
class ValueObjectsSerializationTests(@Autowired val mapper: ObjectMapper) {

    @Test
    fun serialize() {
        var instance = User(UserId.fromString(UUID.randomUUID().toString()), "arek")
        var serialized = mapper.writeValueAsString(instance)
        var deserialized = mapper.readValue<User>(serialized)
    }

}