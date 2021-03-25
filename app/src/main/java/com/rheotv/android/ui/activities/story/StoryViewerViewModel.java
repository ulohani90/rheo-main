package com.rheotv.android.ui.activities.story;

import android.util.Log;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.postlisting.responses.Author;
import com.rheotv.android.data.network.models.postlisting.responses.LeaderboardResponse;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.activities.leaderboard.FollowListenerCallback;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.rx.SchedulerProvider;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoryViewerViewModel extends BaseViewModel {
    public ObservableField<String> watchCount = new ObservableField<>();
    public String storyId = null;
    public String sourceScreen = null;
    public String nextUrl = null;

    public MutableLiveData<ArrayList<Author>> authorList = new MutableLiveData<>();
    public ObservableField<Status> loadingStatus = new ObservableField<>();

    public StoryViewerViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    void loadViewers(boolean showLoading) {
        if (showLoading) loadingStatus.set(Status.LOADING);
        getDataManager().loadStoryViewers(storyId, nextUrl).enqueue(new Callback<LeaderboardResponse>() {
            @Override
            public void onResponse(Call<LeaderboardResponse> call, Response<LeaderboardResponse> response) {
                Log.i(getClass().getSimpleName(), "loadFollowers: " + new Gson().toJson(response));
                if (response.isSuccessful() && response.body() != null) {
                    authorList.setValue(new ArrayList<>(response.body().getAuthors()));
                    nextUrl = response.body().getNext();
                    if (showLoading) loadingStatus.set(Status.SUCCESS);
                } else {
                    if (showLoading) loadingStatus.set(Status.ERROR);
                }
            }

            @Override
            public void onFailure(Call<LeaderboardResponse> call, Throwable t) {
                if (showLoading) loadingStatus.set(Status.ERROR);
            }
        });
    }

    void followUnFollow(boolean isfollowing, String profileId, FollowListenerCallback callback) {
        Log.i(getClass().getSimpleName(), "followUnFollow " + profileId);
        if (isfollowing) {
            getDataManager()
                    .unFollowAuthor(profileId)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
                                if (callback != null)
                                    callback.onToggleFollow(false);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            //do nothing
                            if (callback != null) {
                                callback.onFail();
                            }
                            Log.e(getClass().getSimpleName(), "followUnFollow_1: " + t.getMessage());
                        }
                    });
        } else {
            getDataManager()
                    .followAuthor(profileId)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                            Log.e(getClass().getSimpleName(), "followUnFollow_2: " + new Gson().toJson(response));
                            if (response.isSuccessful()) {
                                EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
                                if (callback != null)
                                    callback.onToggleFollow(true);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            //do nothing
                            if (callback != null) {
                                callback.onFail();
                            }

                            Log.e(getClass().getSimpleName(), "followUnFollow_3: " + t.getMessage());
                        }
                    });
        }
    }
}
