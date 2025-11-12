package com.planit.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import com.planit.PlanItApplication
import com.planit.data.model.Reminder
import com.planit.notification.ReminderNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", -1L)
        val reminderTitle = intent.getStringExtra("reminder_title") ?: "Recordatorio"
        val reminderDescription = intent.getStringExtra("reminder_description")
        val hasVibration = intent.getBooleanExtra("has_vibration", true)

        // Mostrar notificación
        val notificationService = ReminderNotificationService(context)
        notificationService.showReminderNotification(reminderId, reminderTitle, reminderDescription)

        // Vibrar si está habilitado
        if (hasVibration) {
            vibrate(context)
        }

        // Si es repetitivo, programar el próximo recordatorio
        val repetitionType = intent.getStringExtra("repetition_type")
        if (repetitionType != null && repetitionType != "ONCE") {
            val application = context.applicationContext as PlanItApplication
            CoroutineScope(Dispatchers.IO).launch {
                val reminder = application.repository.getReminderById(reminderId)
                reminder?.let {
                    ReminderAlarmManager(context).scheduleReminder(it)
                }
            }
        }
    }

    private fun vibrate(context: Context) {
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { vib ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(500)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ReminderAlarmReceiver", "Error al vibrar: ${e.message}")
        }
    }
}

