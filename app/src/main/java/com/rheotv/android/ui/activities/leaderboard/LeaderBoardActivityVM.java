package com.rheotv.android.ui.activities.leaderboard;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

public class LeaderBoardActivityVM extends BaseViewModel<LeaderBoardNavigator> {
    public LeaderBoardActivityVM(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }
}
