/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 3/1/19 4:19 PM
 *
 */

package com.rheotv.android.ui.activities.tabcontainer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.rheotv.android.BR;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.NetworkChangeReceiver;
import com.rheotv.android.data.network.models.gamify.Reward;
import com.rheotv.android.data.network.models.general.GameDetails;
import com.rheotv.android.data.network.models.general.RTMPDetails;
import com.rheotv.android.databinding.ActivityContainerBinding;
import com.rheotv.android.helpers.MyFirebaseMessagingService;
import com.rheotv.android.ui.activities.clips.ClipsActivity;
import com.rheotv.android.ui.activities.gamify.RewardsActivity;
import com.rheotv.android.ui.activities.home.view.HomeActivity;
import com.rheotv.android.ui.activities.leaderboard.LeaderBoardActivity;
import com.rheotv.android.ui.activities.search.SearchActivity;
import com.rheotv.android.ui.activities.search.fragment.SearchFragment;
import com.rheotv.android.ui.activities.tabcontainer.clips.ClipsFragment;
import com.rheotv.android.ui.activities.tabcontainer.posts.PostListFragment;
import com.rheotv.android.ui.activities.tabcontainer.profile.container.ProfileContainerFragment;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.ui.fragments.LiveStreamingDialogFragment;
import com.rheotv.android.ui.fragments.LoginFragment;
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog;
import com.rheotv.android.ui.fragments.RateUsFragment;
import com.rheotv.android.ui.fragments.RewardProgressDialogFragment;
import com.rheotv.android.ui.fragments.ScratchCardNavigator;
import com.rheotv.android.ui.fragments.ScratchDialogFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.FBconnectionclass.DeviceBandwidthSampler;
import com.rheotv.android.utils.LinkHandler;
import com.rheotv.android.utils.NetworkUtils;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.ViewAnimationUtils;
import com.rheotv.android.utils.customview.AnimatedScratchCardView;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

import static com.rheotv.android.ui.fragments.LoginFragmentBottomDialog.RC_SIGN_IN;
import static com.rheotv.android.utils.AppConstants.REQUEST_CODE_ADD_MODERATORS;

