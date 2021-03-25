
/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 7:07 PM
 *
 */

package com.rheotv.android.data.network.models.postlisting.responses;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.objects.StreamerObject;
import com.rheotv.android.data.network.models.sportsScore.Match;
import com.rheotv.android.data.network.models.vote.Participant;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.TimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Result {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("author")
    @Expose
    private com.rheotv.android.data.network.models.postlisting.responses.Author author;
    @SerializedName("video_url")
    @Expose
    private String videoUrl;

    @SerializedName("video_urls")
    @Expose
    private List<VideoUrlObj> videoUrls;

    @SerializedName("role")
    @Expose
    private int role;
    @SerializedName("slug")
    @Expose
    private String slug;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("banner_image")
    @Expose
    private String bannerImageUrl;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("duration")
    @Expose
    private float duration = 0;
    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;
    @SerializedName("share_thumbnail")
    @Expose
    private String shareThumbnail;

    @SerializedName("carousel_thumbnail")
    @Expose
    private String carouselThumbnail;

    @SerializedName("is_featured")
    @Expose
    private boolean isFeatured;

    @SerializedName("is_followed")
    @Expose
    private boolean isFollowed;

    @SerializedName("region_name")
    @Expose
    private String regionName;
    @SerializedName("rating")
    @Expose
    private int rating;
    @SerializedName("language")
    @Expose
    private String language;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("share_url")
    @Expose
    private String shareUrl;
    @SerializedName("story_address")
    @Expose
    private com.rheotv.android.data.network.models.postlisting.responses.StoryAddress storyAddress;
    @SerializedName("current_address")
    @Expose
    private com.rheotv.android.data.network.models.postlisting.responses.CurrentAddress currentAddress;
    @SerializedName("total_views")
    @Expose
    private int totalViews;
    @SerializedName("gist_url")
    @Expose
    private String gistUrl;
    @SerializedName("total_likes")
    @Expose
    private int totalLikes;
    @SerializedName("total_downloads")
    @Expose
    private int totalDownloads;
    @SerializedName("total_shares")
    @Expose
    private int totalShares;
    @SerializedName("total_facebook_shares")
    @Expose
    private String totalFacebookShares;
    @SerializedName("is_liked")
    @Expose
    private boolean isLiked;
    @SerializedName("type")
    @Expose
    private int type;
    @SerializedName("youtube_video_id")
    @Expose
    private String youtubeVideoId;
    @SerializedName("start_time")
    @Expose
    private String startTime;
    @SerializedName("end_time")
    @Expose
    private Object endTime;

    @SerializedName("hashtags")
    @Expose
    private List<String> hashtags = null;

    @SerializedName("comments")
    @Expose
    private com.rheotv.android.data.network.models.postlisting.responses.Comments comments;

    @SerializedName("is_live")
    @Expose
    private boolean isLive;
    @SerializedName("start_from")
    @Expose
    private Integer startFrom;
    @SerializedName("results")
    @Expose
    private List<Result> results;

    @SerializedName("volume")
    @Expose
    private String volume;

    private boolean isPlaying;

    @SerializedName("participants")
    @Expose
    private List<Participant> participants;

    @SerializedName("match")
    @Expose
    private Match match;

    @SerializedName("game")
    @Expose
    private String game;

    @SerializedName("game_id")
    @Expose
    private String game_id;

    @SerializedName("count")
    @Expose
    private int count;

    @SerializedName("month")
    String month;
    @SerializedName("amount")
    String amount;

    @SerializedName("clap")
    @Expose
    boolean isClapped;

    @SerializedName("thumbnail_url")
    @Expose
    private String thumbnailUrl;

    @SerializedName("clap_count")
    @Expose
    private int clapCount;

    @SerializedName("cover_pic")
    @Expose
    private String gameImageUrl;

    @SerializedName("name")
    @Expose
    private String gameName;

    @SerializedName("can_download_video")
    @Expose
    private boolean canDownloadVideo;

    @SerializedName("pinned_comment")
    @Expose
    private CommentChat pinnedComment;

    @SerializedName("post_gifts")
    @Expose
    private List<PostGift> postGifts;

    @SerializedName("post_share_text")
    @Expose
    private String postShareText;

    @SerializedName("is_share_data_generated")
    @Expose
    private boolean isShareDataGenerated;

    @SerializedName("comment_suggestions")
    @Expose
    private ArrayList<String> commentSuggestions;

    @SerializedName("total_call_count")
    @Expose
    private int totalCallCount;

    public CommentChat getPinnedComment() {
        return pinnedComment;
    }

    public void setPinnedComment(CommentChat pinnedComment) {
        this.pinnedComment = pinnedComment;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    // invoices part goes here
    @SerializedName("invoices")
    @Expose
    private com.rheotv.android.data.network.models.postlisting.responses.Invoice invoice;

    @SerializedName("start_date")
    @Expose
    private String startDate;

    @SerializedName("end_date")
    @Expose
    private String endDate;

    @SerializedName("rules")
    @Expose
    private List<String> rules;

    @SerializedName("prizes")
    @Expose
    private List<Prize> prizes;

    @SerializedName("streamerObjects")
    @Expose
    private List<StreamerObject> streamerObjects;

    @SerializedName("video_mode")
    @Expose
    private String videoMode;

    @SerializedName("promo_video_url")
    @Expose
    private String promoVideoUrl;

    @SerializedName("heart_count")
    @Expose
    private String heartCount;

    @SerializedName("min_viewers")
    @Expose
    private int minViewers;

    @SerializedName("accept_play_request")
    @Expose
    private boolean canRequestPlay;

    @SerializedName("live_watchers_count")
    @Expose
    private Integer watchingCount;

    @SerializedName("feature_custom_room")
    @Expose
    private boolean featureCustomRoom;

    @SerializedName("custom_room_enabled")
    @Expose
    private boolean customRoomEnabled;

    @SerializedName("is_video_call_enabled")
    @Expose
    private boolean isVideoCallEnabled;

    @SerializedName("custom_room_detail_url")
    @Expose
    private String customRoomDetailUrl;

    @SerializedName("show_gift_icon")
    @Expose
    private boolean showRewardIcon = false;

    @SerializedName("intro_video_url")
    @Expose
    private String introVideoUrl;

    @SerializedName("game_rules_video_url")
    @Expose
    private String gamesRuleVideoUrl;

    @SerializedName("show_intro")
    @Expose
    private boolean showIntro;

    public boolean isShowIntro() {
        return showIntro;
    }

    public String getIntroVideoUrl() {
        return introVideoUrl;
    }

    public String getGamesRuleVideoUrl() {
        return gamesRuleVideoUrl;
    }

    public boolean isVideoCallEnabled() {
        return isVideoCallEnabled;
    }

    public void setVideoCallEnabled(boolean videoCallEnabled) {
        isVideoCallEnabled = videoCallEnabled;
    }

    public String getCustomRoomDetailUrl() {
        return customRoomDetailUrl;
    }

    public String getVideoMode() {
        return videoMode;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getCarouselThumbnail() {
        return carouselThumbnail;
    }

    public void setCarouselThumbnail(String carouselThumbnail) {
        this.carouselThumbnail = carouselThumbnail;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getGameId() {
        return game_id;
    }

    public void setGameId(String game_id) {
        this.game_id = game_id;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }


    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public com.rheotv.android.data.network.models.postlisting.responses.Author getAuthor() {
        return author;
    }

    public void setAuthor(com.rheotv.android.data.network.models.postlisting.responses.Author author) {
        this.author = author;
    }

    public List<VideoUrlObj> getVideoUrls() {
        return videoUrls;
    }

    public VideoUrlObj getAudioUrls() {
        for (VideoUrlObj object : videoUrls) {
            if (object.getNetworkType().equalsIgnoreCase("audio"))
                return object;
        }
        return null;
    }

    public void setVideoUrls(List<VideoUrlObj> videoUrl) {
        this.videoUrls = videoUrls;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getDuration() {
        return duration;
    }

    public float getDurationInSeconds() {
        return getDuration() * 60;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<Prize> getPrizes() {
        return prizes;
    }

    public List<String> getRules() {
        return rules;
    }

    public String getFormattedDuration(String duration) {
        try {
            long _SECONDS = Long.parseLong(duration);
            if (TimeUnit.SECONDS.toHours(_SECONDS) > 0) {
                return String.format("%02d:%02d:%02d",
                        TimeUnit.SECONDS.toHours(_SECONDS),
                        TimeUnit.SECONDS.toMinutes(_SECONDS) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(_SECONDS)),
                        TimeUnit.SECONDS.toSeconds(_SECONDS) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(_SECONDS)));
            } else {
                return String.format("%02d:%02d",
                        TimeUnit.SECONDS.toMinutes(_SECONDS) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(_SECONDS)),
                        TimeUnit.SECONDS.toSeconds(_SECONDS) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(_SECONDS)));
            }
        } catch (NumberFormatException nfe) {
            Log.d("parse_duration", nfe.getMessage());
        }
        return "";
    }

    public String getShareThumbnail() {
        return shareThumbnail;
    }

    public void setShareThumbnail(String shareThumbnail) {
        this.shareThumbnail = shareThumbnail;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getoFormattedCreatedAt() {
        return getFormattedDate(createdAt);
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getLeftOutTime() {
        String publishTime;
        if (getStartTime() != null)
            publishTime = getStartTime();
        else
            publishTime = getCreatedAt();
        int days = TimeUtils.getDaysDifference(publishTime);
        int hours = TimeUtils.getHoursDifference(publishTime);
        hours = hours % 24;

        int minutes = TimeUtils.getMinDifference(publishTime);
        minutes = minutes % 60;

        String leftTime = CommonUtils.getPlural("Day", days);

        if (leftTime.isEmpty()) {
            leftTime = CommonUtils.getPlural("Hour", hours);
        } else {
            leftTime = leftTime + " " + CommonUtils.getPlural("Hour", hours);
            leftTime = leftTime.trim();
        }

        if (days == 0) {
            if (leftTime.isEmpty()) {
                leftTime = CommonUtils.getPlural("Minute", minutes);
            } else {
                leftTime = leftTime + " " + CommonUtils.getPlural("Minute", minutes);
                leftTime = leftTime.trim();
            }
        }

        String leftOutTimeString;

        if (getIsLive()) {
            leftOutTimeString = "Streaming for " + leftTime;
        } else {
            leftOutTimeString = "Streamed " + leftTime + " ago.";
        }
        return leftOutTimeString;
    }

    public String getTimeLeftOut() {
        String publishTime;
        if (getStartTime() != null)
            publishTime = getStartTime();
        else
            publishTime = getCreatedAt();
        int days = TimeUtils.getDaysDifference(publishTime);
        int hours = TimeUtils.getHoursDifference(publishTime);
        hours = hours % 24;

        int minutes = TimeUtils.getMinDifference(publishTime);
        minutes = minutes % 60;

        String leftTime = "";
        if (days > 0)
            leftTime += days + "d";

        if (hours > 0) {
            if (leftTime.isEmpty()) {
                leftTime = hours + "h";
            } else {
                leftTime = leftTime + " " + hours + "h";
                leftTime = leftTime.trim();
            }
        }

        if (days == 0) {
            if (leftTime.isEmpty()) {
                leftTime = minutes + "m";
            } else {
                leftTime = leftTime + " " + minutes + "m";
                leftTime = leftTime.trim();
            }
        }

        String leftOutTimeString = "";

        if (getIsLive()) {
            leftOutTimeString = "Since " + leftTime;
        } else {
            leftOutTimeString = "Streamed " + leftTime + " ago.";
        }
        return leftOutTimeString;
    }

    public boolean isCustomRoomEnabled() {
        return customRoomEnabled;
    }

    public void setCustomRoomEnabled(boolean customRoomEnabled) {
        this.customRoomEnabled = customRoomEnabled;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public com.rheotv.android.data.network.models.postlisting.responses.StoryAddress getStoryAddress() {
        return storyAddress;
    }

    public void setStoryAddress(com.rheotv.android.data.network.models.postlisting.responses.StoryAddress storyAddress) {
        this.storyAddress = storyAddress;
    }

    public com.rheotv.android.data.network.models.postlisting.responses.CurrentAddress getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(com.rheotv.android.data.network.models.postlisting.responses.CurrentAddress currentAddress) {
        this.currentAddress = currentAddress;
    }

    public int getTotalViews() {
        return totalViews;
    }


    public void setTotalViews(int totalViews) {
        this.totalViews = totalViews;
    }

    public String getGistUrl() {
        return gistUrl;
    }

    public void setGistUrl(String gistUrl) {
        this.gistUrl = gistUrl;
    }

    public String getTotalLikes() {
        return Integer.toString(totalLikes);
    }

    public void setTotalLikes(int totalLikes) {
        this.totalLikes = totalLikes;
    }

    public String getTotalDownloads() {
        return Integer.toString(totalDownloads);
    }

    public void setTotalDownloads(int totalDownloads) {
        this.totalDownloads = totalDownloads;
    }

    public long getStartFrom() {
        return startFrom * 1000L;
    }

    public long startFrom() {
        if (startFrom == null)
            return 0;
        else
            return startFrom;
    }

    public void setStartFrom(int startFrom) {
        this.startFrom = startFrom;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> hashtags) {
        this.results = results;
    }

    public String getTotalShares() {
        return Integer.toString(totalShares);
    }

    public void setTotalShares(int totalShares) {
        this.totalShares = totalShares;
    }

    public String getTotalFacebookShares() {
        return totalFacebookShares != null ? totalFacebookShares : "0";
    }

    public void setTotalFacebookShares(String totalFacebookShares) {
        this.totalFacebookShares = totalFacebookShares;
    }

    public boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getYoutubeVideoId() {
        return youtubeVideoId;
    }

    public void setYoutubeVideoId(String youtubeVideoId) {
        this.youtubeVideoId = youtubeVideoId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Object getEndTime() {
        return endTime;
    }

    public void setEndTime(Object endTime) {
        this.endTime = endTime;
    }

    public boolean getIsLive() {
        return isLive;
    }

    public void setIsLive(boolean isLive) {
        this.isLive = isLive;
    }

    public String getFormattedDate(String dateString) {
        if (TextUtils.isEmpty(dateString)) {
            return "";
        }
        TimeUtils.getFormattedHindiDate(dateString);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        ;
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date date = null;
        try {
            date = format.parse(dateString);
            long millis = date.getTime();
            return getTimeAgo(millis);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static Date currentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    private static String getTimeAgo(long time) {
        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = currentDate().getTime();
        if (time > now || time <= 0) {
            return "in the future";
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "Moments ago";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "A minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "An hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "Yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }


    public List<com.rheotv.android.data.network.models.postlisting.responses.CommentChat> getLiveChat() {
        if (comments == null || comments.results == null) {
            return new ArrayList<com.rheotv.android.data.network.models.postlisting.responses.CommentChat>();
        }
        return comments.results;
    }


    public String getShareMessageBody(Context context) {
        String appName = context.getString(R.string.app_name);
        //String sampleMessge = "Watch Saksham playing pubg live only on Gamies Live! ";
        String messageBody = "Watch " + ((author != null && author.getUser() != null) ? author.getUser().getUsername() : "player") + " playing " + game + " live only on " + appName + "!!!";
        messageBody = messageBody + "\n" + getShareUrl();
        return messageBody;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public com.rheotv.android.data.network.models.postlisting.responses.Invoice getInvoices() {
        return invoice;
    }

    public void setInvoices(com.rheotv.android.data.network.models.postlisting.responses.Invoice invoices) {
        this.invoice = invoices;
    }

    public List<StreamerObject> getStreamerObjects() {
        return streamerObjects;
    }

    public void setStreamerObjects(List<StreamerObject> streamerObjects) {
        this.streamerObjects = streamerObjects;
    }

    public String getPromoVideoUrl() {
        return promoVideoUrl;
    }

    public void setPromoVideoUrl(String promoVideoUrl) {
        this.promoVideoUrl = promoVideoUrl;
    }

    public boolean isClapped() {
        return isClapped;
    }

    public void setClapped(boolean clapped) {
        isClapped = clapped;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public int getClapCount() {
        return clapCount;
    }

    public void setClapCount(int clapCount) {
        this.clapCount = clapCount;
    }


    public String getGameImageUrl() {
        return gameImageUrl;
    }

    public void setGameImageUrl(String gameImageUrl) {
        this.gameImageUrl = gameImageUrl;
    }

    public String getGameName() {
        return gameName;
    }

    public String getHeartCount() {
        return heartCount;
    }

    public long getHearts() {
        return heartCount == null ? 0 : Long.valueOf(heartCount);
    }

    public void setHeartCount(String heartCount) {
        this.heartCount = heartCount;
    }

    public boolean isCanDownloadVideo() {
        return canDownloadVideo;
    }

    public int getMinViewers() {
        return minViewers;
    }

    public boolean canRequestPlay() {
        return canRequestPlay;
    }

    public boolean isFeatureCustomRoom() {
        return featureCustomRoom;
    }


    public void setCanRequestPlay(boolean canRequestPlay) {
        this.canRequestPlay = canRequestPlay;
    }

    public Integer getWatchingCount() {
        return watchingCount == null ? 0 : watchingCount;
    }

    public void setWatchingCount(Integer watchingCount) {
        this.watchingCount = watchingCount;
    }

    public List<PostGift> getPostGifts() {
        return postGifts;
    }

    public String getStreamingDuration() {
        if (TimeUtils.hasStreamNotStarted(getStartFrom())) {
            return null;
        } else {
            return getTimeLeftOut();
        }
    }

    public String getPostShareText() {
        return postShareText;
    }

    public void setPostShareText(String postShareText) {
        this.postShareText = postShareText;
    }

    public boolean isShareDataGenerated() {
        return isShareDataGenerated;
    }

    public ArrayList<String> getCommentSuggestions() {
        return commentSuggestions;
    }

    public boolean isStreamer() {
        return getAuthor() != null && getAuthor().getUser() != null && CommonUtils.getUserName().equalsIgnoreCase(getAuthor().getUser().getUsername());
    }

    public int getTotalCallCount() {
        return totalCallCount;
    }

    public void setTotalCallCount(int totalCallCount) {
        this.totalCallCount = totalCallCount;
    }

    public boolean isRewardIconEnabled() {
        return showRewardIcon;
    }

    public void setShowRewardIcon(boolean showRewardIcon) {
        this.showRewardIcon = showRewardIcon;
    }

    public class Prize {
        @SerializedName("name")
        @Expose
        String prizeName;

        @SerializedName("value")
        @Expose
        String value;

        public String getPrizeName() {
            return prizeName;
        }

        public void setPrizeName(String prizeName) {
            this.prizeName = prizeName;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
