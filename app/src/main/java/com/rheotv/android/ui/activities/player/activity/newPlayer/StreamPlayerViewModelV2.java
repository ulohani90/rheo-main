package com.rheotv.android.ui.activities.player.activity.newPlayer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.PropertyChangeRegistry;
import androidx.databinding.library.baseAdapters.BR;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.FollowResponse;
import com.rheotv.android.data.network.models.LocalCommentMessageCallback;
import com.rheotv.android.data.network.models.gamify.DailyRewardsResponse;
import com.rheotv.android.data.network.models.gamify.RewardTakenResponse;
import com.rheotv.android.data.network.models.objects.AuthorObject;
import com.rheotv.android.data.network.models.objects.GameObject;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat;
import com.rheotv.android.data.network.models.postlisting.responses.Comments;
import com.rheotv.android.data.network.models.postlisting.responses.PostGift;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.postlisting.responses.TopFans;
import com.rheotv.android.data.network.models.postlisting.responses.TopFansResponse;
import com.rheotv.android.data.network.models.postlisting.responses.VideoUrlObj;
import com.rheotv.android.data.network.models.score.ScoreboardResponse;
import com.rheotv.android.data.network.models.share.ShareData;
import com.rheotv.android.data.network.models.share.ShareResponse;
import com.rheotv.android.data.network.models.stickers.Sticker;
import com.rheotv.android.data.network.models.streamUpdates.StreamEvent;
import com.rheotv.android.data.network.models.streamUpdates.StreamEventResponse;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.db.AppDatabase;
import com.rheotv.android.db.UserFollowDao;
import com.rheotv.android.db.UserFollowItem;
import com.rheotv.android.helpers.grpc.ChatHelper;
import com.rheotv.android.ui.activities.player.activity.ApiCompleteListener;
import com.rheotv.android.ui.activities.player.activity.ChatHelperCallbacks;
import com.rheotv.android.ui.activities.player.activity.ChatMenuOptionData;
import com.rheotv.android.ui.activities.player.activity.FollowResult;
import com.rheotv.android.ui.activities.player.activity.FollowStatusCompleteListener;
import com.rheotv.android.ui.activities.player.activity.FollowStatusListener;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerNavigator;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.ui.customViews.streamPlayer.StreamAuthorHolder;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppUtilsKt;
import com.rheotv.android.utils.ChatLogs;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.StreamHandler;
import com.rheotv.android.utils.TimeUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import goChat.Services;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.utils.AppConstants.MSG_HEART;
import static com.rheotv.android.utils.AppConstants.MSG_PIN;
import static com.rheotv.android.utils.AppConstants.MSG_SCORE;
import static com.rheotv.android.utils.AppConstants.MSG_TYPE_BLOCKED;
import static com.rheotv.android.utils.AppConstants.MSG_TYPE_DELETED;

public class StreamPlayerViewModelV2 extends BaseViewModel<StreamPlayerNavigator> implements Observable {
    private final String TAG = getClass().getSimpleName();
    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();
    public ObservableField<PostObject> postObject = new ObservableField<>();
    public ObservableField<Result> currentPost = new ObservableField<>();
    public MutableLiveData<Status> loadPostStatus = new MutableLiveData<>();
    public ObservableField<CommentChat> pinnedChat = new ObservableField<>();
    public ObservableField<PostGift> postGift = new ObservableField<>();
    public ObservableField<Boolean> live = new ObservableField<>(false);
    public MutableLiveData<Long> showFirstCommentReward = new MutableLiveData<>();

    public ObservableField<Boolean> iconVisibility = new ObservableField<>(false);

    // chat view model variables
    public boolean isModerator = false;
    public boolean isNetworkChangeListening = false;
    public String commentNextUrl;
    protected Handler messageHandler = new Handler(Looper.getMainLooper());
    private Handler liveCountHandler = new Handler(Looper.getMainLooper());
    public ChatHelper chatHelper;
    public boolean isChatSentWhenKeyboardOpened = false;
    private Gson gson = new Gson();
    public HashMap<String, Object> baseProperties = new HashMap<>();

    public ObservableField<Integer> unreadChatCount = new ObservableField<>(0);
    public ObservableField<Boolean> isLoading = new ObservableField<>(false);
    public ObservableField<Boolean> canComment = new ObservableField<>(true);
    public ObservableField<Boolean> isLandscapeScoreboardVisible = new ObservableField<>(false);
    public ObservableField<Integer> orientation = new ObservableField<>(Configuration.ORIENTATION_PORTRAIT);
    public ObservableField<Boolean> isControlVisible = new ObservableField<>(false);
    public ObservableField<String> viewCount = new ObservableField<>("");

    // score card
    public ObservableField<Boolean> isScorecardAdded = new ObservableField<>(false);
    public ObservableField<Boolean> isScorecardOpen = new ObservableField<>(false);

    public MutableLiveData<List<CommentChat>> comments = new MutableLiveData<>();
    public MutableLiveData<CommentChat> incomingComment = new MutableLiveData<>();
    public MutableLiveData<ScoreboardResponse> tournamentScore = new MutableLiveData<>();
    public MutableLiveData<Status> blockUserStatus = new MutableLiveData<>();
    public MutableLiveData<Status> reportComment = new MutableLiveData<>();
    public MutableLiveData<Status> deleteComment = new MutableLiveData<>();
    public ObservableField<String> enterComment = new ObservableField<>();
    public ObservableField<Boolean> isChatBoxVisible = new ObservableField<>(false);
    public ObservableField<Boolean> isChatBoxLandVisible = new ObservableField<>(false);
    public String messageType = AppConstants.MSG_TYPE_TEXT;

    public MutableLiveData<Pair<String, String>> removeChat = new MutableLiveData<>();
    public MutableLiveData<Long> updateCheckViews = new MutableLiveData<>();
    public MutableLiveData<Long> reconnectChat = new MutableLiveData<>();
    public MutableLiveData<Long> onHeartUpdate = new MutableLiveData<>();
    public MutableLiveData<Long> totalHeartCount = new MutableLiveData<>();
    public ObservableField<Boolean> onFollowingUpdate = new ObservableField<>(false);

    private UserFollowDao dao = AppDatabase.Companion.getInstance(RheoTvApp.getNonUiContext()).userFollowDao();
    // stream events
    public ObservableField<String> recentFollowersEventTitle = new ObservableField<>();
    MutableLiveData<List<StreamEvent>> recentFollowerEvent = new MutableLiveData<>();
    public ObservableField<StreamEventResponse> requestPlayEvent = new ObservableField<>();
    public ObservableField<StreamEventResponse> announcementEvent = new ObservableField<>();
    public ObservableField<StreamEventResponse> videoWatchRewardEvent = new ObservableField<>();
    public ObservableField<StreamEventResponse> rewardTimerEvent = new ObservableField<>();
    public ObservableField<StreamEventResponse> followedEvent = new ObservableField<>();
    public MutableLiveData<String> currentEvent = new MutableLiveData<>();
    public ObservableField<StreamEventResponse> acceptedRequest = new ObservableField<>();
    public ObservableField<String> updateCustomRoomPage = new ObservableField<>();
    public ObservableField<Boolean> isCustomRoomEnabled = new ObservableField<>(false);
    public ObservableField<Boolean> isVideoCallEnabled = new ObservableField<>(false);
    public ObservableField<Boolean> isPlayRequestEnabled = new ObservableField<>(false);
    public MutableLiveData<ArrayList<String>> commentSuggestion = new MutableLiveData<>(new ArrayList<>());
    public boolean isPageSelected = false;
    public ObservableField<Boolean> askToComment = new ObservableField<>(false);
    private boolean isPublishingPostGift;

