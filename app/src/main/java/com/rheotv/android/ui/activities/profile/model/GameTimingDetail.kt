package com.rheotv.android.ui.activities.profile.model

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import com.google.gson.annotations.SerializedName
import com.rheotv.android.utils.AppUtilsKt.increaseFontSizeForPath
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.isNullOrEmptyOrBlank
import java.util.*
import kotlin.collections.ArrayList

data class PlayTimingDetail(

        @field:SerializedName("gaming_days")
        var gamingDays: MutableList<GamingDays> = ArrayList(),

        @field:SerializedName("start_time")
        var startTime: String? = null,

        @field:SerializedName("end_time")
        var endTime: String? = null,

        @field:SerializedName("start_ampm")
        var startAMPM: String? = null,

        @field:SerializedName("end_ampm")
        var endAMPM: String? = null
) {

    fun getTotalDays(): MutableList<GamingDays> {
        if (gamingDays.isEmpty())
            gamingDays = getAllWeekDays()
        return gamingDays
    }

    fun getSpannableTime(): SpannableString {
        val timing = "${startTime ?: 0} ${startAMPM ?: 0}  -  ${endTime ?: 0} ${endAMPM ?: 0}"
        val spannable = SpannableString(timing)
        increaseFontSizeForPath(spannable, startTime, 2f, Color.WHITE)
        increaseFontSizeForPath(spannable, endTime, 2f, Color.WHITE)
        increaseFontSizeForPath(spannable, "-", 2f, Color.WHITE)
        return spannable
    }

    fun getNextLiveDay(): String? {
        val currentDayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
        var day: String? = null
        var isDayFound = false
        for (index in 0 until gamingDays.size) {
            if (gamingDays[index].isDaySelected && index >= currentDayIndex) {
                day = when (index) {
                    currentDayIndex -> "Today"
                    currentDayIndex + 1 -> "Tomorrow"
                    else -> getAllWeekDays()[index].day
                }
                isDayFound = true
            }
        }

        if (!isDayFound) {
            val firstGamingIndex = gamingDays.indexOfFirst { it.isDaySelected }
            day = if (currentDayIndex == 6 && firstGamingIndex == 0) {
                "Tomorrow"
            } else if (firstGamingIndex != -1) {
                getAllWeekDays()[firstGamingIndex].day
            } else
                null
        }

        return when {
            day == "Today" || day == "Tomorrow" -> "Next Live $day at $startTime $startAMPM"
            !(day.isNullOrEmptyOrBlank()) -> "Next Live on $day at $startTime $startAMPM"
            else -> null
        }
    }

    private fun getAllWeekDays() =
            mutableListOf(
                    GamingDays(day = "Sunday"),
                    GamingDays(day = "Monday"),
                    GamingDays(day = "Tuesday"),
                    GamingDays(day = "Wednesday"),
                    GamingDays(day = "Thursday"),
                    GamingDays(day = "Friday"),
                    GamingDays(day = "Saturday")
            )
}

data class GamingDays(
        @field:SerializedName("day")
        var day: String? = null,

        @field:SerializedName("selected")
        var isDaySelected: Boolean = false

) : Selectable() {
    override var isSelected: Boolean
        get() = isDaySelected
        set(value) {
            isDaySelected = value
        }

    override val text: CharSequence?
        get() = day?.subSequence(0, 1)

    override var tag: CharSequence?
        get() = day
        set(value) {
            day = value.toString()
        }
}

abstract class Selectable {
    abstract var isSelected: Boolean

    abstract val text: CharSequence?

    abstract var tag: CharSequence?
}