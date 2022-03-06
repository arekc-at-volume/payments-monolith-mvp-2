package com.volume.shared.domain.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.volume.shared.infrastructure.persistence.ValueObject
import com.volume.yapily.YapilyInstitutionId
import java.io.Serializable

data class InstitutionId(private val value: String) : ValueObject, Serializable {

    companion object {
        @JsonCreator
        fun fromString(value: String): InstitutionId = InstitutionId(value)
        fun fromYapilyInstitution(value: YapilyInstitutionId): InstitutionId = InstitutionId(value.value)
    }

    override fun asString(): String = value
    fun toYapily(): YapilyInstitutionId = YapilyInstitutionId(this.value)

}