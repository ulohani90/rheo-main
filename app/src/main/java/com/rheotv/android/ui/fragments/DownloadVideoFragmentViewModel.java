package com.rheotv.android.ui.fragments;

import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.rx.SchedulerProvider;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadVideoFragmentViewModel extends BaseViewModel<DownloadVideoFragmentNavigator> {
    public MutableLiveData<Status> downloadComplete = new MutableLiveData<>();

    public DownloadVideoFragmentViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void submitDownloadVideoRequest(String postId, String videoQuality) {
        downloadComplete.setValue(Status.LOADING);
        getDataManager().downloadVideo(postId, videoQuality).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null && response.body() != null) {
                    downloadComplete.setValue(Status.SUCCESS);
                } else {
                    downloadComplete.setValue(Status.ERROR);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                downloadComplete.setValue(Status.ERROR);
            }
        });
    }
}
