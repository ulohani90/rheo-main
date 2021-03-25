/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 03/01/19 01:16 AM
 *
 */

package com.rheotv.android.ui.activities.tabcontainer.trending;

import android.content.Context;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableList;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.helpers.JsonParseHelper;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.helpers.ShareTaskHelper;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AnalyticsConstants;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;


public class TrendingViewModel extends BaseViewModel<TrendingListNavigator> {

    public final ObservableList<Result> blogObservableArrayList = new ObservableArrayList<>();

    private List<Result> districtList = new ArrayList<>();

    private final MutableLiveData<List<Result>> blogListLiveData;

    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    private JsonParseHelper jsonParseHelper = new JsonParseHelper();

    public ObservableBoolean isFirstPage = new ObservableBoolean(false);

    public TrendingViewModel(DataManager dataManager,
                             SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        blogListLiveData = new MutableLiveData<>();
        fetchTrendingPosts(0, false);
    }

    public void addBlogItemsToList(List<Result> blogs) {
        blogObservableArrayList.clear();
        blogObservableArrayList.addAll(blogs);
    }

    public void fetchTrendingPosts(int offset, boolean isNextPage) {
        setIsLoading(true);
        if(offset == 0){
            isFirstPage.set(true);
        } else{
            isFirstPage.set(false);
        }
        getCompositeDisposable().add(getDataManager()
                .getTrendingList(offset)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(blogResponse -> {
                    if (blogResponse != null && blogResponse.getResults() != null) {
                        if (blogResponse.getResults().size() > 0) {
                            if (!isNextPage) {
                                districtList.clear();
                                sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.HOME_LATEST_POSTS, "");
                                districtList.addAll(blogResponse.getResults());
                                blogListLiveData.setValue(districtList);
                                jsonParseHelper.saveTrendingResponseJson(blogResponse);
                            } else {
                                districtList.addAll(blogResponse.getResults());
                                blogListLiveData.setValue(districtList);
                                blogResponse.getResults().addAll(0, jsonParseHelper.getTrendingPostsFromStorage());
                                jsonParseHelper.saveTrendingResponseJson(blogResponse);
                            }
                        }
                    }
                    setIsLoading(false);
                }, throwable -> {
                    setIsLoading(false);
                    getNavigator().handleError(throwable);
                }));
    }

    public void updateSingleItemInList(Result post) {
        int position = blogObservableArrayList.indexOf(post);

        if (post.getIsLiked()) {
            post.setTotalLikes(Integer.valueOf(post.getTotalLikes()) - 1);
        } else {
            post.setTotalLikes(Integer.valueOf(post.getTotalLikes()) + 1);
        }
        post.setIsLiked(!post.getIsLiked());

        blogObservableArrayList.set(position, post);
        blogListLiveData.getValue().set(position, post);
//
    }

    public void onLikeItemClicked(String body, Result post) {

        updateSingleItemInList(post);
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
        AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext())
                .sendLike(post.getAuthor().getUser().getUsername()
                        , String.valueOf(post.getAuthor().getUser().getId())
                        , post.getId()
                        , post.getTitle()
                        , AnalyticsConstants.SOURCE_HOME_SCREEN);

    }

    public void onShareItemClicked(String body, Result post, Context context) {
        boolean isDownloading = ((RheoTvApp) getNonUiContext()).getDownloadStatus();
        if(isDownloading){
            return;
        }
        ((RheoTvApp) getNonUiContext()).setDownloadStatus(true);
        getDataManager()
                .postShare(body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        //do nothing
                    }
                });

        //analytics for click via the home screen
        AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext())
                .sendPostShareClick(post.getAuthor().getUser().getUsername()
                        , String.valueOf(post.getAuthor().getUser().getId())
                        , post.getId()
                        , post.getTitle()
                        , AnalyticsConstants.SOURCE_HOME_SCREEN);

        //call the sharing method
        ShareTaskHelper.getNewInstance(context).downloadAndSharePostOnWhatsApp(post);

    }

    public void updateFBSharesInPost(Result post) {
        int position = blogObservableArrayList.indexOf(post);
        post.setTotalFacebookShares(String.valueOf(Integer.valueOf(post.getTotalFacebookShares()) + 1));

        blogObservableArrayList.set(position,post);
        blogListLiveData.getValue().set(position,post);
    }

    public void onFBShareClicked(String body, Result post, Context context) {
        boolean isDownloading = ((RheoTvApp) getNonUiContext()).getDownloadStatus();
        if(isDownloading){
            return;
        }
        ((RheoTvApp) getNonUiContext()).setDownloadStatus(true);
        updateFBSharesInPost(post);
        AnalyticsHelper.getInstance(getNonUiContext())
                .sendPostFBShareClick(post.getAuthor().getUser().getUsername()
                        , String.valueOf(post.getAuthor().getUser().getId())
                        , post.getId()
                        , post.getTitle()
                        , AnalyticsConstants.SOURCE_HOME_SCREEN);
        getDataManager()
                .postFBShare(body, AppConstants.SHARE_SOURCE_FACEBOOK)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
        ShareTaskHelper.getNewInstance(context).downloadAndSharePostOnFacebook(context, post);
    }

    public void onDownloadClicked(String body, Result post, Context context) {
        boolean isDownloading = ((RheoTvApp) getNonUiContext()).getDownloadStatus();
        if(isDownloading){
            return;
        }
        ((RheoTvApp) getNonUiContext()).setDownloadStatus(true);
        ShareTaskHelper.getNewInstance(context).downloadFromUrl(post.getGistUrl(), post.getTitle(), context);
        updateDownloadsInPost(post);
        getDataManager()
                .postDownload(body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        //do nothing
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
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

    public void updateDownloadsInPost(Result post) {
        //TODO: download numbers from trending page is not updating realtime.
        int position = blogObservableArrayList.indexOf(post);
        post.setTotalDownloads(Integer.valueOf(post.getTotalDownloads()) + 1);

        blogObservableArrayList.set(position,post);
        blogListLiveData.getValue().set(position,post);
    }

    public void onAuthorClicked(String authorUserName) {
        getNavigator().switchFragment(authorUserName);
    }

    public MutableLiveData<List<Result>> getBlogListLiveData() {
        return blogListLiveData;
    }

    public ObservableList<Result> getBlogObservableList() {
        return blogObservableArrayList;
    }
}
