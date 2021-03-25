/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 1/1/19 2:41 PM
 *
 */

package com.rheotv.android.ui.activities.splash;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.facebook.applinks.AppLinkData;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.rheotv.android.BR;
import com.rheotv.android.BuildConfig;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.databinding.ActivitySplashBinding;
import com.rheotv.android.helpers.AlarmService;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.ui.activities.alertInformation.AlertInformationActivity;
import com.rheotv.android.ui.activities.audioroom.view.AudioChatRoomActivity;
import com.rheotv.android.ui.activities.clips.ClipsActivity;
import com.rheotv.android.ui.activities.gamify.RewardsActivity;
import com.rheotv.android.ui.activities.home.view.HomeActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.selectGame.GameSelectionActivity;
import com.rheotv.android.ui.activities.story.StoryActivity;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppLogger;
import com.rheotv.android.utils.AppUtilsKt;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.LinkHandler;
import com.rheotv.android.utils.ListHolder;
import com.rheotv.android.utils.NetworkUtils;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.json.JSONObject;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.BranchEvent;

import static com.rheotv.android.utils.AppConstants.BRANCH_SELECTED_LANGUAGE;
import static com.rheotv.android.utils.CommonUtils.getHashMapFromQuery;
import static com.rheotv.android.utils.segmentTracker.SegmentConstants.EVENT_BRANCH_REFERRAL;

public class SplashActivity extends BaseActivity<ActivitySplashBinding, SplashViewModel> implements SplashNavigator, InstallReferrerStateListener {

    @Inject
    SplashViewModel mSplashViewModel;
    public static final String TAG = "Splash";
    ActivitySplashBinding activitySplashBinding;
    FirebaseAnalytics analytics;

    SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
    private Map<String, Object> baseProperties = new HashMap<>();
    AtomicReference<String> intentOpenUrl = new AtomicReference<>();
    public static final String INSTALL_REFERRER = "INSTALL REFERRER";
    public static final String INSTALL_REFERRER_FB = "INSTALL REFERRER FB";
    boolean isVersionSupported = true;
    private int attempts = 0;

    Branch branch;

    boolean isRequestComplete;

    private InstallReferrerClient mReferrerClient;

    boolean isGifAnimationComplete;
    private String competitionId;

