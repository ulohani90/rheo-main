package com.rheotv.android.ui.activities.player.activity;

public interface PlayRequestListener {

    void onPlayRequest(String gameUserName);

    void onAction(String requestId, String action, String userName, String gameUserName, String profileUrl);

    void onPlayerClick(String requestId, String userName, String gameUserName, String profileUrl, boolean isAccepted);

    void onSubmitCustomRoomDetailsClick(String roomId, String roomPass, boolean isEdit);
    
    void recordSegmentAction(String event);

    void onRoomDetailsCopied(String gameName, boolean isRoomId);
}
