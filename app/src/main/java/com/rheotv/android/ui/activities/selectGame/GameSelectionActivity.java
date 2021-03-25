package com.rheotv.android.ui.activities.selectGame;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rheotv.android.BR;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.databinding.ActivityGameSelectionBinding;
import com.rheotv.android.ui.activities.alertInformation.AlertInformationActivity;
import com.rheotv.android.ui.activities.audioroom.view.AudioChatRoomActivity;
import com.rheotv.android.ui.activities.clips.ClipsActivity;
import com.rheotv.android.ui.activities.gamify.RewardsActivity;
import com.rheotv.android.ui.activities.home.view.HomeActivity;
import com.rheotv.android.ui.activities.onboarding.v2.view.fragment.OnBoardingContainerFragment;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.story.StoryActivity;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog;
import com.rheotv.android.ui.fragments.TopStreamerSelectionFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.LinkHandler;
import com.rheotv.android.utils.ListHolder;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

public class GameSelectionActivity extends BaseActivity<ActivityGameSelectionBinding, GameActivityViewModel>
        implements HasAndroidInjector, GameSelectionFragment.GameSelectionListener,
        LanguageSelectionFragment.LanguageSelectionListener, LoginFragmentBottomDialog.LoginFragmentCallback {

    @Inject
    DispatchingAndroidInjector<Object> fragmentDispatchingAndroidInjector;

    @Inject
    GameActivityViewModel mViewModel;

    private String intentOpenUrl;
    boolean showUpdateMessage;
    private String languageId;
    boolean showOptionMenu = true;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    private int shareParamOffset;

    boolean isFirebaseDeeplink;

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_game_selection;
    }

    @Override
    public GameActivityViewModel getViewModel() {
        return mViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityGameSelectionBinding mBinding = getViewDataBinding();
        setSupportActionBar(mBinding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        mBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        intentOpenUrl = getIntent().getStringExtra("intent_open_url");
        showUpdateMessage = getIntent().getBooleanExtra("show_update_message", false);
        shareParamOffset = getIntent().getIntExtra("deeplink_offset", 1);
        languageId = getIntent().getStringExtra(AppConstants.BRANCH_SELECTED_LANGUAGE);
        boolean isRelogin = getIntent().getBooleanExtra("is_relogin", false);
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_ONBOARD_GAME_SELECTION);
        String source = SegmentConstants.SCREEN_NAME_ONBOARD_GAME_SELECTION;
        if (getIntent() != null && getIntent().hasExtra(AppConstants.SCREEN_SOURCE))
            source = getIntent().getStringExtra(AppConstants.SCREEN_SOURCE);

        baseProperties.put(AppConstants.SCREEN_SOURCE, source);
        if (!CommonUtils.isSelectedUser()) {
            mBinding.appbar.setVisibility(View.VISIBLE);
            SegmentTracker.getInstance(this).recordScreenName(SegmentConstants.SCREEN_NAME_ONBOARD_GAME_SELECTION, baseProperties);

            getSupportFragmentManager().beginTransaction().add(R.id.game_container, GameSelectionFragment.newInstance(source)).commit();

            mViewModel.competition.observe(this, result -> {
                if (result != null) {
                    showCompetitionPage(result);
                }
            });

            mViewModel.updatingLanguage.observe(this, status -> {
                if (status == Status.SUCCESS) {
                    closeSelection();
                } else if (status == Status.ERROR) {
                    loadLanguageFragment();
                }
            });
        } else {
            showOptionMenu = false;
            invalidateOptionsMenu();
            mBinding.appbar.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().add(R.id.game_container, OnBoardingContainerFragment.Companion.newInstance(source, isRelogin)).commit();
        }
        SegmentTracker.getInstance(this).recordScreenName(SegmentConstants.SCREEN_NAME_ONBOARD_GAME_SELECTION, baseProperties);
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (showOptionMenu)
            getMenuInflater().inflate(R.menu.menu_skip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_next) {
            SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_ONBOARD_GAME_SKIPPED, baseProperties);
            moveToLanguageSelection();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void closeSelection() {
        if (intentOpenUrl != null) {
            if (intentOpenUrl.contains("/user/")) {
                try {
                    intentOpenUrl = CommonUtils.getUrlWithoutParameters(intentOpenUrl);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                String[] params = intentOpenUrl.split("\\/");
                String username = params[params.length - 1];
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_ONBOARD_GAME_SELECTION);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("author_name", username);
                intent.putExtra("is_deeplink", true);

                startActivity(intent);
                finish();
            } else if (intentOpenUrl.contains("/competition/")) {
                String[] params = intentOpenUrl.split("\\/");
                String competitionId = params[params.length - 1];
                mViewModel.getCompetitionData(competitionId);
            } else if (intentOpenUrl.contains("content/clips/")) {
                Intent intent = new Intent(this, ClipsActivity.class);
                String[] params = intentOpenUrl.split("\\/");
                String clipId = params[params.length - 1];
                intent.putExtra("clip_id", clipId);
                intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_ONBOARD_GAME_SELECTION);
                startActivity(intent);
                finish();
            } else if (intentOpenUrl.contains("content/stories/")) {
                Intent intent = new Intent(this, StoryActivity.class);
                String[] params = intentOpenUrl.split("\\/");
                String storyId = params[params.length - 1];
                storyId = storyId.substring(storyId.indexOf("=") + 1);
                intent.putExtra(StoryActivity.ARG_STORY_ID, storyId);
                intent.putExtra(StoryActivity.ARG_IS_FROM_DEEPLINK, true);
                intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_SPLASH);
                startActivity(intent);
                finish();
            } else if (intentOpenUrl.contains("content/story/author")) {
                Intent intent = new Intent(this, StoryActivity.class);
                String storyId = Uri.parse(intentOpenUrl).getLastPathSegment();
                intent.putExtra(StoryActivity.ARG_AUTHOR_ID, storyId);
                intent.putExtra(StoryActivity.ARG_IS_FROM_DEEPLINK, true);
                intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_SPLASH);
            } else if (intentOpenUrl.contains("/redeem/")) {
                Intent intent = new Intent(this, RewardsActivity.class);
                intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_SPLASH);
                intent.putExtra("from", "share");
                startActivity(intent);
                finish();
            } else if (LinkHandler.getMojoTargetPath(intentOpenUrl).contains("/post/")) {
                moveToHomePage(showUpdateMessage);
//                LinkHandler.handleDeepLink(this, intentOpenUrl, SegmentConstants.SCREEN_NAME_SPLASH);
            } else if (intentOpenUrl.contains("/audio_chat_room/")) {
                Uri url = Uri.parse(intentOpenUrl);
                String groupId = "", chatRoomId = "";
                try {
                    for (int index = url.getPathSegments().size() - 1; index > 0; --index) {
                        if (!url.getPathSegments().get(index).isEmpty()) {
                            if (chatRoomId == null || chatRoomId.isEmpty()) {
                                chatRoomId = url.getPathSegments().get(index);
                            } else if (groupId == null || groupId.isEmpty()) {
                                groupId = url.getPathSegments().get(index);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                AudioChatRoomActivity.Companion.startMe(this, groupId, 0, chatRoomId, SegmentConstants.SCREEN_NAME_ONBOARD_GAME_SELECTION, true);
                finish();

            } else {
                moveToHomePage(showUpdateMessage);
            }
        } else {
            moveToHomePage(showUpdateMessage);
        }
    }

    public void moveToHomePage(boolean showUpdateMsg) {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.putExtra("open_url", intentOpenUrl);
        intent.putExtra("show_update_msg", showUpdateMsg);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_ONBOARD_GAME_SELECTION);
        if (intentOpenUrl != null && !intentOpenUrl.isEmpty()) {
            LinkHandler.setIntentOpenUrl(intentOpenUrl);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        startActivity(intent);
        finish();
    }

    public void showCompetitionPage(Result result) {
        Intent intent = new Intent(this, AlertInformationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("is_deep_link", true);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_ONBOARD_GAME_SELECTION);
        ListHolder.getInstance().setAlertInfoObject(result);
        startActivity(intent);
        finish();
    }

    private void moveToLanguageSelection() {
        if (languageId != null && !languageId.equals("")) {
            mViewModel.updateLanguage(languageId);
        } else {
            loadLanguageFragment();
        }
    }

    private void loadLanguageFragment() {
        try {
            showOptionMenu = false;
            invalidateOptionsMenu();
            getSupportFragmentManager().beginTransaction().replace(R.id.game_container, LanguageSelectionFragment.getInstance(SegmentConstants.SCREEN_NAME_ONBOARD_GAME_SELECTION)).addToBackStack("GameSelectionFragment").commit();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGameUpdated() {
        try {
            moveToLanguageSelection();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLanguageUpdated() {
        closeSelection();
    }

    @Override
    public void onBackPressed() {
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_ONBOARD_BACK_PRESS, baseProperties);
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            showOptionMenu = true;
            invalidateOptionsMenu();
            getSupportFragmentManager().popBackStack();
        } else {
            if (CommonUtils.isSelectedUser() && !CommonUtils.isUserLoggedin()) {
                super.onBackPressed();
                return;
            }
            closeSelection();
        }
    }

    @Override
    public void onLoginSuccess() {
//        new SharedPrefsUtils().setBooleanPreference(this, SharedPrefsUtils.IS_ONBOARDING_DONE, true);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.game_container, TopStreamerSelectionFragment.newInstance())
                .commit();
    }

    @Override
    public void onLoginDialogClose() {

    }
}
