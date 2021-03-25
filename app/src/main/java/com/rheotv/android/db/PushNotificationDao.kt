package com.rheotv.android.db

import androidx.room.*

@Dao
interface PushNotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNotification(notification: AppPushNotification): Long

    @Delete
    fun deleteNotification(notification: AppPushNotification)

    @Query("DELETE FROM app_push_notification")
    suspend fun deleteAllNotification()

    @Query("SELECT * FROM app_push_notification")
    suspend fun getNotification(): List<AppPushNotification>
}