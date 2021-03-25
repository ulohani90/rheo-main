package com.rheotv.android.ui.activities.customroom.model

import com.google.gson.annotations.SerializedName

data class CustomRoomDetailResponse(
        @SerializedName("customroom")
        val customRoom: CustomRoomDetail?
)