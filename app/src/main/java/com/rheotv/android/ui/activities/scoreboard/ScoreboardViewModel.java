package com.rheotv.android.ui.activities.scoreboard;

import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.score.TeamsListItem;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.List;

public class ScoreboardViewModel extends BaseViewModel {
    MutableLiveData<List<TeamsListItem>> list = new MutableLiveData<>();
    MutableLiveData<String> unit = new MutableLiveData<>();

    public ScoreboardViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

}
