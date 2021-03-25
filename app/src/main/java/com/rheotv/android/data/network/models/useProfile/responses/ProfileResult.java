package com.rheotv.android.data.network.models.useProfile.responses;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;

import androidx.core.content.ContextCompat;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.general.GameDetails;
import com.rheotv.android.data.network.models.onboarding.LanguageObject;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.postlisting.responses.User;
import com.rheotv.android.ui.activities.audioroom.model.AudioChatRoom;
import com.rheotv.android.ui.activities.audioroom.model.ChatRoomDetails;
import com.rheotv.android.ui.adapters.LevelType;
import com.rheotv.android.utils.AppUtilsKt;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import static com.rheotv.android.utils.CommonUtils.formatValue;
import static com.rheotv.android.utils.CommonUtils.pluralise;

public class ProfileResult implements Parcelable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("intro")
    @Expose
    private String intro;
    @SerializedName("geolocation")
    @Expose
    private Object geolocation;
    @SerializedName("profile_pic")
    @Expose
    private String profilePic;
    @SerializedName("cover_pic")
    @Expose
    private String coverPic = "";
    @SerializedName("is_journalist")
    @Expose
    private Boolean isJournalist;
    @SerializedName("is_verified")
    @Expose
    private Boolean isVerified;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("featured_videos")
    @Expose
    private List<Result> featuredVideos = null;
    @SerializedName("top_videos")
    @Expose
    private List<Result> topVideos = null;
    @SerializedName("followers_count")
    @Expose
    private Integer followersCount;
    @SerializedName("follows_count")
    @Expose
    private Integer followsCount;
    @SerializedName("total_views")
    @Expose
    private Integer totalViews;
    @SerializedName("total_videos")
    @Expose
    private Integer totalVideos;
    @SerializedName("share_url")
    @Expose
    private String shareUrl;
    @SerializedName("is_followed")
    @Expose
    private Boolean isFollowed;
    @SerializedName("referral_amount")
    @Expose
    private Integer referralAmount;
    @SerializedName("show_refer_code")
    @Expose
    private Boolean showReferCode;
    @SerializedName("referral_message")
    @Expose
    private String referralMessage;
    @SerializedName("should_show_progress")
    @Expose
    private Boolean shouldShowPorgress;

    @SerializedName("should_show_wallet")
    @Expose
    private Boolean shouldShowWallet;

    @SerializedName("bio")
    @Expose
    private String bio;

    @SerializedName("is_prime")
    @Expose
    private Boolean isPrimeStreamer;

    @SerializedName("partner_progress_data")
    @Expose
    private PartnerProgressData progressData;

    @SerializedName("wallet")
    @Expose
    private Double wallet;

    @SerializedName("can_allow_payout")
    @Expose
    private Boolean canAllowPayout;

    @SerializedName("disable_payout_reason")
    @Expose
    private String diablePayoutReason;

    @SerializedName("profile_state")
    @Expose
    private int profileState;

    @SerializedName("languages")
    @Expose
    private ArrayList<LanguageObject> languages;

    @SerializedName("level")
    @Expose
    private String level;

    @SerializedName("moderators")
    @Expose
    private String moderators;

    @SerializedName("viewed")
    @Expose
    private Boolean storyViewed;

    @SerializedName("is_story_added")
    @Expose
    private Boolean storyAvailable;

