package com.rheotv.android.ui.activities.player.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.Observable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.TimerObj;
import com.rheotv.android.data.network.models.gamify.Reward;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.PostGift;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.postlisting.responses.User;
import com.rheotv.android.data.network.models.stickers.Sticker;
import com.rheotv.android.data.network.models.streamUpdates.StreamEvent;
import com.rheotv.android.data.network.models.streamUpdates.StreamEventResponse;
import com.rheotv.android.databinding.FragmentStreamPlayerBinding;
import com.rheotv.android.factories.ViewModelProviderFactoryV2;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.helpers.grpc.ChatHelper;
import com.rheotv.android.services.PlayerHeadHolder;
import com.rheotv.android.ui.activities.customroom.view.CustomRoomBottomSheet;
import com.rheotv.android.ui.activities.gamify.RewardsActivity;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.activities.home.view.HomeActivity;
import com.rheotv.android.ui.activities.leaderboard.LeaderBoardActivity;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.scoreboard.ScoreBoardDialogFragment;
import com.rheotv.android.ui.activities.scoreboard.ScoreFragment;
import com.rheotv.android.ui.activities.share.PostShareBottomSheetFragment;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivity;
import com.rheotv.android.ui.adapters.ScorecardAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.customViews.HeartAnimator;
import com.rheotv.android.ui.customViews.TextAnimator;
import com.rheotv.android.ui.customViews.Tooltip.SimpleTooltip;
import com.rheotv.android.ui.customViews.streamPlayer.StreamHolder;
import com.rheotv.android.ui.customViews.streamPlayer.StreamPlayerCallbackListener;
import com.rheotv.android.ui.customViews.streamPlayer.StreamTapPlayerView;
import com.rheotv.android.ui.customViews.streamPlayer.StreamUtils;
import com.rheotv.android.ui.decorators.LeftOverlapDecorator;
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog;
import com.rheotv.android.ui.fragments.ScratchCardNavigator;
import com.rheotv.android.ui.fragments.ScratchDialogFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppUtils;
import com.rheotv.android.utils.AppUtilsKt;
import com.rheotv.android.utils.BackPressUpdateClickListener;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.ScreenUtils;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.TimeUtils;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.animation.CustomRequestAnimation;
import com.rheotv.android.utils.animation.ShareAnimation;
import com.rheotv.android.utils.customview.AnimatedScratchCardView;
import com.rheotv.android.utils.hourglass.HourglassAsync;
import com.rheotv.android.utils.keyboardCheck.KeyboardEventListener;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;
import static com.rheotv.android.utils.AppConstants.REWARD_TYPE_FIRST_COMMENT;
import static com.rheotv.android.utils.AppConstants.REWARD_TYPE_TEN_MINUTE_STREAM;

/**
 * A simple {@link Fragment} subclass.
 */
