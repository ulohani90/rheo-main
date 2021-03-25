package com.rheotv.android.ui.activities.moments.view.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.Observable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.TimerObj;
import com.rheotv.android.data.network.models.gamify.Reward;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat;
import com.rheotv.android.data.network.models.postlisting.responses.PostGift;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.postlisting.responses.User;
import com.rheotv.android.data.network.models.postlisting.responses.UserObject;
import com.rheotv.android.data.network.models.postlisting.responses.VideoCallUsersListObject;
import com.rheotv.android.data.network.models.stickers.Sticker;
import com.rheotv.android.data.network.models.streamUpdates.StreamEventResponse;
import com.rheotv.android.databinding.FragmentMomentsBinding;
import com.rheotv.android.factories.ViewModelProviderFactoryV2;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.helpers.grpc.ChatHelper;
import com.rheotv.android.services.AudioRoomService;
import com.rheotv.android.services.PlayerHeadHolder;
import com.rheotv.android.ui.activities.customroom.view.CustomRoomBottomSheet;
import com.rheotv.android.ui.activities.home.view.HomeActivity;
import com.rheotv.android.ui.activities.inAppBilling.BillingActivity;
import com.rheotv.android.ui.activities.inAppBilling.BuyCoinFragment;
import com.rheotv.android.ui.activities.moments.model.MomentsListItem;
import com.rheotv.android.ui.activities.moments.view.activities.MomentsActivity;
import com.rheotv.android.ui.activities.moments.viewmodel.MomentsFragmentViewModel;
import com.rheotv.android.ui.activities.onboarding.v2.UserLanguage;
import com.rheotv.android.ui.activities.player.activity.ChatBoxBottomSheetDialog;
import com.rheotv.android.ui.activities.player.activity.ChatBoxCallbackListener;
import com.rheotv.android.ui.activities.player.activity.ChatMenuOptionBottomSheet;
import com.rheotv.android.ui.activities.player.activity.ListOption;
import com.rheotv.android.ui.activities.player.activity.PlayerGiftBottomSheet;
import com.rheotv.android.ui.activities.player.activity.PlayerTutorialActivity;
import com.rheotv.android.ui.activities.player.activity.PlayerVideoQualityBottomSheet;
import com.rheotv.android.ui.activities.player.activity.RequestPlayFragment;
import com.rheotv.android.ui.activities.player.activity.StickerBottomSheet;
import com.rheotv.android.ui.activities.player.activity.StickerGridRecyclerAdapter;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerNavigator;
import com.rheotv.android.ui.activities.player.activity.ViewPagerMediator;
import com.rheotv.android.ui.activities.player.activity.newPlayer.VideoCallJobIntentService;
import com.rheotv.android.ui.activities.player.activity.newPlayer.fragments.RequestToVideoCallBottomSheet;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.share.PostShareBottomSheetFragment;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivity;
import com.rheotv.android.ui.adapters.ChatListAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.customViews.HeartAnimator;
import com.rheotv.android.ui.customViews.streamPlayer.StreamHolder;
import com.rheotv.android.ui.customViews.streamPlayer.StreamPlayerCallbackListener;
import com.rheotv.android.ui.customViews.streamPlayer.StreamTapPlayerView;
import com.rheotv.android.ui.customViews.streamPlayer.StreamUtils;
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog;
import com.rheotv.android.ui.fragments.ScratchCardNavigator;
import com.rheotv.android.ui.fragments.ScratchDialogFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppUtils;
import com.rheotv.android.utils.AppUtilsKt;
import com.rheotv.android.utils.BackPressUpdateClickListener;
import com.rheotv.android.utils.ChatLogs;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.utils.NetworkUtils;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.TimeUtils;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.customview.AnimatedScratchCardView;
import com.rheotv.android.utils.hourglass.Hourglass;
import com.rheotv.android.utils.hourglass.HourglassAsync;
import com.rheotv.android.utils.segmentTracker.EqualSpaceItemDecorator;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import eightbitlab.com.blurview.RenderScriptBlur;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;
import static com.rheotv.android.utils.AppConstants.REWARD_TYPE_TEN_MINUTE_STREAM;
import static com.rheotv.android.utils.segmentTracker.SegmentConstants.EVENT_VIDEO_WATCH_WITH_CHATROOM_ACTIVE;

public class MomentsFragment extends BaseFragment<FragmentMomentsBinding, MomentsFragmentViewModel>
        implements StreamPlayerCallbackListener, ViewPagerMediator.ViewPagerOnPageSelectedListener,
        StreamTapPlayerView.PlayerAnalyticsListener, HeartAnimator.HeartAnimatorInteractionListener,
        StreamPlayerNavigator, LoginFragmentBottomDialog.LoginFragmentCallback, StickerGridRecyclerAdapter.StickerSelectionListener,
        ScratchCardNavigator, PlayerVideoQualityBottomSheet.VideoQualityChangeListener, ChatListAdapter.ChatItemClickListenerV2,
        ChatBoxCallbackListener {

    private final String TAG = getClass().getSimpleName();

    @Inject
    ViewModelProviderFactoryV2 mViewModelFactory;
    private MomentsFragmentViewModel mViewModel;
    private FragmentMomentsBinding mBinding;
    private StreamHolder streamHolder;
    //    private StreamChatFragmentV2 chatFragment;
//    private TopThreeFansAdapter topThreeFansAdapter;

    private boolean isFragmentRecreated = false;
    private boolean isStickerBottomSheetVisible = false;
    public String streamFragmentPosition;

    private ChatListAdapter chatAdapter;
    private Observable.OnPropertyChangedCallback askToCommentCallback;
    private Observable.OnPropertyChangedCallback chatBoxCallback;

    private HashMap<String, Object> baseProperties;
    private String sourceScreen;
    private String screenName;
    private long videoStartTime = 0;
    private String videoQuality = "auto";
    public PostObject postObject;

//    private boolean isIntroVideoUrlHandled;


    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkConnected())
                mViewModel.connectChat();
        }
    };

    public static MomentsFragment getInstance(MomentsListItem postObject, String sourceScreen) {
        MomentsFragment fragment = new MomentsFragment();
        fragment.streamFragmentPosition = postObject != null ? postObject.getId() : "Default";
        fragment.sourceScreen = sourceScreen;
        Bundle bundle = new Bundle();
        bundle.putParcelable(AppConstants.ARG_POST, postObject);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        Log.i(TAG, "StreamPlayFragment: getLayoutId");
        return R.layout.fragment_moments;
    }

    @Override
    public MomentsFragmentViewModel getViewModel() {
        if (mViewModel == null) {
            mViewModel = new ViewModelProvider(this, mViewModelFactory)
                    .get(MomentsFragmentViewModel.class);
            mViewModel.setNavigator(this);
            if (getArguments() != null) {
                mViewModel.getMoments().set(getArguments().getParcelable(AppConstants.ARG_POST));
//                moments = mViewModel.getMoments().get();
                mViewModel.postObject.set(mViewModel.getMoments().get().getPostDetails());
                postObject = mViewModel.postObject.get();
                mViewModel.viewCount.set(mViewModel.postObject.get().getViews());
                mViewModel.live.set(mViewModel.postObject.get().isLive());
                mViewModel.loadPost();
//                mViewModel.fetchTopFans();
                //mViewModel.loadInitialComments(null);
            }
        }

//        mViewModel.loadDailyRewards();
        return mViewModel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sourceScreen != null && !sourceScreen.isEmpty()) {
            mViewModel.baseProperties.put(AppConstants.SCREEN_SOURCE, sourceScreen);
        }

        if (getActivity() instanceof HomeActivity)
            screenName = SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE;
        else
            screenName = SegmentConstants.SCREEN_NAME_VIDEO_PLAYER;

        if (getActivity() instanceof HomeActivity && Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mScratchCardBottomMargin += ViewUtils.dpToPx(12);
        }
        mViewModel.baseProperties.put(AppConstants.SCREEN_NAME, screenName);
        baseProperties = new HashMap<>(mViewModel.baseProperties);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() instanceof MomentsActivity) {
            ViewCompat.setOnApplyWindowInsetsListener(getViewDataBinding().getRoot(), (v, insets) -> {
                int bottom = insets.getSystemWindowInsetBottom() - ViewUtils.getNavBarHeight(requireContext());
                if (bottom < 0) {
                    bottom = insets.getSystemWindowInsetBottom();
                }
                return ViewCompat.onApplyWindowInsets(v, insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), 0,
                        insets.getSystemWindowInsetRight(), bottom));
            });
        }
        mBinding = getViewDataBinding();
        if (mBinding.getRoot() instanceof ViewGroup) {
            LayoutTransition layoutTransition = ((ViewGroup) mBinding.getRoot()).getLayoutTransition();
            if (layoutTransition != null) {
                layoutTransition.setAnimateParentHierarchy(false);
            }
        }
        mViewModel.orientation.set(Resources.getSystem().getConfiguration().orientation);
        mViewModel.reconnectChat.observe(getViewLifecycleOwner(), reconnect -> reconnectedChat());
        switchChatContainer(mViewModel.orientation.get());
        if (savedInstanceState != null)
            isFragmentRecreated = true;

        if (chatAdapter == null)
            chatAdapter = new ChatListAdapter(new ArrayList<>(), Resources.getSystem().getConfiguration().orientation, true);
        chatAdapter.setChatStickerSize(stickerSize());
        chatAdapter.setListener(this);
        LinearLayoutManager layoutManager = (LinearLayoutManager) mBinding.recyclerView.getLayoutManager();
        if (mViewModel != null && mViewModel.getMoments().get() != null && !mViewModel.getMoments().get().isContentModerator()) {
            mBinding.recyclerView.addItemDecoration(new EqualSpaceItemDecorator((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics())));
            mBinding.recyclerView.setAdapter(chatAdapter);
            mBinding.videoView.setAnalyticsListener(this);
            mBinding.recyclerView.setNestedScrollingEnabled(false);
            mBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (layoutManager == null) return;
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    if (!mViewModel.isLoading.get() && mViewModel.commentNextUrl != null && totalItemCount >= 10 && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0 && chatAdapter != null) {
                        mViewModel.isLoading.set(true);
                        mBinding.recyclerView.post(() -> {
                            if (chatAdapter != null)
                                chatAdapter.setShowLoading(true);
                        });
                        mViewModel.loadComments();
                    }

                    if (firstVisibleItemPosition == 0)
                        mViewModel.unreadChatCount.set(0);
                }
            });
        }
//        mBinding.actionButtonView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                if (mViewModel.currentPost.get() != null) {
//                    float y = mBinding.videoView.getY();
//                    if (y > 0) {
//                        y += mBinding.videoView.getHeight();
//                        float actionButtonLayoutY = mBinding.actionButtonView.getY();
//                        if (actionButtonLayoutY < y) {
//                            mBinding.videoView.setShouldUpdateRotateButtonPosition(true);
//                        }
//                        mBinding.videoView.updateRotateButtonPosition();
//                        mBinding.actionButtonView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                    }
//                }
//            }
//        });

//        if (topThreeFansAdapter == null)
//            topThreeFansAdapter = new TopThreeFansAdapter();
//        mBinding.topFanRv.addItemDecoration(new RightOverlapDecorator(-24));
//        mBinding.topFanRv.setAdapter(topThreeFansAdapter);

        mBinding.nextActionButton.setOnClickListener(v -> EventBus.getDefault().post(new EventBusModel.Next(mViewModel.getPostId())));
        mBinding.cancelButton.setOnClickListener(v -> mBinding.nextVideoContainer.setVisibility(View.GONE));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel.loadPostStatus.observe(getViewLifecycleOwner(), this::connectPlayer);
        mViewModel.comments.observe(getViewLifecycleOwner(), comments -> chatAdapter.addItems(comments));
        mViewModel.incomingComment.observe(getViewLifecycleOwner(), comment -> chatAdapter.addItem(comment));
        mViewModel.removeChat.observe(getViewLifecycleOwner(), pair -> chatAdapter.removeChatItem(pair.first, pair.second));
        mViewModel.updateCheckViews.observe(getViewLifecycleOwner(), check -> updateChatViews());
        mViewModel.blockUserStatus.observe(getViewLifecycleOwner(), this::onUserBlock);
        mViewModel.reportComment.observe(getViewLifecycleOwner(), this::onCommentReport);
        mViewModel.deleteComment.observe(getViewLifecycleOwner(), this::onCommentDelete);
        mViewModel.commentSuggestion.observe(getViewLifecycleOwner(), this::setSuggestion);
