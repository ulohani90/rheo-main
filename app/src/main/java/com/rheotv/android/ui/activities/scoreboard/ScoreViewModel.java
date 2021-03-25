package com.rheotv.android.ui.activities.scoreboard;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

public class ScoreViewModel extends BaseViewModel {
    public ScoreViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }
}
