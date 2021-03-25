package com.rheotv.android.ui.activities.customroom.model

import com.google.gson.annotations.SerializedName

data class CustomRoomRequest(
        @SerializedName("customroom_id")
        val customRoomId: String?,
        @SerializedName("game_username")
        val gameUsername: String?
)