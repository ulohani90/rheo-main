package com.rheotv.android.data.network.models.useProfile.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.rheotv.android.ui.adapters.AchievementType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AchievementsData(
        @SerializedName("type")
        val type: String?,
        @SerializedName("title")
        val title: String?,
        @SerializedName("amount")
        val amount: Int,
        @SerializedName("value")
        val value: Int,
        @SerializedName("text")
        val infoText: String? = null,
        @SerializedName("target")
        val target: Int,
        @SerializedName("completed")
        val completed: Boolean
) : Parcelable {

    val achievementType: Int
        get() = if (type == "wh_achievement_bonus") AchievementType.Bonus.toString().toInt() else AchievementType.Level.toString().toInt()
}