public class StreamPlayerFragment extends BaseFragment<FragmentStreamPlayerBinding, StreamPlayerViewModel>
        implements ViewPagerMediator.ViewPagerOnPageSelectedListener, PlayerVideoQualityBottomSheet.VideoQualityChangeListener,
        LoginFragmentBottomDialog.LoginFragmentCallback, ScratchCardNavigator, StreamPlayerNavigator,
        StreamPlayerCallbackListener, StreamChatFragment.ChatScrollListener, StreamTapPlayerView.PlayerAnalyticsListener, StickerGridRecyclerAdapter.StickerSelectionListener, HeartAnimator.HeartAnimatorInteractionListener {

    private final String TAG = getClass().getSimpleName();

    private long TOTAL_PROGRESS_TIME = 0;
    private long TIME_DELAY_TO_SHOW_VIDEO_ALERT = 0;
    public long TIME_UNTIL_FINISH = -1;

    public static final int VIEW_PROFILE = 0x00;
    public static final int FOLLOW_USER = 0x01;
    public static final int REPORT_USER = 0x02;
    public static final int BLOCK_USER = 0x03;
    public static final int DELETE_COMMENT = 0x04;
    public static final int BLOCK_COMMENT = 0x05;
    public static final int REPORT_POST = 0x06;
    public static final int PIN_COMMENT = 0x07;
    public static final int MOVE_THRESHOLD = 50;
    private long videoStartTime = 0;

    @Inject
    ViewModelProviderFactoryV2 mViewModelFactory;

    private StreamPlayerViewModel mViewModel;
    private FragmentStreamPlayerBinding mBinding;
    private StreamChatFragment chatFragment;
    private ScorecardAdapter scorecardAdapter;
    private HeartAnimator heartAnimator;
    private TextAnimator textAnimator;
    private ShareAnimation shareAnimation;
    private TextAnimator followAnimation;
    private CustomRequestAnimation customRequestAnimation;
    private StreamHolder streamHolder;
    private LoginFragmentBottomDialog loginDialogFragment;
    private PlayerGiftBottomSheet playerGiftBottomSheet;
    private HourglassAsync nextRewardCountDownTimer;
    private ScratchDialogFragment scratchDialogFragment;
    private HashMap<String, Object> baseProperties;
    private String sourceScreen;
    private StreamRecentFollowerAdapter recentFollowerAdapter;
    private ViewGroup currentEventView;
    private Handler mHandler;
    private boolean shouldPausePlayer = false;

    private int screenWidth;
    private int mSlop;
    private int mScratchCardBottomMargin = AppConstants.PORTRAIT_PLAYER_SCRATCH_CARD_BOTTOM_MARGIN;

    private boolean isFragmentRecreated = false;
    public boolean shouldShowTenMinuteAlert = false;
    private boolean isVisible = false;
    private boolean isFirstWatchEventTracked = false;

    private boolean isFirstWatchEvent5MinsTracked = false;

    SharedPrefsUtils sharedPrefsUtils;

    public String streamFragmentPosition;

    protected Queue<Runnable> customRoomQueue = new LinkedList<>();
    protected Queue<Runnable> actionQueue = new LinkedList<>();


    boolean isStickerBottomSheetVisible;

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            if (isNetworkConnected())
//                mViewModel.connectChat();
        }
    };

    private BroadcastReceiver videoStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mViewModel != null && mBinding != null && mViewModel.isPageSelected && intent.hasExtra(AppConstants.VIDEO_STATE)) {
                boolean isPlay = intent.getBooleanExtra(AppConstants.VIDEO_STATE, false);
                if (isPlay)
                    mBinding.videoView.mutePlayer();
                else
                    mBinding.videoView.unMutePlayer();
            }
        }
    };

    public static StreamPlayerFragment getInstance(PostObject postObject, String sourceScreen) {
        StreamPlayerFragment fragment = new StreamPlayerFragment();
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
        return R.layout.fragment_stream_player;
    }

    @Override
    public StreamPlayerViewModel getViewModel() {
        if (mViewModel == null) {
            mViewModel = new ViewModelProvider(this, mViewModelFactory).get(StreamChatFragment.DEFAULT_KEY + streamFragmentPosition, StreamPlayerViewModel.class);
            if (getArguments() != null) {
                mViewModel.postObject.set(getArguments().getParcelable(AppConstants.ARG_POST));
                mViewModel.viewCount.set(mViewModel.postObject.get().getViews());
                mViewModel.live.set(mViewModel.postObject.get().isLive());
                mViewModel.loadPost(false);
            }
        }
        mViewModel.setNavigator(this);

        mViewModel.loadDailyRewards();
        return mViewModel;
    }

    private void observeLiveData() {
        try {
            if (mViewModel == null) {
                return;
            }

            mViewModel.reconnectChat.observe(getViewLifecycleOwner(), reconnect -> reconnectedChat());
            mViewModel.tournamentScore.observe(getViewLifecycleOwner(), scoreboardResponse -> {
                if (!isAdded() || isDetached() || isRemoving() || mBinding == null) return;
                scorecardAdapter.addItems(scoreboardResponse.getTeamsList());
                Fragment fragment = getChildFragmentManager().findFragmentByTag(ScoreFragment.TAG);
                if (fragment != null) {
                    ((ScoreFragment) fragment).updateView(scoreboardResponse);
                    return;
                }
                fragment = getChildFragmentManager().findFragmentByTag(ScoreBoardDialogFragment.TAG);
                if (fragment != null) {
                    ((ScoreBoardDialogFragment) fragment).updateView(scoreboardResponse);
                }
            });
            mViewModel.totalHearts.observe(getViewLifecycleOwner(), hearts -> {
                if (!isAdded() || isDetached() || isRemoving() || mBinding == null) return;
                heartAnimator.animateHeartUp();
                mBinding.videoView.animateHeartUp(hearts);
                BindingUtils.setNumberFormat(mBinding.heartCountView, hearts);
            });
            mViewModel.loadPostStatus.observe(getViewLifecycleOwner(), this::connectPlayer);
            mViewModel.onFollowingUpdate.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (!isAdded() || isDetached() || isRemoving() || mBinding == null) return;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (mBinding == null || mViewModel == null || isStateSaved() || !isAdded())
                            return;
                        mViewModel.toggleFollowCount();
                        mBinding.videoView.updateFollowing(mViewModel.onFollowingUpdate.get());
                        if (mViewModel.onFollowingUpdate != null && !mViewModel.onFollowingUpdate.get())
                            followAnimation.start();
                        else
                            followAnimation.stop();
                        mBinding.executePendingBindings();
                    });
                }
            });
            mViewModel.viewCount.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (!isAdded() || isDetached() || isRemoving()) return;
                    if (mBinding != null && streamHolder != null)
                        mBinding.videoView.post(() -> {
                            if (mBinding != null)
                                mBinding.videoView.updateViewCount(mViewModel.viewCount.get());
                        });
                }
            });
            mViewModel.recentFollowerEvent.observe(getViewLifecycleOwner(), list -> {
                if (!isAdded() || isDetached() || isRemoving()) return;
                recentFollowerAdapter.submitList(list);
            });
            mViewModel.currentEvent.observe(getViewLifecycleOwner(), this::onNewStreamEvent);
            mViewModel.showFirstCommentReward.observe(getViewLifecycleOwner(), l -> checkAndShowCommentReward());
            mViewModel.shareEventData.observe(getViewLifecycleOwner(), this::handleGameMomentReadyToolTip);
            mViewModel.onPostShareEvent.observe(getViewLifecycleOwner(), postShare -> showShareToolTip(postShare.getText(), false));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    Runnable gameMomentToolTipRunnable;

    private void handleGameMomentReadyToolTip(StreamEventResponse shareData) {
        if (!isAdded() || isDetached() || isRemoving())
            return;
        gameMomentToolTipRunnable = () -> {
            if (!isAdded() || isDetached() || isRemoving() || !isResumed() || mBinding == null)
                return;
            if (getResources().getConfiguration() != null &&
                    getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                return;
            showShareToolTip(shareData.getText(), true);
        };
        if (mHandler != null)
            mHandler.postDelayed(gameMomentToolTipRunnable, 300000);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isFragmentRecreated = true;
            streamFragmentPosition = savedInstanceState.getString("position_stream", "");
        }
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.myLooper());
        mViewModel.initHandlers();
        mViewModel.chatHelper = ChatHelper.getInstance(getContext());
        loginDialogFragment = LoginFragmentBottomDialog.getInstance(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
        loginDialogFragment.setmCallback(this);
        setupRewardTimer();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(screenDetailVisibilityReceiver, new IntentFilter("detail_screen_visibility"));
        if (sourceScreen != null && !sourceScreen.isEmpty()) {
            mViewModel.baseProperties.put(AppConstants.SCREEN_SOURCE, sourceScreen);
        }
        baseProperties = new HashMap<>(mViewModel.baseProperties);
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("position_stream", streamFragmentPosition);
        Fragment fragment = getChildFragmentManager().findFragmentByTag(StreamChatFragment.TAG);
        if (fragment != null && !isStateSaved()) {
            getChildFragmentManager().beginTransaction().remove(fragment).commitNow();
            getChildFragmentManager().executePendingTransactions();
        }
        super.onSaveInstanceState(outState);
    }

    boolean rewardIn5Shown;
    boolean rewardIn1Shown;

    private void setupRewardTimer() {
        if (mViewModel == null || mViewModel.currentPost.get() == null || mBinding == null) return;
        TOTAL_PROGRESS_TIME = mViewModel.getRewardTimeFromPost() > 0 ? mViewModel.getRewardTimeFromPost() : (RewardManager.getInstance().getVideoRewardActivationTime() / 1000);
        TIME_DELAY_TO_SHOW_VIDEO_ALERT = RewardManager.getInstance().getVideoRewardAlertDelayTime();
        if (RewardManager.getInstance().isTenMinuteStreamRewardAvailable() && mViewModel.live.get()) {
            nextRewardCountDownTimer = new HourglassAsync(TOTAL_PROGRESS_TIME, TimeUnit.SECONDS) {
                @Override
                public void onTimerTick(long remainingTime) {
                    TIME_UNTIL_FINISH = remainingTime;
                    if (mViewModel == null) return;
                    mViewModel.setRewardTimeFromPost(remainingTime);
                    if (remainingTime < (TOTAL_PROGRESS_TIME - 60) && !rewardIn1Shown) {
                        //Show Reward Message
                        mViewModel.addRewardTime((TOTAL_PROGRESS_TIME - 60) / 60);
                        rewardIn1Shown = true;
                    } else if (remainingTime < (TOTAL_PROGRESS_TIME / 2) && !rewardIn5Shown) {
                        //Show Reward Message
                        mViewModel.addRewardTime(TOTAL_PROGRESS_TIME / 2 / 60);
                        rewardIn5Shown = true;
                    }
                    if (playerGiftBottomSheet != null && playerGiftBottomSheet.isVisible()) {
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

    private void activateExitAlert() {
        if (mHandler != null)
            mHandler.postDelayed(() -> {
                if (CommonUtils.isUserLoggedin()) {
                    shouldShowTenMinuteAlert = true;
                }
            }, TIME_DELAY_TO_SHOW_VIDEO_ALERT);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusModel.UpdateCoin updateCoin) {
        if (mViewModel == null || mBinding == null || isRemoving() || isStateSaved()) return;
        if (updateCoin != null) {
            if (CommonUtils.isUserLoggedin()) {
                if (!isResumed()) {
                    actionQueue.add(() -> mBinding.totalCoinCountTextView.setText(RewardManager.getInstance().getTotalCoins()));
                } else
                    mBinding.totalCoinCountTextView.setText(RewardManager.getInstance().getTotalCoins());
            } else
                mBinding.totalCoinCountTextView.setText("");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() instanceof StreamPlayerActivity) {
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
        mBinding.setViewModel(mViewModel);
        setUpWork();
    }

    public void setUpWork() {
        showDetails(StreamPlayerContainerFragment.isInfoVisible);
        setupViews();

        mBinding.streamTitleTextView.postDelayed(() -> mViewModel.addDefaultEvent(), 10000);

        if (!CommonUtils.isShareTutorialShown() && CommonUtils.isUserLoggedin())
            showFirstShareToolTip();
    }

    private void showFirstShareToolTip() {
        if (isStateSaved() || mViewModel == null || mBinding == null || !isAdded() || !isVisible)
            return;
        if (mHandler == null) mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(() -> {
            if (isAdded() && !CommonUtils.isShareTutorialShown() && isVisible && RewardManager.getInstance().isShareRewardAvailable()) {
                CommonUtils.setShareTutorialShown();
                showShareToolTip(getString(R.string.share_tooltip_message), false);
            }
        }, 45000);
    }

    private void showShareToolTip(@Nullable String message, boolean autoHide) {
        if (message == null || !isAdded() || isDetached() || isRemoving() || !isVisible || mBinding == null)
            return;
        if (!isResumed()) {
            actionQueue.add(() -> showShareToolTip(message, autoHide));
            return;
        }
        new SimpleTooltip.Builder(getContext())
                .anchorView(mBinding.shareButton)
                .text(message)
                .gravity(Gravity.START)
                .animated(true)
                .onShowListener(tooltip -> {
                    if (autoHide) {
                        new HourglassAsync(2, TimeUnit.SECONDS) {
                            @Override
                            public void onTimerTick(long remainingTime) {
                            }

                            @Override
                            public void onTimerFinish() {
                                mHandler.post(tooltip::dismiss);
                            }
                        }.startTimer();
                    }
                })
                .textColor(ContextCompat.getColor(getContext(), android.R.color.white))
                .arrowColor(ContextCompat.getColor(getContext(), R.color.color_accent))
                .backgroundColor(ContextCompat.getColor(getContext(), R.color.color_accent))
                .transparentOverlay(true)
                .build()
                .show();
    }

    private void setupViews() {
        if (mViewModel == null || mBinding == null || isStateSaved()) return;
        mViewModel.orientation.set(getResources().getConfiguration().orientation);
        if (heartAnimator == null) {
            heartAnimator = new HeartAnimator(mBinding.heartContainer, mViewModel.heartAnimatorCallback);
            heartAnimator.start(new WeakReference<>(mBinding.heartButton));
        }

        if (textAnimator == null) {
            textAnimator = new TextAnimator(getContext(), mBinding.chatHint, R.array.chat_box_hints, mViewModel.authorUsername());
            textAnimator.start();
        }

        if (shareAnimation == null) {
            shareAnimation = new ShareAnimation(mBinding.shareButton);
            shareAnimation.setupDrawableList(getContext());
            shareAnimation.setAnimationDuration(30000);
        }
        if (customRequestAnimation == null) {
            customRequestAnimation = new CustomRequestAnimation(mBinding.playRequestButton);
            customRequestAnimation.setAnimationDuration(15000);
        }

        if (followAnimation == null) {
            followAnimation = new TextAnimator(getContext(), mBinding.followIndicatorView, null, R.anim.scale_down, R.anim.scale_up, 60000, mViewModel.authorUsername());
        }

        if (scorecardAdapter == null) {
            scorecardAdapter = new ScorecardAdapter(new ArrayList<>());
        }

        if (recentFollowerAdapter == null) {
            recentFollowerAdapter = new StreamRecentFollowerAdapter(this::showMenuBottomSheet);
        }
        mBinding.rvRecentFollowers.addItemDecoration(new LeftOverlapDecorator(-24));
        if (CommonUtils.isUserLoggedin()) {
            mBinding.totalCoinCountTextView.setText(RewardManager.getInstance().getTotalCoins());
        }

        if (chatFragment == null) {
            chatFragment = StreamChatFragment.newInstance(mViewModel.getPostId(), mViewModel.isModerator());
            chatFragment.setmListener(this);
        }
        getChildFragmentManager().beginTransaction().replace(getContainerId(), chatFragment, StreamChatFragment.TAG).commit();

        mBinding.scoreRv.setAdapter(scorecardAdapter);
        mBinding.scoreRvLand.setAdapter(scorecardAdapter);
        mBinding.rvRecentFollowers.setAdapter(recentFollowerAdapter);

        mBinding.leaderBoardButton.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                Map<String, Object> properties = new HashMap<>(getViewModel().baseProperties);
                properties.put("is_first", CommonUtils.isFirstTimeLeaderBoardClicked());
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_LEADERBOARD_PAGE_CLICKED, properties);
                CommonUtils.setFirstTimeLeaderBoardClicked();
                Intent intent = new Intent(getContext(), LeaderBoardActivity.class);
                intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
                startActivity(intent);
            }
        });
        mBinding.coinClickableArea.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                Map<String, Object> map = new HashMap<>(getViewModel().baseProperties);
                map.put("is_first", CommonUtils.isFirstTimeCoinsClicked());
                map.put("total_coins_count", RewardManager.getInstance().getTotalCoins());
                SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_TOTAL_COIN_CLICKED, map);
                CommonUtils.setFirstTimeCoinsClicked();
                if (CommonUtils.isUserLoggedin()) {
                    Intent intent = new Intent(getContext(), RewardsActivity.class);
                    intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
                    startActivity(intent);
                } else
                    openLoginFlow(getString(R.string.login_to_get_reward_message));
            }
        });
        mBinding.authorDetailView.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                showProfileMenuBottomSheet();
            }
        });
        mBinding.gameNameTextView.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                gameClicked();
            }
        });
        mBinding.scorecardLayoutClickableArea.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                portraitScorecardClicked();
            }
        });
        mBinding.scoreHolderView.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_SCOREBOARD_INDICATOR_CLICKED, baseProperties);
                mViewModel.onScoreIndicatorClick();
            }
        });
        mBinding.scorecardLayoutLandClickableArea.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                landscapeScoreboardClicked();
            }
        });
        //mBinding.streamDescriptionTextView.setOnClickListener(v -> showDescrip¬tion());
        mBinding.coinImageView.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                onGiftViewClick();
            }
        });
        mBinding.playRequestButton.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                playRequestClicked();
            }
        });
        mBinding.giftButtonPortrait.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                onStickerViewClick();
            }
        });
        mBinding.chatStateButton.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                onChatViewClick();
            }
        });
        mBinding.shareButton.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                onShareViewClick();
            }
        });
        mBinding.heartButton.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                Map<String, Object> map = new HashMap<>(mViewModel.baseProperties);
                map.put("is_first", CommonUtils.isFirstTimeLiked());
                map.put("author", mViewModel.authorUsername());
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STREAM_LIKED, map);
                CommonUtils.setFirstTimeLiked();
                heartAnimator.fadeAndScaleHeart(new WeakReference<>(v));
            }
        });
        mBinding.gameTagButton.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@org.jetbrains.annotations.Nullable View v) {
                handledGameTagClick();
            }
        });
        mBinding.videoView.resizeViewOnSetup();
        mViewModel.isLandscapeScoreboardVisible.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (mViewModel == null || mBinding == null || !isAdded() || isDetached()) return;
                mHandler.post(() -> {
                    if (mViewModel.isLandscapeScoreboardVisible.get() == false) {
                        Fragment fragment = getChildFragmentManager().findFragmentByTag(ScoreFragment.TAG);
                        if (fragment != null) {
                            getChildFragmentManager().beginTransaction().remove(fragment).commit();
                            getChildFragmentManager().executePendingTransactions();
                        }
                    }
                });
            }
        });
        mBinding.pinComment.pinImageView.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                if (mViewModel.isModerator() || mViewModel.isStreamer())
                    mViewModel.unpinComment();
            }
        });
        mBinding.pinCommentLand.pinImageView.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                if (mViewModel.isModerator() || mViewModel.isStreamer())
                    mViewModel.unpinComment();
            }
        });
        mBinding.followedLayout.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                showMenuBottomSheet(mViewModel.getEventFollowed().getUsername(),
                        mViewModel.getEventFollowed().getProfilePic());
            }
        });
        mBinding.lineMessageHolder.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                onGiftViewClick();
            }
        });

        setPageMargin(getContext().getResources().getConfiguration().orientation);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        mSlop = ViewConfiguration.get(getActivity()).getScaledTouchSlop();
        //mBinding.slideView.setOnTouchListener(containerTouchListener);
        //mBinding.infoSlide.setOnTouchListener(containerTouchListener);
        //mBinding.videoView.setOnTouchListener(containerTouchListener);
        //mBinding.chatContainer.setOnTouchListener(containerTouchListener);
        //mBinding.videoView.getParent().requestDisallowInterceptTouchEvent(true);
        adjustVideoViewPosition(false);
        mBinding.videoView.setAnalyticsListener(this);
        mBinding.videoView.adjustPlayerHeight(getResources().getConfiguration().orientation);

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

        mViewModel.isCustomRoomEnabled.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (mViewModel == null || mBinding == null || !isAdded() || isDetached()) return;
                mBinding.getRoot().post(() -> {
                    if (mViewModel.isCustomRoomEnabled.get() != null && mViewModel.isCustomRoomEnabled.get() && customRequestAnimation != null) {
                        customRequestAnimation.startAnimation();
                    }
                });
            }
        });

        mViewModel.updateCustomRoomPage.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (mViewModel == null || mBinding == null || !isAdded() || isDetached()) return;
                if (getContext() == null) return;
                mHandler.post(() -> LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(AppConstants.FILTER_PLAY_REQUEST)));
            }
        });

        mViewModel.commentSuggestion.observe(getViewLifecycleOwner(), this::setSuggestion);
        mBinding.topArrow.setOnClickListener(new BackPressUpdateClickListener() {
            @Override
            public void onViewClick(@Nullable View v) {
                handledGameTagClick();
            }
        });

        mBinding.flagIconButton.setOnClickListener(view -> onFlagBtnClick());
        mBinding.settingsIconButton.setOnClickListener(view -> onSettingViewClick());
    }

    private void handledGameTagClick() {
        if (mViewModel == null || mBinding == null || isStateSaved()) return;
        if (mBinding.topArrow.getVisibility() == View.VISIBLE)
            mBinding.topArrow.setVisibility(View.GONE);
        else
            mBinding.topArrow.setVisibility(View.VISIBLE);
        EventBus.getDefault().post(EventBusModel.ShowTags.INSTANCE);
    }

    private ArrayList<StreamEvent> getRecentFollower() {
        ArrayList<StreamEvent> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            list.add(new StreamEvent("https://mk0trickyphotos51tq5.kinstacdn.com/wp-content/uploads/2017/08/6.jpg", ""));
        }

        return list;
    }

    private void adjustVideoViewPosition(boolean newConfig) {
        if (mBinding == null || isStateSaved()) return;
        ViewTreeObserver observer = mBinding.infoLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(infoLayoutListener);
    }

    ViewTreeObserver.OnGlobalLayoutListener infoLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (!isAdded() || isStateSaved() || mBinding == null) return;
            int marginTop = mBinding.infoLayout.getHeight() + CommonUtils.toPix(58 - (getActivity() instanceof HomeActivity ? 28 : 0));
            ConstraintLayout.LayoutParams newLayoutParams = (ConstraintLayout.LayoutParams) mBinding.videoView.getLayoutParams();
            newLayoutParams.topMargin = Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && !mViewModel.isFullPortrait() ? marginTop : 0;
            newLayoutParams.leftMargin = 0;
            newLayoutParams.rightMargin = 0;
            newLayoutParams.bottomMargin = 0;
                /*if (!newConfig) {
                    setPlayerObserver();
                }*/
            mBinding.videoView.setLayoutParams(newLayoutParams);
            mBinding.infoLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this::onGlobalLayout);
        }
    };


    @Override
    public void setPlayerObserver() {
        if (mViewModel == null || isStateSaved() || !isAdded() || isRemoving())
            return;
        if (mBinding == null) {
            mHandler.postDelayed(this::setPlayerObserver, 500);
            return;
        }
        ViewTreeObserver observer = mBinding.videoView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mBinding != null) {
                    if (isAdded() && !isStateSaved() && getActivity() != null) {
                        int y = (int) (mBinding.videoView.getY());
                        if (y > 0) {
                            y += mBinding.videoView.getHeight();
                            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mBinding.chatContainer.getLayoutParams();
                            lp.topMargin = y - (getActivity() instanceof HomeActivity ? 0 : ScreenUtils.getStatusBarHeight(getActivity()));
                            mBinding.chatContainer.setLayoutParams(lp);
                        }
                    }
                    mBinding.videoView.getViewTreeObserver().removeOnGlobalLayoutListener(this::onGlobalLayout);
                }
            }
        });
    }

    @Override
    public void trackEventForwardSeek(long duration) {

    }

    @Override
    public void trackEventBackwardSeek(long duration) {

    }

    @Override
    public void onPlayerPausedAfterMomentPlayed() {

    }

    PostGift currentGreeting;

    private void showPostGiftCard(PostGift postGift) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (currentGreeting != null) {
            exitPostGiftCard(postGift);
        } else {
            enterPostGiftCard(postGift);

        }
        //mBinding.greetPinned.
    }

    public void exitPostGiftCard(final PostGift postGift) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        mBinding.gitCardContainer.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(mBinding.greetPinned.parent, View.TRANSLATION_X, 0, -screenWidth);
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
        ObjectAnimator animator = ObjectAnimator.ofFloat(mBinding.greetPinned.parent, View.TRANSLATION_X, -screenWidth, 0);
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

                new HourglassAsync(greetingDuration, TimeUnit.MILLISECONDS) {

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
                }
                        .setMaxDuration(greetingDuration)
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


    private void startLiveTextViewAnimation() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        float height = mBinding.gameNameTextView.getMeasuredHeight();
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new LinearInterpolator());
        animationSet.setDuration(500);
        animationSet.playTogether(ObjectAnimator.ofFloat(mBinding.watchingCountTextView, "translationY", -height),
                ObjectAnimator.ofFloat(mBinding.liveTextView, "translationY", -height),
                ObjectAnimator.ofFloat(mBinding.gameNameTextView, "alpha", 0f),
                ObjectAnimator.ofFloat(mBinding.streamDurationTextView, "alpha", 0f));
        animationSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mBinding == null || mViewModel == null || isStateSaved()) return;
                mBinding.watchingCountTextView.setVisibility(View.VISIBLE);
                mBinding.liveTextView.setVisibility(View.VISIBLE);
                mBinding.gameNameTextView.setVisibility(View.INVISIBLE);
                mBinding.streamDurationTextView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animationSet.start();
        mBinding.watchingCountTextView.setVisibility(View.VISIBLE);
