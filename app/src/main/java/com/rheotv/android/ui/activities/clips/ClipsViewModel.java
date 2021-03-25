package com.rheotv.android.ui.activities.clips;

import android.net.Uri;
import android.util.Log;

import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.login.LoginUserRequest;
import com.rheotv.android.data.network.models.login.LoginUserResponse;
import com.rheotv.android.data.network.models.login.UserNameResult;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClipsViewModel extends BaseViewModel<ClipsNavigator> {


    public ClipsViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }


}
