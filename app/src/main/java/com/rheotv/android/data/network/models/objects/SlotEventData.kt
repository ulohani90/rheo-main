package com.rheotv.android.data.network.models.objects

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.rheotv.android.data.network.models.postlisting.responses.Author
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class SlotEventData(
        @SerializedName("pk")
        val id: String?,
        @SerializedName("slot_image_url")
        val slotImageUrl: String?,
        @SerializedName("slot_start_time")
        val slotStartTime: String?,
        @SerializedName("post_id")
        val postId: String?,
        @field:SerializedName("author")
        val author: AuthorObject? = null,
        @SerializedName("title")
        val title: String?,
        @SerializedName("slot_position")
        val slotPosition: Int? = -1
) : Parcelable