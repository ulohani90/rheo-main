package com.rheotv.android.data.network.models.useProfile.responses

import com.google.gson.annotations.SerializedName

data class AchievementsResponse(
        @SerializedName("data")
        val achievements: List<Achievements> = mutableListOf()
)