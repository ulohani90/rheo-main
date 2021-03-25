package com.rheotv.android.ui.activities.player.activity;

import android.util.Log;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.AnalyticsEventsResponse;
import com.rheotv.android.data.network.models.objects.AuthorObject;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.objects.SlotEventData;
import com.rheotv.android.data.network.models.postlisting.responses.User;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.TimeUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class StreamPlayerContainerViewModel extends BaseViewModel {
    public MutableLiveData<List<PostObject>> posts = new MutableLiveData<>();
    public PostObject post;
    public MutableLiveData<Boolean> loadingMutableLiveData = new MutableLiveData<>();
    public static int cardPosition = -1;

    public int currentIndex = 0;
    public String nextUrl = null;
    public boolean showTagOptions = false;
    private boolean isLite = true;
    private boolean isLive;
    private String gameId;
    private String firstPostId;
    private String slug = null;
    int retryRequest;

    public ObservableField<Integer> retryRequestTime = new ObservableField<>();

    public StreamPlayerContainerViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setLite(boolean isLite) {
        this.isLite = isLite;
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    public void setLoading(boolean loading) {
        loadingMutableLiveData.setValue(loading);
    }

    public boolean isLoading() {
        return loadingMutableLiveData.getValue() != null && loadingMutableLiveData.getValue();
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        if (AppConstants.LIVE_GAME_ID.equals(slug))
            this.slug = null;
        else if (slug != null)
            this.slug = slug;
    }

    public void setPost(@Nullable PostObject post) {
        this.post = post;

        List<PostObject> postList = new ArrayList<>();
        if (post != null) {
            post.setShowTagOptions(showTagOptions);
            postList.add(post);
        }
        posts.setValue(postList);
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    public MutableLiveData<Boolean> showError = new MutableLiveData<>();

    public void fetchVideos() {
        setLoading(true);

        if (nextUrl != null) {
            Log.i("Next url ", nextUrl);
        }
        getCompositeDisposable().add(getDataManager()
                .getRecommendedVideos(0, gameId, isLite, isLive, nextUrl, slug, post != null ? post.getId() : null)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(blogResponse -> {
                    retryRequest = 0;
                    Log.e(getClass().getSimpleName(), "fetchUserVideos - success");
                    if (blogResponse != null) {
                        SlotEventData slotEventData = blogResponse.getSlotBannerDetails();
                        if (slotEventData != null && slotEventData.getSlotPosition() != null) {
                            cardPosition = slotEventData.getSlotPosition() > 0 ? slotEventData.getSlotPosition() - 1 : slotEventData.getSlotPosition();
                            PostObject postObject = new PostObject();
                            postObject.setId(slotEventData.getId());
                            postObject.setAuthor(slotEventData.getAuthor());
//                            postObject.setId(slotEventData.getPostId() != null && !slotEventData.getPostId().isEmpty() ? slotEventData.getPostId() : "blablablabla");
                            postObject.setThumbnail(slotEventData.getSlotImageUrl());
                            postObject.setCardType(true);
                            postObject.setPublished(true);
                            postObject.setReminderTime(slotEventData.getSlotStartTime());
                            postObject.setTitle(slotEventData.getTitle());
                            blogResponse.getResults().add(cardPosition, postObject);
                        }
                        if (blogResponse.getResults() != null) {
                            nextUrl = blogResponse.getNext();
                            posts.setValue(blogResponse.getResults());
                        }
                    } else {
                        //TODO-Show null view
                        //getNavigator().showNullView();
                    }
                    showError.setValue(false);
                    setLoading(false);
                }, throwable -> {
                    try {
                        Log.e(getClass().getSimpleName(), "fetchUserVideos " + throwable.getMessage());
                        setLoading(false);
                        if (posts.getValue() == null || posts.getValue().isEmpty())
                            showError.setValue(true);
                        retryRequest += 1;
                        retryRequestTime.set(retryRequest);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    //TODO handle error
                    //getNavigator().handleError(throwable);
                }));
    }

    public void getAnalyticsEventsList() {
        getDataManager().getAnalyticsEventsList().enqueue(new Callback<AnalyticsEventsResponse>() {
            @Override
            public void onResponse(@NotNull Call<AnalyticsEventsResponse> call, @NotNull Response<AnalyticsEventsResponse> response) {
                if (response.body() != null) {
                    SegmentTracker.getInstance(getNonUiContext()).setAnalyticsEvents(response.body().getEvents(), response.body().getMoengageEvents());
                }
            }

            @Override
            public void onFailure(@NotNull Call<AnalyticsEventsResponse> call, @NotNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private boolean mIsRefreshing = false;

    public void setRefreshing(boolean isRefreshing) {
        mIsRefreshing = isRefreshing;
    }

    public boolean isRefreshing() {
        return mIsRefreshing;
    }
}
