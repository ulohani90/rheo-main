package com.rheotv.android.ui.activities.story;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

public class CreateTemplateViewModel extends BaseViewModel {
    public CreateTemplateViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }
}
