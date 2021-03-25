package com.rheotv.android.ui.activities.trim;

import androidx.databinding.ObservableField;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

public class TrimVideoViewModel extends BaseViewModel {
    public ObservableField<String> videoUri = new ObservableField<>();

    public String dirType = "/rheo_stories/";
    public String subPath = System.currentTimeMillis() + "_story.mp4";

    public TrimVideoViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

}
