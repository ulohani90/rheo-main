package com.rheotv.android.db

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "clip")
data class ClipItem(

        @PrimaryKey
        @field:SerializedName("id")
        var id: String = "",

        @field:SerializedName("game")
        var game: String? = null,

        @field:SerializedName("video_url")
        var videoUrl: String? = null,

        @field:SerializedName("author")
        var author: Author? = null,

        @field:SerializedName("clap")
        var clap: Boolean = false,

        @field:SerializedName("video_mode")
        var videoMode: String? = null,

        @field:SerializedName("created_at")
        var createdAt: String? = null,

        @field:SerializedName("title")
        var title: String? = null,

        @field:SerializedName("thumbnail_url")
        var thumbnailUrl: String? = null,

        @field:SerializedName("clap_count")
        var clapCount: Int = 0,

        @field:SerializedName("game_id")
        var gameId: String? = null,

        @SerializedName("live_status")
        @Expose
        @Ignore
        var liveStatus: LiveStatus? = null,

        @field:SerializedName("duration")
        var duration: Float = 0f , // todo add in api

        @field:SerializedName("total_views")
        var viewCount: Int? = 0

) {
    fun durationInSeconds(): Float {
        return duration * 60
    }
}