    public MutableLiveData<StreamEventResponse> callRequestObject = new MutableLiveData<>();
    public ObservableField<Boolean> isRewardIconEnabled = new ObservableField<>(false);

    // share
    public MutableLiveData<StreamEventResponse> shareEventData = new MutableLiveData<>();
    public MutableLiveData<StreamEventResponse> onPostShareEvent = new MutableLiveData<>();
    private PostGiftMessageHandler postGiftMessageHandler;

    public MutableLiveData<List<TopFans>> topThreeFans = new MutableLiveData<>();

    private Runnable updateTotalViewRunnable = () -> {
        if (isPageSelected && getChatHelper() != null && this.chatHelperCallback != null)
            getChatHelper().getTotal(getPostId(), this.chatHelperCallback);
    };

    //    private StreamPlayerViewModel.StreamEventHandler streamEventHandler;
    protected StreamMessageHandler streamMessageHandler;
//    private StreamPlayerViewModel.PostGiftMessageHandler postGiftMessageHandler;

    public StreamPlayerViewModelV2(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
//        streamEventHandler = new StreamPlayerViewModel.StreamEventHandler();
        streamMessageHandler = new StreamMessageHandler();
        postGiftMessageHandler = new PostGiftMessageHandler();
    }

    @Bindable
    public boolean isFullPortrait() {
        return getVideoMode().equalsIgnoreCase("portrait");
    }

    public boolean isFollowing() {
        return onFollowingUpdate != null && onFollowingUpdate.get();
    }

    @Bindable
    public int getFollowCount() {
        return currentPost != null && currentPost.get() != null && currentPost.get().getAuthor() != null ? currentPost.get().getAuthor().getFollowersCount() : 0;
    }

    public void toggleFollowCount() {
        int followCount = getFollowCount();
        if (isFollowing()) {
            followCount++;
        } else {
            followCount--;
        }
        setFollowCount(followCount);
    }

    public void setFollowCount(int count) {
        if (currentPost != null && currentPost.get() != null && currentPost.get().getAuthor() != null) {
            currentPost.get().getAuthor().setFollowersCount(count);
            notifyPropertyChanged(com.rheotv.android.BR.followCount);
        }
    }

    protected void setPinnedComment(CommentChat commentChat) {
        if (currentPost.get() == null) {
            return;
        }
        currentPost.get().setPinnedComment(commentChat != null && MSG_PIN.equalsIgnoreCase(commentChat.getMessageType()) ? commentChat : null);
        pinnedChat.set(commentChat != null && MSG_PIN.equalsIgnoreCase(commentChat.getMessageType()) ? commentChat : null);
    }

    @Bindable
    public int getPinneUserColor() {
        return ContextCompat.getColor(RheoTvApp.getNonUiContext(), R.color.color_accent);
    }

    public String getPostId() {
        return postObject.get() != null ? postObject.get().getId() : "";
    }

    public Integer authorId() {
        return postObject.get() != null && postObject.get().getAuthor() != null && postObject.get().getAuthor().getUser().getId() != null ? postObject.get().getAuthor().getUser().getId() : 0;
    }

    public String followState() {
        return isFollowing() ? "unfollow" : "follow";
    }

    public String authorUsername() {
        return currentPost.get() != null ? (currentPost.get() != null && currentPost.get().getAuthor() != null && currentPost.get().getAuthor().getUser() != null && currentPost.get().getAuthor().getUser().getUsername() != null ? currentPost.get().getAuthor().getUser().getUsername() : "") : (postObject.get() != null && postObject.get().getAuthor() != null && postObject.get().getAuthor().getUser() != null && postObject.get().getAuthor().getUser().getUsername() != null ? postObject.get().getAuthor().getUser().getUsername() : "");
    }

    public String getPostModerators() {
        return currentPost.get() != null && currentPost.get().getAuthor() != null ? currentPost.get().getAuthor().getModerators() : "";
    }

    public boolean isModerator() {
        return (CommonUtils.getUserName().equalsIgnoreCase(authorUsername()) || (CommonUtils.getUserEmailAddress() != null && getPostModerators().contains(CommonUtils.getUserEmailAddress())));
    }

    public boolean isStreamer() {
        return currentPost.get() != null && currentPost.get().getAuthor() != null && CommonUtils.getUserID() == currentPost.get().getAuthor().getUser().getId();
    }

    public String getPostUrl() {
        return currentPost.get() != null ? currentPost.get().getVideoUrl() : "";
    }

    public List<VideoUrlObj> getStreamUrls() {
        return currentPost.get() != null ? currentPost.get().getVideoUrls() : null;
    }

    public String getPromoVideoUrl() {
        return currentPost.get() != null ? currentPost.get().getPromoVideoUrl() : "";
    }

    public long getStartFrom() {
        return currentPost.get() != null ? currentPost.get().getStartFrom() : 0;
    }

    public String getVideoMode() {
        return currentPost != null && currentPost.get() != null ? currentPost.get().getVideoMode() : "";
    }

    public boolean showIntro() {
        //return true;
        return currentPost != null && currentPost.get() != null ? currentPost.get().isShowIntro() : false;
    }

    public String getIntroVideoUrl() {
        return currentPost != null && currentPost.get() != null && currentPost.get().getIntroVideoUrl() != null ? currentPost.get().getIntroVideoUrl() : "";
    }

    public String getGameRulesVideoUrl() {
        return currentPost != null && currentPost.get() != null && currentPost.get().getGamesRuleVideoUrl() != null ? currentPost.get().getGamesRuleVideoUrl() : "";
    }

    public String getCampaignInfo() {
        return currentPost.get() != null ? currentPost.get().getAuthor().getCampaignInfo() : "";
    }

    public String getGameName() {
        return postObject.get() != null && postObject.get().getGame() != null ? postObject.get().getGame().getName() : (currentPost.get() != null ? currentPost.get().getGameName() : "");
    }

    public String getPostShareText() {
        return currentPost.get() != null ? (currentPost.get().getPostShareText() != null ? currentPost.get().getPostShareText() : "") : "";
    }

    public String getThumbnail() {
        return currentPost.get() != null ? currentPost.get().getThumbnail() : "";
    }

    public StreamEvent getEventFollowed() {
        return followedEvent.get().getFirstEvent();
    }

    public long getRewardTimeFromPost() {
        return postObject.get() != null ? postObject.get().getRewardTimeProgress() : 0;
    }

    public void setRewardTimeFromPost(long rewardTime) {
        if (postObject.get() != null)
            postObject.get().setRewardTimeProgress(rewardTime);
    }

    public long getResumePosition() {
        return postObject.get() != null ? postObject.get().getResumePosition() : 0;
    }

    public int getResumeWindow() {
        return postObject.get() != null ? postObject.get().getResumeWindow() : 0;
    }

    public ShareData getShareEventData() {
        return shareEventData == null || shareEventData.getValue() == null ? null : shareEventData.getValue().getShareData();
    }

    public boolean isLive() {
        return currentPost.get() != null ? currentPost.get().getIsLive() : (postObject.get() != null && postObject.get().isLive());
    }

    public String getShareUrl() {
        return currentPost.get() != null ? currentPost.get().getShareUrl() : (postObject.get() != null ? postObject.get().getShareUrl() : "");
    }

