package com.rheotv.android.data.network.models.streamUpdates;

import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.play.ResultsItem;
import com.rheotv.android.data.network.models.share.ShareData;
import com.rheotv.android.ui.activities.audioroom.model.OwnerDetail;
import com.rheotv.android.ui.activities.audioroom.model.Participant;
import com.rheotv.android.ui.activities.audioroom.model.SocialGame;
import com.rheotv.android.ui.activities.customroom.model.CustomRoomDetail;

import java.util.List;

public class StreamEventResponse {

    @SerializedName("result")
    private List<StreamEvent> result;

    @SerializedName("post_id")
    private String postId;

    @SerializedName("text")
    private String text;

    @SerializedName("type")
    private String type;

    @SerializedName("target_url")
    private String targetUrl;

    @SerializedName("user_id")
    private Integer userId;

    @SerializedName("play_request")
    private ResultsItem playRequest;

    @SerializedName("share_data")
    private ShareData shareData;

    @SerializedName("customroom")
    private CustomRoomDetail customRoomDetail;

    @SerializedName("username")
    private String username;

    @SerializedName("action")
    private String action;

    @SerializedName("action_username")
    private String actionUserName;

    @SerializedName("participant")
    private Participant participant;

    @SerializedName("highlighted")
    private List<Participant> highlighterRoomUser;

    @SerializedName("social_game")
    private SocialGame socialGame;

    @SerializedName("user_profile_url")
    private String userProfileUrl;

    @SerializedName("game_name")
    private String gameName;

    @SerializedName("state")
    private String state;

    @SerializedName("channel_id")
    private String channelId;

    @SerializedName("title")
    private String title;

    @SerializedName("user_name")
    private String usernameForCohost;

    @SerializedName("requester_agora_token")
    private String requesterAgoraToken;

    public void setResult(List<StreamEvent> result) {
        this.result = result;
    }

    public List<StreamEvent> getResult() {
        return result;
    }

    public StreamEventResponse() {

    }

    public StreamEventResponse(String text, ShareData shareData) {
        this.text = text;
        this.shareData = shareData;
    }

    public StreamEventResponse(String type, String text) {
        this.type = type;
        this.text = text;
    }

    public StreamEventResponse(String postId, String text, String type, List<StreamEvent> result) {
        this.result = result;
        this.postId = postId;
        this.text = text;
        this.type = type;
    }


    public StreamEventResponse(String postId, String action, Participant participant) {
        this.postId = postId;
        this.action = action;
        this.participant = participant;
    }

    public String getUserProfileUrl() {
        return userProfileUrl;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostId() {
        return postId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public StreamEvent getFirstEvent() {
        return result.get(0);
    }

    public Integer getUserId() {
        return userId == null ? -1 : userId;
    }

    public ResultsItem getPlayRequest() {
        return playRequest;
    }

    public void setPlayRequest(ResultsItem playRequest) {
        this.playRequest = playRequest;

    }

    public String getRequesterAgoraToken() {
        return requesterAgoraToken;
    }

    public String getTitle() {
        return title;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getState() {
        return state;
    }

    public CustomRoomDetail getCustomRoomDetail() {
        return customRoomDetail;
    }

    public void setCustomRoomDetail(CustomRoomDetail customRoomDetail) {
        this.customRoomDetail = customRoomDetail;
    }

    public ShareData getShareData() {
        return shareData;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public String getActionUserName() {
        return actionUserName;
    }

    public void setActionUserName(String actionUserName) {
        this.actionUserName = actionUserName;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getUsernameForCohost() {
        return usernameForCohost;
    }

    public List<Participant> getHighlighterRoomUser() {
        return highlighterRoomUser;
    }

    public void setHighlighterRoomUser(List<Participant> highlighterRoomUser) {
        this.highlighterRoomUser = highlighterRoomUser;
    }

    public SocialGame getSocialGame() {
        return socialGame;
    }

    public void setSocialGame(SocialGame socialGame) {
        this.socialGame = socialGame;
    }

    @Override
    public String toString() {
        return "StreamEventResponse{" +
                "result = '" + result + '\'' +
                ",post_id = '" + postId + '\'' +
                ",text = '" + text + '\'' +
                ",type = '" + type + '\'' +
                ",userId = '" + userId + '\'' +
                ",target_url = '" + targetUrl + '\'' +
                ",play_request = '" + playRequest + "'" +
                ",share_data = '" + shareData + '\'' +
                ",action = '" + action + '\'' +
                ",action_username = '" + actionUserName + '\'' +
                ",participant = '" + participant + '\'' +
                "}";
    }
}