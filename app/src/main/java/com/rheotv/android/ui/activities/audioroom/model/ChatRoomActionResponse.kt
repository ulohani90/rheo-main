package com.rheotv.android.ui.activities.audioroom.model

import com.google.gson.annotations.SerializedName

data class ChatRoomActionResponse(
        @SerializedName("error")
        val error: String?,
        @SerializedName("msg")
        val message: String?,
        @SerializedName("participant_details")
        val ownerDetail: OwnerDetail?
)