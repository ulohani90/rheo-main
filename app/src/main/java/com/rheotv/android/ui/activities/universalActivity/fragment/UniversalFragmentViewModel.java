package com.rheotv.android.ui.activities.universalActivity.fragment;


import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.objects.GameObject;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.activities.tabcontainer.posts.OnFollowActionCompleteListener;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UniversalFragmentViewModel extends BaseViewModel<UniversalFragmentNavigator> {

    private final MutableLiveData<GameObject> gameobjectLiveData;

    public ObservableField<String> posterCover = new ObservableField<>();

    public ObservableField<String> thumbnail = new ObservableField<>();

    public ObservableField<String> totalViews = new ObservableField<>();

    public ObservableField<String> totalVideos = new ObservableField<>();

    public ObservableField<String> title = new ObservableField<>();

    public ObservableBoolean shimmerVisibility = new ObservableBoolean(true);

    private int offset = 0;
    private String gameId = "";

    public UniversalFragmentViewModel(DataManager dataManager,
                                      SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        gameobjectLiveData = new MutableLiveData<>();
        offset = 0;
    }

    public void fetchGamePage(String gameId) {
        setIsLoading(true);
        this.offset = offset;
        getDataManager()
                .getGameDetails(gameId).enqueue(new Callback<GameObject>() {
            @Override
            public void onResponse(Call<GameObject> call, Response<GameObject> response) {
                if (response != null && response.body() != null) {
                    gameobjectLiveData.setValue(response.body());
                    title.set(response.body().getName());
                    posterCover.set(response.body().getCoverPic());
                    thumbnail.set(response.body().getThumbnail());
                    int totalViewsCount = response.body().getTotalViews();
                    totalViews.set(CommonUtils.getPlural("View", totalViewsCount, CommonUtils.formatValue(totalViewsCount)));
                    int totalVideosCount = response.body().getTotalVideos();
                    totalVideos.set(CommonUtils.getPlural("Video", totalVideosCount, ((totalVideosCount / 1000 >= 1) ? (totalVideosCount / 1000) + (((totalVideosCount % 1000) / 100) > 0 ? "." + ((totalVideosCount % 1000) / 100) : "") + "K" : totalVideosCount + "")));
                    setIsLoading(false);
                } else {
                    setIsLoading(false);
                    if (getNavigator() != null)
                        getNavigator().handleError(new Throwable("Error occurred"));
                }
            }

            @Override
            public void onFailure(Call<GameObject> call, Throwable throwable) {
                if (getNavigator() != null) {
                    setIsLoading(false);
                    getNavigator().handleError(throwable);
                }
            }
        });
                /*.subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(blogResponse -> {
                    if (blogResponse != null && blogResponse.getResults() != null) {
                        if (blogResponse.getResults().size() > 0) {
                            gameobjectLiveData.setValue(blogResponse.getResults());
                        }
                        posterCover.set(blogResponse.getCoverPic());
                        thumbnail.set(blogResponse.getThumbnail());
                        int totalViewsCount = blogResponse.getTotalViews();
                        totalViews.set(CommonUtils.getPlural("View", totalViewsCount, ((totalViewsCount / 1000 >= 1) ? (totalViewsCount / 1000) + (((totalViewsCount % 1000) / 100) > 0 ? "." + ((totalViewsCount % 1000) / 100) : "") + "K" : totalViewsCount + "")));
                        int totalVideosCount = blogResponse.getTotalVideos();
                        totalVideos.set(CommonUtils.getPlural("Video", totalVideosCount, ((totalVideosCount / 1000 >= 1) ? (totalVideosCount / 1000) + (((totalVideosCount % 1000) / 100) > 0 ? "." + ((totalVideosCount % 1000) / 100) : "") + "K" : totalVideosCount + "")));

                    }
                    setIsLoading(false);
                }, throwable -> {
                    setIsLoading(false);
                    getNavigator().handleError(throwable);
                }));*/
    }

    public MutableLiveData<GameObject> getGameobjectLiveData() {
        return gameobjectLiveData;
    }


    public void setStaticCover(String url) {
        posterCover.set(url);
    }

    public void onFollowClicked(int id, boolean isFollowed, OnFollowActionCompleteListener listener) {
        if (!CommonUtils.isUserLoggedin()) {
            getNavigator().handleLogin();
            return;
        }

        if (isFollowed) {
            getDataManager().unFollowAuthor(String.valueOf(id)).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
                        listener.onFollowActionComplete(false);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        } else {
            getDataManager().followAuthor(String.valueOf(id)).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
                        listener.onFollowActionComplete(true);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }


    }

    public void reportPost(String postId) {
        //getNavigator().showProgressBarLoading("Reporting post. Please wait..");
        getDataManager().postReport(postId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //getNavigator().hideProgressBarLoading();

                getNavigator().showReportPostSuccessToast();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //getNavigator().hideProgressBarLoading();
            }
        });
    }

}