    public StreamAuthorHolder getAuthorDetail() {
        return new StreamAuthorHolder.Builder()
                .setStreamTitle(postObject.get() != null ? postObject.get().getTitle() : "")
                .setGameName(postObject.get() != null ? (postObject.get().getGame() != null ? postObject.get().getGame().getName() : "") : "")
                .setViewCount(postObject.get() != null ? postObject.get().getViews() : "")
                .setFollowCount(currentPost.get() != null ? currentPost.get().getAuthor().getFollowersCount() : 0)
                .setUsername(postObject.get() != null ? (postObject.get().getAuthor() != null ? (postObject.get().getAuthor().getUser() != null ? postObject.get().getAuthor().getUser().getUsername() : "") : "") : "")
                .setProfileUrl(postObject.get() != null ? (postObject.get().getAuthor() != null ? postObject.get().getAuthor().getProfilePic() : "") : "")
                .setFollowing(isFollowing())
                .build();
    }

    public void loadStreamerFollowState(int userId, String username) {
        if (userId <= 0 || !CommonUtils.isUserLoggedin()) return;
        AppUtilsKt.INSTANCE.runOnIO(this, () -> {
            AtomicBoolean isUserFound = new AtomicBoolean(false);
            UserFollowItem userFollowItem = dao.checkIfIsFollowedWithUserId(userId);
            AppUtilsKt.INSTANCE.runOnMain(this, () -> {
                if (userFollowItem != null) {
                    isUserFound.set(true);
                    onFollowingUpdate.set(userFollowItem.isFollowed());
                }
                getDataManager().checkFollowAuthor(String.valueOf(userId)).enqueue(new Callback<FollowResponse>() {
                    @Override
                    public void onResponse(Call<FollowResponse> call, Response<FollowResponse> response) {
                        if (response != null && response.body() != null) {
                            insertFollowStatusInDB(userId, username, response.body().isFollow(), (isFollowed) -> {
                                if (!isUserFound.get()) {
                                    onFollowingUpdate.set(isFollowed);
                                }
                            });

                            canComment.set(response.body().isCanComment());
                        }
                    }

                    @Override
                    public void onFailure(Call<FollowResponse> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });
                return null;
            });
            return null;
        });

    }

    protected int postRetryCount = 0;

    public void loadPost(boolean isForLiveStatus) {
        postRetryCount++;
        loadPostStatus.setValue(Status.LOADING);
        loadInitialComments(null);
        getDataManager().getSpecificPostWithUid(getPostId()).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if (response != null && response.body() != null) {
                    if (isForLiveStatus) {
                        if (currentPost.get() != null) {
                            currentPost.get().setIsLive(response.body().getIsLive());
                        }

                        if (!response.body().getIsLive()) {
                            Log.i(TAG, "stream_jump_next");
                            EventBus.getDefault().post(new EventBusModel.Next(response.body().getId()));
                        }
                        return;
                    }
                    totalHeartCount.setValue(response.body().getHearts());
                    if (!response.body().getIsLive())
                        viewCount.set(response.body().getTotalViews() + " Views");
                    else if (!isConnectChatRequestMade.get()) {
                        viewCount.set(response.body().getWatchingCount() + " Watching");
                    }
                    populatePost(response.body());
                    if (response.body().getAuthor() != null && response.body().getAuthor().getUser() != null) {
                        loadStreamerFollowState(response.body().getAuthor().getUser().getId(), response.body().getAuthor().getUser().getUsername());
                    }
                    currentPost.set(response.body());
                    isCustomRoomEnabled.set(response.body().isCustomRoomEnabled());
                    isVideoCallEnabled.set(response.body().isVideoCallEnabled());
                    isRewardIconEnabled.set(response.body().isRewardIconEnabled());
                    isPlayRequestEnabled.set(response.body().canRequestPlay());
                    live.set(response.body().getIsLive());
                    if (isPageSelected && response.body().getIsLive() && getChatHelper() != null) {
                        getChatHelper().getTotal(getPostId(), chatHelperCallback);
                    }
                    setPinnedComment(response.body().getPinnedComment());
                    if (response.body().getCommentSuggestions() != null && !response.body().getCommentSuggestions().isEmpty())
                        commentSuggestion.setValue(response.body().getCommentSuggestions());

                    setGreeting(response.body().getPostGifts());
                    loadPostStatus.setValue(Status.SUCCESS);
                    if (response.body().isShareDataGenerated()) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> loadSharableContent(), 10000);
                    }
                } else {
                    if (isForLiveStatus) {
                        return;
                    }
                    loadPostStatus.setValue(Status.ERROR);
                    if (response.errorBody() != null) {
                        Log.d(RheoTvApp.TAG, "Error : " + response.errorBody().toString());
                    } else {
                        Log.d(RheoTvApp.TAG, "Message : ");
                    }
                }
                notifyPropertyChanged(BR.followCount);
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Log.d(RheoTvApp.TAG, "Message : ");
                loadPostStatus.setValue(Status.ERROR);
                if (postRetryCount < 3)
                    new Handler().postDelayed(() -> loadPost(false), (long) (Math.pow(2, postRetryCount) * 1000));
                if (commentRetryCount <= 3)
                    new Handler().postDelayed(() -> loadComments(), (long) (Math.pow(2, commentRetryCount) * 1000));
            }
        });
    }

    protected void setGreeting(List<PostGift> postGifts) {
        if (postGifts == null || postGifts.isEmpty()) return;
        for (PostGift postGift : postGifts) {
            if (postGiftMessageHandler != null)
                postGiftMessageHandler.add(postGift, false);
        }
    }

    /*private void checkPostGiftQueue() {
        if (postGiftsQueue.size() > 0) {
            PostGift postGift = postGiftsQueue.poll();
            this.postGift.set(postGift);
            handleWaitForShowingGreeting(postGift);
        } else {

            postGift.set(null);
        }
    }

    private void handleWaitForShowingGreeting(PostGift postGift) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkPostGiftQueue();
            }
        }, TimeUtils.getTimeDiffInMs(postGift.getStartTimeTs(), postGift.getEndTimeTs()));
    }*/

    void openLoginPage() {
        if (getNavigator() != null) {
            getNavigator().openLoginFlow(null);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        chatHelperCallback = null;
    }

    public void onFollowButtonClick() {
        EventBus.getDefault().post(new EventBusModel.UpdateBackPress(false));
        Map<String, Object> map = new HashMap<>();
        map.put("is_first", CommonUtils.isFirstTimeFollow());
        map.put("author", authorUsername());
        map.put("source", SegmentConstants.SCREEN_NAME_VIDEO_PLAYER_CHAT);
        SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, map);
        CommonUtils.setFirstTimeFollow();
        onFollowButtonClick(followState(), authorId(), authorUsername(), null);
    }

    public void onFollowButtonClick(String followState, int userId, String username, FollowStatusCompleteListener listener) {
        if (CommonUtils.isUserLoggedin()) {
            updateFollowStatusInDB(userId, username, followState == "follow", (isFollowed) -> {
                if (listener != null)
                    listener.success();
                if (userId == authorId())
                    onFollowingUpdate.set(isFollowed);
                getDataManager().toggleFollow(followState, String.valueOf(userId)).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (listener != null) {
                            if (response.isSuccessful()) {
                                EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
//                            listener.success();
                            } else {
//                            listener.error();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            });
        } else {
            if (getNavigator() != null)
                getNavigator().openLoginFlow("");
        }
    }

    public void updateFollowStatusInDB(int userId, String username, boolean followState, FollowStatusListener followStatusListener) {
        AppUtilsKt.INSTANCE.runOnIO(this, () -> {
            dao.updateUserEntry(new UserFollowItem(userId, username, followState));
            Log.i("#######", "updated  ---> " + followState);
            AppUtilsKt.INSTANCE.runOnMain(this, () -> {
                if (followStatusListener != null)
                    followStatusListener.followStatus(followState);
                return null;
            });
            return null;
        });
    }

    public void onCloseScorecardClick() {
        EventBus.getDefault().post(new EventBusModel.UpdateBackPress(false));
        isScorecardOpen.set(false);
    }

    public void onScoreIndicatorClick() {
        isScorecardOpen.set(true);
    }

    protected int commentRetryCount = 0;

    // load 10 comments older (between 10 to 20) immediately
    // load latest comment with fix delay in each comment
    public void loadInitialComments(@Nullable List<CommentChat> previousComments) {
        commentRetryCount++;
        getDataManager().getStreamComments(getPostId(), commentNextUrl).enqueue(new Callback<Comments>() {
            @Override
            public void onResponse(Call<Comments> call, Response<Comments> response) {
                if (response != null && response.body() != null) {
                    commentNextUrl = response.body().getNext();
                    if (previousComments == null) {
                        if (response.body().getResults() != null) {
                            if (!response.body().getResults().isEmpty()) {
                                if (response.body().isCanComment() && response.body().getResults().size() < 10) {
                                    askToComment.set(true);
                                }

                                if (commentNextUrl != null) {
                                    loadInitialComments(response.body().getResults());
                                } else {
                                    Collections.reverse(response.body().getResults());
                                    comments.setValue(response.body().getResults());
                                    // todo
                                    if (streamMessageHandler != null)
                                        streamMessageHandler.addList(response.body().getResults());
                                    isLoading.set(false);
                                }
                            } else {
                                askToComment.set(true);
                            }
                        }
                    } else {
                        if (response.body().getResults() != null && !response.body().getResults().isEmpty())
                            comments.setValue(response.body().getResults());
                        Collections.reverse(previousComments);
                        // todo
                        if (streamMessageHandler != null)
                            streamMessageHandler.addList(previousComments);
//                        comments.postValue(previousComments);
                        isLoading.set(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<Comments> call, Throwable t) {
                isLoading.set(false);
                if (commentRetryCount <= 3)
                    new Handler().postDelayed(() -> loadInitialComments(null), (long) (Math.pow(2, commentRetryCount) * 1000));
            }
        });
    }

    public void loadComments() {
        getDataManager().getStreamComments(getPostId(), commentNextUrl).enqueue(new Callback<Comments>() {
            @Override
            public void onResponse(Call<Comments> call, Response<Comments> response) {
                if (response.body() != null && messageHandler != null) {
                    messageHandler.post(() -> {
                        if (response.body().getResults() != null && !response.body().getResults().isEmpty()) {
                            if (commentNextUrl == null) {
                                List<CommentChat> list = response.body().getResults();
                                Collections.reverse(list);
                                // todo
                                if (streamMessageHandler != null)
                                    streamMessageHandler.addList(list);
                            } else {
                                comments.setValue(response.body().getResults());
                            }
                        }
                        commentNextUrl = response.body().getNext();
                        isLoading.set(false);
                    });
                }
            }

            @Override
            public void onFailure(Call<Comments> call, Throwable t) {
                isLoading.set(false);
                if (commentRetryCount < 3)
                    new Handler().postDelayed(() -> loadInitialComments(null), (long) (Math.pow(2, commentRetryCount) * 1000));
            }
        });
    }

    public void reportComment(String username, String comment, boolean isActionDelete) {
        updateCommentStatus(isActionDelete, Status.LOADING);
        getDataManager().reportComment(getPostId(), username, comment).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    updateCommentStatus(isActionDelete, Status.SUCCESS);
                    if (getNavigator() != null) {
                        getNavigator().onReportUserSuccess();
                    }
                } else {
                    updateCommentStatus(isActionDelete, Status.ERROR);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                updateCommentStatus(isActionDelete, Status.ERROR);
            }
        });
    }

    public void blockUser(String username, String comment) {
        blockUserStatus.setValue(Status.LOADING);
        getDataManager().blockUser(getPostId(), username, comment).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    blockUserStatus.setValue(Status.SUCCESS);
                    if (getNavigator() != null) {
                        getNavigator().onBlockUserSuccess();
                    }
                } else {
                    blockUserStatus.setValue(Status.ERROR);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                blockUserStatus.setValue(Status.ERROR);
            }
        });
    }

    public void reportPost() {
        //getNavigator().showProgressBarLoading("Reporting post. Please wait..");
        getDataManager().postReport(currentPost.get().getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //getNavigator().hideProgressBarLoading();
                if (response.isSuccessful() && getNavigator() != null) {
                    getNavigator().onReportPostSuccess();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //getNavigator().hideProgressBarLoading();
            }
        });
    }

    public void unpinComment() {
        if (pinnedChat.get() == null) return;
        pinCommentInternal(pinnedChat.get(), false);
    }

    public void pinComment(CommentChat commentChat) {
        Log.i(TAG, "username - > " + commentChat.getUsername() + " : post_id -> " + getPostId() + " : text -> " + commentChat.getMessage());
        pinCommentInternal(commentChat, true);
    }

    private void pinCommentInternal(CommentChat commentChat, boolean isPinned) {
        getDataManager()
                .pinComment(getPostId(), commentChat.getUsername(), isPinned ? commentChat.getMessage() : "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            if (responseBody != null && responseBody.string().contains("success")) {
                                if (isPinned) {
                                    setPinnedComment(commentChat);
                                    pinComment(commentChat, MSG_PIN);
                                } else {
                                    CommentChat comment = new CommentChat("", "",
                                            commentChat.getUsername(), commentChat.getProfile_pic(),
                                            MSG_PIN, CommentChat.Type.Normal);
                                    setPinnedComment(comment);
                                    pinComment(comment, MSG_PIN);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error in api pinComment --> " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    private void updateCommentStatus(boolean isActionDelete, Status status) {
        if (isActionDelete)
            deleteComment.setValue(status);
        else
            reportComment.setValue(status);
    }

    public void updateHeart() {
        totalHeartCount.setValue((totalHeartCount.getValue() == null ? 1 : totalHeartCount.getValue()) + 1);
        onHeartUpdate.setValue(totalHeartCount.getValue());
    }

    public ChatHelper getChatHelper() {
        if (chatHelper == null)
            chatHelper = ChatHelper.getInstance();
        return chatHelper;
    }

    /**
     * Chat connect and disconnect handling
     */
    public ChatHelperCallbacks chatHelperCallback = new ChatHelperCallbacks() {
        @Override
        public void onMessageSend(Services.ChatMessage chatMessage) {
            if (messageHandler != null)
                messageHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (chatMessage.getSender() != null && !chatMessage.getSender().isEmpty()) {
                            Log.i(getClass().getName(), "chat_update " + chatMessage.getMsgType() + " and " + chatMessage.getMessage());

                            // todo
                            if (chatMessage.getMsgType().equalsIgnoreCase(MSG_SCORE)) {
                                updateScore(chatMessage);
                                return;
                            }

                            if (MSG_PIN.equalsIgnoreCase(chatMessage.getMsgType())) {
                                CommentChat chat = CommentChat.getComment(chatMessage);
                                if (chat.getMessage().isEmpty())
                                    chat = null;
                                pinnedChat.set(chat);
                                return;
                            }

                            if (chatMessage.getMsgType().equalsIgnoreCase(AppConstants.MSG_DYNAMIC_PLAYER_ACTIONS)) {
                                updateDynamicEvent(chatMessage);
                                return;
                            }

                            if (chatMessage.getSender().equals(CommonUtils.getUserName()) && RewardManager.getInstance().isFirstCommentRewardAvailable())
                                showFirstCommentReward.setValue(System.currentTimeMillis());

                            if (chatMessage.getMsgType().equalsIgnoreCase(MSG_TYPE_DELETED) || chatMessage.getMsgType().equalsIgnoreCase(MSG_TYPE_BLOCKED)) {
                                String message = chatMessage.getMessage();
                                String sender = chatMessage.getSender();
                                removeChat.setValue(new Pair<>(message, sender));
                                if (chatMessage.getMsgType().equalsIgnoreCase(MSG_TYPE_BLOCKED) && CommonUtils.isUserLoggedin() && CommonUtils.getUserName().equalsIgnoreCase(sender)) {
                                    canComment.set(false);
                                }
                            } else if (chatMessage.getMessage().equalsIgnoreCase(MSG_HEART) && !CommonUtils.getUserName().equalsIgnoreCase(chatMessage.getSender())) {
                                updateHeart();
//                                  animateHeartUp();
                            } else {
                                if (chatMessage.getSender() == null || !chatMessage.getSender().equalsIgnoreCase(CommonUtils.getUserName())) {
                                    // todo
                                    /*if (streamMessageHandler != null)
                                        streamMessageHandler.add(CommentChat.getComment(chatMessage));*/
                                    publishChat(CommentChat.getComment(chatMessage));
                                }
                            }
                        }
                    }
                });
        }

        private void updateScore(Services.ChatMessage message) {
            if (!isScorecardAdded.get()) isScorecardAdded.set(true);
            try {
                tournamentScore.setValue(gson.fromJson(message.getMessage(), ScoreboardResponse.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // todo
        private void updateDynamicEvent(Services.ChatMessage message) {
            try {
                StreamEventResponse response = gson.fromJson(message.getMessage(), StreamEventResponse.class);
                if (response.getType().equalsIgnoreCase(AppConstants.EVENT_GREETING)) {
                    PostGift postGift = gson.fromJson(response.getText(), PostGift.class);
                    if (postGift != null && !CommonUtils.getUserName().equalsIgnoreCase(postGift.getUsername()) && postGiftMessageHandler != null)
                        postGiftMessageHandler.add(postGift, CommonUtils.getUserName().equalsIgnoreCase(postGift.getUsername()));
                } else if (response.getType().equalsIgnoreCase(AppConstants.EVENT_CALL_REQUEST)) {
                    callRequestObject.setValue(response);
                }

//                else if (AppUtilsKt.INSTANCE.isCustomRoomMessage(response.getType())) {
//                    if (!"customroom_filled".equalsIgnoreCase(response.getType()))
//                        streamEventHandler.add(response);
//                    if (response.getCustomRoomDetail() != null) {
//                        EventBus.getDefault().post(response.getCustomRoomDetail());
//                    }
//                } else if (response.getType().contains("custom_room") || response.getType().contains("play_request") || response.getType().equalsIgnoreCase(AppConstants.EVENT_CUSTOM_ROOM_WINNER)) {
//                    streamEventHandler.add(response);
//                    if (response.getPlayRequest() != null) {
//                        EventBus.getDefault().post(response.getPlayRequest());
//                    }
//                } else {
//                    streamEventHandler.add(response);
//                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("test_data", message.getMessage());
            }
        }


        @Override
        public void onMessageDelete(Services.ChatMessage chatMessage) {

        }


        @Override
        public void updateLiveCount(String liveCount) {
            try {
                if (live.get()) {
                    viewCount.set(CommonUtils.formatValue(Double.parseDouble(liveCount.trim())) + " Watching");
                } else {
                    viewCount.set(getViewCount());
                }
            } catch (Exception e) {
                viewCount.set(liveCount + " Views");
                e.printStackTrace();
            }
        }


        @Override
        public void setUpViewersRequest() {
            if (liveCountHandler != null)
                liveCountHandler.postDelayed(updateTotalViewRunnable, 5000);
        }

        @Override
        public void showToast(String message) {
            //new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(RheoTvApp.getNonUiContext(), "" + message, Toast.LENGTH_SHORT).show());
        }

        @Override
        public void waitAndReconnect() {
            if (messageHandler != null)
                messageHandler.post(() -> {
                    if (getChatHelper() != null && getChatHelper().getChatState() == ChatHelper.CHAT_STATE_DISCONNECTED) {
                        ChatLogs.getInstance().addEventToFile("Wait and Reconnect", System.currentTimeMillis(), getAuthorDetail().getUsername());
                        isConnectChatRequestMade.set(false);
                        reconnectChat.setValue(System.currentTimeMillis());
                    }
                });
        }

        @Override
        public void onConnectionComplete() {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getChatHelper() != null && getChatHelper().getChatState() == ChatHelper.CHAT_STATE_DISCONNECTED) {
                        isConnectChatRequestMade.set(false);
                        ChatLogs.getInstance().addEventToFile("Checking to reconnect chat client", System.currentTimeMillis(), getAuthorDetail().getUsername());
                        Log.i("ChatConnection", "Checking to reconnect chat client");
                        if (isPageSelected) {
                            ChatLogs.getInstance().addEventToFile("Reconnecting chat client", System.currentTimeMillis(), getAuthorDetail().getUsername());
                            Log.i("ChatConnection", "Reconnecting chat client");
                            connectChat();
                        }
                    }
                }
            }, 2000);

        }
    };


    private void updateBottomSheetWithNewDynamicAction(StreamEventResponse response) {

    }

    String getViewCount() {
        return currentPost.get() != null ? CommonUtils.formatValue(currentPost.get().getTotalViews()) + " Views" : (postObject.get() != null ? postObject.get().getTotalViews() + " Views" : "");
    }

    public void showRewardWonMessage() {
        List<StreamEvent> result = new ArrayList<>();
        result.add(new StreamEvent(CommonUtils.getUserProfilePic(), CommonUtils.getUserName()));
        StreamEventResponse message = new StreamEventResponse(getPostId(), CommonUtils.getUserName() + " completed a 10 min milestone", AppConstants.EVENT_WON_REWARD, result);
        getChatHelper().sendMessage(gson.toJson(message), AppConstants.MSG_DYNAMIC_PLAYER_ACTIONS, getPostId(), chatHelperCallback);
    }

    protected AtomicBoolean isConnectChatRequestMade = new AtomicBoolean(false);

    public void connectChat() {
        if (getPostId() != null && !getPostId().isEmpty() && !isConnectChatRequestMade.get() && getChatHelper() != null) {
            isConnectChatRequestMade.set(true);
            getChatHelper().setPostChatJoinTask(new WeakReference<>(chatHelperCallback), getPostId(), live.get(), getAuthorDetail().getUsername());
        }
    }

    public ChatMenuOptionData getChatOptionMenuBottomSheetData(CommentChat commentChat, String username, String profilePic) {
        return new ChatMenuOptionData(
                username, profilePic, authorUsername(),
                (followUserName, listener, followStatusListener) -> {
                    loadUserFollowStatus(followUserName, listener, followStatusListener);
                    return null;
                },
                (followState, followUserId, followUserName, listener) -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("is_first", CommonUtils.isFirstTimeFollow());
                    map.put("author", authorUsername());
                    map.put("source", SegmentConstants.SCREEN_NAME_VIDEO_PLAYER_CHAT);
                    SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, map);
                    CommonUtils.setFirstTimeFollow();
                    onFollowButtonClick(followState, followUserId, followUserName, listener);
                    return null;
                },
                () -> {
                    openLoginPage();
                    return null;
                }, null);
    }

    private LocalCommentMessageCallback localCommentMessageCallback = chatMessage -> publishChat(CommentChat.getComment(chatMessage));

    private LocalCommentMessageCallback getLocalCommentMessageCallback() {
        return chatMessage -> publishChat(CommentChat.getComment(chatMessage));
    }

    public void sendChat(View view) {
        EventBus.getDefault().post(new EventBusModel.UpdateBackPress(false));
        if (CommonUtils.isUserLoggedin()) {
            if (canComment != null && canComment.get() != null && canComment.get()) {
                isChatSentWhenKeyboardOpened = true;
                if (orientation.get() == Configuration.ORIENTATION_PORTRAIT) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            CommonUtils.hideKeyboard((Activity) view.getContext());
                        }
                    }, 1000);

                    isChatBoxVisible.set(false);
                }
                if (getNavigator() != null) {
                    getNavigator().trackComment(enterComment.get(), false);
                }
                Log.e(TAG, "Sending my chat from publish.");
                if (enterComment != null && !TextUtils.isEmpty(enterComment.get())) {
                    if (localCommentMessageCallback == null) {
                        localCommentMessageCallback = getLocalCommentMessageCallback();
                    }
                    getChatHelper().sendMessage(enterComment.get(), messageType, getPostId(), chatHelperCallback, localCommentMessageCallback);
                    enterComment.set("");
                    messageType = AppConstants.MSG_TYPE_TEXT;
                    askToComment.set(false);
                }
            } else {
                Toast.makeText(RheoTvApp.getNonUiContext(), "You are not allowed to post messages in this live stream.", Toast.LENGTH_SHORT).show();
            }
        } else {
            getNavigator().openLoginFlow("");
        }
    }

    public void sendChat(String comment) {
        EventBus.getDefault().post(new EventBusModel.UpdateBackPress(false));
        if (CommonUtils.isUserLoggedin()) {
            if (canComment != null && canComment.get() != null && canComment.get()) {
                isChatSentWhenKeyboardOpened = true;
                if (orientation.get() == Configuration.ORIENTATION_PORTRAIT) {
                    isChatBoxVisible.set(false);
                }
                if (getNavigator() != null) {
                    getNavigator().trackComment(comment, false);
                }
                Log.e(TAG, "Sending my chat from publish.");
                if (comment != null && !TextUtils.isEmpty(comment)) {
                    if (localCommentMessageCallback == null) {
                        localCommentMessageCallback = getLocalCommentMessageCallback();
                    }
                    getChatHelper().sendMessage(comment, messageType, getPostId(), chatHelperCallback, localCommentMessageCallback);
                    enterComment.set("");
                    messageType = AppConstants.MSG_TYPE_TEXT;
                    askToComment.set(false);
                }
            } else {
                Toast.makeText(RheoTvApp.getNonUiContext(), "You are not allowed to post messages in this live stream.", Toast.LENGTH_SHORT).show();
            }
        } else {
            getNavigator().openLoginFlow("");
        }
    }

    public void sendSuggestionChat(String message) {
        if (CommonUtils.isUserLoggedin()) {
            if (canComment != null && canComment.get() != null && canComment.get()) {
                if (getNavigator() != null) {
                    getNavigator().trackComment(message, true);
                }
                Log.e(TAG, "Sending my chat from publish.");
                if (localCommentMessageCallback == null) {
                    localCommentMessageCallback = getLocalCommentMessageCallback();
                }
                getChatHelper().sendMessage(message, getPostId(), messageType, chatHelperCallback, localCommentMessageCallback);
                messageType = AppConstants.MSG_TYPE_TEXT;
                askToComment.set(false);
            } else {
                Toast.makeText(RheoTvApp.getNonUiContext(), "You are not allowed to post messages in this live stream.", Toast.LENGTH_SHORT).show();
            }
        } else {
            getNavigator().openLoginFlow("");
        }
    }

    public void onPlayRequestSend() {
        ArrayList<StreamEvent> list = new ArrayList<>();
        list.add(new StreamEvent(CommonUtils.getUserProfilePic(), CommonUtils.getUserName()));
        StreamEventResponse response = new StreamEventResponse(getPostId(), "Request to play", AppConstants.EVENT_PLAY_REQUEST, list);
        if (localCommentMessageCallback == null) {
            localCommentMessageCallback = getLocalCommentMessageCallback();
        }
        getChatHelper().sendMessage(gson.toJson(response), AppConstants.MSG_DYNAMIC_PLAYER_ACTIONS, getPostId(), chatHelperCallback, localCommentMessageCallback);
    }

    public void pinComment(CommentChat chat, String messageType) {
        getChatHelper().pinMessage(chat, messageType, getPostId(), chatHelperCallback);
    }

    public void sendDeletedMessage(String message, String username, String messageType) {
        getChatHelper().sendDeletedMessage(message, username, getPostId(), messageType, chatHelperCallback);
    }

    public void disconnectChat() {
        getChatHelper().closeConnection(getPostId(), chatHelperCallback, getAuthorDetail().getUsername(), true);
    }

    @Override
    public void addOnPropertyChangedCallback(
            Observable.OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(
            Observable.OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    /**
     * Notifies observers that all properties of this instance have changed.
     */
    void notifyChange() {
        callbacks.notifyCallbacks(this, 0, null);
    }

    /**
     * Notifies observers that a specific property has changed. The getter for the
     * property that changes should be marked with the @Bindable annotation to
     * generate a field in the BR class to be used as the fieldId parameter.
     *
     * @param fieldId The generated BR id for the Bindable field.
     */
    public void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

    public void updateScratchCard(String rewardId, Context context) {
        getDataManager().updateDailyScratchCard(rewardId).enqueue(new Callback<RewardTakenResponse>() {
            @Override
            public void onResponse(Call<RewardTakenResponse> call, Response<RewardTakenResponse> response) {
                //Log.i(getClass().getName(), "updateScratchCard Success " + new Gson().toJson(response) + " rewardId: " + rewardId);
                if (response.isSuccessful() && response.body().isSuccessful()) {
                    Log.i(getClass().getName(), "updateScratchCard Success");

                    HashMap<String, Object> property = new HashMap<>();
                    property.put("rewardId", rewardId);
                    property.put("postId", getPostId());
                    property.put("userName", CommonUtils.getUserName(RheoTvApp.getNonUiContext()));

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

    public void updateScratchCardStatusShown(String rewardId, String rewardType) {
        getDataManager().updateScratchCardStatusShown(rewardId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Response<RewardTakenResponse>>() {
                    @Override
                    public void onNext(Response<RewardTakenResponse> rewardTakenResponse) {
                        if (rewardTakenResponse != null && rewardTakenResponse.isSuccessful()) {
                            if (rewardType.equalsIgnoreCase(AppConstants.REWARD_TYPE_SHARE))
                                CommonUtils.markFirstShareDone();
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

    protected void populatePost(Result result) {
        if (postObject.get() != null && postObject.get().getVideoUrl() == null) {
            PostObject post = postObject.get();
            post.setThumbnail(result.getThumbnail());
            post.setLive(result.getIsLive());
            post.setTitle(result.getTitle());
            post.setShareUrl(result.getShareUrl());
            post.setGame(new GameObject(result.getGame()));
            post.setTotalViews(result.getTotalViews());
            post.setWatchingCount(result.getWatchingCount());
            post.setAuthor(new AuthorObject(result.getAuthor().getProfilePic(), result.getAuthor().getUser()));
            post.setVideoUrls(result.getVideoUrls());
            postObject.set(post);
            notifyChange();
        }
    }

    public void sendGreeting(Sticker sticker, String message) {
        getCompositeDisposable().add(getDataManager().buySticker(sticker.getId(), getPostId(), message)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(next -> {
                    if (next != null) {
                        Toast.makeText(RheoTvApp.getNonUiContext(), "Greetings sent successfully", Toast.LENGTH_SHORT).show();
                        RewardManager.getInstance().reduceCoin(sticker.getValue());
                        PostGift postGift = new PostGift();
                        postGift.setUser(CommonUtils.getUserID());
                        postGift.setUsername(CommonUtils.getUserName());
                        postGift.setProfilePic(CommonUtils.getUserProfilePic());
                        postGift.setType(sticker.getType());
                        postGift.setMessage(message);
                        long startTime = System.currentTimeMillis();
                        postGift.setStartTimeTs(TimeUtils.getFormattedDate(TimeUtils.YYYY_MM_DD_T_HH_MM_SS_SSSXXX, new Date(startTime)));
                        postGift.setEndTimeTs(TimeUtils.getFormattedDate(TimeUtils.YYYY_MM_DD_T_HH_MM_SS_SSSXXX, new Date(startTime + sticker.getDuration() * 1000)));
                        if (postGiftMessageHandler != null)
                            postGiftMessageHandler.add(postGift, true);
                    }
                }, throwable -> {
                    Log.e(TAG, "Error in api sendSticker --> " + throwable.getMessage());
                }));
    }

    public void sendSticker(Sticker sticker, View view) {
        getCompositeDisposable().add(getDataManager()
                .buySticker(sticker.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(next -> {
                    if (next != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(next.string());
                            if (jsonObject.has("success") && jsonObject.getBoolean("success")) {
                                enterComment.set(sticker.getStickerUrl());
                                messageType = AppConstants.MSG_TYPE_STICKER;
                                sendChat(view);
                                RewardManager.getInstance().reduceCoin(sticker.getValue());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, throwable -> {
                    Log.e(TAG, "Error in api sendSticker --> " + throwable.getMessage());
                }));
    }

    protected void loadSharableContent() {
        if (!isPageSelected) return;
        getDataManager().loadShareContent(getPostId()).enqueue(new Callback<ShareResponse>() {
            @Override
            public void onResponse(@NotNull Call<ShareResponse> call, @NotNull Response<ShareResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    shareEventData.setValue(new StreamEventResponse("Game Moment is ready. \nShare it now!", response.body().getShareData()));
                }
            }

            @Override
            public void onFailure(@NotNull Call<ShareResponse> call, @NotNull Throwable t) {

            }
        });
    }

//    todo
//    void addDefaultEvent() {
//        if (streamEventHandler != null && RewardManager.getInstance().isTenMinuteStreamRewardAvailable()) {
//            streamEventHandler.add(new StreamEventResponse(AppConstants.EVENT_REWARD_TIME, "New Scratch card coming in 10 minutes"));
//        }
//    }
//
//    void addRewardTime(long rewardTime) {
//        if (streamEventHandler != null && RewardManager.getInstance().isTenMinuteStreamRewardAvailable()) {
//            streamEventHandler.add(new StreamEventResponse(AppConstants.EVENT_REWARD_TIME, "New Scratch card coming in " + rewardTime + " minutes"), true);
//        }
//    }

    private void publishChat(CommentChat commentChat) {
//        Log.i(getClass().getSimpleName(), "message_is: " + commentChat.getUsername() + ": " + commentChat.getMessage() + " and " + commentChat.getMessageType());
        incomingComment.setValue(commentChat);
        updateCheckViews.setValue(System.currentTimeMillis());
    }

    public void sendRequestPlayFragmentUpdateMessage() {
        StreamEventResponse message = new StreamEventResponse(getPostId(), getPostId(), AppConstants.MSG_TYPE_CUSTOM_ROOM_UPDATE, null);
        getChatHelper().sendMessage(gson.toJson(message), AppConstants.MSG_DYNAMIC_PLAYER_ACTIONS, getPostId(), chatHelperCallback);
        //getChatHelper().sendMessage(postId, AppConstants.MSG_DYNAMIC_PLAYER_ACTIONS, postId, chatHelperCallback);
    }

    void loadUserFollowStatus(String username, ApiCompleteListener callback, FollowStatusListener followStatusListener) {
        AtomicBoolean isUserFound = new AtomicBoolean(false);
        AppUtilsKt.INSTANCE.runOnIO(this, () -> {
            UserFollowItem userFollowItem = dao.checkIfIsFollowedWithUsername(username);
            AppUtilsKt.INSTANCE.runOnMain(this, () -> {
                if (userFollowItem != null) {
                    Log.i("######", "user found  -->  " + userFollowItem.isFollowed());
                    isUserFound.set(true);
                    if (followStatusListener != null)
                        followStatusListener.followStatus(userFollowItem.isFollowed());
                    if (username.equalsIgnoreCase(authorUsername())) {
                        onFollowingUpdate.set(userFollowItem.isFollowed());
                    }
                }
                if (!CommonUtils.isUserLoggedin() && followStatusListener != null)
                    followStatusListener.followStatus(false);
                getDataManager().getProfile(username).enqueue(new Callback<ProfileResult>() {
                    @Override
                    public void onResponse(Call<ProfileResult> call, Response<ProfileResult> response) {
                        if (response != null && response.body() != null) {
                            if (CommonUtils.isUserLoggedin())
                                insertFollowStatusInDB(response.body().getUser().getId(), username, response.body().getFollowed(), isFollowed -> {
                                    if (!isUserFound.get()) {
                                        if (followStatusListener != null)
                                            followStatusListener.followStatus(isFollowed);
                                        if (username.equalsIgnoreCase(authorUsername())) {
                                            onFollowingUpdate.set(isFollowed);
                                        }
                                    }
                                });
                            if (callback != null) {
                                callback.updateProfileDataForBottomSheet(new FollowResult.Success(response.body()));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileResult> call, Throwable t) {
                        if (callback != null) {
                            callback.updateProfileDataForBottomSheet(new FollowResult.Error(t));
                        }
                    }
                });
                return null;
            });
            return null;
        });
    }

    private void insertFollowStatusInDB(int userId, String username, boolean followState, FollowStatusListener followStatusListener) {
        AppUtilsKt.INSTANCE.runOnIO(this, () -> {
            dao.insertUserWithIgnore(new UserFollowItem(userId, username, followState));
            Log.i("######", "inserted  -->  " + followState);
            AppUtilsKt.INSTANCE.runOnMain(this, () -> {
                if (followStatusListener != null)
                    followStatusListener.followStatus(followState);
                return null;
            });
            return null;
        });
    }

    public void postHeart() {
        getDataManager().postHeart(getPostId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "post_heart_success");
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                Log.i(TAG, "post_heart_fail " + t);
            }
        });
    }

    public void getTopFans() {
        getDataManager().fetchTopFans(authorUsername()).enqueue(new Callback<TopFansResponse>() {
            @Override
            public void onResponse(@NonNull Call<TopFansResponse> call, @NonNull Response<TopFansResponse> response) {

            }

            @Override
            public void onFailure(@NonNull Call<TopFansResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    void addRewardTime(long rewardTime) {
//        if (streamEventHandler != null && RewardManager.getInstance().isTenMinuteStreamRewardAvailable()) {
//            streamEventHandler.add(new StreamEventResponse(AppConstants.EVENT_REWARD_TIME, "New Scratch card coming in " + rewardTime + " minutes"), true);
//        }
    }

    public int getTotalCallRequestCount() {
        return currentPost.get().getTotalCallCount();
    }

    //    private class StreamEventHandler extends StreamHandler<StreamEventResponse> {
//        @Override
//        public void publish() {
//            StreamEventResponse event = queue.poll();
//            // i.e. events are consumed
//            if (event == null) {
//                isPublishing = false;
//                return;
//            }
//
//            if (event.getType().equalsIgnoreCase(AppConstants.EVENT_RECENT_FOLLOWERS)) {
//                recentFollowersEventTitle.set(event.getText());
//                recentFollowerEvent.setValue(event.getResult());
//            } else if (event.getType().equalsIgnoreCase(AppConstants.EVENT_PLAY_REQUEST)) {
//                requestPlayEvent.set(event);
//            } else if (event.getType().equalsIgnoreCase(AppConstants.EVENT_ANNOUNCEMENT)) {
//                announcementEvent.set(event);
//            } else if (event.getType().equalsIgnoreCase(AppConstants.EVENT_FOLLOWED)) {
//                followedEvent.set(event);
//            } else if (event.getType().equalsIgnoreCase(AppConstants.EVENT_WON_REWARD)) {
//                videoWatchRewardEvent.set(event);
//            } else if (event.getType().equalsIgnoreCase(AppConstants.EVENT_REWARD_TIME)) {
//                rewardTimerEvent.set(event);
//            } else if (AppUtilsKt.INSTANCE.isCustomRoomMessage(event.getType())) {
//                updateCustomRoomPage.set(event.getText());
//            } else if (event.getType().equalsIgnoreCase(AppConstants.EVENT_CUSTOM_ROOM) || event.getType().equalsIgnoreCase(AppConstants.EVENT_CUSTOM_ROOM_REJECT) || event.getType().equalsIgnoreCase(AppConstants.EVENT_CUSTOM_ROOM_REFUNDED)) {
//                if (event.getUserId() != CommonUtils.getUserID()) {
//                    eventHandler.post(eventRunner);
//                    updateCustomRoomPage.set(event.getText());
//                    return;
//                }
//                acceptedRequest.set(event);
//            } else if (event.getType().equalsIgnoreCase(AppConstants.MSG_TYPE_CUSTOM_ROOM_UPDATE) || event.getType().equalsIgnoreCase(AppConstants.EVENT_CUSTOM_ROOM_WINNER)) {
//                updateCustomRoomPage.set(event.getText());
//            } else if (event.getType().equalsIgnoreCase(AppConstants.EVENT_SHARE_MOMENT_AVAILABLE)) {
//                shareEventData.setValue(event);
//                eventHandler.post(eventRunner);
//                return;
//            } else if (event.getType().equalsIgnoreCase(AppConstants.EVENT_ON_POST_SHARE)) {
//                if (event.getUserId() == CommonUtils.getUserID()) return;
//                onPostShareEvent.setValue(event);
//                eventHandler.post(eventRunner);
//                return;
//            }
//
//            currentEvent.setValue(event.getType());
//            eventHandler.postDelayed(eventRunner, 5000);
//        }
//
//    }
//
    protected class StreamMessageHandler extends StreamHandler<CommentChat> {

        @Override
        public void publish() {
            if (isQueueEmpty()) {
                return;
            }
            int queueSize = queue.size();
            float pollTime = 20 / queueSize;
            if (pollTime >= 1) {
                pollTime = 1;
            } else {
                pollTime = (float) Math.max(pollTime, 0.2);
            }

            CommentChat commentChat = queue.poll();
            publishChat(commentChat);
            eventHandler.postDelayed(eventRunner, (long) (pollTime * 1000));
        }
    }

    public void initHandlers() {
//        if (liveCountHandler == null)
//            liveCountHandler = new Handler(Looper.getMainLooper());
//        if (messageHandler == null)
//            messageHandler = new Handler(Looper.getMainLooper());
//        if (streamEventHandler == null)
//            streamEventHandler = new StreamPlayerViewModel.StreamEventHandler();
        if (postGiftMessageHandler == null)
            postGiftMessageHandler = new PostGiftMessageHandler();
        if (streamMessageHandler == null)
            streamMessageHandler = new StreamMessageHandler();
    }

    //
    public void removeRunnableFromHandler() {
//        if (streamEventHandler != null)
//            streamEventHandler.removeCallbacks();
        if (liveCountHandler != null)
            liveCountHandler.removeCallbacks(updateTotalViewRunnable);
        if (postGiftMessageHandler != null)
            postGiftMessageHandler.removeCallbacks();
        if (streamMessageHandler != null)
            streamMessageHandler.removeCallbacks();
//        messageHandler = null;
//        liveCountHandler = null;
    }

    //
    String[] colors = {"#10945f", "#945010", "#c8931f", "#9021ff", "#cf1d75", "#2178ff"};

    private class PostGiftMessageHandler extends StreamHandler<PostGift> {

        @Override
        public void publish() {
            if (isQueueEmpty()) {
                postGift.set(null);
                return;
            }
            int position = CommonUtils.getRandomNumberInRange(0, colors.length - 1);
            PostGift postGift = queue.poll();
            postGift.setBackgroundTintColor(colors[position]);
            StreamPlayerViewModelV2.this.postGift.set(postGift);
            eventHandler.postDelayed(eventRunner, TimeUtils.getTimeDiffInMs(postGift.getStartTimeTs(), postGift.getEndTimeTs()));
        }
    }

    public void fetchTopFans() {
        getDataManager().fetchTopFans(authorUsername()).enqueue(new Callback<TopFansResponse>() {
            @Override
            public void onResponse(Call<TopFansResponse> call, Response<TopFansResponse> response) {
                if (response.isSuccessful()) {
                    List<TopFans> serverList = response.body() != null && response.body().getData() != null ? response.body().getData() : new ArrayList<>();
                    if (!serverList.isEmpty() && serverList.size() > 4) {
                        List<TopFans> list = serverList.subList(0, 3);
                        Collections.reverse(list);
                        topThreeFans.setValue(list);
                    } else {
                        Collections.reverse(serverList);
                        topThreeFans.setValue(serverList);
                    }
                }
            }

            @Override
            public void onFailure(Call<TopFansResponse> call, Throwable t) {

            }
        });
    }

    public void onRewind(long startTime, long endTime) {
        try {
            getDataManager().rewindEvent(getPostId(),
                    CommonUtils.getUserName(),
                    authorUsername(),
                    currentPost.get().startFrom(),
                    startTime,
                    endTime,
                    getGameName(),
                    CommonUtils.getUserLanguage(),
                    getPostUrl()
            ).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.i(TAG, "rewind_onResponse");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i(TAG, "rewind_onFailure");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
