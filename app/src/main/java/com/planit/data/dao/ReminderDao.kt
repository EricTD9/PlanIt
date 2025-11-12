package com.planit.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.planit.data.model.Reminder
import com.planit.data.model.ReminderStatus
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY dateTime ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): Reminder?

    @Query("SELECT * FROM reminders WHERE dateTime >= :startDate AND dateTime < :endDate ORDER BY dateTime ASC")
    fun getRemindersByDateRange(startDate: Date, endDate: Date): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE date(dateTime/1000, 'unixepoch') = date(:date/1000, 'unixepoch') ORDER BY dateTime ASC")
    fun getRemindersByDate(date: Date): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE status = :status ORDER BY dateTime ASC")
    fun getRemindersByStatus(status: ReminderStatus): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)
}