//        mViewModel.topThreeFans.observe(getViewLifecycleOwner(), topFans -> topThreeFansAdapter.submitList(topFans));

        if (askToCommentCallback == null) {
            askToCommentCallback = new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
                        return;
                    if (mViewModel != null && mViewModel.askToComment != null && mViewModel.askToComment.get() != null && mViewModel.askToComment.get())
                        chatAdapter.addWelcomeNote(mViewModel.authorUsername());
                }
            };
        }
        if (chatBoxCallback == null) {
            chatBoxCallback = new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
                        return;
                    if (mViewModel != null && mViewModel.isChatBoxVisible != null && mViewModel.isChatBoxVisible.get() != null &&
                            !mViewModel.isChatBoxVisible.get() && mViewModel.isChatSentWhenKeyboardOpened) {
                        mViewModel.isChatSentWhenKeyboardOpened = false;
                        chatAdapter.notifyDataSetChanged();
                        RecyclerView.LayoutManager layoutManager = mBinding.recyclerView.getLayoutManager();
                        if (layoutManager != null) {
                            layoutManager.scrollToPosition(0);
                        }
                    }
                }
            };
        }
        mViewModel.askToComment.addOnPropertyChangedCallback(askToCommentCallback);
        mViewModel.isChatBoxVisible.addOnPropertyChangedCallback(chatBoxCallback);
        mViewModel.isVideoCallEnabled.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                //updateRequestedCount(mViewModel.getTotalCallRequestCount());
                if (mViewModel.isVideoCallEnabled.get() && mViewModel.live.get()) {
                    if (mViewModel.isStreamer()) {
                        //updateRequestedCount(mViewModel.getTotalCallRequestCount());
                    } else {
//                        animateLiveTag(mBinding.liveIconCircle1, mBinding.liveIconCircle2);
                    }
                    HashMap<String, Object> properties = new HashMap<>(baseProperties);

                    baseProperties.put("streamer_name", mViewModel.getAuthorDetail().getUsername());
                    SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_COHOST_ICON_SHOWN, properties);
                }
            }
        });
        mViewModel.callRequestObject.observe(getViewLifecycleOwner(), userObject -> onReceivedCallRequest(userObject));
        setViewModelObservers();
    }

    private void updateRequestedCount(int count) {
        //if (mViewModel.isStreamer()) {
//        if (count > 0) {
//            mBinding.requestsCount.setVisibility(View.VISIBLE);
//            mBinding.requestsCount.setText(count > 50 ? "50+" : count + "");
//        } else {
//            mBinding.requestsCount.setVisibility(View.GONE);
//        }
        // }
    }

    private void animateLiveTag(View liveIconCircle1, View liveIconCircle2) {
        animateLiveTagScaleUp(liveIconCircle1, 0);
        animateLiveTagScaleUp(liveIconCircle2, 500);
    }

    private void animateLiveTagScaleUp(View liveIcon, long delay) {
//        liveIcon.setVisibility(View.VISIBLE);
        ObjectAnimator animX = ObjectAnimator.ofFloat(liveIcon, View.SCALE_X, 1.0f, 10f);
        ObjectAnimator animY = ObjectAnimator.ofFloat(liveIcon, View.SCALE_Y, 1.0f, 10f);
        ObjectAnimator animFadeOut = ObjectAnimator.ofFloat(liveIcon, View.ALPHA, 1f, 0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(800);
        animatorSet.setStartDelay(delay);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animateLiveTagScaleUp(liveIcon, delay);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.playTogether(animX, animY, animFadeOut);
        animatorSet.start();
    }

    private void animateLiveTagScaleIn(View liveIcon, long delay) {
        ObjectAnimator animX = ObjectAnimator.ofFloat(liveIcon, View.SCALE_X, 1.0f, 0.5f);
        ObjectAnimator animY = ObjectAnimator.ofFloat(liveIcon, View.SCALE_Y, 1.0f, 0.5f);
        //ObjectAnimator animFadeIn = ObjectAnimator.ofFloat(liveIcon, View.ALPHA, 0.2f, 1.0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animateLiveTagScaleUp(liveIcon, delay);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.playTogether(animX, animY);
        animatorSet.start();
    }

    public void testCallNotification() {
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("title", "Hurricane01212");
        bundle.putString("post_id", "dsahdsk");
        bundle.putString("channel_id", "dhjsahd");
        bundle.putString("body", "Click to Join");
        bundle.putString("streamer_profile_pic", "url");
        //bundle.putString("streamer_profile_pic", "Hurricane01212");
        bundle.putString("streamer_user_name", "Hurricane01212");
        JobInfo jobInfo = new JobInfo.Builder(123, new ComponentName(getContext(), VideoCallJobIntentService.class))
                .setOverrideDeadline(1).setMinimumLatency(1).setExtras(bundle).build();
        JobScheduler scheduler = (JobScheduler) getContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(jobInfo);
    }

    private void onReceivedCallRequest(StreamEventResponse response) {
        if (response.getState().equalsIgnoreCase(AppConstants.VIDEO_CALL_STATE_INITIATED)) {
            String channelId = response.getChannelId();
            if (response.getUserId() == CommonUtils.getUserID() && !VideoCallJobIntentService.Companion.isServiceRunning() && (CommonUtils.getLastCallChannelId() == null || (CommonUtils.getLastCallChannelId() != null && !CommonUtils.getLastCallChannelId().equalsIgnoreCase(channelId)))) {
                CommonUtils.setLastCallChannelId(channelId);
                PersistableBundle bundle = new PersistableBundle();
                bundle.putString("title", response.getTitle());
                bundle.putString("streamer_profile_pic", response.getUserProfileUrl());
                bundle.putString("streamer_user_name", response.getUsernameForCohost());
                bundle.putString("post_id", response.getPostId());
                bundle.putString("channel_id", response.getChannelId());
                bundle.putString("requester_agora_token", response.getRequesterAgoraToken());
                bundle.putString("body", "Click to Join");
                JobInfo jobInfo = new JobInfo.Builder(123, new ComponentName(getContext(), VideoCallJobIntentService.class))
                        .setOverrideDeadline(1).setMinimumLatency(1).setExtras(bundle).build();
                JobScheduler scheduler = (JobScheduler) getContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
                scheduler.schedule(jobInfo);
            }
        } else if (response.getState().equalsIgnoreCase(AppConstants.VIDEO_CALL_STATE_ENDED)) {
            if (response.getUserId() == CommonUtils.getUserID()) {
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(AppConstants.INTENT_FILTER_DENIED_CALLING_REQUEST));
            }
        }
        VideoCallUsersListObject userObject = new VideoCallUsersListObject(response.getState(), response.getPostId(), response.getChannelId(), new UserObject(new User(response.getUserId(), response.getUsernameForCohost()), response.getUserProfileUrl()));

        showToastForDifferentCallActionCases(userObject.getUserProfile().getUser().getUsername(), userObject.getState());

        Intent intent = new Intent("call_request_action");
        intent.putExtra("user_obj", userObject);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    public void showToastForDifferentCallActionCases(String username, String state) {
        if (state.equalsIgnoreCase(AppConstants.VIDEO_CALL_STATE_REQUESTED)) {
            if (mViewModel.isStreamer()) {
                if (getChildFragmentManager().findFragmentByTag(RequestToVideoCallBottomSheet.TAG) == null || (getChildFragmentManager().findFragmentByTag(RequestToVideoCallBottomSheet.TAG) != null
                        && getChildFragmentManager().findFragmentByTag(RequestToVideoCallBottomSheet.TAG) instanceof RequestToVideoCallBottomSheet
                        && !((RequestToVideoCallBottomSheet) getChildFragmentManager().findFragmentByTag(RequestToVideoCallBottomSheet.TAG)).isVisible())) {
                    mViewModel.currentPost.get().setTotalCallCount(mViewModel.currentPost.get().getTotalCallCount() + 1);
                    updateRequestedCount(mViewModel.currentPost.get().getTotalCallCount());
                    HashMap<String, Object> properties = new HashMap<>(baseProperties);
                    properties.put("request_from", username);
                    SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_COHOST_REQUEST_RECEIVED, properties);
                    showToast(username + " has requested to become a co-host");
                }
            }
        } else if (state.equalsIgnoreCase(AppConstants.VIDEO_CALL_STATE_DENIED)) {
            if (CommonUtils.getUserName().equalsIgnoreCase(username)) {
                showToast("Co-host request is denied");
            }
        } else {
            //showToast(username + " :: " + state);
        }
    }

    private void reconnectedChat() {
        if (mBinding == null || mViewModel == null || isStateSaved() || !isAdded() || isDetached() || isRemoving())
            return;
        if (!NetworkUtils.isNetworkConnected(getContext())) {
            if (!mViewModel.isNetworkChangeListening) {
                getContext().registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                mViewModel.isNetworkChangeListening = true;
            }
        } else {
            ChatLogs.getInstance().addEventToFile("Reconnect chat begin", System.currentTimeMillis(), mViewModel.getAuthorDetail().getUsername());
            mViewModel.connectChat();
            mViewModel.isNetworkChangeListening = false;
        }
    }

    public void checkAndConnectPlayer(Status status) {
        if (handleIntroVideoUrl()) {
            Log.i(TAG, "Handling Intro and Game Rule Video");
        } else {
            connectPlayer(status);
        }
    }

    public void connectPlayer(Status status) {
        if (mBinding == null || mViewModel == null || isStateSaved() || !isAdded() || isDetached() || isRemoving())
            return;

        if (status == Status.SUCCESS) {
            startRewardTimer();
            if (streamHolder == null || isFragmentRecreated) {
                streamHolder = new StreamHolder.Builder()
                        .setContext(getContext())
                        .setPostId(mViewModel.getPostId())
                        .setStartFrom(mViewModel.getStartFrom())
                        .setLive(mViewModel.live.get())
                        .setPlaceholderThumbnail(new WeakReference<>(mBinding.thumbnailImageView))
                        .setSettingIcon(new WeakReference<>(mBinding.settingsIconButton))
                        .setReportIcon(new WeakReference<>(mBinding.flagIconButton))
                        .setProgressView(mBinding.progressBar)
                        .setYoutubeDoubleTap(mBinding.youtubeDoubleTap)
                        .setStreamAuthorHolder(mViewModel.getAuthorDetail())
                        .setStreamUrl(mViewModel.getStreamUrls())
                        .setPromoVideoUrl(mViewModel.getPromoVideoUrl())
                        .setVideoMode(mViewModel.getVideoMode())
                        .setResumePosition(mViewModel.getResumePosition())
                        .setResumeWindow(mViewModel.getResumeWindow())
                        .setGiftEnabled(mViewModel.isRewardIconEnabled.get())
                        .setDoubleTapSendEventEnabled(false)
                        .build();
                mBinding.videoView.setViewCallbackListener(this);
                mBinding.videoView.setHeartAnimatorListener(this);
                isFragmentRecreated = false;
            }

            mBinding.videoView.attach(streamHolder);

            if (mViewModel.isPageSelected) {
//                if (!isIntroVideoUrlHandled) {
//                    if (handleIntroVideoUrl()) {
//                        isIntroVideoUrlHandled = true;
//                    } else {
//                        if (streamHolder != null && mBinding != null && !shouldPausePlayer) {
//                            videoStartTime = System.currentTimeMillis();
//                            mBinding.videoView.attachPlayer();
//                        }
//                    }
//
//                } else {
//                    if (streamHolder != null && mBinding != null && !shouldPausePlayer) {
//                        videoStartTime = System.currentTimeMillis();
//                        mBinding.videoView.attachPlayer();
//                    }
//                }
                if (streamHolder != null && mBinding != null) {
                    mBinding.videoView.attachPlayer();
//                    mBinding.videoView.pausePlayer();
                    if (mViewModel != null && mViewModel.getMoments() != null && mViewModel.getMoments().get() != null) {
                        mBinding.videoView.canSeekVideo(mViewModel.getMoments().get().isContentModerator());
                        mBinding.videoView.setMomentsEndTime(Math.max(mViewModel.getMoments().get().getSeekEndedAt(), mViewModel.getMoments().get().getSeekStartedAt()));
                        mBinding.videoView.momentsSeekStartsFrom(Math.min(mViewModel.getMoments().get().getSeekEndedAt(), mViewModel.getMoments().get().getSeekStartedAt()));
                    }
                }

                /*videoStartTime = System.currentTimeMillis();
                mBinding.videoView.attachPlayer();*/
            }

            Result currentPost = mViewModel.currentPost.get();
            if (currentPost != null) {
                baseProperties.put("is_live", currentPost.getIsLive());
                baseProperties.put("type", currentPost.getIsLive() ? "live" : "fullRecorded");
                baseProperties.put("postId", currentPost.getId());
                baseProperties.put("title", currentPost.getTitle());
                baseProperties.put("language", currentPost.getLanguage());
                baseProperties.put("in_window_mode", false);
                if (currentPost.getAuthor() != null && currentPost.getAuthor().getUser() != null) {
                    User user = currentPost.getAuthor().getUser();
                    baseProperties.put("username", user.getUsername());
                    baseProperties.put("name", user.getUserFullName());
                    baseProperties.put("author_id", user.getId());
                    baseProperties.put("author", user.getUsername());
                }
                baseProperties.put("game_id", currentPost.getGameId());
                baseProperties.put("game_name", currentPost.getGame());
                baseProperties.put("orientation", Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait" : "landscape");
                baseProperties.put("isLoggedIn", CommonUtils.isUserLoggedin());
                SegmentTracker.getInstance(requireContext()).recordScreenName(screenName, baseProperties);
            }
        }
    }

    private boolean handleIntroVideoUrl() {
        if (mViewModel.showIntro() && (!mViewModel.getIntroVideoUrl().isEmpty() || !mViewModel.getGameRulesVideoUrl().isEmpty())) {
            EventBus.getDefault().post(new EventBusModel.LoadIntroAndGameRules(
                    mViewModel.getIntroVideoUrl(),
                    mViewModel.getGameRulesVideoUrl(),
                    mViewModel.getGameName(),
                    mViewModel.authorUsername()));
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == AppConstants.PURCHASE_REQUEST_CODE) && resultCode == Activity.RESULT_OK) {
            if (mViewModel != null) {
                new Handler(Looper.myLooper()).postDelayed(() -> {
                    try {
                        mViewModel.loadDailyRewards();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 1000);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (AudioRoomService.Companion.isRunning())
            SegmentTracker.getInstance().trackEvent(EVENT_VIDEO_WATCH_WITH_CHATROOM_ACTIVE, baseProperties);

        videoStartTime = System.currentTimeMillis();
        if (mHandler == null)
            mHandler = new Handler(Looper.getMainLooper());


        if (mViewModel.chatHelper == null)
            mViewModel.chatHelper = ChatHelper.getInstance(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity().getWindow() != null)
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
        EventBus.getDefault().post(new EventBusModel.UpdateMomentData(mViewModel.getMoments().get()));
        onPageSelected();
//        new Handler(Looper.getMainLooper()).postDelayed(() -> EventBus.getDefault().post(new EventBusModel.End(mViewModel.getPostId())), 10000);
        //getChildFragmentManager().beginTransaction().replace(getContainerId(), chatFragment, StreamChatFragment.TAG).commit();

//        new KeyboardEventListener(new WeakReference<>(this), isOpen -> {
//            if (!isStickerBottomSheetVisible) {
//                mViewModel.isChatBoxVisible.set(isOpen);
////                if (mBinding == null) return null;
////                if (getActivity() instanceof HomeActivity && Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
////                    int errorMargin = ViewUtils.dpToPx(48);
////                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mBinding.container.getLayoutParams();
////                    int isEdgeToEdge = ViewUtils.isEdgeToEdgeEnabled(getActivity());
////                    if (!isOpen)// 3 for Pawan Device  ,2 for Prashant Device(gesture)
////                        layoutParams.bottomMargin = errorMargin;
////                    else {
////                        layoutParams.bottomMargin = 0;
////                    }
////                    ConstraintSet constraintSet = new ConstraintSet();
////                    constraintSet.clear(R.id.message_container);
////                    constraintSet.clone(mBinding.container);
////                    if (!isOpen) {
////                        constraintSet.connect(mBinding.messageContainer.getId(), ConstraintSet.END, mBinding.actionButtonView.getId(), ConstraintSet.START);
////                    } else
////                        constraintSet.connect(mBinding.messageContainer.getId(), ConstraintSet.END, mBinding.guidelineVerticalPlayerEnd.getId(), ConstraintSet.END);
////                    constraintSet.connect(mBinding.messageContainer.getId(), ConstraintSet.BOTTOM, mBinding.guidelineVerticalBottom.getId(), ConstraintSet.BOTTOM);
////                    constraintSet.connect(mBinding.messageContainer.getId(), ConstraintSet.TOP, mBinding.suggestionContainer.getId(), ConstraintSet.BOTTOM);
////                    constraintSet.applyTo(mBinding.container);
////                }
//                Log.i(TAG, "keyboard_isOpen: " + isOpen);
//            }
//            return null;
//        });
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (childFragment instanceof PlayerVideoQualityBottomSheet) {
            ((PlayerVideoQualityBottomSheet) childFragment).setVideoQualitySelectionListener(this);
        } else if (childFragment instanceof PlayerGiftBottomSheet) {
            if (!RewardManager.getInstance().isTenMinuteStreamRewardAvailable()) {
                ((PlayerGiftBottomSheet) childFragment).updateViewOnFinish(100f,
                        "You're Rewarded",
                        getString(R.string.after_video_rewarded_subtitle));
            } else {
                if (mViewModel != null && !mViewModel.live.get()) {
                    ((PlayerGiftBottomSheet) childFragment).updateViewOnFinish(100f,
                            "Watch Live",
                            getString(R.string.reward_live_stream_message));
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (shouldPausePlayer) {
            return;
        }
        onPageUnselected();
        try {
            CommonUtils.hideKeyboard(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "StreamPlayer_called: onPause");
//        Fragment fragment = getChildFragmentManager().findFragmentByTag(StreamChatFragment.TAG);
//        if (fragment != null && !isStateSaved()) {
//            getChildFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
//            getChildFragmentManager().executePendingTransactions();
//        }
    }

    @Override
    public void onDestroy() {
        if (mBinding != null) {
            mBinding.videoView.setViewCallbackListener(null);
            mBinding.videoView.setHeartAnimatorListener(null);
            ViewGroup parent = (ViewGroup) mBinding.videoView.getParent();
            if (parent != null) {
                int index = parent.indexOfChild(mBinding.videoView);
                if (index >= 0) {
                    parent.removeViewAt(index);
                }
            }
            parent = (ViewGroup) mBinding.youtubeDoubleTap.getParent();
            if (parent != null) {
                int index = parent.indexOfChild(mBinding.youtubeDoubleTap);
                if (index >= 0) {
                    parent.removeViewAt(index);
                }
            }
            parent = (ViewGroup) mBinding.thumbnailImageView.getParent();
            if (parent != null) {
                int index = parent.indexOfChild(mBinding.thumbnailImageView);
                if (index >= 0) {
                    parent.removeViewAt(index);
                }
            }
            parent = (ViewGroup) mBinding.progressBar.getParent();
            if (parent != null) {
                int index = parent.indexOfChild(mBinding.progressBar);
                if (index >= 0) {
                    parent.removeViewAt(index);
                }
            }
        }

        if (nextRewardCountDownTimer != null && nextRewardCountDownTimer.isRunning()) {
            nextRewardCountDownTimer.stopTimer();
        }

        streamHolder = null;
//        heartAnimator = null;
        loginDialogFragment = null;
        nextRewardCountDownTimer = null;
        playerGiftBottomSheet = null;
        scratchDialogFragment = null;
        bottomSheet = null;
        mHandler = null;
        unpinViewpager();
//        chatFragment = null;
        mViewModel.chatHelper = null;

        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isStickerBottomSheetVisible) {
            if (mBinding != null)
                mBinding.videoView.onOrientationChange(newConfig.orientation);
            SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_PLAYER_CHANGE_ORIENTATION, new HashMap<>()/*baseProperties*/);
            mViewModel.orientation.set(newConfig.orientation);
            switchChatContainer(newConfig.orientation);
            mBinding.executePendingBindings();
        }
    }


    private void adjustVideoViewPosition(boolean newConfig) {
        if (mBinding == null || isStateSaved()) return;
//        ViewTreeObserver observer = mBinding.infoLayout.getViewTreeObserver();
//        observer.addOnGlobalLayoutListener(infoLayoutListener);
    }

    ViewTreeObserver.OnGlobalLayoutListener infoLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (!isAdded() || isStateSaved() || mBinding == null) return;
//            int marginTop = mBinding.infoLayout.getHeight() + CommonUtils.toPix(58 - (getActivity() instanceof HomeActivity ? 28 : 0));
            ConstraintLayout.LayoutParams newLayoutParams = (ConstraintLayout.LayoutParams) mBinding.videoView.getLayoutParams();
//            newLayoutParams.topMargin = Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && !mViewModel.isFullPortrait() ? marginTop : 0;
            newLayoutParams.leftMargin = 0;
            newLayoutParams.rightMargin = 0;
            newLayoutParams.bottomMargin = 0;
                /*if (!newConfig) {
                    setPlayerObserver();
                }*/
            mBinding.videoView.setLayoutParams(newLayoutParams);
//            mBinding.infoLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    };

    public void addPlayerAndStartVideo() {
        if (streamHolder != null && mBinding != null && !shouldPausePlayer)
            mBinding.videoView.attachPlayer();
    }

    @Override
    public void onPageSelected() {
        if (mViewModel != null) {
            mViewModel.isPageSelected = true;
            mViewModel.connectChat();
            mViewModel.initHandlers();
        }

        if (mBinding != null && !EventBus.getDefault().isRegistered(mBinding.videoView)) {
            EventBus.getDefault().register(mBinding.videoView);
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
//            EventBus.getDefault().post(new EventBusModel.UpdateStreamFragment(this));
        }
//
//        if (!isIntroVideoUrlHandled) {
//            if (handleIntroVideoUrl()) {
//                isIntroVideoUrlHandled = true;
//            } else {
//                if (streamHolder != null && mBinding != null && !shouldPausePlayer)
//                    mBinding.videoView.attachPlayer();
//            }
//
//        } else {
//
//        }
        if (streamHolder != null && mBinding != null && !shouldPausePlayer) {
            mBinding.videoView.attachPlayer();
            if (mViewModel != null && mViewModel.getMoments() != null && mViewModel.getMoments().get() != null) {
                mBinding.videoView.setMomentsEndTime(Math.max(mViewModel.getMoments().get().getSeekEndedAt(), mViewModel.getMoments().get().getSeekStartedAt()));
                mBinding.videoView.momentsSeekStartsFrom(Math.min(mViewModel.getMoments().get().getSeekEndedAt(), mViewModel.getMoments().get().getSeekStartedAt()));
            }
        }

        setupViews();
//        if (heartAnimator != null && mBinding != null && !shouldPausePlayer) {
//            heartAnimator.start(new WeakReference<>(mBinding.heartButton));
//        }
        if (nextRewardCountDownTimer != null && nextRewardCountDownTimer.isPaused())
            nextRewardCountDownTimer.startTimer();
        shouldPausePlayer = false;
    }

    @Override
    public void onPageUnselected() {
        if (mViewModel != null) {
            mViewModel.isPageSelected = false;
            mViewModel.disconnectChat();
            mViewModel.removeRunnableFromHandler();
        }

        if (mHandler != null) {
            try {
                mHandler.removeCallbacks(mShowTutorial);
                mHandler.removeCallbacks(tenMinuteAlertRunnable);
                mHandler.removeCallbacks(setPlayerObserverRunnable);
                mHandler.removeCallbacks(welcomeMessageRunnable);
                mHandler.removeCallbacks(hideWelcomeRunnable);
                getContext().unregisterReceiver(networkStateReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

//        if (heartAnimator != null) {
//            heartAnimator.stop();
//        }
        if (nextRewardCountDownTimer != null && nextRewardCountDownTimer.isRunning()) {
            nextRewardCountDownTimer.stopTimer();
        }

        if (mBinding != null) {
            mBinding.videoView.detachPlayer();
            if (EventBus.getDefault().isRegistered(mBinding.videoView)) {
                EventBus.getDefault().unregister(mBinding.videoView);
            }
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
        }

        trackPageChange();
    }

    private void trackPageChange() {
        if (!isAdded()) return;
        Map<String, Object> resProperties = new HashMap<>(mViewModel.baseProperties);
        resProperties.put("author", mViewModel.authorUsername());
        resProperties.put("is_first_lifetime", CommonUtils.isFirstTimePageChange());
        resProperties.put("duration", (System.currentTimeMillis() - videoStartTime) / 1000);
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_MOMENTS_PAGE_CHANGED, resProperties);
        CommonUtils.setFirstTimePageChange();
    }

    private boolean shouldPausePlayer = false;
    private Runnable mShowTutorial = new Runnable() {
        @Override
        public void run() {
            if (getActivity() == null || mBinding == null || mViewModel == null || isStateSaved())
                return;
            if (!isResumed()) {
                actionQueue.add(this);
                return;
            }
            SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
            if (!sharedPrefsUtils.getBooleanPreference(RheoTvApp.getNonUiContext(), "moments_tutorial_shown", false)) {
                if (getContext() != null && isAdded() && !isStateSaved() && isVisible()) {
                    shouldPausePlayer = true;
                    PlayerTutorialActivity.Companion.startTutorial(getContext(), true);
                }
            }
        }
    };

    @Override
    public void streamEnded() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (TimeUtils.hasStreamNotStarted(mViewModel.getStartFrom())) {
//            mBinding.futureStreamLayout.setVisibility(View.VISIBLE);
            mBinding.videoView.setVisibility(View.INVISIBLE);
            mBinding.youtubeDoubleTap.setVisibility(View.INVISIBLE);
//            BindingUtils.setImageUrlUsingCache(mBinding.coverPic, mViewModel.currentPost.get().getCarouselThumbnail(), true);
            setCountdownForStream();
        } else {
            mViewModel.loadPost();
            EventBus.getDefault().post(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusModel.UpdateCoin updateCoin) {
//        if (mViewModel == null || mBinding == null || isRemoving() || isStateSaved()) return;
//        startRewardTimer();
//        if (updateCoin != null) {
//            if (CommonUtils.isUserLoggedin()) {
//                if (!isResumed()) {
//                    actionQueue.add(() -> mBinding.totalCoinCountTextView.setText(RewardManager.getInstance().getTotalCoins()));
//                } else
//                    mBinding.totalCoinCountTextView.setText(RewardManager.getInstance().getTotalCoins());
//            } else
//                mBinding.totalCoinCountTextView.setText("");
//        }
    }

    @Override
    public void onSettingViewClick() {
        PlayerVideoQualityBottomSheet bottomSheetDialogFragment = PlayerVideoQualityBottomSheet.newInstance(StreamUtils.getFormats(mViewModel.currentPost.get().getVideoUrls()), streamHolder.getQualityFormat());
        if (isStateSaved() || (getActivity() != null && getActivity().isFinishing()))
            return;
        removeFragmentIfAdded(PlayerVideoQualityBottomSheet.TAG);
        bottomSheetDialogFragment.show(getChildFragmentManager(), PlayerVideoQualityBottomSheet.TAG);
    }

    @Override
    public void onCloseViewClick() {
        mBinding.videoView.rotateLayout();
    }

    @Override
    public void onShareViewClick() {
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_PLAYER_SHARE_CLICKED, new HashMap<>(baseProperties));
        if (mViewModel.getPostId() == null) return;
        if (CommonUtils.isUserLoggedin()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(AppConstants.BRANCH_POST_SOURCE_URL, mViewModel.getShareUrl());
            map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_LIVE_STREAM);
            Map<String, Object> baseProperties = new HashMap<>(mViewModel.baseProperties);
//            if (shareAnimation != null) {
//                int selectedImageResource = shareAnimation.getSelectedImageResource();
//                if (selectedImageResource == R.drawable.ic_share_white_24dp) {
//                    baseProperties.put("share_icon", "share");
//                } else if (selectedImageResource == R.drawable.ic_whatsapp) {
//                    baseProperties.put("share_icon", "whatsapp");
//                } else if (selectedImageResource == R.drawable.ic_instagram) {
//                    baseProperties.put("share_icon", "instagram");
//                } else if (selectedImageResource == R.drawable.ic_facebook) {
//                    baseProperties.put("share_icon", "facebook");
//                }
//            }
            if (isStateSaved() || (getActivity() != null && getActivity().isFinishing()))
                return;
            PostShareBottomSheetFragment.show(getParentFragmentManager(),
                    PostShareBottomSheetFragment.Companion.build(
                            PostShareBottomSheetFragment.Companion.builder(baseProperties)
                                    .setGameName(mViewModel.getGameName())
                                    .setPostId(mViewModel.getPostId())
                                    .setSource(screenName)
                                    .setThumbnailUrl(mViewModel.getThumbnail())
                                    .setShareMap(map)
                                    .setAuthor(mViewModel.authorUsername())
                                    .setCampaignInfo(mViewModel.getCampaignInfo())
                                    .setShareIdentifier(AppConstants.SHARE_TYPE_LIVE_STREAM)
                                    .setShareTitle(mViewModel.authorUsername() + " is Live on Rheo")
                                    .setShareDescription(!mViewModel.getPostShareText().isEmpty() ? mViewModel.getPostShareText() : "Watch " + mViewModel.authorUsername() + " playing " + mViewModel.getGameName() + " live on Rheo.\nAmazing gameplay!!!\n\nCome, watch it Now.")
                                    .setPostUrl(mViewModel.getThumbnail())
                                    .setShareUrl(mViewModel.getShareUrl())
                                    .setShareData(mViewModel.getShareEventData())
                                    .setAuthorName(mViewModel.getAuthorDetail().getUsername())
                                    .setIsLive(mViewModel.postObject.get().isLive() ? "true" : "false")
                                    .setContainsiOSParams(true), this::onShareBottomSheetDismiss
                    ));
        } else {
            openLoginFlow();
        }
    }

    @Override
    public void onStickerViewClick() {
        if (CommonUtils.isUserLoggedin()) {
            StickerBottomSheet stickerBottomSheet = StickerBottomSheet.newInstance(mViewModel.currentPost.get() != null ? mViewModel.currentPost.get().getId() : "",
                    mViewModel.authorUsername(), this);
            if (isStateSaved() || (getActivity() != null && getActivity().isFinishing()))
                return;
            removeFragmentIfAdded(StickerBottomSheet.TAG);
            stickerBottomSheet.show(getChildFragmentManager(), StickerBottomSheet.TAG);
            isStickerBottomSheetVisible = true;
        } else {
            openLoginFlow("Login to send stickers.");
        }
    }

    @Override
    public void onHeartViewClick() {

    }

    @Override
    public void onVideoQualityChanged(String videoQuality) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        this.videoQuality = videoQuality;
        mBinding.videoView.updateStreamQuality(videoQuality);
        new SharedPrefsUtils().setStringPreference(getContext(), SharedPrefsUtils.VIDEO_FORMAT_REQUESTED, videoQuality);
        HashMap<String, Object> resProperties = new HashMap<>(baseProperties);
        resProperties.put("resolution", videoQuality);
        resProperties.put("username", CommonUtils.getUserName(getContext()));
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_VIDEO_RESOLUTION_CHANGE, resProperties);
    }

    @Override
    public void onGiftViewClick() {
        try {
            if (isStateSaved() || (getActivity() != null && getActivity().isFinishing()))
                return;

            if (CommonUtils.isUserLoggedin()) {
                removeFragmentIfAdded(PlayerGiftBottomSheet.TAG);
                if (playerGiftBottomSheet == null)
                    playerGiftBottomSheet = PlayerGiftBottomSheet.newInstance(screenName);
                playerGiftBottomSheet.show(getChildFragmentManager(), PlayerGiftBottomSheet.TAG);
            } else {
                openLoginFlow(getString(R.string.new_reward_message));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onChatViewClick() {
        try {
            if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded()) return;
            if (CommonUtils.isUserLoggedin()) {
                if (mViewModel.orientation.get() == Configuration.ORIENTATION_PORTRAIT) {
//                    mViewModel.isChatBoxVisible.set(!mViewModel.isChatBoxVisible.get());
//                    ViewTreeObserver vt = mBinding.chatText.getViewTreeObserver();
//                    vt.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                        @Override
//                        public void onGlobalLayout() {
//                            if (!isAdded() || isStateSaved() || mBinding == null) return;
//                            mBinding.chatText.setFocusable(true);
//                            mBinding.chatText.setFocusableInTouchMode(true);
//                            mBinding.chatText.requestFocus();
//                            mBinding.chatText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                        }
//                    });
//
//                    mBinding.chatText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                        @Override
//                        public void onFocusChange(View v, boolean hasFocus) {
//                            if (!isAdded() || isStateSaved() || mBinding == null) return;
//                            if (hasFocus)
//                                mBinding.chatText.post(() -> CommonUtils.showKeyboard(getActivity(), mBinding.chatText));
//                        }
//                    });
//
                } else {
                    mViewModel.isChatBoxLandVisible.set(!mViewModel.isChatBoxLandVisible.get());
//                    ViewTreeObserver vt = mBinding.chatTextLand.getViewTreeObserver();
//                    vt.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                        @Override
//                        public void onGlobalLayout() {
//                            if (!isAdded() || isStateSaved() || mBinding == null) return;
//                            mBinding.chatTextLand.setFocusable(true);
//                            mBinding.chatTextLand.setFocusableInTouchMode(true);
//                            mBinding.chatTextLand.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                        }
//                    });
                }
            } else {
                openLoginFlow(getString(R.string.login));
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFollowStreamViewClick() {
        mViewModel.onFollowButtonClick();
    }

    @Override
    public void onStreamProfileClick() {
        showProfileMenuBottomSheet();
    }

    @Override
    public void onControllerVisibilityChange(Boolean isVisible) {
        Log.i(TAG, "onControllerVisibilityChange: isVisible: " + isVisible);
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        mViewModel.isControlVisible.set(isVisible);
    }

    @Override
    public void onFlagBtnClick() {
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_REPORT_POST_CLICKED, new HashMap<>(baseProperties));
        if (isStateSaved() || !isAdded() || (getActivity() != null && getActivity().isFinishing()))
            return;
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.report_this_title))
                .setMessage(getString(R.string.report_content))
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_POST_REPORTED, new HashMap<>(baseProperties));
                    dialogInterface.dismiss();
                    reportPost();
                })
                .setNegativeButton("No", (dialogInterface, i) -> {
                    SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_REPORT_POST_DISMISSED, new HashMap<>(baseProperties));
                    dialogInterface.dismiss();
                })
                .show();
    }

    @Override
    public void trackFirstEventWatchStream() {
        if (!isFirstWatchEventTracked) {
            isFirstWatchEventTracked = true;
            if (CommonUtils.isFirstWatchEventNotTracked()) {
                CommonUtils.setFirstWatchEventTracked();
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_FIRST_WATCH_STREAM, new HashMap<>(baseProperties));
            }
        }
    }

    @Override
    public void trackEventWatchStream30Secs(long ttl) {
        if (!isAdded()) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("time_elapsed", ttl);
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_WATCH_STREAM_30_SECS, properties);
    }

    @Override
    public void trackEventWatchStream5mins(long ttl) {
        if (!isAdded()) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("time_elapsed", ttl);
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_WATCH_STREAM_5_MINS, properties);
    }

    @Override
    public void trackEventWatchStream11mins(long ttl) {
        if (!isAdded()) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("time_elapsed", ttl);
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_WATCH_STREAM_11_MINS, properties);
    }

    @Override
    public void trackEventWatchStream30mins(long ttl) {
        if (!isAdded()) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("time_elapsed", ttl);
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_WATCH_STREAM_30_MINS, properties);
    }

    @Override
    public void trackEventWatchStream45mins(long ttl) {
        if (!isAdded()) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("time_elapsed", ttl);
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_WATCH_STREAM_45_MINS, properties);
    }

    @Override
    public void trackEventWatchStream1hrs(long ttl) {
        if (!isAdded()) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("time_elapsed", ttl);
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_WATCH_STREAM_1_HRS, properties);
    }

    @Override
    public void trackEventWatchStream2hrs(long ttl) {
        if (!isAdded()) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("time_elapsed", ttl);
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_WATCH_STREAM_2_HRS, properties);
    }

    @Override
    public void trackFirstEventWatchStream5Mins(long ttl) {
        if (!isFirstWatchEvent5MinsTracked) {
            isFirstWatchEvent5MinsTracked = true;
            if (CommonUtils.isFirstWatchEvent5MinsNotTracked()) {
                CommonUtils.setFirstWatchEvent5MinsTracked();
                HashMap<String, Object> properties = new HashMap<>(baseProperties);
                properties.put("time_elapsed", ttl);
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_FIRST_WATCH_STREAM_5_MINS, properties);
            }
        }
    }

    @Override
    public void makeViewApiCall(int duration, long timeElapsed) {
        if (mViewModel == null || isStateSaved()) return;
        Log.i(getClass().getName(), "makeViewApiCall at " + System.currentTimeMillis() + " for " + duration + "secs and player time" + timeElapsed);
        Result res = mViewModel.currentPost.get();
        if (macAddress == null) {
            macAddress = AppUtils.getMACAddress();
        }
        if (res != null) {
            String device_id = CommonUtils.getDevId(getNonUiContext());
            mViewModel.getDataManager().postVideoView(res, device_id, duration, (int) timeElapsed,
                    macAddress, res.getIsLive(), res.getGame(), res.getGameId(),
                    Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait" : "landscape",
                    videoQuality)
                    .enqueue(new retrofit2.Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response != null) {
                                if (response.body() != null) {
                                    try {
                                        Log.i(TAG, "Response " + response.body().string());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(TAG, "Error -> " + t.getMessage());
                            t.printStackTrace();
                        }
                    });
        }
    }

    @Override
    public void sendVideoAnalytics(long exoPosition, long duration) {
        try {
            Result currentPlayingPost = mViewModel.currentPost.get();
            if (currentPlayingPost == null) {
                return;
            }
            AnalyticsHelper.getInstance(getNonUiContext()).sendVideoPlay(
                    exoPosition,
                    currentPlayingPost.getAuthor().getUser().getUsername(),
                    String.valueOf(currentPlayingPost.getAuthor().getUser().getId()),
                    currentPlayingPost.getId(),
                    currentPlayingPost.getTitle(),
                    currentPlayingPost.getHashtags(),
                    exoPosition,
                    duration / 1000,
                    "bottom_navigation",
                    "",
                    "",
                    new HashMap<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateDuration() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (mViewModel.currentPost.get() != null) {
            if (TimeUtils.hasStreamNotStarted(mViewModel.currentPost.get().getStartFrom())) {
                mBinding.streamDurationTextView.setVisibility(View.GONE);
            } else {
                mBinding.streamDurationTextView.setVisibility(View.VISIBLE);
                mBinding.streamDurationTextView.setText(mViewModel.currentPost.get().getTimeLeftOut());
            }
        }
    }


    @Override
    public void setPlayerObserver() {
        if (mViewModel == null || isStateSaved() || !isAdded() || isRemoving())
            return;
        if (mBinding == null) {
            mHandler.postDelayed(setPlayerObserverRunnable, 500);
            return;
        }
//        ViewTreeObserver observer = mBinding.videoView.getViewTreeObserver();
//        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                if (isAdded() && !isStateSaved() && getActivity() != null && mBinding != null) {
////                    int y = (int) (mBinding.videoView.getY());
////                    if (y > 0) {
////                        y += mBinding.videoView.getHeight();
////                        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mBinding.chatContainer.getLayoutParams();
////                        lp.topMargin = y - (getActivity() instanceof HomeActivity ? 0 : ScreenUtils.getStatusBarHeight(getActivity()));
////                        mBinding.chatContainer.setLayoutParams(lp);
////                        mBinding.videoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
////                    }
//                }
//            }
//        });
    }

    public void showCustomRoomAcceptedEnable(String message) {
        if (mBinding == null || mViewModel == null || isStateSaved() || (getActivity() != null && getActivity().isFinishing()))
            return;
        Snackbar.make(mBinding.getRoot(), message, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(ContextCompat.getColor(getContext(), R.color.color_accent))
//                .setAnchorView(mBinding.chatStateButton)
                .setAction(getString(R.string.view), v -> playRequestClicked())
                .show();
    }

    public void addCustomRoomEvent() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (mViewModel != null && mViewModel.currentPost.get() != null) {
            playRequestClicked();
        } else {
            customRoomQueue.add(this::playRequestClicked);
        }
    }

    private int getContainerId() {
        return mBinding.chatContainer.getId();
    }

    @Override
    public void onHeartUp() {
        Log.i(getClass().getSimpleName(), "heat_up");
//        mViewModel.getChatHelper().sendMessage(MSG_HEART, mViewModel.getPostId(), mViewModel.chatHelperCallback);
        mViewModel.postHeart();
        mViewModel.updateHeart();
    }

    @Override
    public void askLogin() {

    }

    @Override
    public void checkRewardAvailable() {
        if (mBinding == null || mViewModel == null || isStateSaved() || !isAdded()) return;
//        showToast(getString(R.string.user_block_message));
    }

    @Override
    public void onBlockUserSuccess() {
        if (mBinding == null || mViewModel == null || isStateSaved() || !isAdded()) return;
        showToast(getString(R.string.user_block_message));
    }

    @Override
    public void onReportUserSuccess() {
        if (mBinding == null || mViewModel == null || isStateSaved() || !isAdded()) return;
        showToast(getString(R.string.post_report_success));
    }

    @Override
    public void onReportPostSuccess() {
        showToast(getString(R.string.post_report_success));
    }

    @Override
    public void openLoginFlow(String rewardMessage) {
        try {
            if (mBinding == null || mViewModel == null || isStateSaved()) return;
            if (loginDialogFragment == null || loginDialogFragment.isAdded() || loginDialogFragment.isVisible())
                return;

            loginDialogFragment.setRewardText(rewardMessage);
            if (isStateSaved() || (getActivity() != null && getActivity().isFinishing()))
                return;
            loginDialogFragment.showNoAddToBackStack(this.getChildFragmentManager(), AppConstants.LOGIN_FRAGMENT_TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void trackComment(String message, boolean isSuggestedComment) {
        if (CommonUtils.isFirstCommentSendNotTracked()) {
            HashMap<String, Object> property = new HashMap<>(baseProperties);
            property.put("orientation", Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait" : "landscape");
            property.put("message", message);
            property.put("game", mViewModel.postObject.get() != null && mViewModel.postObject.get().getGame() != null ? mViewModel.postObject.get().getGame().getName() : "");
            property.put("is_suggested_comment", isSuggestedComment);
            property.put("author", mViewModel.authorUsername());
            property.put("post_id", mViewModel.getPostId());
            CommonUtils.setFirstCommentSentEventTracked();
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_CHAT_SEND_FIRST_CLICKED, property);
        }
    }

    @Override
    public void onLoginSuccess() {
        if (getContext() == null || mBinding == null || mViewModel == null || isStateSaved())
            return;
        startRewardTimer();
        mViewModel.loadStreamerFollowState(mViewModel.authorId(), mViewModel.authorUsername());
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(AppConstants.UPDATE_STORY_BROADCAST_FILTER));
    }

    @Override
    public void onLoginDialogClose() {

    }

    @Override
    public void onStickerSelected(Sticker sticker) {
        if (sticker.getValue() <= RewardManager.getInstance().getTotalCoin()) {
            if (CommonUtils.isUserLoggedin()) {
                mViewModel.sendSticker(sticker, mBinding.getRoot());
            } else {
                openLoginFlow();
            }
        } else {
            askToBuyCoins("sticker", sticker.getValue());
        }
    }

    // this is for sending greetings
    @Override
    public void onStickerSelected(Sticker sticker, String message) {
        if (sticker.getValue() <= RewardManager.getInstance().getTotalCoin()) {
            mViewModel.sendGreeting(sticker, message);
        } else {
            askToBuyCoins("greetings", sticker.getValue());
        }
    }

    private void askToBuyCoins(String source, int requiredCoins) {
        try {
            Map<String, Object> map = new HashMap<>(baseProperties);
            map.put("coinSource", source);
            map.put("authorName", mViewModel.authorUsername());
            map.put("coinsLeft", RewardManager.getInstance().getTotalCoins());
            map.put("featureCoins", requiredCoins);
            map.put("requiredCoins", requiredCoins - RewardManager.getInstance().getTotalCoin());
            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_BUY_COIN_DIALOG_SHOWN, map);
            BuyCoinFragment.newInstance(() -> {
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_BUY_COIN_BUTTON_CLICKED, map);
                Intent intent = new Intent(getContext(), BillingActivity.class);
                intent.putExtra(AppConstants.SCREEN_SOURCE, screenName);
                intent.putExtra("featureCoins", requiredCoins);
                intent.putExtra("requiredCoins", requiredCoins - RewardManager.getInstance().getTotalCoin());
                intent.putExtra("coinsLeft", RewardManager.getInstance().getTotalCoins());
                intent.putExtra("coinSource", source);
                intent.putExtra(AppConstants.USER_NAME, mViewModel.authorUsername());
                intent.putExtra(AppConstants.KEY_POST_ID, mViewModel.getPostId());
                this.startActivityForResult(intent, AppConstants.PURCHASE_REQUEST_CODE);
            }).show(getChildFragmentManager(), BuyCoinFragment.Companion.getTAG());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBottomSheetClose() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        isStickerBottomSheetVisible = false;
        CommonUtils.hideKeyboard(getActivity());
    }

    @Override
    public void onScratchRevealed(String rewardId) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (shouldShowTenMinuteAlert) shouldShowTenMinuteAlert = false;
        Log.i(getClass().getName(), "onScratchRevealed " + rewardId);
        mViewModel.updateScratchCard(rewardId, getContext());
    }

    public PlayerHeadHolder getPlayerHolder() {
        return mViewModel != null && mBinding != null ? new PlayerHeadHolder(
                mViewModel.postObject.get(),
                mBinding.videoView.getTimeEllipse(),
                mBinding.videoView.getResumeWindow(),
                mBinding.videoView.getResumePosition(),
                mViewModel.currentPost.get() != null ? mViewModel.currentPost.get().getVideoUrls() : null
        ) : null;
    }

    //    private HeartAnimator heartAnimator;
    private LoginFragmentBottomDialog loginDialogFragment;
    private HourglassAsync nextRewardCountDownTimer;
    private long TOTAL_PROGRESS_TIME = 0;
    private long TIME_DELAY_TO_SHOW_VIDEO_ALERT = 0;
    public long TIME_UNTIL_FINISH = -1;
    protected Queue<Runnable> actionQueue = new LinkedList<>();
    protected Queue<Runnable> customRoomQueue = new LinkedList<>();
    public boolean shouldShowTenMinuteAlert = false;
    private boolean isFirstWatchEvent5MinsTracked = false;
    private boolean isFirstWatchEventTracked = false;
    private Handler mHandler;
    private ScratchDialogFragment scratchDialogFragment;
    private int mScratchCardBottomMargin = AppConstants.PORTRAIT_PLAYER_SCRATCH_CARD_BOTTOM_MARGIN;
    private BottomSheetDialogFragment bottomSheet;
    private String macAddress;
    private PostGift currentGreeting;
    //    boolean rewardIn5Shown;
//    boolean rewardIn1Shown;
    private PlayerGiftBottomSheet playerGiftBottomSheet;

    public static final int VIEW_PROFILE = 0x00;
    public static final int FOLLOW_USER = 0x01;
    public static final int REPORT_USER = 0x02;
    public static final int BLOCK_USER = 0x03;
    public static final int DELETE_COMMENT = 0x04;
    public static final int BLOCK_COMMENT = 0x05;
    public static final int REPORT_POST = 0x06;
    public static final int PIN_COMMENT = 0x07;
    public static final int MOVE_THRESHOLD = 50;

    private Runnable tenMinuteAlertRunnable = () -> {
        if (!isAdded() || isStateSaved() || mBinding == null) return;
        if (CommonUtils.isUserLoggedin()) {
            shouldShowTenMinuteAlert = true;
        }
    };

    private Runnable welcomeMessageRunnable = () -> {
        if (!isAdded() || isStateSaved() || mBinding == null) return;
        if (mBinding.welcomeLottieView.getVisibility() == View.GONE) {
            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_WELCOME_ANIMATION_SHOWED, new HashMap<>(mViewModel.baseProperties));
            CommonUtils.setUserWelcomed(true);
            mBinding.welcomeLottieView.setVisibility(View.VISIBLE);
            String language = CommonUtils.getUserLanguage(getContext()).split(",")[0];
            if (language == null || language.isEmpty())
                language = "english";
            String message = UserLanguage.Companion.toUserLanguage(language).getWelcome_message();
            SpannableString spannableString = new SpannableString(String.format(message, CommonUtils.getUserName()));
//            SpannableString spannableString = new SpannableString(getString(R.string.welcome_user_message, CommonUtils.getUserName()));
            int startIndex = spannableString.toString().indexOf(CommonUtils.getUserName());
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mBinding.welcomeTextView.getContext(), R.color.gold_badge)), startIndex, startIndex + CommonUtils.getUserName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mBinding.welcomeTextView.setText(spannableString);
            mBinding.welcomeTextView.setVisibility(View.VISIBLE);
            mBinding.welcomeLottieView.playAnimation();
            mBinding.welcomeLottieView.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (mHandler == null)
                        mHandler = new Handler(Looper.getMainLooper());
                    mHandler.post(hideWelcomeRunnable);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }
    };

    private Runnable hideWelcomeRunnable = () -> {
        if (!isAdded() || isStateSaved() || mBinding == null) return;
        if (mBinding.welcomeLottieView.getVisibility() == View.VISIBLE) {
            mBinding.welcomeLottieView.pauseAnimation();
            mBinding.welcomeTextView.setVisibility(View.GONE);
            mBinding.welcomeLottieView.setVisibility(View.GONE);
        }
    };

    private Runnable setPlayerObserverRunnable = this::setPlayerObserver;

    private void setupViews() {
        if (mBinding == null) return;
        if (loginDialogFragment == null) {
            loginDialogFragment = LoginFragmentBottomDialog.getInstance(screenName);
            loginDialogFragment.setmCallback(this);
        }
        if (mHandler == null)
            mHandler = new Handler(Looper.getMainLooper());
        if (CommonUtils.isSelectedUser() && CommonUtils.isNewUser() && !CommonUtils.getUserWelcomed())
            mHandler.postDelayed(welcomeMessageRunnable, 3000);

        if (mViewModel.getMoments().get() != null) {
            mBinding.startTimeInput.setText("" + AppUtilsKt.INSTANCE.getTimeFromMillis(Math.min(mViewModel.getMoments().get().getSeekStartedAt(), mViewModel.getMoments().get().getSeekEndedAt())).trim());
            mBinding.endTimeInput.setText("" + AppUtilsKt.INSTANCE.getTimeFromMillis(Math.max(mViewModel.getMoments().get().getSeekStartedAt(), mViewModel.getMoments().get().getSeekEndedAt())).trim());
        }
//        if (heartAnimator == null) {
//            heartAnimator = new HeartAnimator(mBinding.heartContainer, heartAnimatorCallback);
//            heartAnimator.start(new WeakReference<>(mBinding.heartButton));
//        }
//        mBinding.totalCoinCountTextView.setText(CommonUtils.isUserLoggedin() ? RewardManager.getInstance().getTotalCoins() : "");
        setOnViewClickListeners();
        mBinding.videoView.setAnalyticsListener(this);
    }

    HeartAnimator.HeartAnimatorInteractionListener heartAnimatorCallback = new HeartAnimator.HeartAnimatorInteractionListener() {
        @Override
        public void onHeartUp() {
            Log.i(TAG, "heat_up");
//            mViewModel.getChatHelper().sendMessage(MSG_HEART, mViewModel.getPostId(), mViewModel.chatHelperCallback);
            mViewModel.postHeart();
            mViewModel.updateHeart();
        }

        @Override
        public void askLogin() {
            openLoginFlow(null);
        }
    };

    private void setOnViewClickListeners() {
//        mBinding.authorNameText.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@org.jetbrains.annotations.Nullable View v) {
//                if (!mViewModel.isFollowing())
//                    mViewModel.onFollowButtonClick();
//                else
//                    showProfileMenuBottomSheet();
//            }
//        });
//        mBinding.followerCountText.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@org.jetbrains.annotations.Nullable View v) {
//                if (!mViewModel.isFollowing())
//                    mViewModel.onFollowButtonClick();
//                else
//                    showProfileMenuBottomSheet();
//            }
//        });
        mBinding.messageContainer.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@org.jetbrains.annotations.Nullable View v) {
                if (CommonUtils.isUserLoggedin())
                    ChatBoxBottomSheetDialog.Companion.newInstance(MomentsFragment.this, mViewModel.authorUsername()).
                            show(getChildFragmentManager(), ChatBoxBottomSheetDialog.Companion.getTAG());
                else
                    openLoginFlow();
            }
        });
        mBinding.acceptButton.setOnClickListener(v -> {
            long starttime = AppUtilsKt.INSTANCE.getMillisFromString(mBinding.startTimeInput.getText().toString());
            long endTime = AppUtilsKt.INSTANCE.getMillisFromString(mBinding.endTimeInput.getText().toString());
            if (Math.abs(starttime - endTime) <= 30 * 1000)
                mViewModel.acceptMoment(starttime, endTime);
            else
                Toast.makeText(getContext(), "The clip duration is more than 30s!", Toast.LENGTH_SHORT).show();
            Map<String, Object> resProperties = new HashMap<>(mViewModel.baseProperties);
            resProperties.put("start_time", starttime);
            resProperties.put("end_time", endTime);
            resProperties.put("author", mViewModel.authorUsername());
            SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_MOMENTS_ACCEPT_CLICKED, resProperties);
        });
        mBinding.rejectButton.setOnClickListener(v -> {
            long starttime = AppUtilsKt.INSTANCE.getMillisFromString(mBinding.startTimeInput.getText().toString());
            long endTime = AppUtilsKt.INSTANCE.getMillisFromString(mBinding.endTimeInput.getText().toString());
            Map<String, Object> resProperties = new HashMap<>(mViewModel.baseProperties);
            resProperties.put("start_time", starttime);
            resProperties.put("end_time", endTime);
            resProperties.put("author", mViewModel.authorUsername());
            SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_MOMENTS_REJECT_CLICKED, resProperties);
            mViewModel.rejectMoment();
        });
