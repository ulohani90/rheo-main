package com.rheotv.android.ui.activities.follower;

import android.util.Log;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.postlisting.responses.Author;
import com.rheotv.android.data.network.models.postlisting.responses.LeaderboardResponse;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.data.network.models.useProfile.responses.RecentViewer;
import com.rheotv.android.data.network.models.useProfile.responses.RecentViewersResponse;
import com.rheotv.android.ui.activities.leaderboard.FollowListenerCallback;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.rx.SchedulerProvider;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowerViewModel extends BaseViewModel {
    public String username = null;
    public String type = null;
    public String nextUrl = null;
    public String sourceScreen = null;
    public String screenName = null;

    public MutableLiveData<ArrayList<Author>> authorList = new MutableLiveData<>();
    public MutableLiveData<ArrayList<RecentViewer>> recentViewers = new MutableLiveData<>();
    public ObservableField<Status> loadingStatus = new ObservableField<>();
    public ObservableField<String> emptyMessage = new ObservableField<>();
    public ObservableField<Boolean> showStreamerButton = new ObservableField<>();

    public FollowerViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void loadFollowers(boolean showLoading) {
        if (username == null || type == null) return;
        if (showLoading) loadingStatus.set(Status.LOADING);
        getDataManager().loadFollowUser(username, type, nextUrl).enqueue(new Callback<LeaderboardResponse>() {
            @Override
            public void onResponse(Call<LeaderboardResponse> call, Response<LeaderboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    authorList.setValue(new ArrayList<>(response.body().getAuthors()));
                    nextUrl = response.body().getNext();
                    if (showLoading) {
                        if (response.body().getAuthors().isEmpty() && response.body().getNext() == null && response.body().getPrevious() == null) {
                            loadingStatus.set(Status.EMPTY);
                            updateEmptyView();
                        } else
                            loadingStatus.set(Status.SUCCESS);
                    }

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

    public void loadRecentViewers(boolean showLoading) {
        if (username == null || type == null) return;
        if (showLoading) loadingStatus.set(Status.LOADING);
        getDataManager().getRecentViewers().enqueue(new Callback<RecentViewersResponse>() {
            @Override
            public void onResponse(Call<RecentViewersResponse> call, Response<RecentViewersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recentViewers.setValue(new ArrayList<>(response.body().getResult()));
                    if (showLoading) {
                        if (response.body().getResult().isEmpty()) {
                            loadingStatus.set(Status.EMPTY);
                            updateEmptyView();
                        } else
                            loadingStatus.set(Status.SUCCESS);
                    }

                } else {
                    if (showLoading) loadingStatus.set(Status.ERROR);
                }
            }

            @Override
            public void onFailure(Call<RecentViewersResponse> call, Throwable t) {

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
//                                Log.e(getClass().getSimpleName(), "followUnFollow_0: " + new Gson().toJson(response));
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

    private void updateEmptyView() {
        emptyMessage.set(type.equalsIgnoreCase(AppConstants.TYPE_FOLLOWER) ? "No Followers" : "Not following any streamer");
        if (type.equalsIgnoreCase(AppConstants.TYPE_FOLLOWING) && username.equalsIgnoreCase("meuser"))
            showStreamerButton.set(true);
    }
}
