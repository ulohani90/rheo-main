package com.rheotv.android.ui.activities.player.activity;

public interface StreamPlayerNavigator {
    void checkRewardAvailable();

    void onBlockUserSuccess();

    void onReportUserSuccess();

    void onReportPostSuccess();

    void openLoginFlow(String rewardMessage);

    void trackComment(String message, boolean isSuggestedComment);
}
