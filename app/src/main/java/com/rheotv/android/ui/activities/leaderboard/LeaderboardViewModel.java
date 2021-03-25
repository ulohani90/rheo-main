package com.rheotv.android.ui.activities.leaderboard;

import android.util.Log;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableList;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.postlisting.responses.Author;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardViewModel extends BaseViewModel<LeaderBoardNavigator> {
    public final ObservableList<Author> leaderBoardList = new ObservableArrayList<>();

    private final MutableLiveData<List<Author>> leaderboardListLiveData;
    private final MutableLiveData<List<Author>> winnersList;
    private int offset = 0;
    private final ObservableBoolean isRefreshing = new ObservableBoolean(true);
    public HashMap<String, Object> baseProperties;


    public LeaderboardViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        offset = 0;
        leaderboardListLiveData = new MutableLiveData<>();
        winnersList = new MutableLiveData<>();
        baseProperties = new HashMap<>();
    }


    void fetchLeaderBoardItems(String gameId, int offset, boolean isNextPage, String sortType) {
        setIsLoading(true);
        getNavigator().setRefreshing(true);
        this.offset = offset;
        Log.i("Offset for Leaderboard", offset + "");
        getCompositeDisposable().add(getDataManager()
                .getLeaderBoardList(gameId, offset, sortType)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(blogResponse -> {
                    if (blogResponse != null && blogResponse.getAuthors() != null) {
//                        if (blogResponse.getAuthors().size() > 3) {
//                            winnersList.setValue(blogResponse.getAuthors().subList(0, 3));
//                            leaderboardListLiveData.setValue(blogResponse.getAuthors().subList(3, blogResponse.getAuthors().size()));
//                        } else {
//                            winnersList.setValue(blogResponse.getAuthors());
//                        }
                        leaderboardListLiveData.setValue(blogResponse.getAuthors());
                    }
                    if (isRefreshing.get()) isRefreshing.set(false);
                    setIsLoading(false);
                }, throwable -> {
                    if (getNavigator() != null) {
                        if (isRefreshing.get()) isRefreshing.set(false);
                        setIsLoading(false);
                        getNavigator().handleError(throwable.getLocalizedMessage());
                    }
                }));
    }

    MutableLiveData<List<Author>> getLeaderboardListLiveData() {
        return leaderboardListLiveData;
    }

    public MutableLiveData<List<Author>> getWinnersList() {
        return winnersList;
    }

    public ObservableList<Author> getLeaderBoardList() {
        return leaderBoardList;
    }

    public ObservableBoolean isRefreshing() {
        return isRefreshing;
    }

    void addToLeaderBoardList(List<Author> blogs) {
        if (offset == 0) {
            leaderBoardList.clear();
        }
        leaderBoardList.addAll(blogs);
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
