package com.rheotv.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.services.PlaybackService;
import com.rheotv.android.services.PlayerHeadHolder;
import com.rheotv.android.services.PlayerHeadService;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.activities.home.view.HomeActivity;
import com.rheotv.android.ui.activities.player.activity.OverlayPermissionBottomSheet;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerFragment;
import com.rheotv.android.ui.activities.player.activity.newPlayer.StreamPlayerFragmentV2;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivity;
import com.rheotv.android.ui.fragments.VideoAlertDialogFragment;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import static com.rheotv.android.utils.AppConstants.PLAYER_MODE_AUDIO;
import static com.rheotv.android.utils.AppConstants.PLAYER_MODE_NON;
import static com.rheotv.android.utils.AppConstants.PLAYER_MODE_VIDEO;

public class PlayerHeadServiceHelper {

    public static final String PLAYER_SERVICE_STOP_BROADCAST = "player service stop broadcast";
    public static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private boolean isFromPlayerHeadWidget;

    static PlayerHeadServiceHelper mHelper;

    private Context mContext;

    private Intent playerHeadService;
    private Intent audioServiceIntent;

    private boolean isServiceRunning;

    public static PlayerHeadServiceHelper getInstance() {
        if (mHelper == null) {
            mHelper = new PlayerHeadServiceHelper(RheoTvApp.getNonUiContext());
        }
        return mHelper;
    }

    private AlertDialog overlayAlertDialog;

    public static PlayerHeadServiceHelper getHelperInstance() {
        return mHelper;
    }

    public PlayerHeadServiceHelper(Context context) {
        this.mContext = context;
    }

