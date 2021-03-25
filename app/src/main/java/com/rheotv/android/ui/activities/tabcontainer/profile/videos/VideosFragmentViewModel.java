package com.rheotv.android.ui.activities.tabcontainer.profile.videos;

import android.util.Log;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.activities.tabcontainer.posts.OnFollowActionCompleteListener;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.CommonUtils;
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

public class VideosFragmentViewModel extends BaseViewModel<VideosFragmentNavigator> {

    public final ObservableList<PostObject> blogObservableArrayList = new ObservableArrayList<>();

    private final MutableLiveData<List<PostObject>> blogListLiveData;

    private int offset = 0;

    String nextUrl;
    private int userId;
    private String gameId;
    private boolean isLite;
    private boolean isLive;

    public void setParams(int user, String gameId, boolean isLite, boolean isLive) {
        this.userId = user;
        this.gameId = gameId;
        this.isLite = isLite;
        this.isLive = isLive;
    }

    public VideosFragmentViewModel(DataManager dataManager,
                                   SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        blogListLiveData = new MutableLiveData<>();
        offset = 0;
    }

    public void addBlogItemsToList(List<PostObject> blogs) {
        if (offset == 0) {
            blogObservableArrayList.clear();
        }
        blogObservableArrayList.addAll(blogs);
    }

    public void fetchUserVideos(int offset) {
        setIsLoading(true);
        this.offset = offset;
        if (nextUrl != null) {
            Log.i("Next url ", nextUrl);
        }
        getCompositeDisposable().add(getDataManager()
                .getVideos(userId, gameId, isLite, isLive, nextUrl)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(blogResponse -> {
//                    Log.e(getClass().getSimpleName(), "fetchUserVideos " + new Gson().toJson(blogResponse));
                    if (blogResponse != null && blogResponse.getResults() != null) {
                        nextUrl = blogResponse.getNext();
                        blogListLiveData.setValue(blogResponse.getResults());
                    } else {
                        getNavigator().showNullView();
                    }
                    setIsLoading(false);
                }, throwable -> {
                    Log.e(getClass().getSimpleName(), "fetchUserVideos " + throwable.getMessage());
                    setIsLoading(false);
                    getNavigator().handleError(throwable);
                }));
    }

    public MutableLiveData<List<PostObject>> getBlogListLiveData() {
        return blogListLiveData;
    }

    public ObservableList<PostObject> getBlogObservableList() {
        return blogObservableArrayList;
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

    public void deleteVideo(String postId, int position) {
        getDataManager().deleteVideo(postId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null && response.body() != null) {
                    getNavigator().onDeleteVideoSuccess(position);
                } else {
                    getNavigator().onDeleteVideoFailure();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getNavigator().onDeleteVideoFailure();
            }
        });
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
}