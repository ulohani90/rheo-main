package com.rheotv.android.ui.activities.moments.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.rheotv.android.data.network.models.objects.PostObject
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MomentsListItem(
        @SerializedName("author_language")
        var authorLanguage: String? = null,
        @SerializedName("author_username")
        var authorUsername: String? = null,
        @SerializedName("clip_creator_username")
        var clipCreatorUsername: String? = null,
        @SerializedName("clip_ended_at")
        var clipEndedAt: String? = null,
        @SerializedName("clip_started_at")
        var clipStartedAt: String? = null,
        @SerializedName("created_at")
        var createdAt: String? = null,
        @SerializedName("game_name")
        var gameName: String? = null,
        @SerializedName("id")
        var id: String? = null,
        @SerializedName("is_active")
        var isActive: Boolean? = false,
        @SerializedName("is_verified_clip")
        var isVerifiedClip: Boolean? = false,
        @SerializedName("post_details")
        var postDetails: PostObject? = null,
        @SerializedName("post_id")
        var postId: String? = null,
        @SerializedName("post_url")
        var postUrl: String? = null,
        @SerializedName("reviewed_by")
        var reviewedBy: String? = null,
        @SerializedName("seek_ended_at")
        var seekEndedAt: Int? = 0,
        @SerializedName("seek_started_at")
        var seekStartedAt: Int? = 0,
        @SerializedName("stream_date")
        var streamDate: String? = null,
        @SerializedName("updated_at")
        var updatedAt: String? = null,
        @SerializedName("post_created_at_timestamp")
        var postCreatedAtTimestamp: Double? = 0.0,
        var isContentModerator: Boolean? = false
) : Parcelable