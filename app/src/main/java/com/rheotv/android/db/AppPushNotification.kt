package com.rheotv.android.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_push_notification")
class AppPushNotification (

        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,

        var createAt: Long = System.currentTimeMillis()

)