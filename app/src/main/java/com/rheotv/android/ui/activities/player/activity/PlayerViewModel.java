package com.rheotv.android.ui.activities.player.activity;


import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.FollowResponse;
import com.rheotv.android.data.network.models.gamify.DailyRewardsResponse;
import com.rheotv.android.data.network.models.gamify.RewardTakenResponse;
import com.rheotv.android.data.network.models.postlisting.responses.Comments;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.score.ScoreboardResponse;
import com.rheotv.android.data.network.models.stickers.StickersResponse;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.helpers.JsonParseHelper;
import com.rheotv.android.helpers.ShareTaskHelper;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AnalyticsConstants;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.ListHolder;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayerViewModel extends BaseViewModel<PlayerNavigator> {

    public final ObservableList<Result> blogObservableArrayList = new ObservableArrayList<>();
    private final MutableLiveData<List<Result>> blogListLiveData;

    public ObservableField<String> author = new ObservableField<>();
    public ObservableField<String> authorFollowers = new ObservableField<>();
    public ObservableField<String> content = new ObservableField<>();
    public ObservableField<String> date = new ObservableField<>();
    public ObservableField<String> imageUrl = new ObservableField<>();
    public ObservableField<String> authorProfileImageUrl = new ObservableField<>();
    public ObservableField<String> videoTitle = new ObservableField<>();
    public ObservableField<String> game = new ObservableField<>();
    public ObservableField<String> followButton = new ObservableField<>();
    public ObservableField<String> totalLikes = new ObservableField<>();
    public ObservableField<String> totalViews = new ObservableField<>();
    public ObservableField<String> totalShares = new ObservableField<>();
    public ObservableField<String> shareURL = new ObservableField<>();
    public ObservableBoolean isLiked = new ObservableBoolean();
    public ObservableBoolean isPlaying = new ObservableBoolean();
    public ObservableField<Boolean> isFollowing = new ObservableField<>(false);
    public ObservableField<String> totalHeartCount = new ObservableField<>();
    public ObservableField<Boolean> isLive = new ObservableField<>(false);
    public List<String> slangs = new ArrayList<>();
    public ObservableBoolean showWatchVideoReward = new ObservableBoolean(true);
    public ObservableBoolean showFollow = new ObservableBoolean(false);
    public MutableLiveData<Boolean> isStreamEnded = new MutableLiveData<>();

    public ObservableField<Boolean> isDataLoadComplete = new ObservableField<>(false);

    private JsonParseHelper jsonParseHelper = new JsonParseHelper();
    private Result mListItem;
    public Result currentPost;
    //private PlayerNavigator playerNavigator;
    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
    private Context context;
    private boolean isFollowed = false;
    public String commentNextUrl = null;

    public String stickersNextUrl = "";

    public boolean canComment;
    private int localHeartCounter = 0;
    public ScoreboardResponse scoreboardResponse;

    // keeping a counter for incoming hearts so we can update ChatListFragment
    public int sessionHeartCounter = 0;

    public PlayerViewModel(DataManager dataManager, SchedulerProvider schedulerProvider, Context context) {
        super(dataManager, schedulerProvider);
        blogListLiveData = new MutableLiveData<>();
        this.context = context;
    }

    private void getFollowStatus() {
        getDataManager().checkFollowAuthor(String.valueOf(getCurrentPlayingPost().getAuthor().getUser().getId())).enqueue(new Callback<FollowResponse>() {
            @Override
            public void onResponse(Call<FollowResponse> call, Response<FollowResponse> response) {
                if (response != null && response.body() != null) {
                    if (response.isSuccessful() && response.body().isFollow()) {
                        isFollowed = true;
                        followButton.set(context.getString(R.string.followed));
                        isFollowing.set(true);
                    } else {
                        isFollowed = false;
                        followButton.set(context.getString(R.string.follow));
                        isFollowing.set(false);
                    }
                    showFollow.set(true);
                    if (getNavigator() != null)
                        getNavigator().updateFollowStatus(response.body().isFollow());
                } else {
                    showFollow.set(false);
                }

                if (getNavigator() != null && response != null && response.body() != null)
                    getNavigator().updateFollowStatus(response.body().isFollow());
            }

            @Override
            public void onFailure(Call<FollowResponse> call, Throwable t) {
                isFollowed = false;
                followButton.set(context.getString(R.string.follow));
            }
        });
    }

    public void addBlogItemsToList(List<Result> blogs) {
        try {
            blogObservableArrayList.clear();
            for (Result result : blogs) {
                if (result.getType() != AppConstants.VIEW_TYPE_CAROUSEL
                        && result.getType() != AppConstants.VIEW_TYPE_MULTI_ITEM_CARD) {
                    blogObservableArrayList.add(result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);

        }

    }

    public void onLikeButtonClicked() {
        getDataManager()
                .postLikeToggle(mListItem.getId())
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

        if (isLiked != null) {
            if (isLiked.get()) {
                isLiked.set(false);
                totalLikes.set(String.valueOf(Integer.parseInt(totalLikes.get()) - 1));
            } else {
                isLiked.set(true);
                totalLikes.set(String.valueOf(Integer.parseInt(totalLikes.get()) + 1));
            }
        }

        //analytics call for click via home screen
        AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext())
                .sendPostShareClick(mListItem.getAuthor().getUser().getUsername()
                        , String.valueOf(mListItem.getAuthor().getUser().getId())
                        , mListItem.getId()
                        , mListItem.getTitle()
                        , AnalyticsConstants.SOURCE_VIDEO_PLAYER);

    }

    public void setPlayerNavigator(PlayerNavigator playerNavigator) {
        super.setNavigator(playerNavigator);
    }

    public void addCoins() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        getDataManager()
                .postAddCoins()
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            int totalCoins = sharedPrefsUtils.getIntegerPreference(
                                    RheoTvApp.getNonUiContext(),
                                    AppConstants.TOTAL_MOJO_COINS, 0
                            );
                            totalCoins = totalCoins + AppConstants.VIDEO_SHARE_COIN_EARN;
                            sharedPrefsUtils.setIntegerPreference(
                                    RheoTvApp.getNonUiContext(),
                                    AppConstants.TOTAL_MOJO_COINS,
                                    totalCoins
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        //do nothing
                    }
                });
    }

    public void onShareButtonClicked(View view) {
        if (mListItem == null) {
            return;
        }
        getDataManager()
                .postShare(mListItem.getId())
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
        addCoins();
        //analytics for click via the home screen
        AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext())
                .sendPostShareClick(mListItem.getAuthor().getUser().getUsername()
                        , String.valueOf(mListItem.getAuthor().getUser().getId())
                        , mListItem.getId()
                        , mListItem.getTitle()
                        , AnalyticsConstants.SOURCE_VIDEO_PLAYER);

        //call the sharing method
        ShareTaskHelper.getNewInstance(view.getContext()).downloadAndSharePostOnWhatsApp(mListItem);

    }

    public void onAuthorClicked() {
        try {
            getNavigator().onAuthorClicked(String.valueOf(getCurrentPlayingPost().getAuthor().getUser().getUsername()));
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public void onPlayListCalled() {
        getNavigator().openPlayList();
    }

    public void expandBottomNavigation() {
    }

    String postId;

    public void setPlayerData(String postId, PlayerNavigator playerNavigator) {
        this.postId = postId;
        super.setNavigator(playerNavigator);

    }

    public void updateFields(Result result) {
        try {
            Log.d("RRRRR", "updated ");

            if (result != null) {
                isDataLoadComplete.set(true);
                if (result.getAuthor() != null) {
                    if (result.getAuthor().getUser() != null) {
                        if (result.getAuthor().getUser().getUserFullName() != null) {
                            author.set(result.getAuthor().getUser().getUsername());
                        }
                    }
                    if (result.getAuthor().getProfilePic() != null) {
                        authorProfileImageUrl.set(result.getAuthor().getProfilePic());
                    }
                    if (result.getAuthor().getFollowersCount() != null) {
                        int totalFollowers = result.getAuthor().getFollowersCount();
                        authorFollowers.set(CommonUtils.getPlural("Follower", totalFollowers, ((totalFollowers / 1000 >= 1) ? (totalFollowers / 1000) + "." + ((totalFollowers % 1000) / 100) + "K" : totalFollowers + "")));
                    }
                }

                if (result.getDescription() != null) {
                    content.set(result.getDescription());
                }
                if (result.getoFormattedCreatedAt() != null) {
                    date.set(result.getoFormattedCreatedAt());
                }
                if (result.getThumbnail() != null) {
                    imageUrl.set(result.getThumbnail());
                }
                if (result.getTitle() != null) {
                    videoTitle.set(result.getTitle());
                }
                if (result.getGame() != null) {
                    game.set(result.getGame());
                }

                if (result.getTotalLikes() != null) {
                    totalLikes.set(result.getTotalLikes());
                }
                int totalNumViews = result.getTotalViews();
                totalViews.set(CommonUtils.getPlural("View", result.getTotalViews(), (totalNumViews / 1000 >= 1) ? (totalNumViews / 1000) + "." + ((totalNumViews % 1000) / 100) + "K" : totalNumViews + ""));

                if (result.getTotalShares() != null) {
                    totalShares.set(result.getTotalShares());
                }

                if (result.isFollowed()) {
                    followButton.set(context.getString(R.string.followed));
                    isFollowing.set(true);
                } else {
                    followButton.set(context.getString(R.string.follow));
                    isFollowing.set(false);
                }

//                Log.i(AppConstants.TAG, "result.getTotalLikes() " + result.getTotalLikes());
                if (result.getHeartCount() != null) {
                    localHeartCounter = Integer.parseInt(Objects.requireNonNull(result.getHeartCount()));
                    totalHeartCount.set((localHeartCounter / 1000 >= 1) ? (localHeartCounter / 1000) + "." + ((localHeartCounter % 1000) / 100) + "K" : localHeartCounter + "");
                }

                isLive.set(result.getIsLive());
                shareURL.set(result.getShareUrl());

                isLiked.set(result.getIsLiked());
                isPlaying.set(result.isPlaying());
            }
            getFollowStatus();
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    public String getLeftOutDuration() {
        if (getCurrentPlayingPost() != null) {
            return getCurrentPlayingPost().getLeftOutTime();
        }
        return "";
    }

    public void playNextVideo() {
        int currentPosition = getCurrentPlayingPosition(ListHolder.getInstance().getPostIds());
        if (currentPosition == ListHolder.getInstance().getPostIds().size() - 1) {
            //end
            if (getNavigator() != null)
                getNavigator().onExitClicked();
        } else {
            if (getNavigator() != null)
                getNavigator().playVideo(ListHolder.getInstance().getPostIds().get(currentPosition + 1));
            //boolean proceedFurther = incrementCurrentPlayingPost(currentPosition);
            /*if (proceedFurther) {
                getNavigator().playNext(getCurrentPlayingPost());
                //updateFields();
            }*/
        }
    }

    private int getCurrentPlayingPosition(List<String> postIds) {
        return ListHolder.getInstance().getPostIds().indexOf(postId);
    }

    public void playPreviousVideo() {
        int currentPosition = getCurrentPlayingPosition(ListHolder.getInstance().getPostIds());
        if (currentPosition == 0) {
            if (getNavigator() != null)
                getNavigator().onExitClicked();
            //end
        } else {
            if (getNavigator() != null)
                getNavigator().playVideo(ListHolder.getInstance().getPostIds().get(currentPosition - 1));
            /*if (proceedFurther) {
                getNavigator().playPrevious(getCurrentPlayingPost());
                //updateFields();
            }*/

        }
    }

    private boolean incrementCurrentPlayingPost(int currentPosition) {

        if (currentPosition != -1) {

            currentPosition = currentPosition + 1;
            if ((currentPosition > (ListHolder.getInstance().getPostIds().size() - 1))) {
                getNavigator().onExitClicked();
                return false;
            }
            return true;
        }
        return false;
        //initialize player from here again
    }


    public Result getCurrentPlayingPost() {
        return getNavigator().getCurrentPlayingPost();
        //return new Result();
    }

    public MutableLiveData<List<Result>> getBlogListLiveData() {
        return blogListLiveData;
    }

    public void setBlogListLiveData(List<Result> postList) {
        blogListLiveData.setValue(postList);
    }

    public ObservableList<Result> getBlogObservableList() {
        return blogObservableArrayList;
    }

    public void closeButtonClicked(View view) {
        getNavigator().onExitClicked();
    }

    public void settingsClicked(View view) {
        getNavigator().settingsClicked();
    }

    public void shareClick(View view) {
        getNavigator().handleShareClick(getCurrentPlayingPost());
    }

    public void onExpandClicked(View view, boolean isExpanded) {
        getNavigator().handleExpandCollapse(isExpanded);

    }


    public void startToggleFlow(View view) {
        if (!CommonUtils.isUserLoggedin()) {
            getNavigator().openLoginFlow();
            return;
        }
        HashMap<String, Object> properties = getNavigator().getProperties();
        properties.put("from", "videoPlayer");
        properties.put("followAction", !isFollowed);
        SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_PLAYER_FOLLOW_CLICKED, properties);

        if (getCurrentPlayingPost() != null && getCurrentPlayingPost().getAuthor() != null && getCurrentPlayingPost().getAuthor().getUser() != null) {
            if (isFollowed) {
                getDataManager()
                        .unFollowAuthor(String.valueOf(getCurrentPlayingPost().getAuthor().getUser().getId()))
                        .enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
                                    isFollowed = false;
                                    followButton.set(context.getString(R.string.follow));
                                    isFollowing.set(false);
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                //do nothing
                            }
                        });
            } else {
                getDataManager()
                        .followAuthor(String.valueOf(getCurrentPlayingPost().getAuthor().getUser().getId()))
                        .enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
                                    isFollowed = true;
                                    followButton.set(context.getString(R.string.followed));
                                    isFollowing.set(true);
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                //do nothing
                            }
                        });
            }
        }
    }


    public void getPostInfoFromServer(String uid) {
        getDataManager().getSpecificPostWithUid(uid).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                try {
                    if (response != null && response.body() != null) {
                        /*List<Result> list = new ArrayList<>();
                        list.add(response.body());
                        if (jsonParseHelper.getLatestPostResponse() != null && jsonParseHelper.getLatestPostResponse().getResults().get(0).getResults() != null) {
                            list.addAll(jsonParseHelper.getLatestPostResponse().getResults().get(0).getResults());
                        }*/

                        //setPlayList(list);
                        //addBlogItemsToList(postList);
                        //setBlogListLiveData(postList);
                        // setDeepLinkPost(list);
                        if (getNavigator() != null)
                            getNavigator().setCurrentPlayingPost(response.body());
                        //getNavigator().handleChat();
                        AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext()).sendNotificationClickedData(response.body(), "notification_bar");
                    } else {
                        if (response.errorBody() != null) {
                            Log.d(RheoTvApp.TAG, "Error : " + response.errorBody().string());
                        } else {
                            Log.d(RheoTvApp.TAG, "Message : ");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Log.d(RheoTvApp.TAG, "Message : ");
            }
        });
    }

    public void fetchComments(String id) {
        getDataManager().getComments(id).enqueue(new Callback<Comments>() {
            @Override
            public void onResponse(Call<Comments> call, Response<Comments> response) {
                if (response != null && response.body() != null && getNavigator() != null) {
                    getNavigator().addItemsInChat(id, response.body().getResults());
                    commentNextUrl = response.body().getNext();
                    try {
                        if (slangs.size() == 0)
                            slangs.addAll(response.body().getSlangs());
                        canComment = response.body().isCanComment();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<Comments> call, Throwable t) {

            }

        });
    }

    public void fetchCommentsFromUrl(String id) {
        if (commentNextUrl == null) {
            return;
        }
        getDataManager().getPagedCommentsFromUrl(commentNextUrl).enqueue(new Callback<Comments>() {
            @Override
            public void onResponse(Call<Comments> call, Response<Comments> response) {
                if (response != null && response.body() != null && response.body().getResults() != null) {
                    getNavigator().addItemsInChat(id, response.body().getResults());
                    commentNextUrl = response.body().getNext();
                }
            }

            @Override
            public void onFailure(Call<Comments> call, Throwable t) {

            }

        });
    }

    private void setDeepLinkPost(List<Result> list) {
        getNavigator().setDeepLinkPost(list);
    }

    public String getCurrentPlayingPostId() {
        return getCurrentPlayingPost().getId();
    }

    public void updateScratchCardStatusShown(String rewardId) {
        getDataManager().updateScratchCardStatusShown(rewardId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Response<RewardTakenResponse>>() {
                    @Override
                    public void onNext(Response<RewardTakenResponse> rewardTakenResponse) {
                        if (rewardTakenResponse != null && rewardTakenResponse.isSuccessful()) {
                            loadDailyRewards();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    public interface PlayerListListener {
        void onItemClicked(String id);

    }

    public void reportPost(String postId) {
        //getNavigator().showProgressBarLoading("Reporting post. Please wait..");
        getDataManager().postReport(postId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //getNavigator().hideProgressBarLoading();
                if (getNavigator() != null)
                    getNavigator().showReportPostSuccessToast();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //getNavigator().hideProgressBarLoading();
            }
        });
    }


    public void onGameTagClicked(View view) {
        if (getNavigator() != null)
            getNavigator().openGamePage(((TextView) view).getText());
    }

    public void onMoreOptionsClicked() {
        getNavigator().onMoreOptionsClicked();
    }

    public void reportComment(String postId, String username, String comment) {
        getDataManager().reportComment(postId, username, comment).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (getNavigator() != null)
                    getNavigator().showReportPostSuccessToast();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void blockUser(String postId, String username, String comment) {
        getDataManager().blockUser(postId, username, comment).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (getNavigator() != null)
                    getNavigator().onBlockUserSuccess();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void addHeart(@Nullable String url) {
        getDataManager().postHeart(getCurrentPlayingPostId(), url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i(PlayerViewModel.class.getName(), "Heart Post Success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(PlayerViewModel.class.getName(), "Heart Post Fail");
            }
        });

        Properties property = new Properties()
                .putValue("postId", getCurrentPlayingPostId())

                .putValue("selfUserName", CommonUtils.getUserName(context));
        if (url != null) property.putValue("url", url);
        if (getCurrentPlayingPost() != null && getCurrentPlayingPost().getAuthor() != null && getCurrentPlayingPost().getAuthor().getUser() != null)
            property.putValue("userName", getCurrentPlayingPost().getAuthor().getUser().getUsername());
//        SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_HEART_CLICK, property);

        localHeartCounter = localHeartCounter + 1;
//        int heartCount = (totalHeartCount.get() == null ? 0 : Integer.parseInt(Objects.requireNonNull(totalHeartCount.get()))) + 1;
        totalHeartCount.set((localHeartCounter / 1000 >= 1) ? (localHeartCounter / 1000) + "." + ((localHeartCounter % 1000) / 100) + "K" : localHeartCounter + "");
        getCurrentPlayingPost().setHeartCount(String.valueOf(localHeartCounter));
    }

    public void updateScratchCard(String rewardId) {
        getDataManager().updateDailyScratchCard(rewardId).enqueue(new Callback<RewardTakenResponse>() {
            @Override
            public void onResponse(Call<RewardTakenResponse> call, Response<RewardTakenResponse> response) {
                //Log.i(getClass().getName(), "updateScratchCard Success " + new Gson().toJson(response) + " rewardId: " + rewardId);
                if (response.isSuccessful() && response.body().isSuccessful()) {
                    Log.i(getClass().getName(), "updateScratchCard Success");

                    HashMap<String, Object> property = new HashMap<>();
                    property.put("rewardId", rewardId);
                    property.put("postId", getCurrentPlayingPostId());
                    property.put("userName", CommonUtils.getUserName(context));

                    SegmentTracker.getInstance(context).trackEvent(SegmentConstants.EVENT_SCRATCH_CARD, property);

                    loadDailyRewards();
                } else {
                    Toast.makeText(context, "Scratch was unsuccessful. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RewardTakenResponse> call, Throwable t) {
                Log.i(getClass().getName(), "dummyLoadDailyRewards " + t.getMessage());
                Toast.makeText(context, "Scratch was unsuccessful. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void loadStickers(String postId) {
        getDataManager().loadStickers(postId, stickersNextUrl).enqueue(new Callback<StickersResponse>() {
            @Override
            public void onResponse(Call<StickersResponse> call, Response<StickersResponse> response) {
                if (response.body() != null && getNavigator() != null) {
                    if (getNavigator() != null)
                        getNavigator().onStickersLoadComplete(response.body().getResults());
                    stickersNextUrl = response.body().getNext();
                }
            }

            @Override
            public void onFailure(Call<StickersResponse> call, Throwable t) {
                if (t != null)
                    Log.i(PlayerViewModel.class.getCanonicalName(), "loadStickers error: " + t.getLocalizedMessage());
            }
        });
    }

    public void loadDailyRewards() {
        if (!CommonUtils.isUserLoggedin()) return;
        getDataManager().getDailyRewards().enqueue(new Callback<DailyRewardsResponse>() {
            @Override
            public void onResponse(Call<DailyRewardsResponse> call, Response<DailyRewardsResponse> response) {
                try {
//                    Log.i(getClass().getName(), "loadDailyRewards " + new Gson().toJson(response));
                    RewardManager.getInstance().setDailyRewards(response.body().getResults());
                    RewardManager.getInstance().setTotalCoins(response.body().getTotalCoins());
                    RewardManager.getInstance().setShouldAskRating(response.body().getCanGiveFeedback());
                    RewardManager.getInstance().setCodaEnabled(response.body().isCodaEnabled());
                    if (getNavigator() != null)
                        getNavigator().checkRewardAvailable();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<DailyRewardsResponse> call, Throwable t) {
                if (t != null)
                    Log.i(getClass().getName(), "loadDailyRewards: " + t.getMessage());
            }
        });
    }

    public void checkStreamEnded() {
        Log.i(getClass().getName(), "checking_Stream_Ended");
        getDataManager().getSpecificPostWithUid(postId).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                try {
                    if (response != null && response.body() != null) {
                        isStreamEnded.setValue(!response.body().getIsLive());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Log.d(RheoTvApp.TAG, "Message : ");
            }
        });
    }
}