package com.planit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.planit.data.database.Converters
import java.util.Date

@Entity(tableName = "reminders")
@TypeConverters(Converters::class)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dateTime: Date,
    val category: Category,
    val repetitionType: RepetitionType = RepetitionType.ONCE,
    val status: ReminderStatus = ReminderStatus.PENDING,
    val location: String? = null,
    val hasVibration: Boolean = true,
    val soundUri: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