//        mBinding.heartButton.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@Nullable View v) {
//                Map<String, Object> map = new HashMap<>(mViewModel.baseProperties);
//                map.put("is_first", CommonUtils.isFirstTimeLiked());
//                map.put("author", mViewModel.authorUsername());
//                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STREAM_LIKED, map);
//                CommonUtils.setFirstTimeLiked();
//                heartAnimator.fadeAndScaleHeart(new WeakReference<>(v));
//            }
//        });
//        mBinding.topArrow.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@Nullable View v) {
//                handledGameTagClick();
//            }
//        });
//        mBinding.giftButtonPortrait.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@Nullable View v) {
//                onStickerViewClick();
//            }
//        });
        mBinding.pinComment.pinImageView.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                if (mViewModel.isModerator() || mViewModel.isStreamer())
                    mViewModel.unpinComment();
            }
        });
        mBinding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    getActivity().onBackPressed();
            }
        });
//        mBinding.leaderBoardButton.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@Nullable View v) {
//                Map<String, Object> properties = new HashMap<>(getViewModel().baseProperties);
//                properties.put("is_first", CommonUtils.isFirstTimeLeaderBoardClicked());
//                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_LEADERBOARD_PAGE_CLICKED, properties);
//                CommonUtils.setFirstTimeLeaderBoardClicked();
//                Intent intent = new Intent(getContext(), LeaderBoardActivity.class);
//                intent.putExtra(AppConstants.SCREEN_SOURCE, screenName);
//                startActivity(intent);
//            }
//        });
//        mBinding.gameTagButton.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@org.jetbrains.annotations.Nullable View v) {
//                handledGameTagClick();
//            }
//        });
//        mBinding.coinClickableArea.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@Nullable View v) {
//                Map<String, Object> map = new HashMap<>(getViewModel().baseProperties);
//                map.put("is_first", CommonUtils.isFirstTimeCoinsClicked());
//                map.put("total_coins_count", RewardManager.getInstance().getTotalCoins());
//                SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_TOTAL_COIN_CLICKED, map);
//                CommonUtils.setFirstTimeCoinsClicked();
//                if (CommonUtils.isUserLoggedin()) {
//                    Intent intent = new Intent(getContext(), RewardsActivity.class);
//                    intent.putExtra(AppConstants.SCREEN_SOURCE, screenName);
//                    startActivity(intent);
//                } else
//                    openLoginFlow(getString(R.string.login_to_get_reward_message));
//            }
//        });
        mBinding.settingsIconButton.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@org.jetbrains.annotations.Nullable View v) {
                onSettingViewClick();
            }
        });
        mBinding.flagIconButton.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@org.jetbrains.annotations.Nullable View v) {
                onFlagBtnClick();
            }
        });
