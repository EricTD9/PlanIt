package com.planit.data.database

import androidx.room.TypeConverter
import com.planit.data.model.Category
import com.planit.data.model.ReminderStatus
import com.planit.data.model.RepetitionType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromCategory(category: Category): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(category: String): Category {
        return Category.valueOf(category)
    }

    @TypeConverter
    fun fromStatus(status: ReminderStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(status: String): ReminderStatus {
        return ReminderStatus.valueOf(status)
    }

    @TypeConverter
    fun fromRepetitionType(type: RepetitionType): String {
        return type.name
    }

    @TypeConverter
    fun toRepetitionType(type: String): RepetitionType {
        return RepetitionType.valueOf(type)
    }
}