    public static Intent newIntent(Context context) {
        return new Intent(context, SplashActivity.class);
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    public SplashViewModel getViewModel() {
        return mSplashViewModel;
    }

    @Override
    public void openPlayStoreLink() {
        activitySplashBinding.splashPbar.setVisibility(View.GONE);
        activitySplashBinding.updateButton.setVisibility(View.VISIBLE);
        activitySplashBinding.updateText.setVisibility(View.VISIBLE);
        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=com.rheotv.android"));
        startActivity(viewIntent);
        finish();
    }

    public void scaleView(View v, float startScale, float endScale) {
        Animation anim = new ScaleAnimation(
                1f, 1f, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(1500);
        v.startAnimation(anim);
    }

    private void rotateView(View v) {
        AnimationSet animSet = new AnimationSet(true);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.setFillEnabled(true);

        final RotateAnimation animRotate = new RotateAnimation(0.0f, 360.0f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);

        animRotate.setDuration(1500);
        animSet.addAnimation(animRotate);
        v.startAnimation(animSet);
    }


    public void checkAndShowOnboarding(boolean showUpdateMessage) {
        openOnBoardingScreen(showUpdateMessage, false);
        if (!CommonUtils.isSelectedUser())
            sharedPrefsUtils.setBooleanPreference(SplashActivity.this, SharedPrefsUtils.IS_ONBOARDING_DONE, true);
    }

    private boolean shouldTrackInstall = true;

    Handler handler = new Handler();

    private String selectedLanguageId;

    boolean fireBranchReferralEvent;

    private int shareParamOffset = 1;


    private void handleIntent() {
        Intent appInntent = new Intent(getIntent());
        AppUtilsKt.INSTANCE.runOnIO(() -> {
            checkMoengageDeepLink(appInntent);
            handleFirebaseLink(appInntent);
            handleBranchLink(appInntent);
            return null;
        });
    }

    private void handleBranchLink(Intent branchIntent) {
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(@Nullable JSONObject referringParams, @Nullable BranchError error) {
                if (referringParams != null && referringParams.optBoolean("+clicked_branch_link")) {
                    if (error == null) {
                        // params are the deep linked params associated with the link that the user clicked -> was re-directed to this app
                        // params will be empty if no data found
                        // ... insert custom logic here ...
                        selectedLanguageId = referringParams.optString("selected_language");
                        String extraInfo = referringParams.optString("extra_info");
                        AppUtilsKt.INSTANCE.runOnMain(() -> {
                            if (mSplashViewModel != null)
                                mSplashViewModel.extraInfo = extraInfo;
                            return null;
                        });


                        if (selectedLanguageId != null && !selectedLanguageId.equals(""))
                            baseProperties.put("selected_language", selectedLanguageId);
                        if (extraInfo != null && !extraInfo.equals("")) {
                            baseProperties.put("extra_info", extraInfo);
                            CommonUtils.setBranchExtraInfo(SplashActivity.this, extraInfo);
                        }
                        fireBranchReferralEvent = true;
                            /*if (!CommonUtils.isInstalledFromBranchTracked() && shouldTrackInstall) {
                                shouldTrackInstall = false;
                                SegmentTracker.getInstance(SplashActivity.this).trackEvent(EVENT_APP_INSTALL, properties);
                                CommonUtils.setInstalledFromBranchTracked();
                            }*/

                        Log.i(getClass().getSimpleName(), "selected_language " + selectedLanguageId + " extraInfo " + extraInfo);
                        if (referringParams.optString(AppConstants.BRANCH_SHARE_TYPE).equalsIgnoreCase(AppConstants.BRANCH_SHARE_TYPE_LIVE_STREAM)) {
                            intentOpenUrl.set(referringParams.optString(AppConstants.BRANCH_POST_SOURCE_URL));
                        } else if (referringParams.optString(AppConstants.BRANCH_SHARE_TYPE).equalsIgnoreCase(AppConstants.BRANCH_SHARE_TYPE_PROFILE)) {
                            intentOpenUrl.set(referringParams.optString(AppConstants.BRANCH_PROFILE_URL_SHARE));
                        } else if (referringParams.optString(AppConstants.BRANCH_SHARE_TYPE).equalsIgnoreCase(AppConstants.BRANCH_SHARE_TYPE_CLIP)) {
                            intentOpenUrl.set(referringParams.optString(AppConstants.BRANCH_CLIP_URL_SHARE));
                        } else if (referringParams.optString(AppConstants.BRANCH_SHARE_TYPE).equalsIgnoreCase(AppConstants.BRANCH_SHARE_TYPE_STORY)) {
                            intentOpenUrl.set(referringParams.optString("story_share_url"));
                        } else if (referringParams.optString(AppConstants.BRANCH_SHARE_TYPE).equalsIgnoreCase(AppConstants.BRANCH_SHARE_TYPE_REDEEM)) {
                            intentOpenUrl.set(referringParams.optString(AppConstants.BRANCH_REDEEM_URL_SHARE));
                        }
                        if (intentOpenUrl.get() != null && !intentOpenUrl.get().isEmpty()) {
                            shareParamOffset = 1;
                            baseProperties.put("deeplink_url", intentOpenUrl.get());
                            baseProperties.put("source", "deeplink");
                        }
                        Log.i("BRANCH SDK", referringParams.toString());
                    } else {
                        Log.i("BRANCH SDK", error.getMessage());
                    }
                    AppUtilsKt.INSTANCE.runOnMain(() -> {
                        if (mSplashViewModel != null)
                            mSplashViewModel.getAnalyticsEventsList();
                        return null;
                    });

                        /*if (!CommonUtils.isInstalledFromBranchTracked() && shouldTrackInstall) {
                            shouldTrackInstall = false;
                            SegmentTracker.getInstance(SplashActivity.this).trackEvent(EVENT_APP_INSTALL, new HashMap<>());
                            CommonUtils.setInstalledFromBranchTracked();
                        }*/
                } else {
                        /*if (!CommonUtils.isInstalledFromBranchTracked() && shouldTrackInstall) {
                            shouldTrackInstall = false;
                            SegmentTracker.getInstance(SplashActivity.this).trackEvent(EVENT_APP_INSTALL, new HashMap());
                            CommonUtils.setInstalledFromBranchTracked();
                        }*/
                    branchLinkHandled = true;
                    proceedIntentHandle(branchIntent);

                }
            }
        }, branchIntent.getData(), this);
    }

