package com.rheotv.android.ui.activities.rank;

import com.rheotv.android.data.network.models.useProfile.responses.AchievementsData;

import java.util.List;

public interface RankListNavigator {
    void setRewardData(List<AchievementsData> list);
}
