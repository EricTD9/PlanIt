package com.planit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.planit.PlanItApplication
import com.planit.data.model.Reminder
import com.planit.data.repository.ReminderRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FocusModeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReminderRepository = (application as PlanItApplication).repository

    private val _timeRemaining = MutableLiveData<Long>(25 * 60 * 1000L) // 25 minutos en millis
    val timeRemaining: LiveData<Long> = _timeRemaining

    private val _isRunning = MutableLiveData<Boolean>(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private var timerJob: Job? = null
    private val focusDurationMinutes = 25

    fun startTimer() {
        if (_isRunning.value == true) return

        _isRunning.value = true
        timerJob = viewModelScope.launch {
            var remaining = _timeRemaining.value ?: (focusDurationMinutes * 60 * 1000L)
            while (remaining > 0 && _isRunning.value == true) {
                delay(1000)
                remaining -= 1000
                _timeRemaining.value = remaining
            }
            if (remaining <= 0) {
                _isRunning.value = false
                _timeRemaining.value = focusDurationMinutes * 60 * 1000L
            }
        }
    }

    fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun stopTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        _timeRemaining.value = focusDurationMinutes * 60 * 1000L
    }

    fun getPendingRemindersToday(): kotlinx.coroutines.flow.Flow<List<Reminder>> {
        return repository.getRemindersForToday()
    }
}

