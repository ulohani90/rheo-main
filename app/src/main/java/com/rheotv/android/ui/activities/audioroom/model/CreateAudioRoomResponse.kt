package com.rheotv.android.ui.activities.audioroom.model

import com.google.gson.annotations.SerializedName
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat

data class CreateAudioRoomResponse(

        @field:SerializedName("max_connections")
        val maxConnections: Int? = 0,

        @field:SerializedName("currently_joined")
        val currentlyJoined: Int? = 0,

        @field:SerializedName("max_duplex_conns")
        val maxDuplexConnections: Int? = 0,

        @field:SerializedName("is_streamer_followed")
        val isStreamerFollowed: Boolean? = false,

        @field:SerializedName("streaming_service")
        val streamingService: StreamingService? = null,

        @field:SerializedName("chatroom_details")
        val chatRoomDetails: ChatRoomDetails? = null,

        @field:SerializedName("is_blocked")
        val isUserBlocked: Boolean? = false
)

data class ChatRoomDetails(

        @field:SerializedName("start_time")
        val startTime: String? = null,

        @field:SerializedName("group_details")
        val groupDetails: AudioGroup? = null,

        @field:SerializedName("name")
        val name: String? = null,

        @field:SerializedName("end_time")
        val endTime: String? = null,

        @field:SerializedName("is_public")
        val isPublic: Boolean? = null,

        @field:SerializedName("id")
        val id: String? = null,

        @field:SerializedName("state")
        val state: Int? = null,

        @field:SerializedName("pinned_comment")
        val pinnedComment: CommentChat? = null,

        @field:SerializedName("grpc_chat_channel_id")
        val grpcConnectionId: String? = null,

        @field:SerializedName("available_social_games")
        val availableSocialGames: MutableList<SocialGame>? = mutableListOf(),

        @field:SerializedName("active_game")
        val activeSocialGame: SocialGame?,

        @field:SerializedName("highlighted_participant")
        val highlightedUser: OwnerDetail?
)

data class SocialGame(
        @SerializedName("id")
        val id: String?,
        @SerializedName("name")
        val name: String?,
        @SerializedName("rules")
        val rules: String?,
        @SerializedName("logo_url")
        val logoUrl: String?
)

data class StreamingService(

        @field:SerializedName("auth_token")
        val authToken: String? = null,

        @field:SerializedName("channel_id")
        val channelId: String? = null
)