//        mBinding.authorDetailView.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@Nullable View v) {
//                showProfileMenuBottomSheet();
//            }
//        });
        mBinding.gameNameTextView.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                gameClicked();
            }
        });
//        mBinding.topFanTextView.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@org.jetbrains.annotations.Nullable View v) {
//                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_TOP_FANS_BUTTON_CLICKED, new HashMap<>(mViewModel.baseProperties));
//                if (CommonUtils.isUserLoggedin())
//                    TopFansBottomSheet.Companion.newInstance(mViewModel.authorUsername(), SegmentConstants.SCREEN_NAME_VIDEO_PLAYER)
//                            .show(getChildFragmentManager(), TopFansBottomSheet.TAG);
//                else
//                    openLoginFlow();
//            }
//        });
//        mBinding.topFanView.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@org.jetbrains.annotations.Nullable View v) {
//                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_TOP_FANS_BUTTON_CLICKED, new HashMap<>(mViewModel.baseProperties));
//                if (CommonUtils.isUserLoggedin())
//                    TopFansBottomSheet.Companion.newInstance(mViewModel.authorUsername(), SegmentConstants.SCREEN_NAME_VIDEO_PLAYER)
//                            .show(getChildFragmentManager(), TopFansBottomSheet.TAG);
//                else
//                    openLoginFlow();
//            }
//        });
//        mBinding.topFansImageButton.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@org.jetbrains.annotations.Nullable View v) {
//                /*Bundle bundle = new Bundle();
//                bundle.putString(FullScreenVideoActivity.ARG_KEY_CHANNEL_ID, "testing");
//                bundle.putString("requester_agora_token", "0063c0d09e7a49c4d44845809ba1c12517bIAAXjYPE5N8eT/GjQeOfFED4N5wHREbLe+YyNOdMmsBhQQZa8+gAAAAAEAA1HXOdMjuIXwEAAQAxO4hf");
//                FullScreenVideoActivity.Companion.startMe(requireContext(), bundle);*/
//                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_TOP_FANS_BUTTON_CLICKED, new HashMap<>(mViewModel.baseProperties));
//                if (CommonUtils.isUserLoggedin())
//                    TopFansBottomSheet.Companion.newInstance(mViewModel.authorUsername(), SegmentConstants.SCREEN_NAME_VIDEO_PLAYER)
//                            .show(getChildFragmentManager(), TopFansBottomSheet.TAG);
//                else
//                    openLoginFlow();
//            }
//        });
//        mBinding.requestVideoCallButton.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@org.jetbrains.annotations.Nullable View v) {
//                HashMap<String, Object> properties = new HashMap<>(baseProperties);
//
//                if (CommonUtils.isUserLoggedin()) {
//                    properties.put("is_author", CommonUtils.getUserName().equalsIgnoreCase(mViewModel.getAuthorDetail().getUsername()));
//                    SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_COHOST_ICON_CLICKED, properties);
//                    if (VideoChatViewActivity.Companion.isOngoingCall()) {
//                        startActivity(new Intent(getContext(), VideoChatViewActivity.class));
//                        return;
//                    }
//                    if (bottomSheet != null)
//                        bottomSheet.dismiss();
//
//                    mViewModel.currentPost.get().setTotalCallCount(0);
//                    updateRequestedCount(mViewModel.currentPost.get().getTotalCallCount());
//
//                    bottomSheet = RequestToVideoCallBottomSheet.Companion.newInstance(mViewModel.getPostId(),
//                            mViewModel.getAuthorDetail().getUsername(), (int requiredCoins) -> askToBuyCoins("cohost", requiredCoins));
//
//                    bottomSheet.show(getChildFragmentManager(), RequestToVideoCallBottomSheet.TAG);
//
//                } else {
//                    properties.put("login_screen_shown", true);
//                    SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_COHOST_ICON_CLICKED, properties);
//                    openLoginFlow();
//                }
//
//                //testCallNotification();
//            }
//        });
//        mBinding.coinImageView.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@Nullable View v) {
//                onGiftViewClick();
//            }
//        });
//        mBinding.playRequestButton.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@Nullable View v) {
//                playRequestClicked();
//            }
//        });
//        mBinding.shareButton.setOnClickListener(new BackPressUpdateClickListener() {
//            @Override
//            public void onViewClick(@Nullable View v) {
//                onShareViewClick();
//            }
//        });
//        mBinding.messageEditText.setOnFocusChangeListener((v, hasFocus) -> {
//            if (!isAdded() || isStateSaved() || mBinding == null) return;
//            long t = System.currentTimeMillis();
//            long delta = t - focusTime;
//            if (hasFocus) {     // gained focus
//                if (delta > minDelta) {
//                    focusTime = t;
//                    focusTarget = v;
//                }
//            } else {              // lost focus
//                if (delta <= minDelta && v == focusTarget) {
//                    // reset focus to target
//                    focusTarget.post(() -> focusTarget.requestFocus());
//                }
//            }
//        });
    }

    private final int minDelta = 1000;           // threshold in ms
    private long focusTime = 0;                 // time of last touch
    private View focusTarget = null;

    ViewTreeObserver.OnGlobalLayoutListener chatTextLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (!isAdded() || isStateSaved() || mBinding == null) return;
            mBinding.messageEditText.setFocusable(true);
            mBinding.messageEditText.setFocusableInTouchMode(true);
            mBinding.messageEditText.requestFocus();
            mBinding.messageContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    };

    private void setViewModelObservers() {
        mViewModel.onHeartUpdate.observe(getViewLifecycleOwner(), hearts -> {
            if (!isAdded() || isDetached() || isRemoving() || mBinding == null)
                return;
//            if (mViewModel.orientation.get() == Configuration.ORIENTATION_PORTRAIT && !mViewModel.isChatBoxVisible.get())
//                heartAnimator.animateHeartUp();
            else if (mViewModel.orientation.get() == Configuration.ORIENTATION_LANDSCAPE)
                mBinding.videoView.animateHeartUp(hearts);
        });

        mViewModel.totalHeartCount.observe(getViewLifecycleOwner(), hearts -> {
            if (!isAdded() || isDetached() || isRemoving() || mBinding == null) return;
//            BindingUtils.setNumberFormat(mBinding.heartCountView, hearts);
        });
        mViewModel.postGift.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (mViewModel == null || mBinding == null || !isAdded() || isDetached()) return;
                mBinding.getRoot().post(() -> {
                    if (mViewModel.postGift.get() != null) {
                        showPostGiftCard(mViewModel.postGift.get());
                    } else if (currentGreeting != null) {
                        exitPostGiftCard(null);
                    }
                });
            }
        });
        mViewModel.onFollowingUpdate.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (mBinding == null || mViewModel == null || getContext() == null) return;
                if (mBinding.videoView.findViewById(R.id.follow_button) != null) {
                    ((TextView) mBinding.videoView.findViewById(R.id.follow_button)).setText(mViewModel.onFollowingUpdate.get() != null && mViewModel.onFollowingUpdate.get() ? getContext().getString(R.string.following) : getContext().getString(R.string.follow));
                    mBinding.videoView.findViewById(R.id.follow_button).setBackgroundTintList(ColorStateList.valueOf(mViewModel.onFollowingUpdate.get() != null && mViewModel.onFollowingUpdate.get() ? ContextCompat.getColor(getContext(), R.color.light_grey) : ContextCompat.getColor(getContext(), R.color.color_accent)));
                }
            }
        });
    }

    private void startRewardTimer() {
        if (mViewModel.isRewardIconEnabled.get()) {
            if (mBinding == null || mViewModel == null || isStateSaved()) return;
            setupRewardTimer();
            if (nextRewardCountDownTimer != null && nextRewardCountDownTimer.isPaused()) {
                nextRewardCountDownTimer.startTimer();
            }
        }
    }

    private void setupRewardTimer() {
        if (mViewModel == null || mViewModel.currentPost.get() == null || mBinding == null)
            return;
        TOTAL_PROGRESS_TIME = mViewModel.getRewardTimeFromPost() > 0 ? mViewModel.getRewardTimeFromPost() : (RewardManager.getInstance().getVideoRewardActivationTime() / 1000);
        TIME_DELAY_TO_SHOW_VIDEO_ALERT = RewardManager.getInstance().getVideoRewardAlertDelayTime();
        if ((nextRewardCountDownTimer == null || !nextRewardCountDownTimer.isRunning()) && RewardManager.getInstance().isTenMinuteStreamRewardAvailable() && mViewModel.live.get()) {
            nextRewardCountDownTimer = new HourglassAsync(TOTAL_PROGRESS_TIME, TimeUnit.SECONDS) {
                @Override
                public void onTimerTick(long remainingTime) {
                    TIME_UNTIL_FINISH = remainingTime;
                    if (mViewModel == null) return;
                    mViewModel.setRewardTimeFromPost(remainingTime);
//                    if (remainingTime < (TOTAL_PROGRESS_TIME - 60) && !rewardIn1Shown) {
                    //Show Reward Message
//                        mViewModel.addRewardTime((TOTAL_PROGRESS_TIME - 60) / 60);
//                        rewardIn1Shown = true;
//                    } else if (remainingTime < (TOTAL_PROGRESS_TIME / 2) && !rewardIn5Shown) {
                    //Show Reward Message
//                        mViewModel.addRewardTime(TOTAL_PROGRESS_TIME / 2 / 60);
//                        rewardIn5Shown = true;
//                    }
                    if (playerGiftBottomSheet != null) {
                        playerGiftBottomSheet.updateViewOnTick(((float) TOTAL_PROGRESS_TIME - remainingTime) / TOTAL_PROGRESS_TIME,
                                CommonUtils.convertToMinAndSec(remainingTime * 1000),
                                getString(R.string.keep_watching_video_message));
                    }
                }

                @Override
                public void onTimerFinish() {
                    TIME_UNTIL_FINISH = -1;
                    if (playerGiftBottomSheet != null && playerGiftBottomSheet.isVisible()) {
                        playerGiftBottomSheet.updateViewOnFinish(1f, "You're Rewarded", getString(R.string.after_video_rewarded_subtitle));
                    }
                    checkAndShowVideoReward();
                }
            };
            activateExitAlert();
        }
    }

    private void checkAndShowVideoReward() {
        Log.i(getClass().getSimpleName(), "checkAndShowVideoReward");
        if (mBinding == null || mViewModel == null || isStateSaved() || !isAdded())
            return;
        if (RewardManager.getInstance().isTenMinuteStreamRewardAvailable())
            if (CommonUtils.isUserLoggedin()) {
                if (playerGiftBottomSheet != null && playerGiftBottomSheet.isVisible()) {
                    playerGiftBottomSheet.dismiss();
                }
                showScratchCardNotification(REWARD_TYPE_TEN_MINUTE_STREAM);
                mViewModel.showRewardWonMessage();
            } else
                openLoginFlow(getString(R.string.new_reward_message));
    }

    private void activateExitAlert() {
        if (mHandler != null)
            mHandler.postDelayed(tenMinuteAlertRunnable, TIME_DELAY_TO_SHOW_VIDEO_ALERT);
    }

    private void showScratchCardNotification(final String... suggestReward) {
        try {
            if (mBinding == null || mViewModel == null || isStateSaved()) return;
            Reward reward = RewardManager.getInstance().getAvailableReward(suggestReward);
            if (reward == null || !CommonUtils.isUserLoggedin())
                return;
            if (!isResumed()) {
                actionQueue.add(() -> showScratchCardNotification(suggestReward));
                return;
            }
            AnimatedScratchCardView bottomScratchCardView = getViewDataBinding().rootContainer.findViewWithTag(AnimatedScratchCardView.getTAG());
            getViewDataBinding().rootContainer.removeView(bottomScratchCardView);
            if (bottomScratchCardView == null) {
                bottomScratchCardView = new AnimatedScratchCardView(getViewDataBinding().rootContainer.getContext());
            }
            int scratchCardImage = bottomScratchCardView.setRandomScratchCard();
            bottomScratchCardView.setAction(new BackPressUpdateClickListener() {


                private Reward rewardScratchCard;

                public BackPressUpdateClickListener setRewardScratchCard(Reward rewardScratchCard) {
                    this.rewardScratchCard = rewardScratchCard;
                    return this;
                }

                @Override
                public void onViewClick(@Nullable View v) {
                    try {
                        if (rewardScratchCard == null) return;
                        scratchDialogFragment = ScratchDialogFragment.getInstance(screenName, rewardScratchCard, scratchCardImage);
                        if ((scratchDialogFragment.isAdded() || scratchDialogFragment.isVisible())) {
                            return;
                        }
                        if (isStateSaved() || (getActivity() != null && getActivity().isFinishing()))
                            return;
                        removeFragmentIfAdded(AppConstants.SCRATCH_FRAGMENT_TAG);
                        scratchDialogFragment.show(MomentsFragment.this.getChildFragmentManager(), AppConstants.SCRATCH_FRAGMENT_TAG, MomentsFragment.this, suggestReward);

                        if (!CommonUtils.isFirstTimeWatchRewardScratched()) {
                            Map<String, Object> map = new HashMap<>(baseProperties);
                            map.put("author", mViewModel.authorUsername());
                            map.put("post_id", mViewModel.getPostId());
                            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FIRST_WATCH_REWARD_SCRATCHED, map);
                            CommonUtils.setFirstTimeWatchRewardScratched();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.setRewardScratchCard(reward));
            bottomScratchCardView.addTo(getViewDataBinding().rootContainer, mScratchCardBottomMargin,
                    AppConstants.SCRATCH_CARD_END_MARGIN, AnimatedScratchCardView.Companion.getPathInAnimator(),
                    AnimatedScratchCardView.Companion.getSlideOutAnimation(), new AnimatedScratchCardView.ScratchCardVisibilityListener() {
                        private Reward rewardScratchCard;

                        public AnimatedScratchCardView.ScratchCardVisibilityListener setRewardScratchCard(Reward rewardScratchCard) {
                            this.rewardScratchCard = rewardScratchCard;
                            return this;
                        }

                        @Override
                        public void performAction() {
                            if (mBinding == null || mViewModel == null || isStateSaved())
                                return;
                            if (rewardScratchCard != null) {
                                mViewModel.updateScratchCardStatusShown(rewardScratchCard.getId(), rewardScratchCard.getRewardType());
                            }
                            Log.i(getClass().getSimpleName(), "showingScratchCard_now");
                        }
                    }.setRewardScratchCard(reward));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeFragmentIfAdded(String tag) {
        try {
            if (getChildFragmentManager().findFragmentByTag(tag) != null)
                getChildFragmentManager().beginTransaction().remove(getChildFragmentManager().findFragmentByTag(tag)).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchChatContainer(int orientation) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mBinding.container);
        constraintSet.clear(mBinding.chatContainer.getId());
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            constraintSet.connect(mBinding.suggestionContainer.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

            constraintSet.connect(mBinding.chatContainer.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            constraintSet.connect(mBinding.chatContainer.getId(), ConstraintSet.TOP, mBinding.videoView.getId(), ConstraintSet.BOTTOM);
//
            constraintSet.connect(mBinding.messageContainer.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            constraintSet.clear(mBinding.messageContainer.getId(), ConstraintSet.BOTTOM);
            constraintSet.clear(mBinding.messageContainer.getId(), ConstraintSet.TOP);
            constraintSet.connect(mBinding.messageContainer.getId(), ConstraintSet.BOTTOM, mBinding.guidelineVerticalBottom.getId(), ConstraintSet.BOTTOM, 0);

            ((ViewGroup.MarginLayoutParams) mBinding.recyclerView.getLayoutParams()).topMargin = 0;
            mBinding.chatWindowBackgroundView.setBackground(null);
            mScratchCardBottomMargin = AppConstants.PORTRAIT_PLAYER_SCRATCH_CARD_BOTTOM_MARGIN;
        } else {
            mBinding.chatWindowBackgroundView.setBackgroundResource(R.drawable.chat_window_gradient_bg);
            ((ViewGroup.MarginLayoutParams) mBinding.recyclerView.getLayoutParams()).topMargin = ViewUtils.dpToPx(12);

            constraintSet.connect(mBinding.suggestionContainer.getId(), ConstraintSet.END, mBinding.centerGuideline.getId(), ConstraintSet.END);

            constraintSet.connect(mBinding.chatContainer.getId(), ConstraintSet.TOP, mBinding.videoView.getId(), ConstraintSet.TOP);
            constraintSet.connect(mBinding.chatContainer.getId(), ConstraintSet.END, mBinding.centerGuideline.getId(), ConstraintSet.END);

            constraintSet.connect(mBinding.messageContainer.getId(), ConstraintSet.END, mBinding.centerGuideline.getId(), ConstraintSet.START);
            constraintSet.clear(mBinding.messageContainer.getId(), ConstraintSet.BOTTOM);
            constraintSet.clear(mBinding.messageContainer.getId(), ConstraintSet.TOP);
            constraintSet.connect(mBinding.messageContainer.getId(), ConstraintSet.BOTTOM, mBinding.videoView.getId(), ConstraintSet.BOTTOM);

            mScratchCardBottomMargin = AppConstants.LANDSCAPE_SCRATCH_CARD_BOTTOM_MARGIN;
        }
        constraintSet.connect(mBinding.chatContainer.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(mBinding.chatContainer.getId(), ConstraintSet.BOTTOM, mBinding.suggestionContainer.getId(), ConstraintSet.TOP);

        constraintSet.connect(mBinding.messageContainer.getId(), ConstraintSet.TOP, mBinding.suggestionContainer.getId(), ConstraintSet.BOTTOM, ViewUtils.dpToPx(12));
        constraintSet.setVerticalBias(mBinding.messageContainer.getId(), 1f);
        constraintSet.applyTo(mBinding.container);
        setPageMargin(orientation);
    }

    private void openLoginFlow() {
        openLoginFlow(null);
    }

    public void playRequestClicked() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (CommonUtils.isUserLoggedin()) {
            if (mViewModel.currentPost.get() == null) return;
            if (mViewModel.currentPost.get().isCustomRoomEnabled())
                bottomSheet = CustomRoomBottomSheet.Companion.newInstance("", mViewModel.currentPost.get());
            else
                bottomSheet = RequestPlayFragment.newInstance("", mViewModel.currentPost.get());
            if (isStateSaved() || (getActivity() != null && getActivity().isFinishing()))
                return;
            removeFragmentIfAdded(CustomRoomBottomSheet.TAG);
            bottomSheet.show(getChildFragmentManager(), CustomRoomBottomSheet.TAG);
        } else {
            openLoginFlow();
        }
    }

    private void reportPost() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (CommonUtils.isUserLoggedin()) {
            mViewModel.reportPost();
        } else {
            openLoginFlow();
        }
    }

    private void showProfileMenuBottomSheet() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (mViewModel.currentPost.get() == null) return;
        ArrayList<ListOption> listOptions = new ArrayList<>();
        listOptions.add(new ListOption.Header(VIEW_PROFILE));
        listOptions.add(new ListOption.Item(REPORT_USER, "Report", R.drawable.ic_add_user_dark, null));
        ChatMenuOptionBottomSheet bottomSheet = ChatMenuOptionBottomSheet.Companion.newInstance(
                listOptions,
                (ListOption listOption) -> {
                    if (listOption instanceof ListOption.Header) {
                        authorClicked(mViewModel.authorUsername());
                    } else {
                        ListOption.Item item = (ListOption.Item) listOption;
                        switch (item.getId()) {
                            case REPORT_USER:
                                reportAuthor();
                                break;
                            case BLOCK_USER:
                                blockAuthor();
                                break;
                        }
                    }
                    return null;
                }
        );
        bottomSheet.setChatMenuOptionData(mViewModel.getChatOptionMenuBottomSheetData(null, mViewModel.authorUsername(), mViewModel.postObject.get().getAuthor() != null ? mViewModel.postObject.get().getAuthor().getProfilePic() : ""));
        if (isStateSaved() || (getActivity() != null && getActivity().isFinishing()))
            return;
        removeFragmentIfAdded(ChatMenuOptionBottomSheet.TAG);
        bottomSheet.show(getChildFragmentManager(), ChatMenuOptionBottomSheet.TAG);
    }

    private void authorClicked(String username) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        Intent intent = ProfileActivity.getCallingIntent(getContext());
        intent.putExtra("author_name", username);
        startActivity(intent);
    }

    private void reportAuthor() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        mViewModel.reportComment(mViewModel.authorUsername(), "", false);
    }

    private void blockAuthor() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (CommonUtils.isUserLoggedin()) {
            HashMap<String, Object> property = new HashMap<>(baseProperties);
            property.put("blocked_user", mViewModel.authorUsername());
            property.put("blocked_msg", "");
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_BLOCK_USER, property);
            mViewModel.blockUser(mViewModel.authorUsername(), "");
        } else {
            openLoginFlow();
        }
    }

    private void onShareBottomSheetDismiss() {
//        if (shareAnimation != null) {
//            shareAnimation.stopAnimation();
//            shareAnimation = null;
//        }
        checkAndShowShareReward();
    }

    private void checkAndShowShareReward() {
        if (CommonUtils.isUserLoggedin() && RewardManager.getInstance().isShareRewardAvailable()) {
            showScratchCardNotification(AppConstants.REWARD_TYPE_SHARE);
        }
    }

    private void setCountdownForStream() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        final long[] timeLeft = {mViewModel.getStartFrom() - System.currentTimeMillis()};
        //TODO-On Video Finish
        //startsIn.set(mContext.getString(R.string.view_leaderboard));
        //updateView();
        //initializePlayer(false);
        new CountDownTimer(timeLeft[0], 1000) {

            public void onTick(long millisUntilFinished) {
                List<TimerObj> objs = TimeUtils.getTimerObjsList(timeLeft[0] / 1000);
                setCountDownData(objs);
                timeLeft[0] -= 1000;
            }

            public void onFinish() {
                //TODO-On Video Finish
                //startsIn.set(mContext.getString(R.string.view_leaderboard));
                //updateView();
                //initializePlayer(false);
            }
        }.start();
    }

    private void setCountDownData(List<TimerObj> objs) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
