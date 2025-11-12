package com.planit.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.planit.data.model.Reminder
import com.planit.data.model.RepetitionType
import java.util.Calendar

class ReminderAlarmManager(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val TAG = "ReminderAlarmManager"

    fun scheduleReminder(reminder: Reminder): Boolean {
        // Verificar si podemos programar alarmas exactas (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "No se puede programar alarma exacta - permiso no otorgado")
                // Intentar programar con setAlarmClock que es más permisivo
                return scheduleWithAlarmClock(reminder)
            }
        }

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_title", reminder.title)
            putExtra("reminder_description", reminder.description)
            putExtra("has_vibration", reminder.hasVibration)
            putExtra("repetition_type", reminder.repetitionType.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = reminder.dateTime.time

        val triggerAtMillis = calendar.timeInMillis

        try {
            // Android 12+ (API 31+): Usar setExactAndAllowWhileIdle con permisos
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            // Android 6+ (API 23+): Usar setExactAndAllowWhileIdle
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            // Android 4.4+ (API 19+): Usar setExact
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
            // Versiones anteriores: Usar set
            else {
                @Suppress("DEPRECATION")
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "Error al programar alarma: ${e.message}")
            // Fallback a setAlarmClock
            return scheduleWithAlarmClock(reminder)
        }

        // Si es repetitivo, programar la siguiente repetición
        if (reminder.repetitionType != RepetitionType.ONCE) {
            scheduleNextRepetition(reminder)
        }
        
        return true
    }

    /**
     * Método alternativo usando setAlarmClock (más permisivo en Android 15+)
     */
    private fun scheduleWithAlarmClock(reminder: Reminder): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false
        }

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_title", reminder.title)
            putExtra("reminder_description", reminder.description)
            putExtra("has_vibration", reminder.hasVibration)
            putExtra("repetition_type", reminder.repetitionType.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = reminder.dateTime.time

        try {
            val alarmClockInfo = android.app.AlarmManager.AlarmClockInfo(
                calendar.timeInMillis,
                pendingIntent
            )
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error al programar con AlarmClock: ${e.message}")
            return false
        }
    }

    private fun scheduleNextRepetition(reminder: Reminder) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = reminder.dateTime.time

        when (reminder.repetitionType) {
            RepetitionType.DAILY -> {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            RepetitionType.WEEKLY -> {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }
            else -> return
        }

        val nextReminder = reminder.copy(
            id = 0, // Nuevo ID se generará al insertar
            dateTime = calendar.time
        )

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_title", nextReminder.title)
            putExtra("reminder_description", nextReminder.description)
            putExtra("has_vibration", nextReminder.hasVibration)
            putExtra("repetition_type", nextReminder.repetitionType.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (reminder.id + System.currentTimeMillis()).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val triggerAtMillis = calendar.timeInMillis

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    // Fallback a setAlarmClock
                    val alarmClockInfo = android.app.AlarmManager.AlarmClockInfo(
                        triggerAtMillis,
                        pendingIntent
                    )
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Error al programar repetición: ${e.message}")
        }
    }

    fun cancelReminder(reminderId: Long) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }
}

