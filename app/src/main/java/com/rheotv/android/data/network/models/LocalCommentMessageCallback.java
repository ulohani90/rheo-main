package com.rheotv.android.data.network.models;

import goChat.Services;

public interface LocalCommentMessageCallback {
    void ownMessageSent(Services.ChatMessage chatMessage);
}
