package com.rheotv.android.ui.activities.tabcontainer;


import com.rheotv.android.data.network.models.general.GameDetails;
import com.rheotv.android.data.network.models.general.RTMPDetails;

import java.util.List;

public interface TabContainerNavigator {

    void openPlayStoreLink();

    void openWhatsappForVideoUpload();

    void goLiveClicked(String source);

    void updateGameList(List<GameDetails> response);

    void updateRTMPDetails(RTMPDetails rtmpDetails);

    void showToast(String message);

    void goSearchClick(String toolbar);

    void viewClipsScreen();

    void viewTotalCoins();

    void checkRewardAvailable();

    void onRewardStreakClick();

    void onLeaderBoardClick();

    void showForceUpdateDialog();

    void showUpdateOptions();
}
