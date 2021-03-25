package com.rheotv.android.data.network.models.useProfile.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Achievements(
        @SerializedName("level")
        val level: String,
        @SerializedName("wh_level")
        val currentWatchHour: String? = null,
        @SerializedName("data")
        val data: List<AchievementsData>
) : Parcelable