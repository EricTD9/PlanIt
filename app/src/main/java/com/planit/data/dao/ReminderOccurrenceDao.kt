package com.planit.data.dao

import androidx.room.*
import com.planit.data.model.ReminderOccurrence
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ReminderOccurrenceDao {
    @Query("SELECT * FROM reminder_occurrences WHERE reminderId = :reminderId AND occurrenceDate = :date")
    suspend fun getOccurrence(reminderId: Long, date: Date): ReminderOccurrence?

    @Query("SELECT * FROM reminder_occurrences WHERE reminderId = :reminderId")
    fun getOccurrencesByReminderId(reminderId: Long): Flow<List<ReminderOccurrence>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrence(occurrence: ReminderOccurrence): Long

    @Update
    suspend fun updateOccurrence(occurrence: ReminderOccurrence)

    @Query("DELETE FROM reminder_occurrences WHERE reminderId = :reminderId")
    suspend fun deleteOccurrencesByReminderId(reminderId: Long)
}
