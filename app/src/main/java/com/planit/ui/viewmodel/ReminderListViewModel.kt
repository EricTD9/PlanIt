package com.planit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asFlow
import com.planit.PlanItApplication
import com.planit.data.model.Reminder
import com.planit.data.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReminderRepository = (application as PlanItApplication).repository

    private val _filterType = MutableLiveData<FilterType>(FilterType.TODAY)
    val filterType: LiveData<FilterType> = _filterType

    fun setFilterType(type: FilterType) {
        _filterType.value = type
    }

    // Usar flatMapLatest para que el Flow se actualice cuando cambia el filtro
    // Convertir LiveData a Flow usando asFlow()
    @OptIn(ExperimentalCoroutinesApi::class)
    val reminders: Flow<List<Reminder>> = _filterType.asFlow().flatMapLatest { filterType ->
        when (filterType) {
            FilterType.TODAY -> repository.getRemindersForToday()
            FilterType.WEEK -> repository.getRemindersForWeek()
            FilterType.ALL -> repository.getAllReminders()
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    fun completeReminder(reminder: Reminder) {
        viewModelScope.launch {
            // Si es un recordatorio repetitivo, marcar solo la ocurrencia de hoy
            if (reminder.repetitionType != com.planit.data.model.RepetitionType.ONCE) {
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                repository.completeReminderOccurrence(reminder, today)
            } else {
                // Si es único, actualizar el recordatorio completo
                val updatedReminder = reminder.copy(
                    status = com.planit.data.model.ReminderStatus.COMPLETED
                )
                repository.updateReminder(updatedReminder)
            }
        }
    }

    enum class FilterType {
        TODAY, WEEK, ALL
    }
}

