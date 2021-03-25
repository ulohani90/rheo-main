package com.rheotv.android.ui.activities.tabcontainer.posts.multiviewitem.seemore;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

public class SeeMoreViewModel extends BaseViewModel<SeeMoreNavigator> {

    public SeeMoreViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }
}
