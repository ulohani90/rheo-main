/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 03/01/19 01:16 AM
 *
 */

package com.rheotv.android.ui.activities.tabcontainer.posts;

import android.content.Context;
import android.util.Log;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.objects.FeedObject;
import com.rheotv.android.data.network.models.objects.TagResults;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.story.StoryAuthorResponse;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.helpers.ShareTaskHelper;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AnalyticsConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class PostViewModel extends BaseViewModel<PostListNavigator> {


    //public final ObservableList<Result> blogObservableArrayList = new ObservableArrayList<>();
    private MutableLiveData<List<FeedObject>> blogListLiveData;
    public MutableLiveData<ArrayList<TagResults>> tags = new MutableLiveData<>();
    public HashMap<String, String> selectedTags = new HashMap<>();
    public MutableLiveData<ArrayList<ProfileResult>> storyAuthors = new MutableLiveData<>();
    public MutableLiveData<ArrayList<ProfileResult>> pagedStoryAuthors = new MutableLiveData<>();
    public String nextStoryAuthorUrl = null;
    public MutableLiveData<Boolean> storyLoading = new MutableLiveData<>();

    private List<FeedObject> feedObjects;
    public String nextUrl;
    private String categoryName = "Latest Videos"; //hardcoded prakash
    private int count;
    public ObservableBoolean isFirstPage = new ObservableBoolean(false);
    public HashMap<String, Object> properties = new HashMap<>();
    public boolean shouldUpdateTags = true;

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public PostViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        blogListLiveData = new MutableLiveData<>();
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    public void fetchHomePage(boolean isNextPage) {
        Log.d("TTTTT", "next dependent page url : " + nextUrl);
        if (isNextPage) {
            isFirstPage.set(false);
        } else {
            isFirstPage.set(true);
        }
        if (nextUrl != null)
            Log.i(PostViewModel.class.getCanonicalName() + "::Url ", nextUrl);
        getCompositeDisposable().add(getDataManager()
                .fetchHomePage(nextUrl, selectedTags)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(blogResponse -> {
                    setIsLoading(false);
                    Log.i(getClass().getSimpleName(), "fetchHomePage: " + new Gson().toJson(blogResponse));
                    if (blogResponse != null && blogResponse.getResults() != null) {
                        try {
                            if (isFirstPage.get()) {
                                feedObjects = new ArrayList<>();
                            }
                            feedObjects.addAll(blogResponse.getResults());
                            blogListLiveData.setValue(blogResponse.getResults());

                            if (shouldUpdateTags && blogResponse.getTags() != null && blogResponse.getTags().size() > 0) {
                                shouldUpdateTags = false;
                                tags.setValue(blogResponse.getTags().get(0).getTags());
                            }

                            count = blogResponse.getCount();
                            nextUrl = blogResponse.getNext();
                            if (getNavigator() != null) {
                                getNavigator().setEnableClips(blogResponse.isEnableClips());
                                getNavigator().setEnableGoLive(blogResponse.isShowGoLive());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    setIsLoading(false);
                    if (getNavigator() != null)
                        getNavigator().stopLoading();
                }, throwable -> {
                    if (getNavigator() != null) {
                        if (nextUrl == null) {
                            setIsLoading(false);
                            throwable.printStackTrace();
                            getNavigator().handleError(throwable);
                        } else {
                            setIsLoading(false);
                            getNavigator().hidePaginationLoader();
                        }
                        getNavigator().stopLoading();
                    }
                }));
    }

    /*public void addBlogItemsToList(List<Result> blogs) {
        blogObservableArrayList.clear();
        blogObservableArrayList.addAll(blogs);
    }

    public void updateLikesInPost(Result post) {
        int position = blogObservableArrayList.indexOf(post);

        if (post.getIsLiked()) {
            post.setTotalLikes(Integer.valueOf(post.getTotalLikes()) - 1);
        } else {
            post.setTotalLikes(Integer.valueOf(post.getTotalLikes()) + 1);
        }
        post.setIsLiked(!post.getIsLiked());

        blogObservableArrayList.set(position, post);
        blogListLiveData.getValue().set(position, post);
    }*/


    /*public void onLikeItemClicked(String body, Result post) {
        updateLikesInPost(post);
        getDataManager()
                .postLikeToggle(body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        //do nothing
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        //do nothing
                    }
                });

        //analytics call for click via home screen
        AnalyticsHelper.getInstance(getNonUiContext())
                .sendLike(post.getAuthor().getUser().getUsername()
                        , String.valueOf(post.getAuthor().getUser().getId())
                        , post.getId()
                        , post.getTitle()
                        , AnalyticsConstants.SOURCE_HOME_SCREEN);

    }*/


    public void onShareItemClicked(String body, Result post, Context context) {
        boolean isDownloading = ((RheoTvApp) getNonUiContext()).getDownloadStatus();
        if (isDownloading) {
            return;
        }
        ((RheoTvApp) getNonUiContext()).setDownloadStatus(true);
        getDataManager()
                .postShare(body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        //do nothing
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        //do nothing
                    }
                });
        //analytics for click via the home screen
        /*AnalyticsHelper.getInstance(getNonUiContext())
                .sendPostShareClick(post.getAuthor().getUser().getUsername()
                        , String.valueOf(post.getAuthor().getUser().getId())
                        , post.getId()
                        , post.getTitle()
                        , AnalyticsConstants.SOURCE_HOME_SCREEN);*/

        //call the sharing method

    }


    public void onAuthorClicked(String authorUserName) {
        getNavigator().switchFragment(authorUserName);
    }

    public void onDownloadClicked(String body, Result post, Context context) {
        boolean isDownloading = ((RheoTvApp) getNonUiContext()).getDownloadStatus();
        if (isDownloading) {
            return;
        }
        ((RheoTvApp) getNonUiContext()).setDownloadStatus(true);
        ShareTaskHelper.getNewInstance(context).downloadFromUrl(post.getGistUrl(), post.getTitle(), context);
        getDataManager()
                .postDownload(body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        //do nothing
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        //do nothing
                    }
                });
        //analytics call for click via home screen
        AnalyticsHelper.getInstance(getNonUiContext())
                .sendPostDownloadClicked(post.getAuthor().getUser().getUsername()
                        , String.valueOf(post.getAuthor().getUser().getId())
                        , post.getId()
                        , post.getTitle()
                        , AnalyticsConstants.SOURCE_HOME_SCREEN);
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
                getNavigator().showToast();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //getNavigator().hideProgressBarLoading();
            }
        });
    }

    public MutableLiveData<List<FeedObject>> getBlogListLiveData() {
        if (blogListLiveData.getValue() == null) {
            blogListLiveData.setValue(new ArrayList<>());
        }
        return blogListLiveData;
    }


    public void clearBlogLiveData() {
        blogListLiveData.getValue().clear();
    }

   /* public ObservableList<Result> getBlogObservableList() {
        return blogObservableArrayList;
    }*/

    public int getCount() {
        return count;
    }

    public void clearTags() {
        if (!selectedTags.isEmpty())
            selectedTags.clear();
        shouldUpdateTags = true;
    }

    public void loadStoryAuthor(boolean refresh) {
        if (refresh)
            nextStoryAuthorUrl = null;
        String userId = CommonUtils.getUserName();
        if (!CommonUtils.isUserLoggedin())
            userId = "notloggedin";

        getDataManager().loadStoryAuthors(userId, nextStoryAuthorUrl).enqueue(new Callback<StoryAuthorResponse>() {
            @Override
            public void onResponse(Call<StoryAuthorResponse> call, Response<StoryAuthorResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getResult() != null) {
                        if (response.body().getSelfId() != null)
                            CommonUtils.setAuthorId(response.body().getSelfId());
                        if (response.body().getProfilePic() != null)
                            CommonUtils.setProfileImageUrl(response.body().getProfilePic());


                        if (nextStoryAuthorUrl == null)
                            storyAuthors.setValue(response.body().getResult());
                        else
                            pagedStoryAuthors.setValue(response.body().getResult());
                        nextStoryAuthorUrl = response.body().getNext();
                        if (nextStoryAuthorUrl == null)
                            storyLoading.setValue(false);
                    } else {
                        storyAuthors.setValue(new ArrayList<>());
                    }
                }
            }

            @Override
            public void onFailure(Call<StoryAuthorResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

}
