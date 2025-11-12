package com.planit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.planit.PlanItApplication
import com.planit.data.model.ActivityItem
import com.planit.data.model.Reminder
import com.planit.data.model.ReminderStatus
import com.planit.data.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ReminderDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReminderRepository = (application as PlanItApplication).repository

    private val _reminder = MutableLiveData<Reminder?>()
    val reminder: LiveData<Reminder?> = _reminder

    fun loadReminder(id: Long) {
        viewModelScope.launch {
            _reminder.value = repository.getReminderById(id)
        }
    }

    fun getActivities(reminderId: Long): Flow<List<ActivityItem>> {
        return repository.getActivitiesByReminderId(reminderId)
    }

    fun updateActivityCompletion(activity: ActivityItem, isCompleted: Boolean) {
        viewModelScope.launch {
            val updatedActivity = activity.copy(isCompleted = isCompleted)
            repository.updateActivity(updatedActivity)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    fun markReminderAsCompleted(reminder: Reminder) {
        viewModelScope.launch {
            val updatedReminder = reminder.copy(status = ReminderStatus.COMPLETED)
            repository.updateReminder(updatedReminder)
            _reminder.value = updatedReminder
        }
    }
}
