package com.rheotv.android.ui.activities.customroom.model

import com.google.gson.annotations.SerializedName

data class AddCustomRoomIdPasswordRequest(
        @SerializedName("customroom_id")
        val customRoomId: String?,
        @SerializedName("room_id")
        val roomId: String?,
        @SerializedName(" customroom_password")
        val customRoomPassword: String?
)