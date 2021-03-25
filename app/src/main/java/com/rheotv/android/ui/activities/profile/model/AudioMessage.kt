package com.rheotv.android.ui.activities.profile.model

import com.google.gson.annotations.SerializedName
import com.rheotv.android.utils.TimeUtils
import com.rheotv.android.utils.format
import java.util.*

data class AudioMessage (
        @field:SerializedName("audio_url")
        var url: String? = null,

        @field:SerializedName("message_duration")
        var duration: Long? = null,

        @field:SerializedName("updated_at")
        var updatedAt: Long? = null
) {
        val formattedDuration: String
                get() = if (duration == null) "No Message" else Date(duration ?: 0).format(TimeUtils.HH_MM)

        val formattedDate: String
                get() = if (updatedAt == 0L) "Add One" else Date(updatedAt ?: 0).format(TimeUtils.DD_MM_YYYY)

        companion object {
                fun getMessageFromUrl(url: String) = AudioMessage(url, 0, System.currentTimeMillis())
        }
}