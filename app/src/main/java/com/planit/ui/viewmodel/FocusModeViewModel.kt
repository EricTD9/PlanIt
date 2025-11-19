package com.planit.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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

class FocusModeViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository: ReminderRepository = (application as PlanItApplication).repository
    
    private val prefs: SharedPreferences = application.getSharedPreferences("focus_timer_prefs", Context.MODE_PRIVATE)
    private val TIMER_START_TIME_KEY = "timer_start_time"
    private val TIMER_DURATION_KEY = "timer_duration"
    private val IS_RUNNING_KEY = "is_running"
    private val IS_PAUSED_KEY = "is_paused"
    private val PAUSED_REMAINING_KEY = "paused_remaining"

    private val _durationMinutes = MutableLiveData<Float>(25f)
    val durationMinutes: LiveData<Float> = _durationMinutes

    private val _timeRemaining = MutableLiveData<Long>()
    val timeRemaining: LiveData<Long> = _timeRemaining

    private val _isRunning = MutableLiveData<Boolean>(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _isPaused = MutableLiveData<Boolean>(false)
    val isPaused: LiveData<Boolean> = _isPaused

    private var timerJob: Job? = null
    private var onTimerComplete: (() -> Unit)? = null
    
    // Timestamp para calcular el tiempo restante basado en tiempo real
    private var timerStartTime: Long?
        get() = if (prefs.contains(TIMER_START_TIME_KEY)) prefs.getLong(TIMER_START_TIME_KEY, 0).takeIf { it > 0 } else null
        set(value) {
            if (value != null) {
                prefs.edit().putLong(TIMER_START_TIME_KEY, value).apply()
            } else {
                prefs.edit().remove(TIMER_START_TIME_KEY).apply()
            }
        }
    
    private var pausedRemainingTime: Long?
        get() = if (prefs.contains(PAUSED_REMAINING_KEY)) prefs.getLong(PAUSED_REMAINING_KEY, 0).takeIf { it > 0 } else null
        set(value) {
            if (value != null) {
                prefs.edit().putLong(PAUSED_REMAINING_KEY, value).apply()
            } else {
                prefs.edit().remove(PAUSED_REMAINING_KEY).apply()
            }
        }

    init {
        // Cargar estado guardado
        val savedDuration = prefs.getFloat(TIMER_DURATION_KEY, 25f)
        _durationMinutes.value = savedDuration
        
        val savedIsRunning = prefs.getBoolean(IS_RUNNING_KEY, false)
        val savedIsPaused = prefs.getBoolean(IS_PAUSED_KEY, false)
        
        _isRunning.value = savedIsRunning
        _isPaused.value = savedIsPaused
        
        // Solo inicializar si no hay un valor ya establecido
        if (_timeRemaining.value == null) {
            if (savedIsPaused && pausedRemainingTime != null) {
                _timeRemaining.value = pausedRemainingTime
            } else {
                _timeRemaining.value = savedDuration.toInt() * 60 * 1000L
            }
        }
        
        // Si el temporizador estaba corriendo, restaurar el estado
        if (savedIsRunning && timerStartTime != null) {
            updateTimeRemaining()
        }
    }

    fun setDuration(minutes: Float) {
        _durationMinutes.value = minutes
        prefs.edit().putFloat(TIMER_DURATION_KEY, minutes).apply()
        if ((_isRunning.value == false || _isRunning.value == null) && 
            (_isPaused.value == false || _isPaused.value == null)) {
            _timeRemaining.value = minutes.toInt() * 60 * 1000L
        }
    }

    fun setOnTimerComplete(callback: () -> Unit) {
        onTimerComplete = callback
    }

    fun startTimer() {
        if (_isRunning.value == true) return

        val duration = (_durationMinutes.value ?: 25f).toInt()
        val totalDurationMs = duration * 60 * 1000L
        
        if (_isPaused.value != true) {
            // Iniciar nuevo temporizador
            _timeRemaining.value = totalDurationMs
            pausedRemainingTime = null
            timerStartTime = System.currentTimeMillis()
        } else {
            // Reanudar desde donde se pausó
            val remaining = pausedRemainingTime ?: totalDurationMs
            _timeRemaining.value = remaining
            timerStartTime = System.currentTimeMillis() - (totalDurationMs - remaining)
            pausedRemainingTime = null
        }

        _isRunning.value = true
        _isPaused.value = false
        prefs.edit().putBoolean(IS_RUNNING_KEY, true).putBoolean(IS_PAUSED_KEY, false).apply()

        timerJob = viewModelScope.launch {
            while (_isRunning.value == true) {
                val startTime = timerStartTime
                if (startTime != null) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val remaining = totalDurationMs - elapsed
                    
                    if (remaining > 0) {
                        _timeRemaining.value = remaining
                    } else {
                        // Temporizador completado
                        _timeRemaining.value = 0L
                        _isRunning.value = false
                        _isPaused.value = false
                        timerStartTime = null
                        pausedRemainingTime = null
                        prefs.edit().putBoolean(IS_RUNNING_KEY, false).putBoolean(IS_PAUSED_KEY, false).apply()
                        onTimerComplete?.invoke()
                        return@launch
                    }
                }
                delay(1000)
            }
        }
    }

    fun pauseTimer() {
        _isRunning.value = false
        _isPaused.value = true
        timerJob?.cancel()
        timerJob = null
        
        // Guardar el tiempo restante actual
        pausedRemainingTime = _timeRemaining.value
        timerStartTime = null
        prefs.edit().putBoolean(IS_RUNNING_KEY, false).putBoolean(IS_PAUSED_KEY, true).apply()
    }

    fun stopTimer() {
        _isRunning.value = false
        _isPaused.value = false
        timerJob?.cancel()
        timerJob = null
        timerStartTime = null
        pausedRemainingTime = null
        prefs.edit().putBoolean(IS_RUNNING_KEY, false).putBoolean(IS_PAUSED_KEY, false).apply()
        
        val duration = (_durationMinutes.value ?: 25f).toInt()
        _timeRemaining.value = duration * 60 * 1000L
    }
    
    // Función para actualizar el tiempo restante basado en el timestamp
    // Esto se llama cuando el Fragment vuelve a estar visible
    fun updateTimeRemaining() {
        if (_isRunning.value == true && timerStartTime != null) {
            val duration = (_durationMinutes.value ?: 25f).toInt()
            val totalDurationMs = duration * 60 * 1000L
            val elapsed = System.currentTimeMillis() - timerStartTime!!
            val remaining = totalDurationMs - elapsed
            
            if (remaining > 0) {
                _timeRemaining.value = remaining
                // Reiniciar el job si se perdió, pero sin reiniciar el temporizador
                if (timerJob?.isActive != true) {
                    // Reiniciar solo el job, manteniendo el estado actual
                    _isRunning.value = true
                    _isPaused.value = false
                    
                    timerJob = viewModelScope.launch {
                        while (_isRunning.value == true) {
                            val startTime = timerStartTime
                            if (startTime != null) {
                                val elapsed = System.currentTimeMillis() - startTime
                                val remaining = totalDurationMs - elapsed
                                
                                if (remaining > 0) {
                                    _timeRemaining.value = remaining
                                } else {
                                    // Temporizador completado
                                    _timeRemaining.value = 0L
                                    _isRunning.value = false
                                    _isPaused.value = false
                                    timerStartTime = null
                                    pausedRemainingTime = null
                                    prefs.edit().putBoolean(IS_RUNNING_KEY, false).putBoolean(IS_PAUSED_KEY, false).apply()
                                    onTimerComplete?.invoke()
                                    return@launch
                                }
                            }
                            delay(1000)
                        }
                    }
                }
            } else {
                // El temporizador ya terminó mientras estaba fuera
                _timeRemaining.value = 0L
                _isRunning.value = false
                _isPaused.value = false
                timerStartTime = null
                prefs.edit().putBoolean(IS_RUNNING_KEY, false).putBoolean(IS_PAUSED_KEY, false).apply()
                onTimerComplete?.invoke()
            }
        }
    }

    fun getPendingRemindersToday(): kotlinx.coroutines.flow.Flow<List<Reminder>> {
        return repository.getRemindersForToday()
    }
}

