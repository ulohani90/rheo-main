package com.rheotv.android.ui.customViews.streamPlayer;

public interface StreamPlayerCallbackListener {

    void streamEnded();

    void onSettingViewClick();

    void onCloseViewClick();

    void onShareViewClick();

    void onStickerViewClick();

    void onHeartViewClick();

    void onGiftViewClick();

    void onChatViewClick();

    void onFollowStreamViewClick();

    void onStreamProfileClick();

    void onControllerVisibilityChange(Boolean isVisible);

    void onFlagBtnClick();
}