//        if (objs.size() >= 1) {
//            mBinding.layout1.setVisibility(View.VISIBLE);
//            mBinding.layout1Value.setText(objs.get(0).getValue());
//            mBinding.layout1Label.setText(objs.get(0).getType());
//            if (objs.size() >= 2) {
//                mBinding.layout2.setVisibility(View.VISIBLE);
//                mBinding.layout2Value.setText(objs.get(1).getValue());
//                mBinding.layout2Label.setText(objs.get(1).getType());
//                mBinding.separator12.setVisibility(View.VISIBLE);
//                if (objs.size() >= 3) {
//                    mBinding.layout3.setVisibility(View.VISIBLE);
//                    mBinding.layout3Value.setText(objs.get(2).getValue());
//                    mBinding.layout3Label.setText(objs.get(2).getType());
//                    mBinding.separator23.setVisibility(View.VISIBLE);
//                    if (objs.size() >= 4) {
//                        mBinding.layout4.setVisibility(View.VISIBLE);
//                        mBinding.layout4Value.setText(objs.get(3).getValue());
//                        mBinding.layout4Label.setText(objs.get(3).getType());
//                        mBinding.separator34.setVisibility(View.VISIBLE);
//                    } else {
//                        mBinding.layout4.setVisibility(View.GONE);
//                    }
//                } else {
//                    mBinding.layout3.setVisibility(View.GONE);
//                    mBinding.layout4.setVisibility(View.GONE);
//                }
//            } else {
//                mBinding.layout2.setVisibility(View.GONE);
//                mBinding.layout3.setVisibility(View.GONE);
//                mBinding.layout4.setVisibility(View.GONE);
//            }
//        } else {
//            mBinding.layout1.setVisibility(View.GONE);
//            mBinding.layout2.setVisibility(View.GONE);
//            mBinding.layout3.setVisibility(View.GONE);
//            mBinding.layout4.setVisibility(View.GONE);
//        }
    }

    private void gameClicked() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (mViewModel.currentPost.get() == null) return;
        Intent intent = new Intent(getContext(), UniversalActivity.class);
        intent.putExtra(AppConstants.SEE_ALL_TYPE, mViewModel.currentPost.get().getGame());
        intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, mViewModel.currentPost.get().getGameId());
        startActivity(intent);
    }

    private void handledGameTagClick() {
        if (mViewModel == null || mBinding == null || isStateSaved()) return;
//        if (mBinding.topArrow.getVisibility() == View.VISIBLE)
//            mBinding.topArrow.setVisibility(View.GONE);
//        else
//            mBinding.topArrow.setVisibility(View.VISIBLE);
//        EventBus.getDefault().post(EventBusModel.ShowTags.INSTANCE);
    }

    private void setPageMargin(int orientation) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        FrameLayout.LayoutParams newLayoutParams = (FrameLayout.LayoutParams) mBinding.container.getLayoutParams();
        newLayoutParams.topMargin = !(getActivity() instanceof HomeActivity) && orientation == Configuration.ORIENTATION_PORTRAIT ? ViewUtils.getStatusBarHeight(getContext()) : 0;
        newLayoutParams.leftMargin = 0;
        newLayoutParams.rightMargin = 0;
        int isEdgeToEdge = ViewUtils.isEdgeToEdgeEnabled(getContext());
        if (isEdgeToEdge != ViewUtils.MODE_EDGE_TO_EDGE && isEdgeToEdge != 3)// 3 for Pawan Device  ,2 for Prashant Device(gesture)
            newLayoutParams.bottomMargin = orientation == Configuration.ORIENTATION_PORTRAIT ? ViewUtils.getNavBarHeight(getContext()) : 0;
        else {
            int errorMargin = 0;
            if (getActivity() instanceof HomeActivity)
                errorMargin = ViewUtils.dpToPx(48);
            newLayoutParams.bottomMargin = orientation == Configuration.ORIENTATION_PORTRAIT ? errorMargin : 0;
        }
        mBinding.container.setLayoutParams(newLayoutParams);
    }

    @Override
    public void onUserClicked(CommentChat commentChat) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        if (commentChat instanceof CommentChat.WelcomeComment) {
            return;
        }
        showMenuBottomSheet(commentChat);
    }

    @Override
    public void onCommentClicked(CommentChat commentChat) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        if (commentChat instanceof CommentChat.WelcomeComment) {
            return;
        }
        showMenuBottomSheet(commentChat);
    }

    @Override
    public void onMediaClicked(CommentChat commentChat) {

    }

    private void updateChatViews() {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        if (!(mBinding.recyclerView.getLayoutManager() instanceof LinearLayoutManager)) return;
        LinearLayoutManager chatLayoutManager = (LinearLayoutManager) mBinding.recyclerView.getLayoutManager();
        if (chatLayoutManager.findFirstVisibleItemPosition() != 0) {
            mViewModel.unreadChatCount.set(mViewModel.unreadChatCount.get() + 1);
        } else {
            mBinding.recyclerView.scrollToPosition(0);
        }
    }

    private int stickerSize() {
        if (Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            return (Resources.getSystem().getDisplayMetrics().widthPixels - (int) (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()))) / 3;
        else
            return ((Resources.getSystem().getDisplayMetrics().widthPixels / 2) - (int) (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()))) / 3;
    }

    public void onReportButtonClick(int position, String username, String comment) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        HashMap<String, Object> property = new HashMap<>(mViewModel.baseProperties);
        if (CommonUtils.isUserLoggedin() && mViewModel.isModerator()) {
            property.put("reported_comment_user", username);
            property.put("reported_comment", comment);
            SegmentTracker.getInstance(getActivity()).trackEvent(SegmentConstants.EVENT_REPORT_COMMENT_ON_SELF_STREAM, property);
            onDeleteCommentClick(position, comment, username);
            mViewModel.reportComment(username, comment, true);
        } else {
            property.put("reported_comment_user", username);
            property.put("reported_comment", comment);
            SegmentTracker.getInstance(getActivity()).trackEvent(SegmentConstants.EVENT_REPORT_COMMENT, property);
            mViewModel.reportComment(username, comment, false);
        }
    }

    private void onDeleteCommentClick(int position, String message, String username) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        chatAdapter.removeChatItem(position);
        mViewModel.sendDeletedMessage(message, username, AppConstants.MSG_TYPE_DELETED);
    }

    private void onBlockCommentClick(int position, String message, String username) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        chatAdapter.removeChatItem(position);
        mViewModel.sendDeletedMessage(message, username, AppConstants.MSG_TYPE_BLOCKED);
    }

    public void onUserProfileClicked(String username) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        Intent intent = ProfileActivity.getCallingIntent(getActivity());
        intent.putExtra("author_name", username);
        startActivity(intent);
    }

    public void onBlockUserClicked(int position, String username, String comment) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        HashMap<String, Object> property = new HashMap<>(mViewModel.baseProperties);
        property.put("blocked_user", username);
        property.put("blocked_msg", comment);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_BLOCK_USER, property);
        onBlockCommentClick(position, comment, username);
        mViewModel.blockUser(username, comment);
    }

    private void onCommentDelete(Status status) {
        if (status == Status.SUCCESS) showToast(getString(R.string.delete_comment_success));
    }

    private void onCommentReport(Status status) {
        if (status == Status.SUCCESS) showToast(getString(R.string.post_report_success));
    }

    private void onUserBlock(Status status) {
        if (status == Status.SUCCESS) showToast(getString(R.string.user_block_message));
    }

    private void showToast(String message) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded() || getContext() == null)
            return;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showMenuBottomSheet(CommentChat commentChat) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        if (commentChat == null || commentChat.getUsername() == null)
            return;
        ArrayList<ListOption> listOptions = new ArrayList<>();
        if (!commentChat.getUsername().equalsIgnoreCase(mViewModel.authorUsername()))
            listOptions.add(new ListOption.Header(VIEW_PROFILE));
        if (mViewModel.isModerator() || mViewModel.isStreamer()) {
            if (!commentChat.getUsername().equalsIgnoreCase(CommonUtils.getUserEmailAddress())) {
                Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_block);
                listOptions.add(new ListOption.Item(REPORT_USER, "Report", R.drawable.avd_report, null));
                listOptions.add(new ListOption.Item(BLOCK_USER, "Block User", -1, ViewUtils.setTint(drawable, Color.rgb(251, 251, 251))));
                listOptions.add(new ListOption.Item(DELETE_COMMENT, "Delete Comment", R.drawable.ic_delete_outline_white, null));
            }
            listOptions.add(new ListOption.Item(PIN_COMMENT, "Pin Comment", -1, ViewUtils.setTint(ContextCompat.getDrawable(getContext(), R.drawable.avd_pin), Color.rgb(251, 251, 251))));
        } else {
            listOptions.add(new ListOption.Item(REPORT_USER, "Report", R.drawable.avd_report, null));
        }

        ChatMenuOptionBottomSheet bottomSheet = ChatMenuOptionBottomSheet.Companion.newInstance(
                listOptions,
                (ListOption listOption) -> {
                    if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
                        return null;
                    if (listOption instanceof ListOption.Header) {
                        onUserProfileClicked(commentChat.getUsername());
                    } else {
                        switch (((ListOption.Item) listOption).getId()) {
                            case VIEW_PROFILE:
                                onUserProfileClicked(commentChat.getUsername());
                                break;
                            case REPORT_USER:
                                onReportButtonClick(chatAdapter.getList().indexOf(commentChat), commentChat.getUsername(), commentChat.getMessage());
                                break;
                            case BLOCK_USER:
                                onBlockUserClicked(chatAdapter.getList().indexOf(commentChat), commentChat.getUsername(), commentChat.getMessage());
                                break;
                            case DELETE_COMMENT:
                                onDeleteCommentClick(chatAdapter.getList().indexOf(commentChat), commentChat.getMessage(), commentChat.getUsername());
                                break;
                            case PIN_COMMENT:
                                mViewModel.pinComment(commentChat);
                                break;
                        }
                    }
                    return null;
                }
        );
        bottomSheet.setChatMenuOptionData(mViewModel.getChatOptionMenuBottomSheetData(commentChat, commentChat.getUsername(), commentChat.getProfile_pic()));
        try {
            if (isStateSaved() || (getActivity() != null && getActivity().isFinishing()))
                return;
            removeFragmentIfAdded(ChatMenuOptionBottomSheet.TAG);
            bottomSheet.show(getChildFragmentManager(), ChatMenuOptionBottomSheet.TAG);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void unpinViewpager() {
        if (chatAdapter != null) {
            chatAdapter.setListener(null);
            chatAdapter = null;
        }
        if (mBinding != null)
            mBinding.recyclerView.setAdapter(null);
    }

    private void setSuggestion(ArrayList<String> suggestions) {
        if (mBinding == null || mViewModel == null || isStateSaved() || suggestions == null || getContext() == null)
            return;
        mBinding.tagChipGroup.removeAllViews();
        for (String suggestion : suggestions) {
            Chip chip = new Chip(getContext(), null, R.attr.chipSuggestionStyle);
            chip.setTag(suggestion.hashCode());
            chip.setText(suggestion);
            chip.setOnCheckedChangeListener((compoundButton, b) -> mViewModel.sendSuggestionChat(compoundButton.getText().toString()));
            mBinding.tagChipGroup.addView(chip);
        }
    }

    private void showPostGiftCard(PostGift postGift) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (currentGreeting != null) {
            exitPostGiftCard(postGift);
        } else {
            enterPostGiftCard(postGift);

        }
    }

    public void exitPostGiftCard(final PostGift postGift) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        mBinding.gitCardContainer.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(mBinding.greetPinned.parent,
                View.TRANSLATION_X, 0, -ViewUtils.getScreenWidthInPx(mBinding.getRoot().getContext()));
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mBinding.greetPinned.parent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mBinding == null || mViewModel == null || isStateSaved()) return;
                mBinding.greetPinned.parent.setVisibility(View.GONE);
                mBinding.gitCardContainer.setVisibility(View.GONE);
                currentGreeting = null;
                if (postGift != null)
                    enterPostGiftCard(postGift);

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }

    public void enterPostGiftCard(PostGift postGift) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        currentGreeting = postGift;
        mBinding.gitCardContainer.setVisibility(View.VISIBLE);

        mBinding.greetPinned.parent.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(postGift.getBackgroundTintColor())));
        ObjectAnimator animator = ObjectAnimator.ofFloat(mBinding.greetPinned.parent, View.TRANSLATION_X,
                -ViewUtils.getScreenWidthInPx(mBinding.getRoot().getContext()), 0);
        animator.setDuration(300);
        int greetingDuration = (int) TimeUtils.getTimeDiffInMs(postGift.getStartTimeTs(), postGift.getEndTimeTs()) - 300;
        Log.e(TAG, "anim duration ---> " + greetingDuration);
        mBinding.greetPinned.timerProgressBar.setMax(greetingDuration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mBinding.greetPinned.parent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mBinding == null || mViewModel == null || isStateSaved()) return;

                new WeakReference<>(new HourglassAsync(greetingDuration, TimeUnit.MILLISECONDS) {
                    private int duration;

                    public HourglassAsync setMaxDuration(int duration) {
                        this.duration = duration;
                        return this;
                    }

                    @Override
                    public void onTimerTick(long remainingTime) {
                        if (mBinding == null || mViewModel == null || isStateSaved()) return;
                        if (remainingTime % 50 == 0) {
                            mBinding.greetPinned.timerProgressBar.setProgress(duration - (int) remainingTime);
                        }
                    }

                    @Override
                    public void onTimerFinish() {
                        if (mBinding == null || mViewModel == null || isStateSaved()) return;
                        mBinding.greetPinned.timerProgressBar.setProgress(duration);
                    }
                }.setMaxDuration(greetingDuration)
                ).get()
                        .startTimer();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
        /*mBinding.greetPinned.parent.animate().x(0).setDuration(300).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();*/
    }

    @Override
    public void onChatSend(@NotNull String message) {
        mViewModel.sendChat(message);
    }

    private long lastSeekStartAt = 0;
    private long lastSeekEndedAt = 0;
    private int rewindCount = 0;
    private Hourglass rewindCountWatcher = new Hourglass(new Date().getTime(), 1000) {
        @Override
        public void onTimerTick(long timeRemaining, long passedTime) {
            if (passedTime == 0) return;
//            Log.i(TAG, "rewind_time: " + passedTime + " and " + (passedTime / 1000) % 10 + " and " + mViewModel.getPostId());
            if ((passedTime / 1000) % 10 == 0L) {
                rewindCount = 0;
                rewindCountWatcher.pauseTimer();
//                Log.i(TAG, "rewind_time: stop and " + mViewModel.getPostId());
            }
        }

        @Override
        public void onTimerFinish() {

        }
    };

    @Override
    public void trackEventForwardSeek(long duration) {
        Map<String, Object> resProperties = new HashMap<>(mViewModel.baseProperties);
        resProperties.put("author", mViewModel.authorUsername());
        resProperties.put("seek_at", duration / 1000);
        resProperties.put("is_live", mViewModel.isLive());
        resProperties.put("share_url", mViewModel.getShareUrl());
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_PLAYER_FORWARD_SEEK_CLICKED, resProperties);
    }

    @Override
    public void trackEventBackwardSeek(long duration) {
        Map<String, Object> resProperties = new HashMap<>(mViewModel.baseProperties);
        resProperties.put("author", mViewModel.authorUsername());
        resProperties.put("seek_at", duration / 1000);
        resProperties.put("is_live", mViewModel.isLive());
        resProperties.put("share_url", mViewModel.getShareUrl());
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_PLAYER_BACKWARD_SEEK_CLICKED, resProperties);
//        Log.i(TAG, "rewind_seek");
        lastSeekStartAt = Math.max(duration, lastSeekStartAt);
        rewindCount++;

        if (rewindCount == 1) {
            rewindCountWatcher.stopTimer();
            rewindCountWatcher.startTimer();
        }

        if (rewindCount >= 3) {
            rewindCountWatcher.pauseTimer();
            rewindCount = 0;
            lastSeekEndedAt = duration;
//            Log.i(TAG, "rewind_moment: " + rewindCount + " and lastSeekStartAt: " + lastSeekStartAt + " and lastSeekEndedAt: " + lastSeekEndedAt);
            mViewModel.onRewind(lastSeekStartAt, lastSeekEndedAt);
            lastSeekStartAt = 0;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNextVideo(EventBusModel.End state) {
        if (!state.getId().equals(mViewModel.getPostId()))
            return;
        mBinding.nextVideoContainer.setVisibility(View.VISIBLE);
        blurView();
        ObjectAnimator animation = ObjectAnimator.ofInt(mBinding.progressTimer, "progress", 100);
        animation.setDuration(3000);
        animation.setInterpolator(new LinearInterpolator());
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                try {
                    if (mBinding.nextVideoContainer.getVisibility() == View.VISIBLE) {
                        mHandler.postDelayed(() ->
                                EventBus.getDefault().post(new EventBusModel.Next(mViewModel.getPostId())), 1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animation.start();
    }

    private void blurView() {
        try {
            float radius = 20f;
            View decorView = getActivity().getWindow().getDecorView();
            ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
            Drawable windowBackground = decorView.getBackground();
            mBinding.blurView.setupWith(rootView)
                    .setFrameClearDrawable(windowBackground)
                    .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                    .setBlurRadius(radius)
                    .setBlurAutoUpdate(true)
                    .setHasFixedTransformationMatrix(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlayerPausedAfterMomentPlayed() {
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        if (!sharedPrefsUtils.getBooleanPreference(RheoTvApp.getNonUiContext(), "moments_tutorial_shown", false) && mHandler != null) {
            mHandler.postDelayed(mShowTutorial, 1000);
        }
    }
}

