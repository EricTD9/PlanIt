package com.planit.data.repository

import com.planit.data.dao.ActivityDao
import com.planit.data.dao.ReminderDao
import com.planit.data.model.ActivityItem
import com.planit.data.model.Reminder
import com.planit.data.model.ReminderStatus
import com.planit.data.model.RepetitionType
import com.planit.data.model.ReminderOccurrence
import com.planit.data.dao.ReminderOccurrenceDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class ReminderRepository(
    private val reminderDao: ReminderDao,
    private val activityDao: ActivityDao,
    private val occurrenceDao: ReminderOccurrenceDao
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

        // Obtener todos los recordatorios y expandir los repetitivos
        return getAllReminders().map { allReminders ->
            allReminders.flatMap { reminder ->
                if (reminder.repetitionType == RepetitionType.ONCE) {
                    // Si es único, solo incluirlo si está en el rango de hoy
                    if (reminder.dateTime.after(startOfDay) || reminder.dateTime == startOfDay) {
                        if (reminder.dateTime.before(endOfDay)) {
                            listOf(reminder)
                        } else {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                } else {
                    // Expandir recordatorios repetitivos para hoy
                    expandRepetitiveReminder(reminder, startOfDay, endOfDay)
                }
            }
        }
    }

    fun getRemindersForWeek(): Flow<List<Reminder>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.time

        // Agregar 7 días (hoy + 6 días siguientes = 7 días en total)
        calendar.add(Calendar.DAY_OF_MONTH, 7)
        val endOfWeek = calendar.time

        // Obtener todos los recordatorios y expandir los repetitivos
        return getAllReminders().map { allReminders ->
            allReminders.flatMap { reminder ->
                if (reminder.repetitionType == RepetitionType.ONCE) {
                    // Si es único, solo incluirlo si está en el rango
                    if (reminder.dateTime.after(startOfWeek) || reminder.dateTime == startOfWeek) {
                        if (reminder.dateTime.before(endOfWeek)) {
                            listOf(reminder)
                        } else {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                } else {
                    // Expandir recordatorios repetitivos para la semana
                    expandRepetitiveReminder(reminder, startOfWeek, endOfWeek)
                }
            }
        }
    }

    fun getRemindersByDateRange(startDate: Date, endDate: Date): Flow<List<Reminder>> {
        return reminderDao.getRemindersByDateRange(startDate, endDate)
    }

    fun getRemindersByDate(date: Date): Flow<List<Reminder>> {
        return reminderDao.getRemindersByDate(date)
    }

    suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        activityDao.deleteActivitiesByReminderId(reminder.id)
        occurrenceDao.deleteOccurrencesByReminderId(reminder.id)
        reminderDao.deleteReminder(reminder)
    }

    suspend fun deleteReminderById(id: Long) {
        activityDao.deleteActivitiesByReminderId(id)
        occurrenceDao.deleteOccurrencesByReminderId(id)
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

    // Función para marcar una ocurrencia específica como completada
    suspend fun completeReminderOccurrence(reminder: Reminder, occurrenceDate: Date) {
        // Normalizar la fecha (solo día, sin hora)
        val calendar = Calendar.getInstance()
        calendar.time = occurrenceDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val normalizedDate = calendar.time

        val occurrence = occurrenceDao.getOccurrence(reminder.id, normalizedDate)
        if (occurrence != null) {
            occurrenceDao.updateOccurrence(occurrence.copy(isCompleted = true))
        } else {
            occurrenceDao.insertOccurrence(
                ReminderOccurrence(
                    reminderId = reminder.id,
                    occurrenceDate = normalizedDate,
                    isCompleted = true
                )
            )
        }
    }

    // Función para verificar si una ocurrencia está completada
    suspend fun isOccurrenceCompleted(reminderId: Long, date: Date): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val normalizedDate = calendar.time

        val occurrence = occurrenceDao.getOccurrence(reminderId, normalizedDate)
        return occurrence?.isCompleted ?: false
    }

    // Función para obtener el estado de completado de múltiples ocurrencias
    suspend fun getOccurrenceCompletionMap(reminderId: Long, dates: List<Date>): Map<Date, Boolean> {
        val result = mutableMapOf<Date, Boolean>()
        dates.forEach { date ->
            result[date] = isOccurrenceCompleted(reminderId, date)
        }
        return result
    }

    // Función helper para expandir un recordatorio repetitivo
    fun expandRepetitiveReminder(reminder: Reminder, startDate: Date, endDate: Date): List<Reminder> {
        if (reminder.repetitionType == RepetitionType.ONCE) {
            return listOf(reminder)
        }

        val occurrences = mutableListOf<Reminder>()
        val calendar = Calendar.getInstance()
        calendar.time = reminder.dateTime

        // Calcular todas las ocurrencias dentro del rango
        while (calendar.time.before(endDate) || calendar.time == endDate) {
            val occurrenceDate = calendar.time
            if (occurrenceDate.after(startDate) || occurrenceDate == startDate) {
                occurrences.add(
                    reminder.copy(
                        id = reminder.id, // Mantener el mismo ID para identificar que es el mismo recordatorio
                        dateTime = occurrenceDate
                    )
                )
            }

            // Avanzar según el tipo de repetición
            when (reminder.repetitionType) {
                RepetitionType.DAILY -> calendar.add(Calendar.DAY_OF_MONTH, 1)
                RepetitionType.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                else -> break
            }
        }

        return occurrences
    }
}