    public void startPlayerHeadService(String username,
                                       String game,
                                       String videoUrl,
                                       String postId,
                                       long timeElapsed,
                                       long timeUntilVideoReward,
                                       String authorId,
                                       boolean isLive,
                                       String language,
                                       String title,
                                       String authorName,
                                       String gameId,
                                       int resumeWindow,
                                       long resumePosition
    ) {
        if (playerHeadService == null) {
            isServiceRunning = true;
            playerHeadService = new Intent(mContext, PlayerHeadService.class);
            playerHeadService.putExtra("username", username);
            playerHeadService.putExtra("game", game);
            playerHeadService.putExtra("media_url", videoUrl);
            playerHeadService.putExtra("post_id", postId);
            playerHeadService.putExtra("time_elapsed", timeElapsed);
            playerHeadService.putExtra(AppConstants.ARG_GLOBAL_VIDEO_REWARD_TIME, timeUntilVideoReward);
            playerHeadService.putExtra("author_id", authorId);
            playerHeadService.putExtra("is_live", isLive);
            playerHeadService.putExtra("type", isLive ? "live" : "fullRecorded");
            playerHeadService.putExtra("isLoggedIn", CommonUtils.isUserLoggedin());
            playerHeadService.putExtra("language", language);
            playerHeadService.putExtra("title", title);
            playerHeadService.putExtra("name", authorName);
            playerHeadService.putExtra("game_id", gameId);
            playerHeadService.putExtra("resume_window", resumeWindow);
            playerHeadService.putExtra("resume_position", resumePosition);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mContext.startForegroundService(playerHeadService);
            } else {
                mContext.startService(playerHeadService);
            }

        }
    }

    public void startPlayerHeadService(PlayerHeadHolder holder) {
        if (playerHeadService == null) {
            isServiceRunning = true;
            playerHeadService = new Intent(mContext, PlayerHeadService.class);
            playerHeadService.putExtra(AppConstants.ARG_PLAYER_HOLDER, holder);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mContext.startForegroundService(playerHeadService);
            } else {
                mContext.startService(playerHeadService);
            }
        }
    }

    public void stopPlayerHeaderService() {
        if (mContext != null && playerHeadService != null) {
            mContext.stopService(playerHeadService);
            playerHeadService = null;
            isServiceRunning = false;
            sendLocalBroadcastForPlayerClose();
        }

        stopPlayAudioService();
    }

    public void stopPlayAudioService() {
        if (mContext != null && audioServiceIntent != null) {
            mContext.stopService(audioServiceIntent);
            audioServiceIntent = null;
            isServiceRunning = false;
            sendLocalBroadcastForPlayerClose();
        }
    }

    private void sendLocalBroadcastForPlayerClose() {
        Intent intent = new Intent(PLAYER_SERVICE_STOP_BROADCAST);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public boolean isServiceRunning() {
        return isServiceRunning;
    }

    /**
     * if player Mode == non
     * sgowBottomsheet
     * else if player mode == video
     * checkPermissionAndStartVideoService
     * else
     * startAudioService
     * <p>
     * 1. permission for video service and start the video service
     * 2. audio option selected or not
     * 3. else start audio service
     */
    public boolean startService(StreamPlayerFragmentV2 streamPlayerFragment, AppCompatActivity activity,
                                Map<String, Object> baseProperties, boolean isFromPlayerActivity) {
        int playerMode = CommonUtils.getPlayerMode();

        // option dialog is not shown
        if (playerMode == PLAYER_MODE_NON) {  // no option selected yet
            showPlayerChooseDialog(streamPlayerFragment, activity, baseProperties, isFromPlayerActivity);
            return false;
        } else if (playerMode == PLAYER_MODE_VIDEO) {

            return canShowOverlayPermission(streamPlayerFragment, activity, baseProperties, isFromPlayerActivity);
        } else {
            checkAndStartAudioService(streamPlayerFragment, activity, isFromPlayerActivity);
        }

        return true;
    }

    public boolean canShowOverlayPermission(StreamPlayerFragmentV2 streamPlayerFragment, AppCompatActivity activity,
                                            Map<String, Object> baseProperties, boolean isFromPlayerActivity
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(activity))
                runVideoService(streamPlayerFragment, activity, isFromPlayerActivity);
            else
                askOverlayPermissionAndReward(streamPlayerFragment, activity, baseProperties, isFromPlayerActivity);
            return true;
        } else {
            return false;
        }
    }

    public void showPlayerChooseDialog(StreamPlayerFragmentV2 streamPlayerFragment, AppCompatActivity activity,
                                       Map<String, Object> baseProperties, boolean isFromPlayerActivity) {
        OverlayPermissionBottomSheet.Companion.show(activity.getSupportFragmentManager(),
                OverlayPermissionBottomSheet.Companion.newInstance(new OverlayPermissionBottomSheet.OnOptionSelected() {
                    @Override
                    public void onNothingSelected() {
                        CommonUtils.setPlayerMode(PLAYER_MODE_AUDIO);
                    }

                    @Override
                    public void onVideoSelected() {
                        CommonUtils.setPlayerMode(PLAYER_MODE_VIDEO);
                        checkAndRunVideoService(streamPlayerFragment, activity, baseProperties, isFromPlayerActivity);
                    }

                    @Override
                    public void onAudioSelected() {
                        CommonUtils.setPlayerMode(PLAYER_MODE_AUDIO);
                        if (activity instanceof HomeActivity)
                            if (streamPlayerFragment != null) {
                                startAudioService(streamPlayerFragment.getPlayerHolder());
                                if (isFromPlayerActivity) {
                                    startHomeActivity(activity);
                                } else {
                                    activity.finish();
                                }
                            }
                    }
                })
        );
    }

    public void checkAndRunVideoService(StreamPlayerFragmentV2 streamPlayerFragment, AppCompatActivity activity,
                                        Map<String, Object> baseProperties, boolean isFromPlayerActivity) {
        if (streamPlayerFragment == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(activity)) {
                    long lastShownTimeDiff = (System.currentTimeMillis() - new SharedPrefsUtils().getLongPreference(activity, SharedPrefsUtils.LAST_SHOW_VIDEO_HEADER_PERMISSION_TS, 0));
                    if (lastShownTimeDiff >= TimeUtils.MILLIS_IN_DAY) {
                        new SharedPrefsUtils().setLongPreference(activity, SharedPrefsUtils.LAST_SHOW_VIDEO_HEADER_PERMISSION_TS, System.currentTimeMillis());
                        // ask permission now
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
                        activity.startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
                        SegmentTracker.getInstance(activity).trackEvent(SegmentConstants.EVENT_OVERLAY_PERMISSION_GO_TO_SETTINGS_CLICK, baseProperties);
                    } else if (CommonUtils.isUserLoggedin() && RewardManager.getInstance().isTenMinuteStreamRewardAvailable() && (streamPlayerFragment != null && streamPlayerFragment.shouldShowTenMinuteAlert)) {
                        showRewardAlert(streamPlayerFragment, activity);
                    } else {
                        sendBackPressEvent(activity);
//                                checkAndFinish(activity, isFromPlayerActivity);
                    }
                } else {
                    if (startPlayerService(streamPlayerFragment.getPlayerHolder(), isFromPlayerActivity)) {
                        finishPageFallback(activity, isFromPlayerActivity);
                    }
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void checkAndRunVideoWidgetService(StreamPlayerFragmentV2 streamPlayerFragment, AppCompatActivity activity,
                                        Map<String, Object> baseProperties, boolean isFromPlayerActivity) {
//
//        Log.i(getClass().getSimpleName(), "play_video: " + (streamPlayerFragment == null) + " and " +
//                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) + " and " + Settings.canDrawOverlays(activity) + " and " + isFromPlayerActivity);

        if (streamPlayerFragment == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(activity))
                    runVideoService(streamPlayerFragment, activity, isFromPlayerActivity);
            } else {
                runVideoService(streamPlayerFragment, activity, isFromPlayerActivity);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void askOverlayPermissionAndReward(StreamPlayerFragmentV2 streamPlayerFragment, AppCompatActivity activity,
                                              Map<String, Object> baseProperties, boolean isFromPlayerActivity) {
        if (streamPlayerFragment == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
                long lastShownTimeDiff = (System.currentTimeMillis() - new SharedPrefsUtils().getLongPreference(activity, SharedPrefsUtils.LAST_SHOW_VIDEO_HEADER_PERMISSION_TS, 0));
                if (lastShownTimeDiff >= TimeUtils.MILLIS_IN_DAY) {
                    new SharedPrefsUtils().setLongPreference(activity, SharedPrefsUtils.LAST_SHOW_VIDEO_HEADER_PERMISSION_TS, System.currentTimeMillis());
                    // ask permission now
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
                    SegmentTracker.getInstance(activity).trackEvent(SegmentConstants.EVENT_OVERLAY_PERMISSION_GO_TO_SETTINGS_CLICK, baseProperties);
                } else if (CommonUtils.isUserLoggedin() && RewardManager.getInstance().isTenMinuteStreamRewardAvailable() && (streamPlayerFragment != null && streamPlayerFragment.shouldShowTenMinuteAlert)) {
                    showRewardAlert(streamPlayerFragment, activity);
                } else {
//                    EventBus.getDefault().post(new EventBusModel.UpdateBackPress(true));
                    sendBackPressEvent(activity);

//                    checkAndStartAudioService(streamPlayerFragment, activity, isFromPlayerActivity);
                }
            } else {
                runVideoService(streamPlayerFragment, activity, isFromPlayerActivity);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void sendBackPressEvent(Activity activity) {
        if (activity instanceof HomeActivity)
            EventBus.getDefault().post(new EventBusModel.UpdateBackPress(true));
        else
            EventBus.getDefault().post(EventBusModel.StartStreamService.INSTANCE);
    }

    private void runVideoService(StreamPlayerFragmentV2 streamPlayerFragment, AppCompatActivity activity, boolean isFromPlayerActivity) {
        if (startPlayerService(streamPlayerFragment.getPlayerHolder(), isFromPlayerActivity)) {
            finishPageFallback(activity, isFromPlayerActivity);
        }
    }

    public boolean canShowPermissionDialog(Activity activity, StreamPlayerFragment streamPlayerFragment) {
        long lastShownTimeDiff = (System.currentTimeMillis() - new SharedPrefsUtils().getLongPreference(activity, SharedPrefsUtils.LAST_SHOW_VIDEO_HEADER_PERMISSION_TS, 0));
        return lastShownTimeDiff >= TimeUtils.MILLIS_IN_DAY || (CommonUtils.isUserLoggedin() && RewardManager.getInstance().isTenMinuteStreamRewardAvailable() && (streamPlayerFragment != null && streamPlayerFragment.shouldShowTenMinuteAlert));
    }

    private void showRewardAlert(StreamPlayerFragmentV2 streamPlayerFragment, AppCompatActivity activity) {
        VideoAlertDialogFragment videoAlertDialogFragment;
        if (activity.getSupportFragmentManager().findFragmentByTag(AppConstants.ALERT_VIDEO_REWARD_TAG) != null)
            videoAlertDialogFragment = (VideoAlertDialogFragment) activity.getSupportFragmentManager().findFragmentByTag(AppConstants.ALERT_VIDEO_REWARD_TAG);
        else
            videoAlertDialogFragment = VideoAlertDialogFragment.newInstance(streamPlayerFragment.TIME_UNTIL_FINISH, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
        if (videoAlertDialogFragment != null && (videoAlertDialogFragment.isAdded() || videoAlertDialogFragment.isVisible()))
            return;
        if (videoAlertDialogFragment == null) return;
        videoAlertDialogFragment.show(activity.getSupportFragmentManager(), AppConstants.ALERT_VIDEO_REWARD_TAG, streamPlayerFragment.TIME_UNTIL_FINISH);
        streamPlayerFragment.shouldShowTenMinuteAlert = false;
    }

    private void showPermissionRequiredDialog(PlayerHeadHolder holder, AppCompatActivity activity, Map<String, Object> baseProperties) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogStyle);
        builder.setCancelable(true);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.overlay_permission_dialog_layout, null);

        dialogView.findViewById(R.id.allow_action).setOnClickListener(view -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
                    SegmentTracker.getInstance(activity).trackEvent(SegmentConstants.EVENT_OVERLAY_PERMISSION_GO_TO_SETTINGS_CLICK, baseProperties);
                }

                if (overlayAlertDialog != null) overlayAlertDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        dialogView.findViewById(R.id.cancel_action).setOnClickListener(view -> {
            SegmentTracker.getInstance(activity).trackEvent(SegmentConstants.EVENT_OVERLAY_PERMISSION_CANCEL_CLICK, baseProperties);
            if (overlayAlertDialog != null) overlayAlertDialog.dismiss();
//            checkAndFinish();
        });
        builder.setView(dialogView);
        overlayAlertDialog = builder.show();
        SegmentTracker.getInstance(activity).trackEvent(SegmentConstants.EVENT_OVERLAY_PERMISSION_ALERT_DIALOG, baseProperties);
    }

    private boolean startPlayerService(PlayerHeadHolder holder, boolean isFromPlayerActivity) {
        Log.i(getClass().getName(), "startPlayerService_called " + (holder == null));
        if (holder == null || holder.getPost().getVideoUrl() == null || holder.getPost().getVideoUrl().isEmpty())
            return false;
        holder.setFromDeeplink(isFromPlayerActivity);
        PlayerHeadServiceHelper.getInstance().startPlayerHeadService(holder);
        return true;
    }

    public boolean startAudioService(PlayerHeadHolder holder) {
        if (!CommonUtils.getAudioModeFlag()) return true;
        if (isServiceRunning) return false;
        if (holder == null || holder.getPost() == null || holder.getPost().getAudioUrl() == null)
            return true;
        if (audioServiceIntent == null) {
            isServiceRunning = true;
            audioServiceIntent = new Intent(mContext, PlaybackService.class);
            audioServiceIntent.putExtra(AppConstants.ARG_PLAYER_HOLDER, holder);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mContext.startForegroundService(audioServiceIntent);
            } else {
                mContext.startService(audioServiceIntent);
            }
        }
        return false;
    }

    public void checkAndStartAudioService(StreamPlayerFragmentV2 streamPlayerFragment,
                                          AppCompatActivity activity,
                                          boolean isFromPlayerActivity
    ) {
        if (startAudioService(streamPlayerFragment.getPlayerHolder())) {
            finishPageFallback(activity, isFromPlayerActivity);
        }
    }

    public void checkAndFinish(AppCompatActivity activity, boolean isFromPlayerActivity, PlayerHeadHolder playerHeadHolder) {
        try {
            if (isFromPlayerActivity) {
                // todo get playholder
                startAudioService(playerHeadHolder);
                startHomeActivity(activity);
            } else {
                sendBackPressEvent(activity);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public boolean shouldShowDPermissionDialog(Activity activity) {
        long lastShownTimeDiff = (System.currentTimeMillis() - new SharedPrefsUtils().getLongPreference(activity, SharedPrefsUtils.LAST_SHOW_VIDEO_HEADER_PERMISSION_TS, 0));
        return !isServiceRunning || lastShownTimeDiff >= TimeUtils.MILLIS_IN_DAY;
    }

    public void checkPermission(StreamPlayerFragmentV2 streamPlayerFragment, AppCompatActivity
            activity, boolean isFromPlayerActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(activity)) {
                if (streamPlayerFragment != null && streamPlayerFragment.getPlayerHolder() != null) {
                    if (startPlayerService(streamPlayerFragment.getPlayerHolder(), isFromPlayerActivity)) {
                        finishPageFallback(activity, isFromPlayerActivity);
                        return;
                    }
                    checkAndFinish(activity, isFromPlayerActivity, streamPlayerFragment.getPlayerHolder());
                }
            } else {
                CommonUtils.setPlayerMode(PLAYER_MODE_AUDIO);
                Toast.makeText(activity,
                        "Draw over other app permission not available. Closing the application",
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void finishPageFallback(AppCompatActivity activity, boolean isFromPlayerActivity) {
        try {
            if (isFromPlayerActivity) {
                startHomeActivity(activity);
            } else {
                activity.finish();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void startHomeActivity(Context context) {
        Intent intent = TabContainerActivity.newIntent(context);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public void checkAndStartService(StreamPlayerFragmentV2 streamPlayerFragment, AppCompatActivity activity,
                                     Map<String, Object> baseProperties, boolean isFromPlayerActivity) {
        if (CommonUtils.getPlayerMode() == AppConstants.PLAYER_MODE_NON)
            showPlayerChooseDialog(streamPlayerFragment, activity,
                    baseProperties, isFromPlayerActivity);
        else if (
                CommonUtils.getPlayerMode() == AppConstants.PLAYER_MODE_VIDEO)
            PlayerHeadServiceHelper.getInstance().canShowOverlayPermission(streamPlayerFragment, activity,
                    baseProperties, isFromPlayerActivity);
        else
            sendBackPressEvent(activity);
    }
}
