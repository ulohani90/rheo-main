package com.rheotv.android.ui.activities.tabcontainer.profile.analytics;


import android.util.Log;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.StreamerData;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalyticsFragmentViewModel extends BaseViewModel<AnalyticsFragmentNavigator> {

    public final ObservableField<StreamerData> authorProfileData = new ObservableField<>();
    private final MutableLiveData<StreamerData> mAuthorProfileData;


    public AnalyticsFragmentViewModel(DataManager dataManager,
                                      SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        mAuthorProfileData = new MutableLiveData<>();
    }

    public void getStreamerData(String authorUserName, String sortType) {
        setIsLoading(true);
        Log.i(getClass().getName(), "getStreamerData 1");
        getDataManager().getStreamerData(authorUserName, sortType).enqueue(new Callback<StreamerData>() {
            @Override
            public void onResponse(Call<StreamerData> call, Response<StreamerData> response) {
//                Log.i(getClass().getName(), "getStreamerData " + new Gson().toJson(response));
                if (response.body() != null && getNavigator() != null) {
                    authorProfileData.set(response.body());
                    mAuthorProfileData.setValue(response.body());
//                    Log.i(getClass().getName(), "getStreamerData " + new Gson().toJson(response));
                }
            }

            @Override
            public void onFailure(Call<StreamerData> call, Throwable t) {
                Log.i(getClass().getName(), "getStreamerData fail " + t.getMessage());
                if (getNavigator() != null) {
                    Log.d("mirage", "fetching profile failed. Probably not loggedIn");
                    getNavigator().throwError();
                }
            }
        });
    }

    public void updateProfileData(StreamerData data) {
        authorProfileData.set(data);
    }

    public MutableLiveData<StreamerData> getProfileData() {
        return mAuthorProfileData;
    }
}

