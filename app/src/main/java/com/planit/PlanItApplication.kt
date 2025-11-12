package com.planit

import android.app.Application
import com.planit.data.database.PlanItDatabase
import com.planit.data.repository.ReminderRepository

class PlanItApplication : Application() {
    val database by lazy { PlanItDatabase.getDatabase(this) }
    val repository by lazy { ReminderRepository(database.reminderDao(), database.activityDao()) }
}