    public void trackInstallRefererFirebaseEvent(String intentOpenUrl) {
        Log.d("trackEvent_is:", "trackEvent_is:" + intentOpenUrl);
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        try {
            if (intentOpenUrl != null) {
                Map<String, String> getParams;
                if (intentOpenUrl.contains("referrer"))
                    getParams = CommonUtils.getHashMapFromQueryFirebase(intentOpenUrl);
                else
                    getParams = CommonUtils.getQueryParams(intentOpenUrl);
                for (String key : getParams.keySet()) {
                    String value;
                    if ((value = getParams.get(key)) != null && !value.isEmpty())
                        properties.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("trackEvent_is:", "Error");
        } finally {
            properties.put("source", "deeplink");
            Log.d("trackEvent_is:", "query_params:" + properties);
            SegmentTracker.getInstance(this).trackEvent(INSTALL_REFERRER, properties);
        }
    }


    private void handleFirebaseLink(Intent firebaseIntent) {
        try {
            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(firebaseIntent)
                    .addOnSuccessListener(this, pendingDynamicLinkData -> {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink;
                        if (pendingDynamicLinkData != null) {
                            analytics = FirebaseAnalytics.getInstance(SplashActivity.this);
                            deepLink = pendingDynamicLinkData.getLink();
                            if (deepLink != null) {
                                intentOpenUrl.set(deepLink.toString());//got the deeplink
                                shareParamOffset = 2;
                                Log.i(TAG, intentOpenUrl.get());
                            }
                            Log.i("#######f", intentOpenUrl.get());
                            if (!CommonUtils.isInstallRefererEventTracked()) {
                                Log.i("#######", "event tracked");
                                trackInstallRefererFirebaseEvent(intentOpenUrl.get());
                                CommonUtils.setInstallRefererEventTracked(true);
                            }
                            Log.d(TAG, intentOpenUrl.get());
                            try {
                                handleFireBaseLinkData();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            firebaseLinkHandled = true;
                            proceedIntentHandle(firebaseIntent);
                        }

                    })
                    .addOnFailureListener(this, e -> {
                        Log.d(TAG, "Fail");
                        firebaseLinkHandled = true;
                        proceedIntentHandle(firebaseIntent);
                    });
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private boolean branchLinkHandled = false;
    private boolean firebaseLinkHandled = false;
    private boolean facebookLinkHandled = false;

    private void proceedIntentHandle(Intent fbIntent) {
        if (branchLinkHandled && firebaseLinkHandled) {
            checkFbDeferredDeepLink(fbIntent);
            initReferalClient();
        }
    }

    private void handleFireBaseLinkData() {
        Map<String, String> params = null;

        try {
            if (intentOpenUrl.get() != null) {
                if (intentOpenUrl.get().contains("referrer"))
                    params = CommonUtils.getHashMapFromQueryFirebase(intentOpenUrl.get());
                else
                    params = CommonUtils.getQueryParams(intentOpenUrl.get());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "params" + params);
        if (params != null) {
            // params are the deep linked params associated with the link that the user clicked -> was re-directed to this app
            // params will be empty if no data found
            // ... insert custom logic here ...
            String extraInfo = "";

            if (params.containsKey("selected_language") && params.get("selected_language") != null)
                selectedLanguageId = params.get("selected_language");

            if (params.containsKey("utm_campaign") && params.get("utm_campaign") != null) {
                extraInfo = params.get("utm_campaign");
                mSplashViewModel.extraInfo = extraInfo;

            }
            baseProperties.put("source", "deeplink");
            for (String key : params.keySet()) {
                String value;
                if ((value = params.get(key)) != null && !value.isEmpty())
                    baseProperties.put(key, value);
            }


            if (selectedLanguageId != null && !selectedLanguageId.equals(""))
                baseProperties.put("selected_language", selectedLanguageId);

            if (extraInfo != null && !extraInfo.equals("")) {
                CommonUtils.setBranchExtraInfo(SplashActivity.this, extraInfo);
            }
            fireBranchReferralEvent = true;
            if (intentOpenUrl.get().contains("/post/")) {
                String[] paramsOfUrl = intentOpenUrl.get().split("/");
                String postId = paramsOfUrl[paramsOfUrl.length - 2];
                baseProperties.put("postId", postId);
            }
            if (intentOpenUrl.get().contains("content/clips/")) {
                String[] paramsOfUrl = intentOpenUrl.get().split("/");
                String postId = paramsOfUrl[paramsOfUrl.length - 2];
                baseProperties.put("clipId", postId);
            }
            if (intentOpenUrl.get().contains("/user/")) {
                String[] paramsOfUrl = intentOpenUrl.get().split("/");
                String username = paramsOfUrl[paramsOfUrl.length - 2];
                baseProperties.put("profile_username", username);
            }

            Log.i(getClass().getSimpleName(), "selected_language " + selectedLanguageId + " extraInfo " + extraInfo + " intent " + intentOpenUrl.get());

            Log.i("BRANCH SDK", params.toString());

            mSplashViewModel.getAnalyticsEventsList();

        }
    }


    private void checkMoengageDeepLink(Intent moengageIntent) {
        try {
            if (moengageIntent != null && moengageIntent.getData() != null) {
                String link = moengageIntent.getData().toString();
                int index = link.indexOf("?");
                if (index != -1)
                    intentOpenUrl.set(link.substring(0, index));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setupAnalyticsEvents() {
        try {
            CommonUtils.trackAmplitudeInstallAndUpdateEvent(this);
            if (getIntent() != null && getIntent().getExtras() != null) {
                if (getIntent().getExtras().containsKey("notification_payload")) {
                    Serializable data = getIntent().getExtras().getSerializable("notification_payload");
                    if (data != null && data instanceof HashMap) {
                        baseProperties.put("source", "notification");
                        Map<String, Object> notificationData = (HashMap<String, Object>) data;
                        baseProperties.put("notification_title", notificationData.get("title"));
                        baseProperties.put("notification_body", notificationData.get("body"));
                        baseProperties.put("notification_url", notificationData.get("target_url"));
                    }
                } else if (getIntent().getExtras().containsKey("alarm_notification") && getIntent().getBooleanExtra("alarm_notification", false)) {
                    baseProperties.put("source", getIntent().getStringExtra(AppConstants.SCREEN_SOURCE));
                    baseProperties.put("postId", getIntent().getStringExtra(AppConstants.EVENT_POST_ID));
                    baseProperties.put("title", getIntent().getStringExtra(AppConstants.ARG_TITLE));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fireBranchReferralEvent) {
                SegmentTracker.getInstance(this).trackEvent(EVENT_BRANCH_REFERRAL, baseProperties);
            }
        }
    }

    public void showToast(String text) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(SplashActivity.this, text, Toast.LENGTH_LONG).show();
//            }
//        });
    }

    private void checkFbDeferredDeepLink(Intent fbIntent) {

        fbIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        AppLinkData.fetchDeferredAppLinkData(RheoTvApp.getNonUiContext(), appLinkData -> {
            if (appLinkData != null) {
                Uri targetUrl = appLinkData.getTargetUri();
                if (targetUrl == null) {
                    return;
                }

                Log.i("FB_Target_url", targetUrl.toString());
                showToast("Received url " + targetUrl.toString());

                intentOpenUrl.set(targetUrl.toString());
                /*if (!CommonUtils.isInstallRefererEventTracked()) {*/
                if (!CommonUtils.isInstallRefererFBEventTracked()) {
                    Log.i("#######", "event tracked");
                    showToast("Install Referrer Event tracking started");
                    trackInstallRefererEventForFB(intentOpenUrl.get());
                    CommonUtils.setInstallReferrerFbEventTracked(true);
                }
                /*Log.i("#######", "event tracked");
                showToast("Install Referrer Event tracking started");
                trackInstallRefererEvent(intentOpenUrl.get());
                CommonUtils.setInstallRefererEventTracked(true);*/
                //}
                sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), "fb_clid", targetUrl.getQuery());
            } else if (intentOpenUrl.get() == null || intentOpenUrl.get().isEmpty()) {
                showToast("Received Data from " + fbIntent);
                Uri data = fbIntent.getData();
                if (data != null) {
                    intentOpenUrl.set(data.toString());
                } else if (fbIntent.getExtras() != null) {
                    intentOpenUrl.set(fbIntent.getExtras().getString("target_url"));
                }
            }
        });

        String queryParams = sharedPrefsUtils.getStringPreference(RheoTvApp.getNonUiContext(), "fb_clid");
        if (queryParams != null && queryParams.isEmpty()) {
            AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext()).setDefaultProperty("fb_clid", queryParams);
        }
    }


    public void trackInstallRefererEventForFB(String intentOpenUrl) {
        HashMap<String, Object> properties = new HashMap<>();
        try {
            if (intentOpenUrl != null) {
                String referrerString = CommonUtils.getReferrerString(intentOpenUrl);
                if (referrerString != null && !referrerString.isEmpty()) {
                    Map<String, String> getParams = getHashMapFromQuery(referrerString);
                    properties.put("utm_source", getParams.get("utm_source"));
                    properties.put("utm_medium", getParams.get("utm_medium"));
                    properties.put("utm_term", getParams.get("utm_term"));
                    properties.put("utm_content", getParams.get("utm_content"));
                    properties.put("utm_campaign", getParams.get("utm_campaign"));
                }
            }
        } catch (Exception e) {
            showToast("Error in tracking INSTALL Referrer");
            e.printStackTrace();
        } finally {
            Log.i("#######", "event tracked" + intentOpenUrl);
            SegmentTracker.getInstance(this).trackEvent(INSTALL_REFERRER_FB, properties);
            showToast("Successfully tracked INSTALL Referrer");
        }
    }

    private void openOnBoardingScreen(boolean showUpdateMessage, boolean isReLogin) {
//        Intent intent = new Intent(this, OnBoardingActivity.class);
        Intent intent = new Intent(this, GameSelectionActivity.class);
        intent.putExtra("intent_open_url", intentOpenUrl.get());
        intent.putExtra("show_update_message", showUpdateMessage);
        intent.putExtra("deeplink_offset", shareParamOffset);
        intent.putExtra("is_relogin", isReLogin);
        intent.putExtra(BRANCH_SELECTED_LANGUAGE, selectedLanguageId);
        startActivity(intent);
        finish();
    }

    public void renderHomePage(boolean showUpdateMsg) {
        if (baseProperties == null) {
            baseProperties = new HashMap<>();
        }
        Log.i("########", "open url ----> " + (intentOpenUrl.get() != null ? intentOpenUrl.get() : "empty"));
        baseProperties.put("build", BuildConfig.VERSION_CODE);
        baseProperties.put("version", BuildConfig.VERSION_NAME);
        SegmentTracker.getInstance(SplashActivity.this).trackEvent(SegmentConstants.EVENT_APPLICATION_OPEN_SOURCE, baseProperties);
        if (!sharedPrefsUtils.getBooleanPreference(SplashActivity.this, SharedPrefsUtils.IS_ONBOARDING_DONE, false)) {
            checkAndShowOnboarding(showUpdateMsg);
        } else if (!sharedPrefsUtils.getBooleanPreference(SplashActivity.this, SharedPrefsUtils.IS_LOGGED_IN, false)) {
            openOnBoardingScreen(showUpdateMsg, true);
        } else {
            if (intentOpenUrl.get() != null) {
                if (intentOpenUrl.get().contains("/user/")) {
                    try {
                        intentOpenUrl.set(CommonUtils.getUrlWithoutParameters(intentOpenUrl.get()));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    Log.i("InstalledSplash", intentOpenUrl.get());
                    String[] params = intentOpenUrl.get().split("\\/");
                    String username = params[params.length - 1];
                    Log.i("InstalledSplashUsername", username);
                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_SPLASH);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("author_name", username);
                    intent.putExtra("is_deeplink", true);
                    startActivity(intent);
                    finish();
                } else if (intentOpenUrl.get().contains("/competition/")) {
                    String[] params = intentOpenUrl.get().split("\\/");
                    competitionId = params[params.length - shareParamOffset];
                    mSplashViewModel.getCompetitionData(competitionId);
                } else if (intentOpenUrl.get().contains("content/clips/")) {
                    Intent intent = new Intent(this, ClipsActivity.class);
                    String[] params = intentOpenUrl.get().split("\\/");
                    String clipId = params[params.length - shareParamOffset];
                    intent.putExtra("clip_id", clipId);
                    intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_SPLASH);
                    startActivity(intent);
                    finish();
                } else if (intentOpenUrl.get().contains("content/stories/")) {
                    Intent intent = new Intent(this, StoryActivity.class);
                    String[] params = intentOpenUrl.get().split("\\/");
                    String storyId = params[params.length - shareParamOffset];
                    Log.d(TAG, storyId + "param" + params);
                    storyId = storyId.substring(storyId.indexOf("=") + 1);
                    intent.putExtra(StoryActivity.ARG_STORY_ID, storyId);
                    intent.putExtra(StoryActivity.ARG_IS_FROM_DEEPLINK, true);
                    intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_SPLASH);
                    startActivity(intent);
                    finish();
                } else if (intentOpenUrl.get().contains("content/story/author")) {
                    Intent intent = new Intent(this, StoryActivity.class);
                    String[] params = intentOpenUrl.get().split("\\/");
                    String storyId = params[params.length - shareParamOffset];
                    Log.i("intercept_url", "story" + intentOpenUrl.get() + " \n " + storyId);
                    intent.putExtra(StoryActivity.ARG_AUTHOR_ID, storyId);
                    Log.d(TAG, storyId + "params2" + params);
                    intent.putExtra(StoryActivity.ARG_IS_FROM_DEEPLINK, true);
                    intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_SPLASH);
                    startActivity(intent);
                    finish();
                } else if (intentOpenUrl.get().contains("/redeem/")) {
                    Intent intent = new Intent(this, RewardsActivity.class);
                    intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_SPLASH);
                    intent.putExtra("from", "share");
                    startActivity(intent);
                    finish();
                } else if (LinkHandler.getMojoTargetPath(intentOpenUrl.get()).contains("/post/")) {
                    boolean isForCustomRoom = false;
                    try {
                        if (getIntent().hasExtra("type") && getIntent().getStringExtra("type") != null && ("play_request".equalsIgnoreCase(getIntent().getStringExtra("type"))
                                || AppUtilsKt.INSTANCE.isCustomRoomMessage(getIntent().getStringExtra("type"))))
                            isForCustomRoom = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    moveToHomePage(showUpdateMsg);
//                    LinkHandler.handleDeepLink(this, intentOpenUrl, SegmentConstants.SCREEN_NAME_SPLASH, isForCustomRoom);
                } else if (intentOpenUrl.get().contains("/audio_chat_room/")) {
                    Uri url = Uri.parse(intentOpenUrl.get());
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
                    AudioChatRoomActivity.Companion.startMe(this, groupId, 0, chatRoomId, SegmentConstants.SCREEN_NAME_SPLASH, true);
                    finish();

                } else {
                    moveToHomePage(showUpdateMsg);
                }
            } else {
                moveToHomePage(showUpdateMsg);
            }
        }
    }

    public void moveToHomePage(boolean showUpdateMsg) {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.putExtra("open_url", intentOpenUrl.get());
        intent.putExtra("show_update_msg", showUpdateMsg);
        intent.putExtra("check_version", !mSplashViewModel.versionChecked);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_SPLASH);
        if (intentOpenUrl.get() != null && !intentOpenUrl.get().isEmpty()) {
            LinkHandler.setIntentOpenUrl(intentOpenUrl.get());
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }


    @Override
    public void showCompetionPage(Result result) {
        Intent intent = new Intent(this, AlertInformationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("is_deep_link", true);
        ListHolder.getInstance().setAlertInfoObject(result);
        startActivity(intent);
        finish();
    }

    @Override
    public void openMainActivity() {
        boolean isFirstLaunch = sharedPrefsUtils.getBooleanPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.FIRST_LAUNCH, true);
        /*if(isFirstLaunch) {
            launchOnboardingPage();
        } else {
            renderHomePage();
        }*/
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                renderHomePage(false);
            }
        }, 100);
    }