//        mBinding.watchingIcon.setVisibility(View.VISIBLE);
    }

    private Runnable mLiveTextViewAnimationRunnable = this::startLiveTextViewAnimation;

   /* private View.OnTouchListener containerTouchListener = new View.OnTouchListener() {
        private int initialX;

        private float initialTouchX;

        private float initialY;

        private float initialTouchY;

        int lastAction;

        boolean isDirectionFound;

        boolean isHorizontalScroll;

        private boolean shouldPassEventToParent = false;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    //remember the initial position.
                    initialX = (int) mBinding.container.getX();
                    initialY = (int) mBinding.container.getY();

                    //get the touch location
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    shouldPassEventToParent = initialTouchX > screenWidth * 0.8 && mBinding.container.getX() <= screenWidth * 0.1;

                    lastAction = event.getAction();
                    isDirectionFound = false;
                    isHorizontalScroll = false;
                    if (view.getId() == mBinding.slideView.getId()) {
                        return true;
                    }

                    break;

                case MotionEvent.ACTION_CANCEL:

                case MotionEvent.ACTION_UP:
                    //As we implemented on touch listener with ACTION_MOVE,
                    //we have to check if the previous action was ACTION_DOWN
                    //to identify if the user clicked the view or not.

                    lastAction = event.getAction();
                    boolean isVisible;

                    Intent intent = new Intent("detail_screen_visibility");
                    if (mBinding.container.getX() > screenWidth / 2) {
                        isVisible = false;
                        intent.putExtra("show_detail", false);
                        mBinding.container.animate().x(screenWidth).setDuration(300).start();
                        //mBinding.slideChild.setVisibility(View.VISIBLE);
                    } else {
                        //isVisible = true;
                        intent.putExtra("show_detail", true);
                        mBinding.container.animate().x(0).setDuration(300).start();
                        //mBinding.slideChild.setVisibility(View.GONE);
                    }
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    if (isDirectionFound) {
                        return true;
                    } else {
                        if (view.getId() == mBinding.infoSlide.getId()) {
                            mBinding.infoLayout.dispatchTouchEvent(event);
                        }
                        return false;
                    }

                case MotionEvent.ACTION_MOVE:
                    //Calculate the X and Y coordinates of the view.


                    int dx = (int) (event.getRawX() - initialTouchX);
                    int dy = (int) (event.getRawY() - initialTouchY);
                    Log.i("SCROLL_TAG", "MOVE::" + "dx::" + dx + " --- dy::" + dy);
                    if (shouldPassEventToParent) {
                        shouldPassEventToParent = false;
                        if (getView() != null && getView().getParent() != null)
                            getView().getParent().requestDisallowInterceptTouchEvent(false);
                    } else {
                        if (getView() != null && getView().getParent() != null)
                            getView().getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    if (!isDirectionFound) {
                        Log.i("SCROLL_TAG", "MOVE::" + "Direction not found");
                        if (Math.abs(dy) > mSlop && Math.abs(dx) < mSlop) {
                            //Vertical Scroll More
                            mBinding.container.getParent().requestDisallowInterceptTouchEvent(false);
                            isDirectionFound = true;

                        } else if (Math.abs(dx) > mSlop && Math.abs(dy) < mSlop) {
                            //Horizontal Scroll More
                            mBinding.container.getParent().requestDisallowInterceptTouchEvent(true);
                            isHorizontalScroll = true;
                            isDirectionFound = true;

                        }

                    } else if (isHorizontalScroll) {
                        long finalX = (initialX + (int) (event.getRawX() - initialTouchX));
                        if (finalX < 0) {
                            finalX = 0;
                        }
                        if (finalX == 0) {
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                        } else {
                            mBinding.container.animate().x(finalX).setDuration(0).start();
                        }
                        Log.i("SCROLL_TAG", "MOVE::" + "Direction found Allow parent");
                    } else {
                        Log.i("SCROLL_TAG", "MOVE::" + "Direction found Allow parent");
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        return false;
                    }
                    lastAction = event.getAction();
                    return true;
            }
            return false;
        }
    };*/

    private BottomSheetDialogFragment bottomSheet;

    public void playRequestClicked() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (CommonUtils.isUserLoggedin()) {
            if (mViewModel.currentPost.get() == null) return;
            if (mViewModel.currentPost.get().isCustomRoomEnabled())
                bottomSheet = CustomRoomBottomSheet.Companion.newInstance("", mViewModel.currentPost.get());
            else
                bottomSheet = RequestPlayFragment.newInstance("", mViewModel.currentPost.get());
            bottomSheet.show(getChildFragmentManager(), CustomRoomBottomSheet.TAG);
        } else {
            openLoginFlow();
        }
    }


    private void connectPlayer(Status status) {
        if (mBinding == null || mViewModel == null || isStateSaved() || !isAdded() || isDetached() || isRemoving())
            return;
        if (status == Status.SUCCESS) {
            startRewardTimer();
            if (!customRoomQueue.isEmpty()) {
                customRoomQueue.poll().run();
            }
            if (streamHolder == null || isFragmentRecreated) {

                if (sharedPrefsUtils == null) {
                    sharedPrefsUtils = new SharedPrefsUtils();
                }
                streamHolder = new StreamHolder.Builder()
                        .setContext(getContext())
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
                        .build();
                mBinding.videoView.setViewCallbackListener(this);
                mBinding.videoView.setHeartAnimatorListener(this);
                isFragmentRecreated = false;
            }

            mBinding.videoView.attach(streamHolder);
            if (mViewModel.isPageSelected) {
                videoStartTime = System.currentTimeMillis();
                mBinding.videoView.attachPlayer();
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
                baseProperties.put("orientation", getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait" : "landscape");
                baseProperties.put("isLoggedIn", CommonUtils.isUserLoggedin());
                SegmentTracker.getInstance(requireContext()).recordScreenName(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER, baseProperties);
            }
        }
    }

    private void startRewardTimer() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        setupRewardTimer();
        if (nextRewardCountDownTimer != null && nextRewardCountDownTimer.isPaused()) {
            nextRewardCountDownTimer.startTimer();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "StreamPlayer_called: onAttach");
    }


    @Override
    public void onDetach() {
        super.onDetach();

        Log.i(TAG, "StreamPlayer_called: onDetach");
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
        bottomSheet.show(getChildFragmentManager(), ChatMenuOptionBottomSheet.TAG);
    }

    private void reportPost() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (CommonUtils.isUserLoggedin()) {
            mViewModel.reportPost();
        } else {
            openLoginFlow();
        }
    }

    @Override
    public void onReportPostSuccess() {
        showToast(getString(R.string.post_report_success));
    }

    private void followAuthor() {
        if (CommonUtils.isUserLoggedin()) {
            mViewModel.onFollowButtonClick();
        } else {
            openLoginFlow();
        }
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

    private void reportAuthor() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        mViewModel.reportComment(mViewModel.authorUsername(), "", false);
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

    private void showToast(String message) {
        if (!isAdded()) return;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void reconnectedChat() {
//        if (mBinding == null || mViewModel == null || isStateSaved() || !isAdded() || isDetached() || isRemoving())
//            return;
//        if (!NetworkUtils.isNetworkConnected(getContext())) {
//            if (!mViewModel.isNetworkChangeListening) {
//                getContext().registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//                mViewModel.isNetworkChangeListening = true;
//            }
//        } else {
//            mViewModel.connectChat();
//            mViewModel.isNetworkChangeListening = false;
//        }
    }

    private void authorClicked(String username) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        Intent intent = ProfileActivity.getCallingIntent(getContext());
        intent.putExtra("author_name", username);
        startActivity(intent);
    }

    private void gameClicked() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (mViewModel.currentPost.get() == null) return;
        Intent intent = new Intent(getContext(), UniversalActivity.class);
        intent.putExtra(AppConstants.SEE_ALL_TYPE, mViewModel.currentPost.get().getGame());
        intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, mViewModel.currentPost.get().getGameId());
        startActivity(intent);
    }

    private void portraitScorecardClicked() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (mViewModel.tournamentScore.getValue() == null) {
            showToast("Scoreboard is not available.");
            return;
        }
        ScoreBoardDialogFragment bottomSheet = ScoreBoardDialogFragment.newInstance("", mViewModel.getPostId(), mViewModel.tournamentScore.getValue());
        bottomSheet.show(getChildFragmentManager(), ScoreBoardDialogFragment.TAG);
    }

    private void landscapeScoreboardClicked() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (mViewModel.tournamentScore.getValue() == null) {
            showToast("Scoreboard is not available.");
            return;
        }
        Fragment fragment = getChildFragmentManager().findFragmentByTag(ScoreFragment.TAG);
        if (fragment == null) {
            ScoreFragment scoreFragment = ScoreFragment.newInstance("", mViewModel.getPostId(), mViewModel.tournamentScore.getValue(), v -> {
                mViewModel.isLandscapeScoreboardVisible.set(false);
                Fragment inflateFragment = getChildFragmentManager().findFragmentByTag(ScoreFragment.TAG);
                if (inflateFragment == null) return;
                getChildFragmentManager().beginTransaction().remove(inflateFragment).commit();
            });
            getChildFragmentManager().beginTransaction().add(mBinding.scoreboardContainerLand.getId(), scoreFragment, ScoreFragment.TAG).commit();
        }
        if (mViewModel.isChatBoxLandVisible.get()) {
            mViewModel.isChatBoxLandVisible.set(false);
        }
        mViewModel.onCloseScorecardClick();
        mViewModel.isLandscapeScoreboardVisible.set(true);
    }

    private void showDescription() {
        if (mViewModel.currentPost.get() == null) return;
        new DescriptionBottomSheetDialog.Builder()
                .addTitle(mViewModel.currentPost.get().getTitle())
                .addDescription(mViewModel.currentPost.get().getDescription())
                .addGame(mViewModel.getGameName())
                .addStreamDuration(mViewModel.currentPost.get().getStreamingDuration())
                .addSource(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER)
                .build()
                .show(getChildFragmentManager(), AppConstants.ARG_DESCRIPTION);
    }

    private void openLoginFlow() {
        openLoginFlow(null);
    }

    @Override
    public void openLoginFlow(String rewardMessage) {
        try {
            if (mBinding == null || mViewModel == null || isStateSaved()) return;
            if (loginDialogFragment.isAdded() || loginDialogFragment.isVisible())
                return;

            loginDialogFragment.setRewardText(rewardMessage);
            loginDialogFragment.showNoAddToBackStack(this.getChildFragmentManager(), AppConstants.LOGIN_FRAGMENT_TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAndShowVideoReward() {
        Log.i(getClass().getSimpleName(), "checkAndShowVideoReward");
        if (mBinding == null || mViewModel == null || isStateSaved() || !isVisible || !isAdded())
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
                    if (rewardScratchCard == null) return;
                    scratchDialogFragment = ScratchDialogFragment.getInstance(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER, rewardScratchCard, scratchCardImage);
                    if ((scratchDialogFragment.isAdded() || scratchDialogFragment.isVisible())) {
                        return;
                    }
                    scratchDialogFragment.show(StreamPlayerFragment.this.getChildFragmentManager(), AppConstants.SCRATCH_FRAGMENT_TAG, StreamPlayerFragment.this, suggestReward);
                    if (!CommonUtils.isFirstTimeWatchRewardScratched()) {
                        Map<String, Object> map = new HashMap<>(baseProperties);
                        map.put("author", mViewModel.authorUsername());
                        map.put("post_id", mViewModel.getPostId());
                        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FIRST_WATCH_REWARD_SCRATCHED, map);
                        CommonUtils.setFirstTimeWatchRewardScratched();
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
                            if (mBinding == null || mViewModel == null || isStateSaved()) return;
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

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isStickerBottomSheetVisible) {
            if (mBinding != null)
                mBinding.videoView.onOrientationChange(newConfig.orientation);
            SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_PLAYER_CHANGE_ORIENTATION, baseProperties);
            mViewModel.orientation.set(newConfig.orientation);
            switchChatContainer(newConfig.orientation);
            adjustVideoViewPosition(true);
        }

    }

    private void switchChatContainer(int orientation) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        int containerId;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mScratchCardBottomMargin = AppConstants.PORTRAIT_PLAYER_SCRATCH_CARD_BOTTOM_MARGIN;
            containerId = mBinding.chatContainer.getId();
        } else {
            mScratchCardBottomMargin = AppConstants.LANDSCAPE_SCRATCH_CARD_BOTTOM_MARGIN;
            containerId = mBinding.chatContainerLand.getId();
        }
        swapContainer(containerId);
        setPageMargin(orientation);

    }

    private void swapContainer(int containerId) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.remove(chatFragment);
        fragmentTransaction.commit();
        getChildFragmentManager().executePendingTransactions();

        fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(containerId, chatFragment, StreamChatFragment.TAG);
        fragmentTransaction.commit();
        getChildFragmentManager().executePendingTransactions();
    }

    @Override
    public void onPageSelected() {

        observeLiveData();
        Log.i(getClass().getSimpleName(), "pageCall_onPageSelected ");
        if (mViewModel == null) return;
        if (mBinding != null && !EventBus.getDefault().isRegistered(mBinding.videoView)) {
            EventBus.getDefault().register(mBinding.videoView);
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
//            EventBus.getDefault().post(new EventBusModel.UpdateStreamFragment(this));
        }

        mViewModel.isPageSelected = true;
        mViewModel.connectChat();

        isVisible = true;
        if (shareAnimation != null) {
            shareAnimation.startAnimation();
        }
        //mBinding.getRoot().postDelayed(mLiveTextViewAnimationRunnable, 10000);
        if (nextRewardCountDownTimer != null && nextRewardCountDownTimer.isPaused())
            nextRewardCountDownTimer.startTimer();
        if (streamHolder != null && mBinding != null)
            mBinding.videoView.attachPlayer();

        if (heartAnimator != null && mBinding != null) {
            heartAnimator.start(new WeakReference<>(mBinding.heartButton));
        }
        if (textAnimator != null)
            textAnimator.start();

        if (followAnimation != null && (mViewModel != null && mViewModel.onFollowingUpdate != null && !mViewModel.onFollowingUpdate.get()))
            followAnimation.start();
    }

    @Override
    public void onPageUnselected() {
        if (mViewModel != null)
            mViewModel.isPageSelected = false;
        Log.i(getClass().getSimpleName(), "pageCall_onPageUnselected ");
        isVisible = false;

        if (mHandler != null) {
            mHandler.removeCallbacks(mShowTutorial);
        }
        if (shareAnimation != null) {
            shareAnimation.stopAnimation();
        }
        if (customRequestAnimation != null) {
            customRequestAnimation.stopAnimation();
        }
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        sharedPrefsUtils.setBooleanPreference(RheoTvApp.getNonUiContext(), "player_tutorial_shown", true);
        if (mViewModel != null)
            mViewModel.disconnectChat();

        if (mBinding != null) {
            mBinding.videoView.detachPlayer();
            if (EventBus.getDefault().isRegistered(mBinding.videoView)) {
                EventBus.getDefault().unregister(mBinding.videoView);
            }
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
        }

        if (nextRewardCountDownTimer != null && nextRewardCountDownTimer.isRunning()) {
            nextRewardCountDownTimer.stopTimer();
        }

        if (heartAnimator != null) {
            heartAnimator.stop();
        }

        if (textAnimator != null)
            textAnimator.stop();

        if (followAnimation != null && (mViewModel != null && mViewModel.onFollowingUpdate != null && !mViewModel.onFollowingUpdate.get()))
            followAnimation.stop();
        if (mHandler != null && gameMomentToolTipRunnable != null) {
            mHandler.removeCallbacks(gameMomentToolTipRunnable);
        }
        trackPageChange();
    }

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
            if (!sharedPrefsUtils.getBooleanPreference(RheoTvApp.getNonUiContext(), "player_tutorial_shown", false)) {
                if (getContext() != null && isAdded() && !isStateSaved() && isVisible()) {
                    shouldPausePlayer = true;
                    PlayerTutorialActivity.Companion.startTutorial(getContext(), false);
                }
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        videoStartTime = System.currentTimeMillis();
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        if (!sharedPrefsUtils.getBooleanPreference(RheoTvApp.getNonUiContext(), "player_tutorial_shown", false) && mHandler != null) {
            mHandler.postDelayed(mShowTutorial, 30000);
        }
        Log.i(TAG, "StreamPlayer_called: onStart");
        if (nextRewardCountDownTimer != null && nextRewardCountDownTimer.isPaused()) {
            nextRewardCountDownTimer.startTimer();
        }

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(videoStateReceiver, new IntentFilter(AppConstants.FILTER_VIDEO_STATE));
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "StreamPlayer_called: onStop");
        if (networkStateReceiver != null && mViewModel.isNetworkChangeListening && getContext() != null) {
            try {
                getContext().unregisterReceiver(networkStateReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(e);
            }

        }
        if (nextRewardCountDownTimer != null && nextRewardCountDownTimer.isRunning()) {
            nextRewardCountDownTimer.stopTimer();
        }

        if (getContext() != null) {
            try {
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(videoStateReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        if (mViewModel != null) {
            mViewModel.removeRunnableFromHandler();
            mViewModel.isPageSelected = false;
        }
    }

    @Override
    public void onBottomSheetClose() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        isStickerBottomSheetVisible = false;
        CommonUtils.hideKeyboard(getActivity());
    }


    @Override
    public void onResume() {
        super.onResume();
        onPageSelected();
        new KeyboardEventListener(new WeakReference<>(this), isOpen -> {
            if (!isStickerBottomSheetVisible) {
                mViewModel.isChatBoxVisible.set(isOpen);
                Log.i(TAG, "keyboard_isOpen: " + isOpen);
            }
            return null;
        });

        Fragment fragment = getChildFragmentManager().findFragmentByTag(StreamChatFragment.TAG);
        if (fragment == null) {
            getChildFragmentManager().beginTransaction().replace(getContainerId(), chatFragment, StreamChatFragment.TAG).commitNow();
            getChildFragmentManager().executePendingTransactions();
        }
        runPendingTask();
    }

    private void runPendingTask() {
        if (!actionQueue.isEmpty()) {
            if (mHandler == null) mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mBinding == null || mViewModel == null || isStateSaved()) return;
                    Runnable runnable = actionQueue.poll();
                    if (runnable != null) {
                        runnable.run();
                    }
                    if (!actionQueue.isEmpty()) {
                        mHandler.postDelayed(this, 1000);
                    }
                }
            }, 1000);
        }
    }

    private void showDetails(boolean showDetail) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (showDetail) {
            mBinding.container.animate().x(0).setDuration(0).start();
        } else {
            mBinding.container.animate().x(ViewUtils.getScreenWidthInPx(RheoTvApp.getNonUiContext())).setDuration(0).start();
        }
        StreamPlayerContainerFragment.isInfoVisible = showDetail;
    }

    private BroadcastReceiver screenDetailVisibilityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showDetails(intent.getBooleanExtra("show_detail", true));
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (shouldPausePlayer) {
            shouldPausePlayer = false;
            return;
        }
        onPageUnselected();
        try {
            CommonUtils.hideKeyboard(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "StreamPlayer_called: onPause");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "StreamPlayer_called: onDestroy");
        /*if (mBinding != null)
            mBinding.videoView.detachPlayer();*/
        if (mViewModel != null) {
            mViewModel.disconnectChat();
        }

        if (mBinding.videoView != null) {
            mBinding.videoView.setViewCallbackListener(null);
            mBinding.videoView.setHeartAnimatorListener(null);
            ViewGroup parent = (ViewGroup) mBinding.videoView.getParent();
            if (parent != null) {
                int index = parent.indexOfChild(mBinding.videoView);
                if (index >= 0) {
                    parent.removeViewAt(index);
                }
            }
        }
        if (mBinding.youtubeDoubleTap != null) {
            ViewGroup parent = (ViewGroup) mBinding.youtubeDoubleTap.getParent();
            if (parent != null) {
                int index = parent.indexOfChild(mBinding.youtubeDoubleTap);
                if (index >= 0) {
                    parent.removeViewAt(index);
                }
            }
        }
        if (mBinding.thumbnailImageView != null) {
            ViewGroup parent = (ViewGroup) mBinding.thumbnailImageView.getParent();
            if (parent != null) {
                int index = parent.indexOfChild(mBinding.thumbnailImageView);
                if (index >= 0) {
                    parent.removeViewAt(index);
                }
            }
        }

        if (mBinding.progressBar != null) {
            ViewGroup parent = (ViewGroup) mBinding.progressBar.getParent();
            if (parent != null) {
                int index = parent.indexOfChild(mBinding.progressBar);
                if (index >= 0) {
                    parent.removeViewAt(index);
                }
            }
        }
        if (infoLayoutListener != null) {
            mBinding.infoLayout.getViewTreeObserver().removeOnGlobalLayoutListener(infoLayoutListener);
            infoLayoutListener = null;
        }
        if (chatTextLayoutListener != null) {
            mBinding.chatText.getViewTreeObserver().removeOnGlobalLayoutListener(chatTextLayoutListener);
            chatTextLayoutListener = null;
        }
        if (chatTextLandLayoutListener != null) {
            mBinding.chatTextLand.getViewTreeObserver().removeOnGlobalLayoutListener(chatTextLandLayoutListener);
            chatTextLandLayoutListener = null;
        }
        if (streamHolder != null) {
            streamHolder = null;
        }

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(screenDetailVisibilityReceiver);
        super.onDestroy();
        AppUtilsKt.INSTANCE.runGC();
    }

    @Override
    public void onLoginSuccess() {
        if (getContext() == null || mBinding == null || mViewModel == null || isStateSaved())
            return;
        startRewardTimer();
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(AppConstants.UPDATE_STORY_BROADCAST_FILTER));
    }

    @Override
    public void onLoginDialogClose() {

    }

    public void onSettingViewClick() {
        PlayerVideoQualityBottomSheet bottomSheetDialogFragment = PlayerVideoQualityBottomSheet.newInstance(StreamUtils.getFormats(mViewModel.currentPost.get().getVideoUrls()), streamHolder.getQualityFormat());
        bottomSheetDialogFragment.show(getChildFragmentManager(), PlayerVideoQualityBottomSheet.TAG);
    }

    @Override
    public void onCloseViewClick() {
        mBinding.videoView.rotateLayout();
    }

    @Override
    public void onScratchRevealed(String rewardId) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (shouldShowTenMinuteAlert) shouldShowTenMinuteAlert = false;
        Log.i(getClass().getName(), "onScratchRevealed " + rewardId);
        mViewModel.updateScratchCard(rewardId, getContext());
    }

    @Override
    public void checkRewardAvailable() {
        setupRewardTimer();
        checkRewards();
    }

    @Override
    public void onVideoQualityChanged(String videoQuality) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        mBinding.videoView.updateStreamQuality(videoQuality);
        sharedPrefsUtils.setStringPreference(getContext(), SharedPrefsUtils.VIDEO_FORMAT_REQUESTED, videoQuality);
        HashMap<String, Object> resProperties = new HashMap<>(baseProperties);
        resProperties.put("resolution", videoQuality);
        resProperties.put("username", CommonUtils.getUserName(getContext()));
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_VIDEO_RESOLUTION_CHANGE, resProperties);
    }

    private void trackPageChange() {
        if (!isAdded()) return;
        Map<String, Object> resProperties = new HashMap<>(mViewModel.baseProperties);
        resProperties.put("author", mViewModel.authorUsername());
        resProperties.put("is_first_lifetime", CommonUtils.isFirstTimePageChange());
        resProperties.put("duration", (System.currentTimeMillis() - videoStartTime) / 1000);
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_VIDEO_PAGE_CHANGE, resProperties);
        CommonUtils.setFirstTimePageChange();
    }

    @Override
    public void onShareViewClick() {
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_PLAYER_SHARE_CLICKED, baseProperties);
        if (mViewModel.getPostId() == null) return;
        if (CommonUtils.isUserLoggedin()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(AppConstants.BRANCH_POST_SOURCE_URL, mViewModel.getShareUrl());
            map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_LIVE_STREAM);
            Map<String, Object> baseProperties = new HashMap<>(mViewModel.baseProperties);
            if (shareAnimation != null) {
                int selectedImageResource = shareAnimation.getSelectedImageResource();
                if (selectedImageResource == R.drawable.ic_share_white_24dp) {
                    baseProperties.put("share_icon", "share");
                } else if (selectedImageResource == R.drawable.ic_whatsapp) {
                    baseProperties.put("share_icon", "whatsapp");
                } else if (selectedImageResource == R.drawable.ic_instagram) {
                    baseProperties.put("share_icon", "instagram");
                } else if (selectedImageResource == R.drawable.ic_facebook) {
                    baseProperties.put("share_icon", "facebook");
                }
            }
            PostShareBottomSheetFragment.show(getParentFragmentManager(),
                    PostShareBottomSheetFragment.Companion.build(
                            PostShareBottomSheetFragment.Companion.builder(baseProperties)
                                    .setGameName(mViewModel.getGameName())
                                    .setPostId(mViewModel.getPostId())
                                    .setSource(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER)
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
    public void onStickerSelected(Sticker sticker, String message) {
        mViewModel.sendGreeting(sticker, message);
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
            showToast("You don't have enough coin to send this sticker.");
        }
    }


    @Override
    public void onStickerViewClick() {
        if (CommonUtils.isUserLoggedin()) {
            StickerBottomSheet stickerBottomSheet = StickerBottomSheet.newInstance(mViewModel.currentPost.get() != null ? mViewModel.currentPost.get().getId() : "",
                    mViewModel.authorUsername(),
                    this);
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
    public void onGiftViewClick() {
        if (CommonUtils.isUserLoggedin()) {
            playerGiftBottomSheet = PlayerGiftBottomSheet.newInstance(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
            playerGiftBottomSheet.show(getChildFragmentManager(), PlayerGiftBottomSheet.TAG);
        } else {
            openLoginFlow(getString(R.string.new_reward_message));
        }
    }

    ViewTreeObserver.OnGlobalLayoutListener chatTextLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (!isAdded() || isStateSaved() || mBinding == null) return;
            mBinding.chatText.setFocusable(true);
            mBinding.chatText.setFocusableInTouchMode(true);
            mBinding.chatText.requestFocus();
            mBinding.chatText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    };

    ViewTreeObserver.OnGlobalLayoutListener chatTextLandLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (!isAdded() || isStateSaved() || mBinding == null) return;
            mBinding.chatTextLand.setFocusable(true);
            mBinding.chatTextLand.setFocusableInTouchMode(true);
            mBinding.chatTextLand.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    };

    @Override
    public void onChatViewClick() {
        try {
            if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded()) return;
            if (CommonUtils.isUserLoggedin()) {
                if (mViewModel.orientation.get() == Configuration.ORIENTATION_PORTRAIT) {
                    mViewModel.isChatBoxVisible.set(!mViewModel.isChatBoxVisible.get());
                    ViewTreeObserver vt = mBinding.chatText.getViewTreeObserver();
                    vt.addOnGlobalLayoutListener(chatTextLayoutListener);

                    mBinding.chatText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (!isAdded() || isStateSaved() || mBinding == null) return;
                            if (hasFocus)
                                mBinding.chatText.post(() -> CommonUtils.showKeyboard(getActivity(), mBinding.chatText));
                        }
                    });

                } else {
                    mViewModel.isChatBoxLandVisible.set(!mViewModel.isChatBoxLandVisible.get());
                    ViewTreeObserver vt = mBinding.chatTextLand.getViewTreeObserver();
                    vt.addOnGlobalLayoutListener(chatTextLandLayoutListener);
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
    public void streamEnded() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (TimeUtils.hasStreamNotStarted(mViewModel.getStartFrom())) {
            mBinding.futureStreamLayout.setVisibility(View.VISIBLE);
            mBinding.videoView.setVisibility(View.INVISIBLE);
            mBinding.youtubeDoubleTap.setVisibility(View.INVISIBLE);
            BindingUtils.setImageUrlUsingCache(mBinding.coverPic, mViewModel.currentPost.get().getCarouselThumbnail(), true);
            setCountdownForStream();
        } else {
            mViewModel.loadPost(true);
            EventBus.getDefault().post(true);
        }
    }

    @Override
    public void onControllerVisibilityChange(Boolean isVisible) {
        Log.i(TAG, "onControllerVisibilityChange: isVisible: " + isVisible);
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        mViewModel.isControlVisible.set(isVisible);
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
        if (objs.size() >= 1) {
            mBinding.layout1.setVisibility(View.VISIBLE);
            mBinding.layout1Value.setText(objs.get(0).getValue());
            mBinding.layout1Label.setText(objs.get(0).getType());
            if (objs.size() >= 2) {
                mBinding.layout2.setVisibility(View.VISIBLE);
                mBinding.layout2Value.setText(objs.get(1).getValue());
                mBinding.layout2Label.setText(objs.get(1).getType());
                mBinding.separator12.setVisibility(View.VISIBLE);
                if (objs.size() >= 3) {
                    mBinding.layout3.setVisibility(View.VISIBLE);
                    mBinding.layout3Value.setText(objs.get(2).getValue());
                    mBinding.layout3Label.setText(objs.get(2).getType());
                    mBinding.separator23.setVisibility(View.VISIBLE);
                    if (objs.size() >= 4) {
                        mBinding.layout4.setVisibility(View.VISIBLE);
                        mBinding.layout4Value.setText(objs.get(3).getValue());
                        mBinding.layout4Label.setText(objs.get(3).getType());
                        mBinding.separator34.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.layout4.setVisibility(View.GONE);
                    }
                } else {
                    mBinding.layout3.setVisibility(View.GONE);
                    mBinding.layout4.setVisibility(View.GONE);
                }
            } else {
                mBinding.layout2.setVisibility(View.GONE);
                mBinding.layout3.setVisibility(View.GONE);
                mBinding.layout4.setVisibility(View.GONE);
            }
        } else {
            mBinding.layout1.setVisibility(View.GONE);
            mBinding.layout2.setVisibility(View.GONE);
            mBinding.layout3.setVisibility(View.GONE);
            mBinding.layout4.setVisibility(View.GONE);
        }
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

    private int getContainerId() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_PORTRAIT ? mBinding.chatContainer.getId() : mBinding.chatContainerLand.getId();
    }

    @Override
    public void updatePos(long pos) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        mBinding.container.animate().x(pos).setDuration(0).start();
    }

    @Override
    public void handleChatContentVisibility() {
        if (mBinding.container.getX() > screenWidth / 2) {
            mBinding.container.animate().x(screenWidth).setDuration(300).start();
        } else {
            mBinding.container.animate().x(0).setDuration(300).start();
        }
    }

    @Override
    public int[] currentContainerPos() {
        return new int[]{(int) mBinding.container.getX(), (int) mBinding.container.getY()};
    }

    @Override
    public void trackFirstEventWatchStream() {
        if (!isFirstWatchEventTracked) {
            isFirstWatchEventTracked = true;
            if (CommonUtils.isFirstWatchEventNotTracked()) {
                CommonUtils.setFirstWatchEventTracked();
                SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_FIRST_WATCH_STREAM, baseProperties);
            }
        }
    }

    @Override
    public void trackFirstEventWatchStream5Mins(long ttl) {
        if (!isFirstWatchEvent5MinsTracked) {
            isFirstWatchEvent5MinsTracked = true;
            if (CommonUtils.isFirstWatchEvent5MinsNotTracked()) {
                CommonUtils.setFirstWatchEvent5MinsTracked();
                HashMap<String, Object> properties = new HashMap<>(baseProperties);
                properties.put("time_elapsed", ttl);
                SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_FIRST_WATCH_STREAM_5_MINS, properties);
            }
        }
    }

    @Override
    public void trackEventWatchStream30Secs(long ttl) {
        if (!isAdded()) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("time_elapsed", ttl);
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_WATCH_STREAM_30_SECS, properties);
    }

    @Override
    public void trackEventWatchStream5mins(long ttl) {
        if (!isAdded()) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("time_elapsed", ttl);
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_WATCH_STREAM_5_MINS, properties);
    }

    @Override
    public void trackEventWatchStream11mins(long ttl) {
        if (!isAdded()) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("time_elapsed", ttl);
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_WATCH_STREAM_11_MINS, properties);
    }

    @Override
    public void trackEventWatchStream30mins(long ttl) {
        if (!isAdded()) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("time_elapsed", ttl);
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_WATCH_STREAM_30_MINS, properties);
    }

    @Override
    public void trackEventWatchStream45mins(long ttl) {
        if (!isAdded()) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("time_elapsed", ttl);
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_WATCH_STREAM_45_MINS, properties);
    }

    @Override
    public void trackEventWatchStream1hrs(long ttl) {
        if (!isAdded()) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("time_elapsed", ttl);
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_WATCH_STREAM_1_HRS, properties);
    }

    @Override
    public void trackEventWatchStream2hrs(long ttl) {
        if (!isAdded()) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("time_elapsed", ttl);
        SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_WATCH_STREAM_2_HRS, properties);
    }

    public static String macAddress = null;

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
                    macAddress, res.getIsLive(), res.getGame(), res.getGameId())
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

    private void onNewStreamEvent(String event) {
        if (!isAdded() || isDetached() || isRemoving() || mBinding == null || mViewModel == null)
            return;
        if (currentEventView == null || mBinding.descriptionLayout.getVisibility() == View.VISIBLE)
            currentEventView = mBinding.descriptionLayout;
        currentEventView.animate()
                .x(-screenWidth)
                .setDuration(300)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        if (!isAdded() || isDetached() || isRemoving() || mBinding == null || mViewModel == null)
                            return;
                        currentEventView.setVisibility(View.INVISIBLE);
                        animationEventViewIn(event);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
    }

    private void animationEventViewIn(String event) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        switch (event) {
            case AppConstants.EVENT_RECENT_FOLLOWERS:
                currentEventView = mBinding.followLayout;
                break;
            case AppConstants.EVENT_PLAY_REQUEST:
                currentEventView = mBinding.newRequestLayout;
                break;
            case AppConstants.EVENT_ANNOUNCEMENT:
                currentEventView = mBinding.announcementLayout;
                break;
            case AppConstants.EVENT_CUSTOM_ROOM:
            case AppConstants.EVENT_CUSTOM_ROOM_REJECT:
            case AppConstants.EVENT_CUSTOM_ROOM_REFUNDED:
                currentEventView = mBinding.acceptedRequestLayout;
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(AppConstants.FILTER_PLAY_REQUEST));
                if (mViewModel.acceptedRequest.get() != null && bottomSheet == null || (bottomSheet != null && !bottomSheet.isAdded()))
                    showCustomRoomAcceptedEnable(mViewModel.acceptedRequest.get().getText());
                break;
            case AppConstants.EVENT_REWARD_TIME:
                currentEventView = mBinding.lineMessageLayout;
                break;
            case AppConstants.EVENT_WON_REWARD:
                currentEventView = mBinding.videoWatchRewardLayout;
                break;
            case AppConstants.EVENT_FOLLOWED:
                currentEventView = mBinding.followedLayout;
                break;
        }

        currentEventView.animate()
                .x(mBinding.gameTagButton.getMeasuredWidth() + CommonUtils.toPix(6))
                .setDuration(300)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        currentEventView.setVisibility(View.VISIBLE);
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
                }).start();
    }

    private void showMenuBottomSheet(String username, String profilePic) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        ArrayList<ListOption> listOptions = new ArrayList<>();
        listOptions.add(new ListOption.Header(StreamPlayerFragment.VIEW_PROFILE));
        ChatMenuOptionBottomSheet bottomSheet = ChatMenuOptionBottomSheet.Companion.newInstance(
                listOptions,
                (ListOption listOption) -> {
                    if (listOption instanceof ListOption.Header) {
                        onUserProfileClicked(username);
                    }
                    return null;
                }
        );
        bottomSheet.setChatMenuOptionData(mViewModel.getChatOptionMenuBottomSheetData(null, username, profilePic));
        bottomSheet.show(getChildFragmentManager(), ChatMenuOptionBottomSheet.TAG);
    }

    private void onUserProfileClicked(String username) {
        if (mBinding == null || mViewModel == null || isStateSaved() || !isAdded()) return;
        Intent intent = ProfileActivity.getCallingIntent(getActivity());
        intent.putExtra("author_name", username);
        startActivity(intent);
    }

    @Override
    public void trackComment(String message, boolean isSuggestedComment) {
        if (CommonUtils.isFirstCommentSendNotTracked()) {
            HashMap<String, Object> property = new HashMap<>(baseProperties);
            property.put("orientation", getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait" : "landscape");
            property.put("message", message);
            property.put("game", mViewModel.postObject.get() != null && mViewModel.postObject.get().getGame() != null ? mViewModel.postObject.get().getGame().getName() : "");
            property.put("is_suggested_comment", isSuggestedComment);
            property.put("author", mViewModel.authorUsername());
            property.put("post_id", mViewModel.getPostId());
            CommonUtils.setFirstCommentSentEventTracked();
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_CHAT_SEND_FIRST_CLICKED, property);
        }
    }

    public void checkAndShowCommentReward() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (!isAdded() || isDetached() || isRemoving()) return;
        if (RewardManager.getInstance().isFirstCommentRewardAvailable())
            if (CommonUtils.isUserLoggedin())
                showScratchCardNotification(REWARD_TYPE_FIRST_COMMENT);
            else {
                openLoginFlow(getString(R.string.new_reward_message));
            }
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

    public void addCustomRoomEvent() {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        if (mViewModel != null && mViewModel.currentPost.get() != null) {
            playRequestClicked();
        } else {
            customRoomQueue.add(this::playRequestClicked);
        }
    }

    public void showCustomRoomAcceptedEnable(String message) {
        if (mBinding == null || mViewModel == null || isStateSaved()) return;
        Snackbar.make(mBinding.getRoot(), message, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(ContextCompat.getColor(getContext(), R.color.color_accent))
                .setAnchorView(mBinding.chatStateButton)
                .setAction(getString(R.string.view), v -> playRequestClicked())
                .show();
    }

    @Override
    public void onFlagBtnClick() {
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_REPORT_POST_CLICKED, baseProperties);
        new AlertDialog.Builder(getContext()).setTitle(getString(R.string.report_this_title)).setMessage(getString(R.string.report_content)).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_POST_REPORTED, baseProperties);
                dialogInterface.dismiss();
                reportPost();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_REPORT_POST_DISMISSED, baseProperties);
                dialogInterface.dismiss();
            }
        }).show();
    }

    public void sendRefreshCustomRoomMessage() {
        if (mViewModel != null)
            mViewModel.sendRequestPlayFragmentUpdateMessage();
    }

    public void checkRewards() {
        if (CommonUtils.isUserLoggedin()) {
            if (RewardManager.getInstance().isLoginOrSeventhDayAvailable() && isVisible)
                showScratchCardNotification(AppConstants.REWARD_TYPE_DAILY_LOGIN, AppConstants.REWARD_TYPE_SEVENTH_DAY);
        } else
            openLoginFlow(getString(R.string.new_reward_message));
    }

    private void checkAndShowShareReward() {
        if (isVisible && CommonUtils.isUserLoggedin() && RewardManager.getInstance().isShareRewardAvailable()) {
            showScratchCardNotification(AppConstants.REWARD_TYPE_SHARE);
        }
    }

    private void onShareBottomSheetDismiss() {
        if (shareAnimation != null) {
            shareAnimation.stopAnimation();
            shareAnimation = null;
        }
        checkAndShowShareReward();
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

    @Override
    public void onHeartUp() {
        Log.i(getClass().getSimpleName(), "heart_up");
    }

    @Override
    public void askLogin() {

    }
}
