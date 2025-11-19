package com.planit.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.planit.data.dao.ActivityDao
import com.planit.data.dao.ReminderDao
import com.planit.data.dao.ReminderOccurrenceDao
import com.planit.data.model.ActivityItem
import com.planit.data.model.Reminder
import com.planit.data.model.ReminderOccurrence

@Database(
    entities = [Reminder::class, ActivityItem::class, ReminderOccurrence::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PlanItDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun activityDao(): ActivityDao
    abstract fun reminderOccurrenceDao(): ReminderOccurrenceDao

    companion object {
        @Volatile
        private var INSTANCE: PlanItDatabase? = null

        fun getDatabase(context: Context): PlanItDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlanItDatabase::class.java,
                    "planit_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

