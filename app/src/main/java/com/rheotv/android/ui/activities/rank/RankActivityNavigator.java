package com.rheotv.android.ui.activities.rank;

import com.rheotv.android.data.network.models.useProfile.responses.StreamerLevelResponseBody;

public interface RankActivityNavigator {

    void setStreamerLevelInfo(StreamerLevelResponseBody response);

    void showErrorToast(String message);
}
