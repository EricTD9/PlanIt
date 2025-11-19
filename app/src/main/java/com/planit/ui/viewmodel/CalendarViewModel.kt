package com.planit.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.planit.PlanItApplication
import com.planit.data.model.Reminder
import com.planit.data.model.RepetitionType
import com.planit.data.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReminderRepository = (application as PlanItApplication).repository

    private val _currentMonth = MutableLiveData<Calendar>()
    val currentMonth: LiveData<Calendar> = _currentMonth

    private val _selectedDate = MutableLiveData<Date?>()
    val selectedDate: LiveData<Date?> = _selectedDate

    init {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        _currentMonth.value = calendar
        Log.d("CalendarViewModel", "init: currentMonth set to ${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}")
    }

    fun getCurrentMonth(): Calendar {
        // Devolver una copia para evitar que callers externos muten
        // la instancia interna del ViewModel (Calendar es mutable).
        val stored = _currentMonth.value
        return if (stored != null) {
            stored.clone() as Calendar
        } else {
            Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }
    }

    /**
     * Fuerza que el mes actual en el ViewModel sea el mes de la fecha actual.
     * Usar cuando se quiera garantizar que en un arranque frío de la app
     * se muestre el mes presente.
     */
    fun resetToCurrentMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        _currentMonth.value = calendar
        Log.d("CalendarViewModel", "resetToCurrentMonth: currentMonth set to ${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}")
    }

    fun navigateToPreviousMonth() {
        // Obtener una copia, modificarla y almacenar la nueva instancia
        val newCal = getCurrentMonth()
        newCal.add(Calendar.MONTH, -1)
        _currentMonth.value = newCal
    }

    fun navigateToNextMonth() {
        val newCal = getCurrentMonth()
        newCal.add(Calendar.MONTH, 1)
        _currentMonth.value = newCal
    }

    fun selectDate(date: Date) {
        _selectedDate.value = date
    }

    fun getRemindersForMonth(): Flow<List<Reminder>> {
        val cal = getCurrentMonth()
        val startOfMonth = cal.time

        cal.add(Calendar.MONTH, 1)
        val endOfMonth = cal.time

        // Obtener todos los recordatorios que podrían aparecer en este mes
        // (incluyendo los que se repiten)
        return repository.getAllReminders().map { allReminders ->
            allReminders.flatMap { reminder ->
                if (reminder.repetitionType == RepetitionType.ONCE) {
                    // Si es único, solo incluirlo si está en el rango
                    if (reminder.dateTime.after(startOfMonth) && reminder.dateTime.before(endOfMonth)) {
                        listOf(reminder)
                    } else {
                        emptyList()
                    }
                } else {
                    // Expandir recordatorios repetitivos
                    repository.expandRepetitiveReminder(reminder, startOfMonth, endOfMonth)
                }
            }
        }
    }

    fun getRemindersForDate(date: Date): Flow<List<Reminder>> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time
        
        // Guardar el año y día del año antes de modificar el calendar
        val targetYear = calendar.get(Calendar.YEAR)
        val targetDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.time
        
        return repository.getAllReminders().map { allReminders ->
            allReminders.flatMap { reminder ->
                if (reminder.repetitionType == RepetitionType.ONCE) {
                    // Verificar si es el mismo día
                    val reminderCal = Calendar.getInstance()
                    reminderCal.time = reminder.dateTime
                    reminderCal.set(Calendar.HOUR_OF_DAY, 0)
                    reminderCal.set(Calendar.MINUTE, 0)
                    reminderCal.set(Calendar.SECOND, 0)
                    reminderCal.set(Calendar.MILLISECOND, 0)
                    
                    if (reminderCal.get(Calendar.YEAR) == targetYear &&
                        reminderCal.get(Calendar.DAY_OF_YEAR) == targetDayOfYear) {
                        listOf(reminder)
                    } else {
                        emptyList()
                    }
                } else {
                    // Expandir recordatorios repetitivos y filtrar por fecha específica
                    repository.expandRepetitiveReminder(reminder, startOfDay, endOfDay)
                        .filter { expandedReminder ->
                            val expandedCal = Calendar.getInstance()
                            expandedCal.time = expandedReminder.dateTime
                            expandedCal.set(Calendar.HOUR_OF_DAY, 0)
                            expandedCal.set(Calendar.MINUTE, 0)
                            expandedCal.set(Calendar.SECOND, 0)
                            expandedCal.set(Calendar.MILLISECOND, 0)
                            
                            expandedCal.get(Calendar.YEAR) == targetYear &&
                            expandedCal.get(Calendar.DAY_OF_YEAR) == targetDayOfYear
                        }
                }
            }
        }
    }

    fun getSelectedDateReminders(): Flow<List<Reminder>> {
        val selected = _selectedDate.value
        return if (selected != null) {
            getRemindersForDate(selected)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }
}