    @Override
    public void handleError(Throwable throwable) {
        AppLogger.d("Error", throwable.getLocalizedMessage());
    }

    @Override
    public void showUpdateOptions() {
        /*activitySplashBinding.updateText.setVisibility(View.VISIBLE);
        activitySplashBinding.skipButton.setVisibility(View.VISIBLE);
        activitySplashBinding.updateButton.setVisibility(View.VISIBLE);*/
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                renderHomePage(true);
            }
        }, 1600);

    }

    @Override
    public void showForceUpdateDialog() {
        try {
            isVersionSupported = false;
            new AlertDialog.Builder(this).setTitle(getString(R.string.force_update_app)).setMessage(getString(R.string.force_update_msg))
                    .setPositiveButton(getString(R.string.update_text), (dialogInterface, i) -> openPlayStoreLink()).setCancelable(false).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void isDataLoaded() {

    }

    private void initReferalClient() {
        try {
            if (!CommonUtils.isInstallRefererEventTracked()) {
                Log.i("#######", "event tracked");
                mReferrerClient = InstallReferrerClient.newBuilder(this).build();
                mReferrerClient.startConnection(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySplashBinding = getViewDataBinding();
        mSplashViewModel.setNavigator(this);
        baseProperties.put("source", "launcher");

        AlarmService.Companion.stopService();
        // setIntentOpenUrl();
        /*if (!PermissionUtils.hasWriteStoragePermission(this)) {
            PermissionUtils.requestWriteStoragePermission(this);
        } else {
            doOnCreateWork();
        }*/
        //Initialize Amplitude

        mSplashViewModel.getAnalyticsEventsList();
        if (CommonUtils.isUserLoggedin())
            mSplashViewModel.loadDailyRewards();
        mSplashViewModel.checkVersionSupport();


    }

    private void showAddBanner() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("splash_ad_name", CommonUtils.getSplashBannerAdName());
        properties.put("splash_ad_url", CommonUtils.getSplashBannerAdName());
        properties.put("splash_ad_target", CommonUtils.getSplashBannerTarget());
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_SPLASH_AD_BANNER_SHOWN, properties);
        activitySplashBinding.skipTimerBtn.setVisibility(View.VISIBLE);
        activitySplashBinding.skipTimerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SegmentTracker.getInstance(SplashActivity.this).trackEvent(SegmentConstants.EVENT_SPLASH_AD_SKIP_CLICKED, properties);
                checkAndMoveForward();
            }
        });
        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
                activitySplashBinding.skipTimerBtn.setText("Will Skip in " + (millisUntilFinished / 1000));
            }

            public void onFinish() {
                /*activitySplashBinding.skipTimerBtn.setText("Skip Now");*/
                checkAndMoveForward();
            }

        }.start();
        BindingUtils.setImageUrl(activitySplashBinding.splashAdIv, CommonUtils.getSplashBannerUrl());
        activitySplashBinding.splashAdIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SegmentTracker.getInstance(SplashActivity.this).trackEvent(SegmentConstants.EVENT_SPLASH_AD_OPEN_TARGET_CLICKED, properties);
                intentOpenUrl.set(CommonUtils.getSplashBannerTarget());
                //intentOpenUrl = "https://www.rheotv.com/item/post/rheo-battle-arena-season-2-qualifiers-day-2-group-16-20FufOVo/78ec9961-fab3-4a06-be6e-b6faf0f8b761/";
                renderHomePage(mSplashViewModel.isShowUpdate());
            }
        });
    }

    public void onGifFinished() {
        Log.i("Splash Tag", "Gif anim complete");
        isGifAnimationComplete = true;
        checkAndMoveForward();
    }

    @Override
    public void onNetworkRequestComplete() {
        Log.i("Splash Tag", "Network request complete");
        isRequestComplete = true;
//        checkAndMoveForward();
    }

    @Override
    public Activity getCallingActivityInstance() {
        return this;
    }

    public void checkAndMoveForward() {
        if (!NetworkUtils.isNetworkConnected(this)) {
            renderHomePage(false);
        } else
            openMainActivity();
    }

    private void setLinkHandleInit() {
        branch = Branch.getInstance(this);
        if (!branch.isUserIdentified() && CommonUtils.getDevId(this) != null) {
            branch.setIdentity(CommonUtils.getDevId(this));
            new BranchEvent("app open").logEvent(this);
        }
        FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        doOnCreateWork();
        handleIntent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setLinkHandleInit();
        // Branch init
        sharedPrefsUtils.setLongPreference(this, SharedPrefsUtils.LAST_APP_OPEN_TIME, System.currentTimeMillis());
//        cancelOfflineNotification();
        trackAppOpen();
    }

    private void trackAppOpen() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("build", BuildConfig.VERSION_CODE);
        properties.put("version", BuildConfig.VERSION_NAME);
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_APP_OPENED, properties);
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_APPLICATION_OPENED, properties);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        Glide.with(this).clear(activitySplashBinding.gifImageView);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mSplashViewModel.setVersionChecked(false);
        isGifAnimationComplete = false;
        setIntent(intent);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,
                    "Writing to external storage permission given", Toast.LENGTH_LONG).show();
            doOnCreateWork();
        }
    }


    private void doOnCreateWork() {
        boolean isAppInstallSent = sharedPrefsUtils.getBooleanPreference(RheoTvApp.getNonUiContext(), AppConstants.IS_APP_INSTALL_SENT, false);
        boolean isLocationPermissionAsked = sharedPrefsUtils.getBooleanPreference(RheoTvApp.getNonUiContext(), "permission_asked", false);
        if (isAppInstallSent || isLocationPermissionAsked) {
            AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext()).sendAppOpen();
        }
