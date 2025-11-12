package com.planit.data.repository

import com.planit.data.dao.ActivityDao
import com.planit.data.dao.ReminderDao
import com.planit.data.model.ActivityItem
import com.planit.data.model.Reminder
import com.planit.data.model.ReminderStatus
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date

class ReminderRepository(
    private val reminderDao: ReminderDao,
    private val activityDao: ActivityDao
) {
    fun getAllReminders(): Flow<List<Reminder>> = reminderDao.getAllReminders()

    suspend fun getReminderById(id: Long): Reminder? = reminderDao.getReminderById(id)

    fun getRemindersForToday(): Flow<List<Reminder>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.time

        return reminderDao.getRemindersByDateRange(startOfDay, endOfDay)
    }

    fun getRemindersForWeek(): Flow<List<Reminder>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, 7)
        val endOfWeek = calendar.time

        return reminderDao.getRemindersByDateRange(startOfWeek, endOfWeek)
    }

    suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        activityDao.deleteActivitiesByReminderId(reminder.id)
        reminderDao.deleteReminder(reminder)
    }

    suspend fun deleteReminderById(id: Long) {
        activityDao.deleteActivitiesByReminderId(id)
        reminderDao.deleteReminderById(id)
    }

    fun getActivitiesByReminderId(reminderId: Long): Flow<List<ActivityItem>> {
        return activityDao.getActivitiesByReminderId(reminderId)
    }

    suspend fun insertActivity(activity: ActivityItem): Long {
        return activityDao.insertActivity(activity)
    }

    suspend fun insertActivities(activities: List<ActivityItem>) {
        activityDao.insertActivities(activities)
    }

    suspend fun updateActivity(activity: ActivityItem) {
        activityDao.updateActivity(activity)
    }

    suspend fun deleteActivity(activity: ActivityItem) {
        activityDao.deleteActivity(activity)
    }

    suspend fun deleteActivitiesByReminderId(reminderId: Long) {
        activityDao.deleteActivitiesByReminderId(reminderId)
    }
}
