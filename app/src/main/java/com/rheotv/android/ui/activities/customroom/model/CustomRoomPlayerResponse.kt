package com.rheotv.android.ui.activities.customroom.model

import com.google.gson.annotations.SerializedName

data class CustomRoomPlayerResponse(
        @SerializedName("count")
        val count: Int = 0,
        @SerializedName("next")
        val next: String?,
        @SerializedName("previous")
        val previous: String?,
        @SerializedName("results")
        val results: MutableList<CustomRoomPlayer>? = mutableListOf()

)

data class CustomRoomPlayer(
        @SerializedName("id")
        val id: String?,
        @SerializedName("username")
        var username: String?,
        @SerializedName("profile_pic_url")
        var profilePicUrl: String?,
        @SerializedName("game_username")
        var gameUsername: String?,
        @SerializedName("is_winner")
        var isWinner: Boolean = false
)