package com.planit.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.planit.MainActivity
import com.planit.R

class ReminderNotificationService(private val context: Context) {
    private val channelId = "reminder_channel"
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de recordatorios de PlanIt!"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(
        reminderId: Long,
        title: String,
        description: String?
    ) {
        // Verificar si las notificaciones están habilitadas (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationManager.areNotificationsEnabled()) {
                android.util.Log.w("ReminderNotification", "Las notificaciones no están habilitadas")
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminder_id", reminderId)
        }
        
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                context,
                reminderId.toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            @Suppress("DEPRECATION")
            PendingIntent.getActivity(
                context,
                reminderId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(description ?: context.getString(R.string.reminder_notification_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))

        // En Android 15+, PRIORITY está deprecado, usar importancia del canal
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
        }

        val notification = notificationBuilder.build()

        try {
            notificationManager.notify(reminderId.toInt(), notification)
        } catch (e: SecurityException) {
            android.util.Log.e("ReminderNotification", "Error al mostrar notificación: ${e.message}")
        }
    }

    fun cancelNotification(reminderId: Long) {
        notificationManager.cancel(reminderId.toInt())
    }
}

