package com.planit.data.dao

import androidx.room.*
import com.planit.data.model.ActivityItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE reminderId = :reminderId ORDER BY `order` ASC")
    fun getActivitiesByReminderId(reminderId: Long): Flow<List<ActivityItem>>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: Long): ActivityItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<ActivityItem>)

    @Update
    suspend fun updateActivity(activity: ActivityItem)

    @Delete
    suspend fun deleteActivity(activity: ActivityItem)

    @Query("DELETE FROM activities WHERE reminderId = :reminderId")
    suspend fun deleteActivitiesByReminderId(reminderId: Long)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteActivityById(id: Long)
}

