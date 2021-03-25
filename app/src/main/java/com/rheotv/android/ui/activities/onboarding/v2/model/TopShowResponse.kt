package com.rheotv.android.ui.activities.onboarding.v2.model

import android.text.format.DateUtils
import com.google.gson.annotations.SerializedName
import com.rheotv.android.data.network.models.objects.PostObject
import com.rheotv.android.data.network.models.postlisting.responses.Author
import com.rheotv.android.data.network.models.postlisting.responses.Result
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.TimeUtils
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*

data class TopShowResponse(

		@field:SerializedName("count")
		val count: Int? = null,

		@field:SerializedName("results")
		val results: List<ShowData>? = null
)

data class ShowData(

		@field:SerializedName("pk")
		val id: String? = null,

		@field:SerializedName("slot_start_time")
		val slotStartTime: String? = null,

		@field:SerializedName("start_time")
		val startTime: String? = null,

		@field:SerializedName("slot_image_url")
		val slotImageUrl: String? = null,

		@field:SerializedName("post_id")
		val postId: String? = null,

		@field:SerializedName("author")
		val author: Author? = null,

		@field:SerializedName("end_time")
		val endTime: String? = null,

		@field:SerializedName("title")
		val title: String? = null,

		@field:SerializedName("game_name")
		val gameName: String? = null,

		@field:SerializedName("slot_position")
		val slotPosition: Int? = null,

		@field:SerializedName("slot_end_time")
		val slotEndTime: String? = null

) {

    fun displayAttribute(): String = CommonUtils.formatValue((author?.followersCount ?: 0).plus(10000).toDouble()) + " \n Waiting"

	fun nextLiveAt(): String {
		val timeLong = (TimeUtils.getDateFromString(startTime ?: "",
				TimeUtils.YYYY_MM_DD_T_HH_MM_SS) ?:
				TimeUtils.getDateFromString(startTime ?: "", TimeUtils.YYYY_MM_DD_T_HH_MM_SS_SSSXXX) ?:
				Date()).time

		// get hour in 24 hour time
		val hourFormatter: Format = SimpleDateFormat("hh:mm aa")
		val timeHour: String = hourFormatter.format(timeLong)

		return "$timeHour"
	}

	fun streamStartAt() = TimeUtils.getDateFromString(startTime ?: "",
			TimeUtils.YYYY_MM_DD_T_HH_MM_SS_SSSXXX) ?: Date()

	fun getDay(): String {
		val timeLong = (TimeUtils.getDateFromString(startTime ?: "",
				TimeUtils.YYYY_MM_DD_T_HH_MM_SS) ?:
		TimeUtils.getDateFromString(startTime ?: "", TimeUtils.YYYY_MM_DD_T_HH_MM_SS_SSSXXX) ?:
		Date()).time
		val timeNow = System.currentTimeMillis()

		// get day in relative time
		val timeDayRelative: CharSequence
		timeDayRelative = DateUtils.getRelativeTimeSpanString(timeLong, timeNow, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)
		return "$timeDayRelative"
	}

}

data class LatestPostResponse(

		@field:SerializedName("results")
		val results: Result? = null

)
