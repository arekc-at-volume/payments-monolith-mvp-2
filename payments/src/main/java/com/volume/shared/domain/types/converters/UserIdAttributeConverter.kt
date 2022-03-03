package com.volume.shared.domain.types.converters

import com.volume.shared.domain.types.UserId
import java.util.*
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class UserIdAttributeConverter : AttributeConverter<UserId?, String?> {
    override fun convertToDatabaseColumn(attribute: UserId?): String? {
        return attribute?.value?.toString()
    }

    override fun convertToEntityAttribute(dbData: String?): UserId? {
        return if (dbData != null) UserId(UUID.fromString(dbData)) else null
    }
}