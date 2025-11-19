package com.planit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "reminder_occurrences")
data class ReminderOccurrence(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reminderId: Long,
    val occurrenceDate: Date,
    val isCompleted: Boolean = false
)
