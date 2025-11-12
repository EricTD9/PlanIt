package com.planit.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "activities",
    foreignKeys = [
        ForeignKey(
            entity = Reminder::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ActivityItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reminderId: Long,
    val name: String,
    val isCompleted: Boolean = false,
    val order: Int = 0
)

