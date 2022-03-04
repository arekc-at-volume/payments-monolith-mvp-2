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

        fun fromString(value: String): UserId {
            return UserId(UUID.fromString(value));
        }
    }

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