//    @SerializedName("redeem_statements")
//    @Expose
//    private Object statement;

    @SerializedName("redeem_statements")
    @Expose
    private List<RedeemStatement> redeemStatement;

    @SerializedName("automatic_fraud_score")
    @Expose
    private int automaticFraudScore;

    @SerializedName("available_redeem_balance")
    @Expose
    private int redeemBalance;

    @SerializedName("can_redeem_available_balance")
    @Expose
    private Boolean canRedeemBalance;

    @SerializedName("next_redeem_date")
    @Expose
    private String redeemDate;

    @SerializedName("payment_model")
    @Expose
    private int paymentModel;

    @SerializedName("is_level_assigned")
    @Expose
    private Boolean isLevelAssigned;

    @SerializedName("current_rheo_diamond_value")
    @Expose
    private float rheoDiamondValue;

    @SerializedName("minimum_redeem_balance")
    @Expose
    private int minimumRedeemBalance;

    @SerializedName("streaming_opening_statement")
    @Expose
    private String streamingOpeningStatement;

    @SerializedName("is_content_moderator")
    @Expose
    private boolean isContentModerator;

    @SerializedName("has_voted_for_moderator")
    @Expose
    private boolean isVotedAsModerator;

    @SerializedName("show_content_moderator_voting")
    @Expose
    private boolean enableContentModerator;

    @SerializedName("new_data")
    @Expose
    private ProfileDetail profileDetail;

    @SerializedName("live_status")
    @Expose
    private LiveStatus liveStatus;

    @SerializedName("is_moderator_voting_enabled")
    private boolean isModeratorVotingEnabled;

    @SerializedName("selected_games")
    @Expose
    private ArrayList<GameDetails> selectedGames;

    @SerializedName("chatroom_details")
    private ChatRoomDetails activeChatRooms;

    public ProfileResult(String id, String profilePic, User user) {
        this.id = id;
        this.profilePic = profilePic;
        this.user = user;
    }

    @SerializedName("campaign_info")
    @Expose
    private String campaignInfo;

    protected ProfileResult(Parcel in) {
        id = in.readString();
        intro = in.readString();
        profilePic = in.readString();
        coverPic = in.readString();
        byte tmpIsJournalist = in.readByte();
        isJournalist = tmpIsJournalist == 0 ? null : tmpIsJournalist == 1;
        byte tmpIsVerified = in.readByte();
        isVerified = tmpIsVerified == 0 ? null : tmpIsVerified == 1;
        createdAt = in.readString();
        updatedAt = in.readString();
        if (in.readByte() == 0) {
            followersCount = null;
        } else {
            followersCount = in.readInt();
        }
        if (in.readByte() == 0) {
            followsCount = null;
        } else {
            followsCount = in.readInt();
        }
        if (in.readByte() == 0) {
            totalViews = null;
        } else {
            totalViews = in.readInt();
        }
        if (in.readByte() == 0) {
            totalVideos = null;
        } else {
            totalVideos = in.readInt();
        }
        shareUrl = in.readString();
        byte tmpIsFollowed = in.readByte();
        isFollowed = tmpIsFollowed == 0 ? null : tmpIsFollowed == 1;
        if (in.readByte() == 0) {
            referralAmount = null;
        } else {
            referralAmount = in.readInt();
        }
        byte tmpShowReferCode = in.readByte();
        showReferCode = tmpShowReferCode == 0 ? null : tmpShowReferCode == 1;
        referralMessage = in.readString();
        byte tmpShouldShowPorgress = in.readByte();
        shouldShowPorgress = tmpShouldShowPorgress == 0 ? null : tmpShouldShowPorgress == 1;
        bio = in.readString();
        moderators = in.readString();
        storyViewed = in.readByte() != 0;
        user = in.readParcelable(User.class.getClassLoader());
        campaignInfo = in.readString();
        byte tmpIsStoryAvailable = in.readByte();
        storyAvailable = tmpIsStoryAvailable == 0 ? null : tmpIsStoryAvailable == 1;
        automaticFraudScore = in.readInt();
        redeemBalance = in.readInt();
        canRedeemBalance = in.readByte() != 0;
        redeemDate = in.readString();
        redeemStatement = new ArrayList<>();
        in.readTypedList(redeemStatement, RedeemStatement.CREATOR);
        paymentModel = in.readInt();
        byte tmpIsLevelAssigned = in.readByte();
        isLevelAssigned = tmpIsLevelAssigned == 0 ? null : tmpIsLevelAssigned == 1;
        rheoDiamondValue = in.readFloat();
        minimumRedeemBalance = in.readInt();
        streamingOpeningStatement = in.readString();
        isContentModerator = in.readByte() != 0;
        isVotedAsModerator = in.readByte() != 0;
        enableContentModerator = in.readByte() != 0;
        isModeratorVotingEnabled = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(intro);
        dest.writeString(profilePic);
        dest.writeString(coverPic);
        dest.writeByte((byte) (isJournalist == null ? 0 : isJournalist ? 1 : 2));
        dest.writeByte((byte) (isVerified == null ? 0 : isVerified ? 1 : 2));
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
        if (followersCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(followersCount);
        }
        if (followsCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(followsCount);
        }
        if (totalViews == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(totalViews);
        }
        if (totalVideos == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(totalVideos);
        }
        dest.writeString(shareUrl);
        dest.writeByte((byte) (isFollowed == null ? 0 : isFollowed ? 1 : 2));
        if (referralAmount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(referralAmount);
        }
        dest.writeByte((byte) (showReferCode == null ? 0 : showReferCode ? 1 : 2));
        dest.writeString(referralMessage);
        dest.writeByte((byte) (shouldShowPorgress == null ? 0 : shouldShowPorgress ? 1 : 2));
        dest.writeString(bio);
        dest.writeString(moderators);
        dest.writeByte((byte) (storyViewed == null ? 0 : 1));
        dest.writeParcelable(user, flags);
        dest.writeString(campaignInfo);
        dest.writeByte((byte) (storyAvailable == null ? 0 : storyAvailable ? 1 : 2));
        dest.writeInt(automaticFraudScore);
        dest.writeInt(redeemBalance);
        dest.writeByte((byte) (canRedeemBalance == null ? 0 : 1));
        dest.writeString(redeemDate);
        dest.writeTypedList(redeemStatement);
        dest.writeInt(paymentModel);
        dest.writeByte((byte) (isLevelAssigned == null ? 0 : isLevelAssigned ? 1 : 2));
        dest.writeFloat(rheoDiamondValue);
        dest.writeInt(minimumRedeemBalance);
        dest.writeString(streamingOpeningStatement);
        dest.writeByte((byte) (isContentModerator ? 0 : 1));
        dest.writeByte((byte) (isVotedAsModerator ? 0 : 1));
        dest.writeByte((byte) (enableContentModerator ? 0 : 1));
        dest.writeByte((byte) (isModeratorVotingEnabled ? 0 : 1));
    }

    public boolean isEnableContentModerator() {
        return enableContentModerator;
    }

    public void setEnableContentModerator(boolean enableContentModerator) {
        this.enableContentModerator = enableContentModerator;
    }

    public boolean isVotedAsModerator() {
        return isVotedAsModerator;
    }

    public void setVotedAsModerator(boolean votedAsModerator) {
        isVotedAsModerator = votedAsModerator;
    }

    public boolean getContentModerator() {
        return isContentModerator;
    }

    public void setContentModerator(boolean contentModerator) {
        isContentModerator = contentModerator;
    }

    public boolean canRequestForModerator() {
        return isEnableContentModerator() && isSelfProfile() && !getContentModerator();
    }

    public String getModerators() {
        return moderators;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProfileResult> CREATOR = new Creator<ProfileResult>() {
        @Override
        public ProfileResult createFromParcel(Parcel in) {
            return new ProfileResult(in);
        }

        @Override
        public ProfileResult[] newArray(int size) {
            return new ProfileResult[size];
        }
    };

    public ButtonData getButtonData() {
        return buttonData;
    }

    public void setButtonData(ButtonData buttonData) {
        this.buttonData = buttonData;
    }

    @SerializedName("partner_button_data")
    private ButtonData buttonData;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public Object getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(Object geolocation) {
        this.geolocation = geolocation;
    }

    public String getProfilePic() {
        if (profilePic.equals("")) {
            return "@drawable/avd_avatar";
        }
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getCoverPic() {
        if (coverPic.equals("")) {
            return "@drawable/profile_cover";
        }
        return coverPic;
    }

    public void setCoverPic(String coverPic) {
        this.coverPic = coverPic;
    }

    public Boolean getIsJournalist() {
        return isJournalist;
    }

    public void setIsJournalist(Boolean isJournalist) {
        this.isJournalist = isJournalist;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getJournalist() {
        return isJournalist;
    }

    public void setJournalist(Boolean journalist) {
        isJournalist = journalist;
    }

    public Boolean getVerified() {
        return isVerified;
    }

    public void setVerified(Boolean verified) {
        isVerified = verified;
    }

    public List<Result> getFeaturedVideos() {
        return featuredVideos;
    }

    public void setFeaturedVideos(List<Result> featuredVideos) {
        this.featuredVideos = featuredVideos;
    }

    public List<Result> getTopVideos() {
        return topVideos;
    }

    public void setTopVideos(List<Result> topVideos) {
        this.topVideos = topVideos;
    }

    public Boolean getFollowed() {
        return isFollowed;
    }

    public void setFollowed(Boolean followed) {
        isFollowed = followed;
    }

    public Integer getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Integer followersCount) {
        this.followersCount = followersCount;
    }

    public String getFollowersCountString() {
        if (followersCount == 0)
            return "0 Follower";

        String count = CommonUtils.formatValue(followersCount);
        return CommonUtils.getPlural("Follower", followersCount, count);
    }

    public String getVideosCountString() {
        return String.valueOf(totalVideos) + " videos";
    }

    public Integer getFollowsCount() {
        return followsCount;
    }

    public void setFollowsCount(Integer followsCount) {
        this.followsCount = followsCount;
    }

    public Integer getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(Integer totalViews) {
        this.totalViews = totalViews;
    }

    public Integer getTotalVideos() {
        return totalVideos;
    }

    public void setTotalVideos(Integer totalVideos) {
        this.totalVideos = totalVideos;
    }

    public String getTotalViewsString() {
        if (totalViews == 0)
            return "0 View";
        return CommonUtils.getPlural("View", totalViews, (totalViews / 1000 >= 1) ? (totalViews / 1000) + "." + ((totalViews % 1000) / 100) + "K" : totalViews + "");
    }

    public String getTotalVideosString() {
        if (totalVideos == 0)
            return "0 Videos";
        return CommonUtils.getPlural("Video", totalVideos, (totalVideos / 1000 >= 1) ? (totalVideos / 1000) + "." + ((totalVideos % 1000) / 100) + "K" : totalVideos + "");
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public Boolean getIsFollowed() {
        return isFollowed;
    }

    public void setIsFollowed(Boolean isFollowed) {
        this.isFollowed = isFollowed;
    }

    public Integer getReferralAmount() {
        return referralAmount;
    }

    public void setReferralAmount(Integer referralAmount) {
        this.referralAmount = referralAmount;
    }

    public Boolean getShowReferCode() {
        return showReferCode;
    }

    public void setShowReferCode(Boolean showReferCode) {
        this.showReferCode = showReferCode;
    }

    public String getReferralMessage() {
        return referralMessage;
    }

    public void setReferralMessage(String referralMessage) {
        this.referralMessage = referralMessage;
    }

    public Boolean getShouldShowPorgress() {
        //return false;
        return shouldShowPorgress;
    }

    public void setShouldShowPorgress(Boolean shouldShowPorgress) {
        this.shouldShowPorgress = shouldShowPorgress;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public PartnerProgressData getProgressData() {
        return progressData;
    }

    public Double getWallet() {
        return wallet;
    }

    public Boolean getCanAllowPayout() {
        return canAllowPayout;
    }

    public String getDiablePayoutReason() {
        return diablePayoutReason;
    }

    public int getProfileState() {
        return profileState;
    }

    public Boolean getShouldShowWallet() {
        return shouldShowWallet;
    }

    public void setShouldShowWallet(Boolean shouldShowWallet) {
        this.shouldShowWallet = shouldShowWallet;
    }

    public Boolean getPrimeStreamer() {
        return isPrimeStreamer;
    }

    public void setPrimeStreamer(Boolean primeStreamer) {
        isPrimeStreamer = primeStreamer;
    }

    public ArrayList<LanguageObject> getLanguages() {
        return languages;
    }

    public String getLevel() {
        return level;
    }

    public LevelType getLevelType() {
        if (level != null) {
            if (level.equalsIgnoreCase("bronze")) {
                if (isLevelAssigned)
                    return LevelType.Bronze.INSTANCE;
                else
                    return LevelType.Unassigned.INSTANCE;
            } else if (level.equalsIgnoreCase("silver")) {
                return LevelType.Silver.INSTANCE;
            } else if (level.equalsIgnoreCase("gold")) {
                return LevelType.Gold.INSTANCE;
            }
        }
        return LevelType.Unassigned.INSTANCE;
    }

    public Drawable getBadgeDrawable() {
        return ContextCompat.getDrawable(RheoTvApp.getNonUiContext(), getBadge());
    }

    public int getBadge() {
        if ("gold".equalsIgnoreCase(level))
            return R.drawable.ic_gold;
        else if ("silver".equalsIgnoreCase(level))
            return R.drawable.ic_silver_selected;
        else
            return R.drawable.ic_bronze;
    }

    public int getBadgeColor() {
        if ("gold".equalsIgnoreCase(level))
            return R.color.gold;
        else if ("silver".equalsIgnoreCase(level))
            return R.color.silver;
        else
            return R.color.bronze;
    }

    public Drawable getProfileBadgeDrawable() {
        return ContextCompat.getDrawable(RheoTvApp.getNonUiContext(), getProfileBadge());
    }

    public int getProfileBadge() {
        if ("gold".equalsIgnoreCase(level))
            return R.drawable.gold_p;
        else if ("silver".equalsIgnoreCase(level))
            return R.drawable.silver_p;
        else
            return R.drawable.bronze_p;
    }

    public void setModerators(String moderators) {
        this.moderators = moderators;
    }

    public Boolean getStoryViewed() {
        return storyViewed == null ? false : storyViewed;
    }

    public void setStoryViewed(Boolean storyViewed) {
        this.storyViewed = storyViewed;
    }

    public boolean isSelfProfile() {
        return this.id.equalsIgnoreCase(CommonUtils.getAuthorId());
    }

    public String getCampaignInfo() {
        return campaignInfo;
    }

    public Boolean getStoryAvailable() {
        return storyAvailable == null ? false : storyAvailable;
    }

    public List<RedeemStatement> getRedeemStatement() {
        return redeemStatement;
    }

    public void setRedeemStatement(List<RedeemStatement> redeemStatement) {
        this.redeemStatement = redeemStatement;
    }

    public int getAutomaticFraudScore() {
        return automaticFraudScore;
    }

    public void setAutomaticFraudScore(int automaticFraudScore) {
        this.automaticFraudScore = automaticFraudScore;
    }

    public int getRedeemBalance() {
        return redeemBalance;
    }

    public void setRedeemBalance(int redeemBalance) {
        this.redeemBalance = redeemBalance;
    }

    public Boolean getCanRedeemBalance() {
        return canRedeemBalance;
    }

    public void setCanRedeemBalance(Boolean canRedeemBalance) {
        this.canRedeemBalance = canRedeemBalance;
    }

    public String getRedeemDate() {
        return redeemDate;
    }

    public void setRedeemDate(String redeemDate) {
        this.redeemDate = redeemDate;
    }

    public int getPaymentModel() {
        return paymentModel;
    }

    public void setPaymentModel(int paymentModel) {
        this.paymentModel = paymentModel;
    }

    public String getNextRedeemDate() {
        String date = TimeUtils.getFormattedDate(TimeUtils.DD_MMM_YYYY, TimeUtils.getDateFromString(redeemDate, TimeUtils.YYYY_MM_DD));
        return date != null ? date.replace("-", " ") : "";
    }

    public Boolean isLevelAssigned() {
        return isLevelAssigned;
    }

    public void setLevelAssigned(Boolean levelAssigned) {
        isLevelAssigned = levelAssigned;
    }

    public float getRheoDiamondValue() {
        return rheoDiamondValue;
    }

    public void setRheoDiamondValue(float rheoDiamondValue) {
        this.rheoDiamondValue = rheoDiamondValue;
    }

    public int getMinimumRedeemBalance() {
        return minimumRedeemBalance;
    }

    public void setMinimumRedeemBalance(int minimumRedeemBalance) {
        this.minimumRedeemBalance = minimumRedeemBalance;
    }

    public String getStreamingOpeningStatement() {
        return streamingOpeningStatement;
    }

    public void setStreamingOpeningStatement(String streamingOpeningStatement) {
        this.streamingOpeningStatement = streamingOpeningStatement;
    }

    public SpannableString getUserMatrix() {
        String followerCount = formatValue(followersCount);
        String videoCount = formatValue(totalVideos);
        String viewCount = formatValue(totalViews);
        String matrix = followerCount + " " + pluralise(followersCount, "Follower") + "   " + videoCount + " " + pluralise(totalVideos, "Video") + "   " + viewCount + " " + pluralise(totalViews, "View");
        SpannableString spannable = new SpannableString(matrix);
        AppUtilsKt.INSTANCE.boldFontSizeForPath(spannable, followerCount, Color.WHITE);
        AppUtilsKt.INSTANCE.boldFontSizeForPath(spannable, videoCount, Color.WHITE);
        return spannable;
    }

    public SpannableString getFollowerMatrix() {
        int count = 0;
        if (followersCount != null)
            count = followersCount;
        String followerCount = formatValue(count);
        String matrix = followerCount + "\n " + pluralise(count, "Follower");
        SpannableString spannable = new SpannableString(matrix);
        AppUtilsKt.INSTANCE.boldFontSizeForPath(spannable, followerCount, Color.WHITE);
        spannable.setSpan(new TypefaceSpan("sans-serif-medium"), 0, followerCount.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public SpannableString getVideoMatrix() {
        int count = 0;
        if (totalVideos != null)
            count = totalVideos;
        String videoCount = formatValue(count);
        String matrix = videoCount + "\n " + pluralise(count, "Video");
        SpannableString spannable = new SpannableString(matrix);
        AppUtilsKt.INSTANCE.boldFontSizeForPath(spannable, videoCount, Color.WHITE);
        spannable.setSpan(new TypefaceSpan("sans-serif-medium"), 0, videoCount.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public SpannableString getViewsMatrix() {
        int count = 0;
        if (totalViews != null)
            count = totalViews;
        String viewCount = formatValue(count);
        String matrix = viewCount + "\n " + pluralise(count, "View");
        SpannableString spannable = new SpannableString(matrix);
        AppUtilsKt.INSTANCE.boldFontSizeForPath(spannable, viewCount, Color.WHITE);
        spannable.setSpan(new TypefaceSpan("sans-serif-medium"), 0, viewCount.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public SpannableString getRecentViewSpan() {
        SpannableString spannable = new SpannableString("Recent\n Viewers");
        AppUtilsKt.INSTANCE.boldFontSizeForPath(spannable, "Recent", Color.WHITE);
        return spannable;
    }

    public ProfileDetail getProfileDetail() {
        return profileDetail;
    }

    public void setProfileDetail(ProfileDetail profileDetail) {
        this.profileDetail = profileDetail;
    }

    public LiveStatus getLiveStatus() {
        return liveStatus;
    }

    public void setLiveStatus(LiveStatus liveStatus) {
        this.liveStatus = liveStatus;
    }

    public void setModeratorVotingEnabled(boolean moderatorVotingEnabled) {
        isModeratorVotingEnabled = moderatorVotingEnabled;
    }

    public boolean isModeratorVotingEnabled() {
        return isModeratorVotingEnabled;
    }

    public String getNextLiveTiming() {
        if (profileDetail == null || profileDetail.getGameSchedule() == null)
            return null;
        return profileDetail.getGameSchedule().getNextLiveDay();
    }

    public boolean showStreamerBadge() {
        if (paymentModel == 2)
            return isLevelAssigned;
        else
            return true;
    }

    public ArrayList<GameDetails> getSelectedGames() {
        return selectedGames;
    }

    public void setSelectedGames(ArrayList<GameDetails> selectedGames) {
        this.selectedGames = selectedGames;
    }

    public ChatRoomDetails getActiveChatRooms() {
        return activeChatRooms;
    }
}
