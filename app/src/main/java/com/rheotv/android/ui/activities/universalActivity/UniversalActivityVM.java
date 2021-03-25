package com.rheotv.android.ui.activities.universalActivity;

import android.net.Uri;
import android.util.Log;

import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.login.LoginUserRequest;
import com.rheotv.android.data.network.models.login.LoginUserResponse;
import com.rheotv.android.data.network.models.login.UserNameResult;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.ui.activities.universalActivity.fragment.UniversalFragmentNavigator;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UniversalActivityVM extends BaseViewModel<UniversalFragmentNavigator> {

    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    public UniversalActivityVM(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

}