//        scaleView(activitySplashBinding.splashIvLogo, 0f, 1f);
        rotateView(activitySplashBinding.logo);
        /*SegmentTracker.getInstance(this).recordScreenName(SegmentConstants.SCREEN_NAME_SPLASH, new Properties());
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_APP_LAUNCHED, new Properties());*/

        try {
            if (getIntent() != null && getIntent().hasExtra("notification_payload")) {
                HashMap<String, Object> map = (HashMap<String, Object>) getIntent().getSerializableExtra("notification_payload");
                HashMap<String, Object> payloadProperties = new HashMap<>();
                if (map.containsValue("title"))
                    payloadProperties.put("title", map.get("title"));

                if (map.containsValue("body"))
                    payloadProperties.put("body", map.get("body"));

                if (map.containsValue("target_url"))
                    payloadProperties.put("target_url", map.get("target_url"));

                if (map.containsValue("image_url"))
                    payloadProperties.put("image_url", map.get("image_url"));

                if (map.containsValue("author_username"))
                    payloadProperties.put("author_username", map.get("author_username"));

                SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_NOTIFICATION_CLICKED, payloadProperties);
            } else if (getIntent() != null && getIntent().getExtras().containsKey("alarm_notification") && getIntent().getBooleanExtra("alarm_notification", false)) {
                baseProperties.put("source", getIntent().getStringExtra(AppConstants.SCREEN_SOURCE));
                baseProperties.put("postId", getIntent().getStringExtra(AppConstants.EVENT_POST_ID));
                baseProperties.put("title", getIntent().getStringExtra(AppConstants.ARG_TITLE));
                SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_ALARM_NOTIFICATION_CLICKED, baseProperties);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