public class TabContainerActivity extends BaseActivity<ActivityContainerBinding, TabContainerViewModel>
        implements HasAndroidInjector, TabContainerNavigator, ScratchCardNavigator,
        RateUsFragment.RateUsListener, LoginFragmentBottomDialog.LoginFragmentCallback {

    @Inject
    DispatchingAndroidInjector<Object> fragmentDispatchingAndroidInjector;
    @Inject
    TabContainerViewModel mFeedViewModel;

    private Fragment fragment;
    private LoginFragmentBottomDialog loginDialogFragment;
    private RewardProgressDialogFragment rewardProgressDialogFragment;
    private ScratchDialogFragment scratchDialogFragment;
    private ActivityContainerBinding mActivityFeedBinding;
    private SharedPrefsUtils sharedPrefsUtils;

    LiveStreamingDialogFragment liveStreamingDialogFragment;
    NetworkChangeReceiver networkChangeReceiver;

    private boolean isBackPressed = false;
    boolean shouldRefreshProfile = false;
    private int toolbarHeight, currentSelectedItemId;
    private boolean shouldShowLoginDialog;
    boolean isClipsEnabled;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    private Handler ratingHandler = new Handler();
    private Runnable ratingRunner = this::askForRating;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            getContainerBinding().goLiveBtn.setVisibility(View.GONE);
            if (isClipsEnabled) {
                getContainerBinding().clipsBtn.setVisibility(View.VISIBLE);
                getContainerBinding().newClipsIndicator.setVisibility(View.GONE);
            } else {
                getContainerBinding().clipsBtn.setVisibility(View.GONE);
                getContainerBinding().newClipsIndicator.setVisibility(View.GONE);
            }

            getContainerBinding().toolbarHeading.setVisibility(View.GONE);

            switch (item.getItemId()) {
                case R.id.navigation_feeds:
                    SegmentTracker.getInstance(TabContainerActivity.this).trackEvent(SegmentConstants.EVENT_HOME_FEED_TAB_CLICKED, baseProperties);
                    if (item.getItemId() == currentSelectedItemId) {
                        if (fragment != null && fragment instanceof PostListFragment) {
                            ((PostListFragment) fragment).smootScrollRVToTop();
                        } else if (fragment != null && fragment instanceof ClipsFragment) {
                            showSearchAndAddMargin();
                        }
                        return false;
                    } else {
                        showSearchAndAddMargin();
                        currentSelectedItemId = R.id.navigation_feeds;
                        sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.BOTTOM_NAV_TAB_SELECTED, "home_screen");
                        fragment = PostListFragment.newInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
                        loadFragment(fragment, false, false, R.id.frame_container);
                        return true;
                    }
                case R.id.navigation_search:
                    SegmentTracker.getInstance(TabContainerActivity.this).trackEvent(SegmentConstants.EVENT_HOME_SEARCH_TAB_CLICKED, baseProperties);
                    if (item.getItemId() == currentSelectedItemId) {
                        return false;
                    } else {
                        if (fragment instanceof PostListFragment) {
                            if (((PostListFragment) fragment).getPostListadapter() != null)
                                ((PostListFragment) fragment).getPostListadapter().releasePlayer();
                        }
                        hideToolbar();
                        currentSelectedItemId = R.id.navigation_search;
                        sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.BOTTOM_NAV_TAB_SELECTED, "search_screen");
                        fragment = SearchFragment.newInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
                        loadFragment(fragment, false, false, R.id.frame_container);
                        return true;
                    }

                case R.id.navigation_profile:
                    SegmentTracker.getInstance(TabContainerActivity.this).trackEvent(SegmentConstants.EVENT_HOME_PROFILE_TAB_CLICKED, baseProperties);
                    if (item.getItemId() == currentSelectedItemId) {
                        return false;
                    } else {
                        if (fragment instanceof PostListFragment) {
                            if (((PostListFragment) fragment).getPostListadapter() != null)
                                ((PostListFragment) fragment).getPostListadapter().releasePlayer();
                        }
                        hideToolbar();
                        currentSelectedItemId = R.id.navigation_profile;
                        sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.BOTTOM_NAV_TAB_SELECTED, "profile_screen");
                        if (CommonUtils.isUserLoggedin()) {
                            fragment = com.rheotv.android.ui.activities.profile.view.ProfileContainerFragment.Companion.newInstance(CommonUtils.getUserName(), SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE,false);
                            loadFragment(fragment, false, false, R.id.frame_container);
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
                            fragment = LoginFragment.newInstance(bundle);
                            loadFragment(fragment, false, false, R.id.frame_container);
                        }
                        return true;
                    }

                case R.id.navigation_community:
                    SegmentTracker.getInstance(TabContainerActivity.this).trackEvent(SegmentConstants.EVENT_HOME_CLIPS_TAB_CLICKED, baseProperties);
                    if (item.getItemId() == currentSelectedItemId)
                        return false;
                    if (fragment instanceof PostListFragment) {
                        if (((PostListFragment) fragment).getPostListadapter() != null)
                            ((PostListFragment) fragment).getPostListadapter().releasePlayer();
                    }
                    hideToolbar();
                    currentSelectedItemId = R.id.navigation_community;
                    if (getSupportFragmentManager().findFragmentByTag(AppConstants.REWARD_STREAK_FRAGMENT_TAG) != null)
                        rewardProgressDialogFragment = (RewardProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(AppConstants.REWARD_STREAK_FRAGMENT_TAG);
                    fragment = ClipsFragment.newInstance(null, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
                    sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.BOTTOM_NAV_TAB_SELECTED, "clips_screen");
                    loadFragment(fragment, false, false, R.id.frame_container);
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onLeaderBoardClick() {
        Map<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("is_first", CommonUtils.isFirstTimeLeaderBoardClicked());
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_LEADERBOARD_PAGE_CLICKED, properties);
        CommonUtils.setFirstTimeLeaderBoardClicked();
        Intent intent = new Intent(this, LeaderBoardActivity.class);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
        startActivity(intent);
    }

    public void hideToolbar() {
        if (mActivityFeedBinding.toolbar.getVisibility() == View.VISIBLE)
            toolbarHeight = mActivityFeedBinding.toolbar.getHeight();
        ViewAnimationUtils.collapse(mActivityFeedBinding.toolbar);
    }

    public void showSearchAndAddMargin() {
        if (mActivityFeedBinding.toolbar.getVisibility() == View.GONE) {
            ViewAnimationUtils.expand(mActivityFeedBinding.toolbar, toolbarHeight);
            ((ViewGroup.MarginLayoutParams) mActivityFeedBinding.toolbar.getLayoutParams()).setMargins(0, 0, 0, 0);
        }
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    public void openPlayStoreLink() {
        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=com.rheotv.android"));
        startActivity(viewIntent);
        finish();
    }

    @Override
    public void openWhatsappForVideoUpload() {

    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_container;
    }

    @Override
    public TabContainerViewModel getViewModel() {
        return mFeedViewModel;
    }

    public ActivityContainerBinding getContainerBinding() {
        return mActivityFeedBinding;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home)
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFeedViewModel.setNavigator(this);
        mActivityFeedBinding = getViewDataBinding();
        setUp();
        sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.BOTTOM_NAV_TAB_SELECTED, "home_screen");
        currentSelectedItemId = R.id.navigation_feeds;
        initNetworkReceiver();
        DeviceBandwidthSampler.getInstance().stopSampling();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("go-live-event"));
        checkUpdateAppMsgState();
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
        if (getIntent() != null) {
            baseProperties.put(AppConstants.SCREEN_SOURCE, getIntent().getStringExtra(AppConstants.SCREEN_SOURCE));

        }
        if (CommonUtils.isFirstEventHomeViewNotTracked()) {
            CommonUtils.setFirstEventHomeViewEventTracked();
            SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_FIRST_HOMEVIEW, baseProperties);
        }
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_HOMEVIEW, baseProperties);

        if (NetworkUtils.isNetworkConnected(this)) {
            if (CommonUtils.isUserLoggedin()) {
                mFeedViewModel.fetchProfile(CommonUtils.getUserName());
            }
            mFeedViewModel.getAnalyticsEventsList();
        } else {
            mActivityFeedBinding.navigation.findViewById(R.id.navigation_community).performClick();
        }
        //fragment.show(getSupportFragmentManager(), AppConstants.UPLOAD_CONTACTS_DIALOG_FRAGMENT);
    }

    private void checkUpdateAppMsgState() {
        boolean showUpdateMsg = getIntent().getBooleanExtra("show_update_msg", false);
        mActivityFeedBinding.updateStrip.setVisibility(showUpdateMsg ? View.VISIBLE : View.GONE);
        if (mActivityFeedBinding.updateStrip.getVisibility() == View.VISIBLE)
            SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_UPDATE_AVAILABLE_SHOWN, baseProperties);
        if (getIntent().hasExtra("version_check") && getIntent().getBooleanExtra("check_version", false))
            mFeedViewModel.checkVersionSupport();
    }

    private void setUp() {
        sharedPrefsUtils = new SharedPrefsUtils();
        BottomNavigationView navigation = mActivityFeedBinding.navigation;
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        fragment = PostListFragment.newInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
        loadFragment(fragment, false, false, R.id.frame_container);
        mActivityFeedBinding.goLiveButton.setOnClickListener(view -> goLiveClicked(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE));
    }

    public void moveToPostLIstFragment() {
        showSearchAndAddMargin();
        currentSelectedItemId = R.id.navigation_feeds;
        sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.BOTTOM_NAV_TAB_SELECTED, "home_screen");
        fragment = PostListFragment.newInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
        loadFragment(fragment, false, false, R.id.frame_container);
        mActivityFeedBinding.navigation.getMenu().findItem(currentSelectedItemId).setChecked(true);
    }

    @Override
    protected void onStart() {
        sharedPrefsUtils.setBooleanPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.CONTAINER_IS_SECOND_LAUNCH, true);
        RewardManager.getInstance().updateNonLoggedInScratchCardShown(this);
        super.onStart();
    }

    public void loadFragment(@NonNull Fragment fragment, boolean addToBackStack, boolean animate, int container) {
        this.fragment = fragment;
//        if (fragment instanceof TrendingListFragment) {
//            mActivityFeedBinding.trendingHeading.setText("Trending");
//            mActivityFeedBinding.trendingHeading.setVisibility(View.VISIBLE);
//            mFeedViewModel.districtFragmentCalled.set(false);
//        } else {
//            mActivityFeedBinding.trendingHeading.setVisibility(View.GONE);
//            mFeedViewModel.districtFragmentCalled.set(false);
//        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!fragment.isAdded()) {
            try {
                if (addToBackStack) {
                    if (animate) {
                        transaction.setCustomAnimations(R.anim.slide_in_down, R.anim.slide_in_up);
                    }
//                    getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    transaction.replace(container, fragment).addToBackStack(fragment.getTag());
                } else {
                    if (animate) {
                        transaction.setCustomAnimations(R.anim.slide_in_down, R.anim.slide_in_up);
                    }
                    transaction.replace(container, fragment);
                }
                transaction.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            transaction.show(fragment);
        }
    }

    private boolean ratingActionPerformed = false;

    boolean isLiveStreamDialogPlatformSelected = false;

    public void setLiveStreamDialogPlatformSelected(boolean liveStreamDialogPlatformSelected) {
        isLiveStreamDialogPlatformSelected = liveStreamDialogPlatformSelected;
    }

    @Override
    public void onBackPressed() {


        if (currentSelectedItemId != R.id.navigation_feeds) {
            moveToPostLIstFragment();
            return;
        }
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() != 0) {
//            fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mActivityFeedBinding.navigation.setSelectedItemId(R.id.navigation_feeds);
        } else {
            if (ratingActionPerformed) {
                finish();
                return;
            }

            if (isBackPressed) {
                finish();
            } else {
                isBackPressed = true;
                Toast.makeText(this, "Press back again to close the app. ", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        LinkHandler.triggerLink(this);
    }

    private void initNetworkReceiver() {
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
    }

    public void goLiveClicked(String source) {
        try {
            SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_GO_LIVE_CLICKED, baseProperties);
            if (!CommonUtils.isUserLoggedin()) {
                loginDialogFragment = LoginFragmentBottomDialog.getInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
                if (loginDialogFragment.isAdded()) {
                    return;
                }
                loginDialogFragment.setmCallback(this);
                loginDialogFragment.show(this.getSupportFragmentManager(), AppConstants.LOGIN_FRAGMENT_TAG);
                return;
            }
            Bundle args = new Bundle();
            args.putString(AppConstants.SCREEN_SOURCE, source);
            liveStreamingDialogFragment = LiveStreamingDialogFragment.getInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
            liveStreamingDialogFragment.setArguments(args);
            liveStreamingDialogFragment.show(getSupportFragmentManager(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateGameList(List<GameDetails> response) {

    }

    @Override
    public void updateRTMPDetails(RTMPDetails rtmpDetails) {

    }

    public void handleLogout() {
        if (currentSelectedItemId == R.id.navigation_profile) {
            Bundle bundle = new Bundle();
            bundle.putString(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
            fragment = LoginFragment.newInstance(bundle);
            loadFragment(fragment, false, false, R.id.frame_container);
        }

        mFeedViewModel.updateRewardViews();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void goSearchClick(String toolbar) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
        startActivity(intent);
    }

    @Override
    public void viewClipsScreen() {
        Intent intent = new Intent(this, ClipsActivity.class);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
        startActivity(intent);
    }

    public void launchLogInFragment() {
        try {
            if (getSupportFragmentManager().findFragmentByTag(AppConstants.LOGIN_FRAGMENT_TAG) != null) {
                loginDialogFragment = (LoginFragmentBottomDialog) getSupportFragmentManager().findFragmentByTag(AppConstants.LOGIN_FRAGMENT_TAG);
            } else {
                loginDialogFragment = LoginFragmentBottomDialog.getInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
            }
            if (loginDialogFragment != null && (loginDialogFragment.isAdded() || loginDialogFragment.isVisible())) {
                return;
            }
            loginDialogFragment.setmCallback(this);

            loginDialogFragment.showNoAddToBackStack(this.getSupportFragmentManager(), AppConstants.LOGIN_FRAGMENT_TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (networkChangeReceiver != null)
            unregisterReceiver(networkChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    // test things
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showLiveNowDialog();
        }
    };

    private void showLiveNowDialog() {
        if (mActivityFeedBinding != null) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    //set title
                    .setTitle("Congratulations!")
                    //set message
                    .setMessage("You are now Live!")
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.REQUEST_CODE_EDIT_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                shouldRefreshProfile = data.getBooleanExtra("refresh_profile", false);
            }
        } else if (requestCode == RC_SIGN_IN) {
            if (loginDialogFragment != null) {
                loginDialogFragment.onActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == REQUEST_CODE_ADD_MODERATORS) {
            if (fragment instanceof ProfileContainerFragment && data != null) {
                ((ProfileContainerFragment) fragment).updateModerators(data.getStringExtra("moderators"));
            }
        }
    }

    public void loadProfileFragment() {
        showSearchAndAddMargin();
        currentSelectedItemId = R.id.navigation_profile;
        sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.BOTTOM_NAV_TAB_SELECTED, "profile_screen");
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_HOME_PROFILE_TAB_CLICKED, baseProperties);
        if (CommonUtils.isUserLoggedin())
            fragment = com.rheotv.android.ui.activities.profile.view.ProfileContainerFragment.Companion.newInstance(CommonUtils.getUserName(), SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE,false);

        else {
            Bundle bundle = new Bundle();
            bundle.putString(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
            fragment = LoginFragment.newInstance(bundle);
        }
        loadFragment(fragment, false, false, R.id.frame_container);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldRefreshProfile) {
            loadProfileFragment();
            shouldRefreshProfile = false;
        }

        loadDailyRewards();
        ratingHandler.postDelayed(ratingRunner, 3000);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void launchLogInFragment(String rewardMessage) {
        try {
            if (getSupportFragmentManager().findFragmentByTag(AppConstants.LOGIN_FRAGMENT_TAG) != null) {
                loginDialogFragment = (LoginFragmentBottomDialog) getSupportFragmentManager().findFragmentByTag(AppConstants.LOGIN_FRAGMENT_TAG);
            } else {
                loginDialogFragment = LoginFragmentBottomDialog.getInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
            }

            if (loginDialogFragment != null && (loginDialogFragment.isAdded() || loginDialogFragment.isVisible())) {
                return;
            }

            if (rewardMessage != null && loginDialogFragment != null) {
                loginDialogFragment.setRewardText(rewardMessage);
            }
            loginDialogFragment.setmCallback(this);

            loginDialogFragment.showNoAddToBackStack(this.getSupportFragmentManager(), AppConstants.LOGIN_FRAGMENT_TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadDailyRewards() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> mFeedViewModel.loadDailyRewards(), 1000);
    }

    @Override
    public void checkRewardAvailable() {
        if (CommonUtils.isUserLoggedin()) {
            if (RewardManager.getInstance().isLoginOrSeventhDayAvailable())
                showScratchCardNotification(AppConstants.REWARD_TYPE_DAILY_LOGIN, AppConstants.REWARD_TYPE_SEVENTH_DAY);
        } else {
            if (!RewardManager.getInstance().isNonLoggedInScratchCardShown(this))
                showScratchCardNotification();
            else if (shouldShowLoginDialog)
                launchLogInFragment(getString(R.string.new_reward_message));
        }
    }

    private void showScratchCardNotification(final String... rewardTypes) {
        try {
            Reward reward = null;
            if (rewardTypes != null && rewardTypes.length > 0) {
                reward = RewardManager.getInstance().getAvailableReward(rewardTypes);
            }
            AnimatedScratchCardView bottomScratchCardView = mActivityFeedBinding.container.findViewWithTag(AnimatedScratchCardView.getTAG());
            mActivityFeedBinding.container.removeView(bottomScratchCardView);
            if (bottomScratchCardView == null) {
                bottomScratchCardView = new AnimatedScratchCardView(this);
            }
            int scratchCardImage = bottomScratchCardView.setRandomScratchCard();
            bottomScratchCardView.setAction(new View.OnClickListener() {

                private Reward currentReward;

                public View.OnClickListener setCurrentReward(Reward currentReward) {
                    this.currentReward = currentReward;
                    return this;
                }

                @Override
                public void onClick(View v) {
                    if (CommonUtils.isUserLoggedin()) {
                        if (currentReward != null) {
                            scratchDialogFragment = ScratchDialogFragment.getInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE, currentReward, scratchCardImage, AppConstants.REWARD_TYPE_DAILY_LOGIN, AppConstants.REWARD_TYPE_SEVENTH_DAY);
                            scratchDialogFragment.show(TabContainerActivity.this.getSupportFragmentManager(), AppConstants.SCRATCH_FRAGMENT_TAG, TabContainerActivity.this, rewardTypes);
                        }
                    } else {
                        launchLogInFragment(getString(R.string.new_reward_message));
                    }
                }
            }.setCurrentReward(reward));
            bottomScratchCardView.addTo(mActivityFeedBinding.container, AppConstants.PORTRAIT_SCRATCH_CARD_BOTTOM_MARGIN,
                    AppConstants.SCRATCH_CARD_END_MARGIN, AnimatedScratchCardView.Companion.getPathInAnimator(),
                    AnimatedScratchCardView.Companion.getSlideOutAnimation(), new AnimatedScratchCardView.ScratchCardVisibilityListener() {
                        private Reward currentReward;

                        public AnimatedScratchCardView.ScratchCardVisibilityListener setCurrentReward(Reward currentReward) {
                            this.currentReward = currentReward;
                            return this;
                        }

                        @Override
                        public void performAction() {
                            if (!CommonUtils.isUserLoggedin())
                                RewardManager.getInstance().updateNonLoggedInScratchCard(TabContainerActivity.this);
                            if (currentReward != null)
                                mFeedViewModel.updateScratchCardStatusShown(currentReward.getId());
                        }
                    }.setCurrentReward(reward));
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public void onScratchRevealed(String rewardId) {
        // post api call here with rewardType
        mFeedViewModel.updateScratchCard(rewardId, CommonUtils.getUserName(this));
    }

    @Override
    public void viewTotalCoins() {
        if (CommonUtils.isUserLoggedin()) {
            Intent intent = new Intent(this, RewardsActivity.class);
            intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
            startActivity(intent);
        } else
            launchLogInFragment(getString(R.string.login_to_get_reward_message));

        //OverlayPermissionBottomSheet.Companion.show(getSupportFragmentManager(), OverlayPermissionBottomSheet.Companion.newInstance());

//        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(NotificationWorker.class).build();
//        WorkManager.getInstance(this).enqueue(request);
//        showNotification();
    }

    private void showNotification() {
        MyFirebaseMessagingService messagingService = new MyFirebaseMessagingService();
        Map<String, String> map = new HashMap<>();
        map.put("title", "Dummy");
        map.put("image_url", "https://picsum.photos/seed/picsum/200/300");
//        map.put("is_live", "true");
//        map.put("created_at", (System.currentTimeMillis()) + "");
        messagingService.buildNotification(map, this);
    }

    @Override
    public void onRewardStreakClick() {
        if (CommonUtils.isUserLoggedin()) {
            if (getSupportFragmentManager().findFragmentByTag(AppConstants.REWARD_STREAK_FRAGMENT_TAG) != null)
                rewardProgressDialogFragment = (RewardProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(AppConstants.REWARD_STREAK_FRAGMENT_TAG);
            else
                rewardProgressDialogFragment = RewardProgressDialogFragment.getInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
            if (rewardProgressDialogFragment != null && (rewardProgressDialogFragment.isAdded() || rewardProgressDialogFragment.isVisible()))
                return;
            rewardProgressDialogFragment.show(this.getSupportFragmentManager(), AppConstants.REWARD_STREAK_FRAGMENT_TAG);
        } else {
            launchLogInFragment(getString(R.string.login_to_get_reward_message));
        }
    }

    /**
     * Rate Us callbacks
     */
    @Override
    public void onSubmitClick(int rating, String feedback) {
        ratingActionPerformed = true;
        CommonUtils.markAppRated(this);
        mFeedViewModel.rateApp(rating, feedback);
    }

    @Override
    public void onPlayStoreClick(int rating, String feedback) {
        ratingActionPerformed = true;
        CommonUtils.markAppRated(this);
        String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException ex) {
                ex.printStackTrace();
                Toast.makeText(this, "No Application found to handle this action", Toast.LENGTH_LONG).show();
            }
        }

        mFeedViewModel.rateApp(rating, feedback);
    }

    @Override
    public void onRatingCancelClick() {
        ratingActionPerformed = true;
        mFeedViewModel.rateApp(0, null);
    }

    private void askForRating() {
        if (!isAnyDialogShowing()) {
            if (CommonUtils.shouldRateNow(this)) {
                RateUsFragment fragment = RateUsFragment.newInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
                fragment.show(getSupportFragmentManager(), AppConstants.RATING_FRAGMENT_TAG);
                ratingHandler.removeCallbacks(ratingRunner);
            }
        } else {
            ratingHandler.postDelayed(ratingRunner, 5000);
        }
    }

    private boolean isAnyDialogShowing() {
        return (scratchDialogFragment != null && scratchDialogFragment.isVisible()) ||
                (loginDialogFragment != null && loginDialogFragment.isVisible()) ||
                (rewardProgressDialogFragment != null && rewardProgressDialogFragment.isVisible());
    }

    @Override
    public void onLoginSuccess() {
        if (currentSelectedItemId == R.id.navigation_profile) {
            CommonUtils.setHideSyncContacts(true);
            loadProfileFragment();
        } else if (currentSelectedItemId == R.id.navigation_feeds)
            loadPostFragment();
        mFeedViewModel.loadDailyRewards();
    }

    @Override
    public void onLoginDialogClose() {

    }

    private void loadPostFragment() {
        fragment = PostListFragment.newInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
        loadFragment(fragment, false, false, R.id.frame_container);
    }

    @Override
    public void showForceUpdateDialog() {
        new android.app.AlertDialog.Builder(this).setTitle(getString(R.string.force_update_app)).setMessage(getString(R.string.force_update_msg))
                .setPositiveButton(getString(R.string.update_text), (dialogInterface, i) -> openPlayStoreLink()).setCancelable(false).show();
    }

    @Override
    public void showUpdateOptions() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> mActivityFeedBinding.updateStrip.setVisibility(View.VISIBLE), 300);
    }
}