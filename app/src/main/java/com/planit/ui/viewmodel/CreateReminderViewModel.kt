package com.planit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.planit.PlanItApplication
import com.planit.alarm.ReminderAlarmManager
import com.planit.data.model.ActivityItem
import com.planit.data.model.Category
import com.planit.data.model.Reminder
import com.planit.data.model.ReminderStatus
import com.planit.data.model.RepetitionType
import com.planit.data.repository.ReminderRepository
import kotlinx.coroutines.launch
import java.util.Date

class CreateReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReminderRepository = (application as PlanItApplication).repository
    private val alarmManager = ReminderAlarmManager(application)

    fun saveReminder(
        title: String,
        description: String?,
        dateTime: Date,
        category: Category,
        repetitionType: RepetitionType,
        location: String?,
        hasVibration: Boolean,
        activities: List<String>
    ): Long? {
        if (title.isBlank()) {
            return null
        }

        var reminderId: Long? = null
        viewModelScope.launch {
            val reminder = Reminder(
                title = title,
                description = description,
                dateTime = dateTime,
                category = category,
                repetitionType = repetitionType,
                location = location,
                hasVibration = hasVibration,
                status = ReminderStatus.PENDING
            )

            reminderId = repository.insertReminder(reminder)

            // Insertar actividades
            if (activities.isNotEmpty() && reminderId != null) {
                val activityItems = activities.mapIndexed { index, name ->
                    ActivityItem(
                        reminderId = reminderId!!,
                        name = name,
                        isCompleted = false,
                        order = index
                    )
                }
                repository.insertActivities(activityItems)
            }

            // Programar alarma
            reminderId?.let { id ->
                val savedReminder = repository.getReminderById(id)
                savedReminder?.let {
                    alarmManager.scheduleReminder(it)
                }
            }
        }

        return reminderId
    }

    fun updateReminder(
        reminderId: Long,
        title: String,
        description: String?,
        dateTime: Date,
        category: Category,
        repetitionType: RepetitionType,
        location: String?,
        hasVibration: Boolean,
        activities: List<String>
    ) {
        viewModelScope.launch {
            val existingReminder = repository.getReminderById(reminderId) ?: return@launch

            // Cancelar alarma anterior
            alarmManager.cancelReminder(reminderId)

            // Actualizar recordatorio
            val updatedReminder = existingReminder.copy(
                title = title,
                description = description,
                dateTime = dateTime,
                category = category,
                repetitionType = repetitionType,
                location = location,
                hasVibration = hasVibration,
                updatedAt = Date()
            )

            repository.updateReminder(updatedReminder)

            // Actualizar actividades
            repository.deleteActivitiesByReminderId(reminderId)
            if (activities.isNotEmpty()) {
                val activityItems = activities.mapIndexed { index, name ->
                    ActivityItem(
                        reminderId = reminderId,
                        name = name,
                        isCompleted = false,
                        order = index
                    )
                }
                repository.insertActivities(activityItems)
            }

            // Programar nueva alarma
            alarmManager.scheduleReminder(updatedReminder)
        }
    }
}