//        showAddBanner();

        if (CommonUtils.getSplashBannerUrl() != null && CommonUtils.getSplashBannerValidTillTS() >= System.currentTimeMillis()) {
            showAddBanner();
        } else {
            activitySplashBinding.skipTimerBtn.setVisibility(View.GONE);
            activitySplashBinding.animationView.playAnimation();
            activitySplashBinding.animationView.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (isVersionSupported)
                        onGifFinished();
                    //Your code for remove the fragment
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        //Amplitude Events
    }


    @Override
    public void onInstallReferrerSetupFinished(int responseCode) {
        switch (responseCode) {
            case InstallReferrerClient.InstallReferrerResponse.OK:
                // Connection established
                getReferralUser();

                break;
            case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                // API not available on the current Play Store app
                Log.i(TAG, "FEATURE_NOT_SUPPORTED");
                break;
            case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                // Connection could not be established
                Log.i(TAG, "SERVICE_UNAVAILABLE");
                break;
            case InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR:
                //Developer error
                Log.i(TAG, "DEVELOPER_ERROR");
                break;
            case InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED:
                //Service Disconnected
                Log.i(TAG, "SERVICE_DISCONNECTED");
                break;
        }
    }

    public void startLanguageSelectionActivity() {

    }

    private void getReferralUser() {

        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        try {
            ReferrerDetails response = mReferrerClient.getInstallReferrer();
            String referrerData = response.getInstallReferrer();
            Log.e("install", "Install referrer:" + response.getInstallReferrer());
            if (referrerData != null && !referrerData.isEmpty()) {
                Map<String, String> getParams = getHashMapFromQuery(Objects.requireNonNull(referrerData));
                for (String key : getParams.keySet()) {
                    String value;
                    if ((value = getParams.get(key)) != null && !value.isEmpty())
                        properties.put(key, value);
                }
            }

            Log.e("install", properties.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!CommonUtils.isInstallRefererEventTracked()) {
                Log.i("#######", "event tracked" + intentOpenUrl);
                properties.put("source", "deeplink");
                SegmentTracker.getInstance(this).trackEvent(INSTALL_REFERRER, properties);
                CommonUtils.setInstallRefererEventTracked(true);
            }
            if (mReferrerClient != null) {
                mReferrerClient.endConnection();
            }
        }

    }

    @Override
    public void onInstallReferrerServiceDisconnected() {
        if (mReferrerClient == null) return;
        mReferrerClient.endConnection();
    }
}

