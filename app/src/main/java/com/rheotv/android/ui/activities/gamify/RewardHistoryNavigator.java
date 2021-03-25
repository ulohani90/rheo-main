package com.rheotv.android.ui.activities.gamify;

import com.rheotv.android.data.network.models.gamify.RewardHistoryItem;

import java.util.List;

public interface RewardHistoryNavigator {

    void addItemInRewards(List<RewardHistoryItem> rewards);

    void handleError(String error);

}
