package com.rheotv.android.ui.activities.customroom.model

import com.google.gson.annotations.SerializedName

data class CustomRoomIdPasswordResponse(
        @SerializedName("result")
        val result: CustomRoomDetail?
)