package com.rheotv.android.ui.activities.customroom.model

import com.google.gson.annotations.SerializedName

data class CreateCustomRoomRequest(
        @SerializedName("post_id")
        val postId: String?,
        @SerializedName("start_time")
        val startTime: String?,
        @SerializedName("entry_coins")
        val entryCoins: Int,
        @SerializedName("max_allowed_users")
        val maxAllowedPlayers: Int
)