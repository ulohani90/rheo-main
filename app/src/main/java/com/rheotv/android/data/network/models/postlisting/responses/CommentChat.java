package com.rheotv.android.data.network.models.postlisting.responses;

import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppExtensionKt;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import goChat.Services;

public class CommentChat {
    /*
    *
    *  "id": "b85b7fd7-7123-4ed6-85af-cb88f20d904d",
                               "message": "hello world",
                               "username": "saksham",
                               "profile_pic":
    * */

    @SerializedName("id")
    @Expose
    String id;

    @SerializedName("message")
    @Expose
    String message;

    @SerializedName("username")
    @Expose
    String username;

    @SerializedName("profile_pic")
    @Expose
    String profile_pic;

    @SerializedName("message_type")
    @Expose
    String messageType;

    @SerializedName("is_content_moderator")
    @Expose
    boolean isContentModerator;

    int progress;

    Status status;

    public static enum Type {
        Normal,
        JOIN
    }

    Type type = Type.Normal;

    public CommentChat() {
    }

    public CommentChat(String id, String message, String username, String profile_pic, boolean isContentModerator, String messageType) {
        this.id = id;
        this.message = message;
        this.username = username;
        this.profile_pic = profile_pic;
        this.isContentModerator = isContentModerator;
        this.messageType = messageType;
    }

    public CommentChat(String id, String message, String username, String profile_pic, boolean isContentModerator) {
        this.id = id;
        this.message = message;
        this.username = username;
        this.profile_pic = profile_pic;
        this.isContentModerator = isContentModerator;
    }

    public CommentChat(String id, String message, String username, String profile_pic) {
        this(id, message, username, profile_pic, false);
    }

    public CommentChat(String message) {
        this.message = message;
    }

    public CommentChat(String id, String message, String username, String profile_pic, String messageType, Type type) {
        this.id = id;
        this.message = message;
        this.username = username;
        this.profile_pic = profile_pic;
        this.messageType = messageType;
        this.type = type;
    }

    public CommentChat(String message, String username, String profile_pic) {
        this.id = "";
        this.message = message;
        this.username = username;
        this.profile_pic = profile_pic;
        this.messageType = AppConstants.MSG_TYPE_TEXT;
        this.type = Type.Normal;
    }

    public CommentChat(String id, String message, String username, String profile_pic, String messageType) {
        this.id = id;
        this.message = message;
        this.username = username;
        this.profile_pic = profile_pic;
        this.messageType = messageType;
        this.type = Type.Normal;
    }

    public CommentChat(String id, String message, String username, String profile_pic, String messageType, Status status) {
        this.id = id;
        this.message = message;
        this.username = username;
        this.profile_pic = profile_pic;
        this.messageType = messageType;
        this.type = Type.Normal;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public boolean isContentModerator() {
        return isContentModerator;
    }

    public void setContentModerator(boolean contentModerator) {
        isContentModerator = contentModerator;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public SpannableString getSpannableMessage(int color) {
        SpannableString message = new SpannableString(getUsername() + " :" + getMessage());
        message.setSpan(new ForegroundColorSpan(color), 0, getUsername().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return message;
    }

    public String getUsernameText() {
        return getUsername() == null || getUsername().equals("") || getUsername().equals(CommonUtils.getDevId(RheoTvApp.getNonUiContext())) ? "Me" : getUsername();
    }

    public String getStrickerUrl() {
        return hasSticker() ? getMessage() : "";
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public boolean hasSticker() {
        return getMessage() != null && (getMessage().equalsIgnoreCase(AppConstants.MSG_TYPE_STICKER) || getMessage().contains("https://"));
    }

    public boolean hasLocalMedia() {
        return getMessage().startsWith("/storage");
    }

    public boolean isMedia() {
        return !AppConstants.MSG_TYPE_STICKER.equalsIgnoreCase(getMessageType()) && (AppConstants.MSG_TYPE_IMAGE.equalsIgnoreCase(getMessageType()) || AppConstants.MSG_TYPE_VIDEO.equalsIgnoreCase(getMessageType()) ||
                (getMessage() != null && getMessage().contains("https://") || getMessage().startsWith("/storage")));
    }

    public boolean isVideo() {
        return AppConstants.MSG_TYPE_VIDEO.equalsIgnoreCase(getMessageType()) || (getMessage() != null && AppExtensionKt.getMimeType(getMessage()).equalsIgnoreCase(AppConstants.VIDEO));
    }

    public static CommentChat getComment(Services.ChatMessage message) {
        return new CommentChat("", message.getMessage(), message.getSender(), message.getProfilePic(), "true".equalsIgnoreCase(message.getIsContentModerator()), message.getMsgType());
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static class WelcomeComment extends CommentChat {
        private String colouredText;
        private float textSize;
        private int textColor;

        public WelcomeComment(String message, String colouredText, float textSize, int textColor) {
            super(message);
            this.colouredText = colouredText;
            this.textSize = textSize;
            this.textColor = textColor;
        }

        @Override
        public String getUsernameText() {
            return super.getUsername();
        }

        private int getStartIndex() {
            return message != null ? message.indexOf(colouredText) : 0;
        }

        private int getEndIndex() {
            return message != null ? message.indexOf(colouredText) + colouredText.length() : 0;
        }

        public String getColouredText() {
            return colouredText;
        }

        public SpannableString getSpannableMessage() {
            SpannableString spannableString = new SpannableString(message);
            spannableString.setSpan(new ForegroundColorSpan(textColor), getStartIndex(), getEndIndex(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannableString;
        }

        public float getTextSize() {
            return textSize;
        }

        public int getTextColor() {
            return textColor;
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        CommentChat chat = (CommentChat) obj;
        if (chat.username != null && chat.username.equalsIgnoreCase(this.username) &&
                chat.message != null && chat.message.equalsIgnoreCase(this.message)
        ) {
            if (chat.id != null)
                return chat.id.equalsIgnoreCase(this.id);
            else
                return true;
        }
        return false;
    }
}
