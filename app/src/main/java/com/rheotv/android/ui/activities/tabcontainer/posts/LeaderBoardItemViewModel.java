package com.rheotv.android.ui.activities.tabcontainer.posts;


import androidx.databinding.ObservableField;

import com.rheotv.android.helpers.AnalyticsHelper;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class LeaderBoardItemViewModel {
    public ObservableField<String> title = new ObservableField<String>();
    public ObservableField<String> description = new ObservableField<String>();
    private LeaderBoardItemViewModelListener leaderBoardItemViewModelListener;

    public LeaderBoardItemViewModel(LeaderBoardItemViewModelListener leaderBoardItemViewModelListener) {
        this.leaderBoardItemViewModelListener = leaderBoardItemViewModelListener;
    }

    public void onCardClicked() {
        leaderBoardItemViewModelListener.onLeaderBoardCardClicked("");
        AnalyticsHelper.getInstance(getNonUiContext()).sendLeaderboardClicked();
    }

    public void setData(String titleText, String description) {
        title.set(titleText);
        this.description.set(description);
    }

    public interface LeaderBoardItemViewModelListener {
        void onLeaderBoardCardClicked(String id);
    }
}
