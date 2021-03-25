package com.rheotv.android.data.network.models.postlisting.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rheotv.android.data.network.models.postlisting.responses.support.ChatModel;

import java.util.List;

public class SupportChatResponse {
    @SerializedName("chat_items")
    @Expose
    private List<ChatModel> commentChatList;

    public List<ChatModel> getCommentChatList() {
        return commentChatList;
    }

    public void setCommentChatList(List<ChatModel> commentChatList) {
        this.commentChatList = commentChatList;
    }
}
