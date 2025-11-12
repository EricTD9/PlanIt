package com.planit.util

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionHelper {
    /**
     * Verifica si la app puede programar alarmas exactas
     * En Android 15+, se requieren permisos especiales
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }
        // En versiones anteriores, asumimos que tenemos permiso
        return true
    }

    /**
     * Verifica si las notificaciones están habilitadas
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val notificationManager = ContextCompat.getSystemService(
                context,
                android.app.NotificationManager::class.java
            )
            return notificationManager?.areNotificationsEnabled() ?: false
        }
        return true
    }
}

