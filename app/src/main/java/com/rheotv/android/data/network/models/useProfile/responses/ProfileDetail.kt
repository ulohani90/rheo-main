package com.rheotv.android.data.network.models.useProfile.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.rheotv.android.data.network.models.AnalyticsStreamerData
import com.rheotv.android.data.network.models.StreamerData
import com.rheotv.android.ui.activities.profile.model.*
import com.rheotv.android.ui.activities.profile.viewprofile.model.GameRule

data class ProfileDetail(

        @SerializedName("configuration_used")
        @Expose
        var configurationUsed: String? = null,

        @SerializedName("donation")
        @Expose
        var donation: UserDonation? = null,

        @SerializedName("online_presence")
        @Expose
        var socialMediaList: MutableList<SocialMedia>? = null,

        @SerializedName("game_rules")
        @Expose
        var gameRules: MutableList<GameRule>? = null,

        @SerializedName("featured_photo")
        @Expose
        var featuredPhotos: MutableList<FeaturedPhoto>? = null,

        @SerializedName("game_wise_username")
        @Expose
        var gameUserNames: MutableList<GameWiseUser>? = null,

        @SerializedName("game_schedule")
        @Expose
        val gameSchedule: PlayTimingDetail? = null,

        @SerializedName("audio_message")
        @Expose
        var audioMessage: AudioMessage? = null,

        @SerializedName("analytics")
        @Expose
        var analyticsData: StreamerData? = null,


        @SerializedName("analytics_data")
        @Expose
        var streamerAnalyticsData: MutableList<AnalyticsStreamerData>? = null,

        @field:SerializedName("live_reminder_message")
        @Expose
        var liveReminderMessage: String? = null,

        @field:SerializedName("on_live_reminder_added")
        var liveReminderAdded: Boolean = false,

        @SerializedName("welcome_text")
        var welcomeText: String? = null,

        @SerializedName("is_chat_allowed")
        @Expose
        var isChatAllowed: Boolean = false,

        @SerializedName("chat_criteria")
        @Expose
        var chatCriteria: String? = null
)

data class LiveStatus(
        @SerializedName("is_live")
        @Expose
        var isLive: Boolean = false,

        @SerializedName("live_post_id")
        @Expose
        var livePostId: String? = null
)