package com.rheotv.android.ui.activities.story;

import androidx.databinding.ObservableField;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.story.model.Story;

public class CreateStoryVideoViewModel extends BaseViewModel {
    public ObservableField<Story> video = new ObservableField<>();

    public CreateStoryVideoViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }
}
