package com.rheotv.android.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "user_follow")
data class UserFollowItem(
        @PrimaryKey
        @ColumnInfo(name = "user_id")
        var userId: Int = 0,

        @ColumnInfo(name = "user_name")
        var userName: String?,

        @ColumnInfo(name = "is_followed")
        var isFollowed: Boolean = false
)
