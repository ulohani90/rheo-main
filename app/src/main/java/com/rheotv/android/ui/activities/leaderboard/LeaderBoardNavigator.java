package com.rheotv.android.ui.activities.leaderboard;

public interface LeaderBoardNavigator {
    void handleError(String error);

    String getGameId();

    void clearLeaderboardItems();

    void setRefreshing(boolean isRefreshing);
}
