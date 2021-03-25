package com.rheotv.android.data.network.models.useProfile.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat

data class ChatGroupDetails(
        @SerializedName("connection_id")
        @Expose
        var connectionId: String? = null,

        @SerializedName("post_id")
        @Expose
        var postId: String? = null,

        @SerializedName("pinned_comment")
        @Expose
        var pinnedComment: CommentChat? = null,

        @SerializedName("comment_suggestions")
        @Expose
        var messageSuggestion: MutableList<String>? = null
)