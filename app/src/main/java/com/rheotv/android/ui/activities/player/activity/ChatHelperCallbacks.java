package com.rheotv.android.ui.activities.player.activity;

import goChat.Services;

public interface ChatHelperCallbacks {

    void onMessageSend(Services.ChatMessage chatMessage);

    void onMessageDelete(Services.ChatMessage chatMessage);

    void waitAndReconnect();

    void updateLiveCount(String liveCount);

    void setUpViewersRequest();

    void showToast(String message);

    void onConnectionComplete();
}
