package com.rheotv.android.ui.activities.scoreboard;

import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

public class PastMatchViewModel extends BaseViewModel {
    MutableLiveData<ArrayList<String>> list = new MutableLiveData<>();

    public PastMatchViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void loadPastMatches() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            list.add(i + "");

        this.list.setValue(list);
    }
}
