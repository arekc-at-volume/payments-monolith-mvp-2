package com.volume.shared.domain.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.volume.shared.infrastructure.persistence.ValueObject
import com.volume.yapily.YapilyInstitutionId
import java.io.Serializable
import java.util.*

data class InstitutionId(private val value: String) : ValueObject, Serializable {

    companion object {
        @JsonCreator
        fun fromString(value: String): InstitutionId = InstitutionId(value)
        fun fromYapilyInstitution(value: YapilyInstitutionId): InstitutionId = InstitutionId(value.value)
    }

    override fun asString(): String = value

}

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

/**
 * Temporary DeviceId. In the future it will become more complex entity.
 */
data class DeviceId(val value: UUID) : ValueObject, Serializable {

    init {
        // TODO: Add validation
    }

    override fun asString(): String {
        return this.value.toString()
    }

    companion object {
        fun random() : DeviceId {
            return DeviceId(UUID.randomUUID())
        }
        fun fromString(value: String) : DeviceId {
            return DeviceId(UUID.fromString(value))
        }
    }

}

data class TransferIdempotencyId(private val value: String) : ValueObject, Serializable {
    companion object {
        fun random(): TransferIdempotencyId = TransferIdempotencyId(UUID.randomUUID().toString().replace("-", ""))

        @JsonCreator
        fun fromString(value: String): TransferIdempotencyId = TransferIdempotencyId(value)
    }

    override fun asString(): String = value;
}