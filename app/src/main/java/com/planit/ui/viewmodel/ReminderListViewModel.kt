package com.planit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.planit.PlanItApplication
import com.planit.data.model.Reminder
import com.planit.data.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ReminderListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReminderRepository = (application as PlanItApplication).repository

    private val _filterType = MutableLiveData<FilterType>(FilterType.TODAY)
    val filterType: LiveData<FilterType> = _filterType

    fun setFilterType(type: FilterType) {
        _filterType.value = type
    }

    fun getReminders(): Flow<List<Reminder>> {
        return when (_filterType.value) {
            FilterType.TODAY -> repository.getRemindersForToday()
            FilterType.WEEK -> repository.getRemindersForWeek()
            FilterType.ALL -> repository.getAllReminders()
            null -> repository.getRemindersForToday()
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    fun completeReminder(reminder: Reminder) {
        viewModelScope.launch {
            val updatedReminder = reminder.copy(
                status = com.planit.data.model.ReminderStatus.COMPLETED
            )
            repository.updateReminder(updatedReminder)
        }
    }

    enum class FilterType {
        TODAY, WEEK, ALL
    }
}

