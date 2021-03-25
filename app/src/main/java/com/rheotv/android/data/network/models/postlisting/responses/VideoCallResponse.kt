package com.rheotv.android.data.network.models.postlisting.responses

import com.google.gson.annotations.SerializedName

data class VideoCallResponse(
        @SerializedName("results")
        val results: VideoCallResult?,
        @SerializedName("status")
        val status: String?,
        @SerializedName("error")
        val error: String?
)


data class VideoCallResult(
        @SerializedName("channel_id")
        val channelId: String?,

        @SerializedName("streamer_agora_token")
        val streamerAgoraToken: String?,

        @SerializedName("sorted_position")
        val sortedPosition: Int?
)


