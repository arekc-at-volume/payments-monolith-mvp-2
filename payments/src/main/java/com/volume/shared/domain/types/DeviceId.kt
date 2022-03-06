package com.volume.shared.domain.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.volume.shared.infrastructure.persistence.ValueObject
import java.io.Serializable
import java.util.*

/**
 * Temporary DeviceId. In the future it will become more complex entity.
 */
data class DeviceId(private val value: UUID) : ValueObject, Serializable {

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
        @JsonCreator
        fun fromString(value: String) : DeviceId {
            return DeviceId(UUID.fromString(value))
        }
    }

}