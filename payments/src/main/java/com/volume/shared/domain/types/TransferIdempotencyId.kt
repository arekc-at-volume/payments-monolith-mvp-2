package com.volume.shared.domain.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.volume.shared.infrastructure.persistence.ValueObject
import com.volume.yapily.YapilyPaymentIdempotencyId
import java.io.Serializable
import java.util.*

data class TransferIdempotencyId(private val value: String) : ValueObject, Serializable {
    companion object {
        fun random(): TransferIdempotencyId = TransferIdempotencyId(UUID.randomUUID().toString().replace("-", ""))

        @JsonCreator
        fun fromString(value: String): TransferIdempotencyId = TransferIdempotencyId(value)
    }

    override fun asString(): String = value;
    fun toYapily(): YapilyPaymentIdempotencyId = YapilyPaymentIdempotencyId(this.value)
}