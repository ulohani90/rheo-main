package com.rheotv.android.utils;

import android.content.Context;
import android.util.Log;

import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.gamify.DailyRewardsResponse;
import com.rheotv.android.data.network.models.gamify.Reward;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.inject.Singleton;

import static com.rheotv.android.utils.AppConstants.REWARD_TYPE_DAILY_LOGIN;
import static com.rheotv.android.utils.AppConstants.REWARD_TYPE_FIRST_COMMENT;
import static com.rheotv.android.utils.AppConstants.REWARD_TYPE_SEVENTH_DAY;
import static com.rheotv.android.utils.AppConstants.REWARD_TYPE_SHARE;
import static com.rheotv.android.utils.AppConstants.REWARD_TYPE_TEN_MINUTE_STREAM;
import static com.rheotv.android.utils.AppConstants.STATUS_ACTIVATED;
import static com.rheotv.android.utils.AppConstants.STATUS_PENDING;

@Singleton
public class RewardManager {

    private static final String TAG = "RewardManager";
    private static final String LAST_LOGGED_IN_TIME = "last_logged_in_time";
    private static final String IS_NON_LOGIN_SCRATCH_CARD_SHOWN = "is_non_login_scratch_card_shown";

    private static volatile RewardManager rewardManager;
    private static List<Reward> dailyRewards = new ArrayList<>();
    private static String totalCoins = "0";
    private boolean shouldAskRating = false;
    private boolean recentlyRewarded = false;
    private boolean recentActionTaken = false;
    private boolean codaEnabled = false;

    private RewardManager() {
    }

    public static RewardManager getInstance() {
        if (rewardManager == null) {
            synchronized (RewardManager.class) {
                if (rewardManager == null)
                    rewardManager = new RewardManager();
            }
        }

        return rewardManager;
    }

    public String getTotalCoins() {
        return totalCoins;
    }

    public int getTotalCoin() {
        return totalCoins == null || totalCoins.isEmpty() ? 0 : Integer.parseInt(totalCoins);
    }

    public void setTotalCoins(String coins) {
        totalCoins = coins;
    }

    public List<Reward> getDailyRewards() {
        return dailyRewards;
    }

    public void setDailyRewards(List<Reward> rewards) {
        dailyRewards = rewards;
    }

    public boolean isLoginOrSeventhDayAvailable() {
        if (dailyRewards == null || dailyRewards.isEmpty()) return false;
        for (Reward reward : dailyRewards) {
            Log.i(getClass().getName(), "isLoginOrSeventhDayAvailable " + reward.getRewardType() + " and " + reward.getRewardStatus());
            if (reward.getRewardType().equals(REWARD_TYPE_DAILY_LOGIN) && reward.getRewardStatus().equals(STATUS_ACTIVATED))
                return true;
            else if (reward.getRewardType().equals(REWARD_TYPE_SEVENTH_DAY) && reward.getRewardStatus().equals(STATUS_ACTIVATED))
                return true;
        }

        return false;
    }

    public boolean isDailyLoginRewardAvailable() {
        for (Reward reward : dailyRewards) {
            if (reward.getRewardType().equals(REWARD_TYPE_DAILY_LOGIN))
                return reward.getRewardStatus().equals(STATUS_ACTIVATED);
        }

        return false;
    }

    public boolean isShareRewardAvailable() {
        for (Reward reward : dailyRewards) {
            if (reward.getRewardType().equals(REWARD_TYPE_SHARE))
                return reward.getRewardStatus().equals(STATUS_ACTIVATED) && CommonUtils.firstShareRewardState().equals(AppConstants.SHARE_AVAILABLE);
        }

        return false;
    }

    public boolean isTenMinuteStreamRewardAvailable() {
        if (dailyRewards == null || dailyRewards.isEmpty()) return false;
        for (Reward reward : dailyRewards) {
            if (reward.getRewardType().equals(REWARD_TYPE_TEN_MINUTE_STREAM))
                return reward.getRewardStatus().equals(STATUS_ACTIVATED);
        }

        return false;
    }

    public boolean isSeventhDayRewardAvailable() {
        for (Reward reward : dailyRewards) {
            if (reward.getRewardType().equals(REWARD_TYPE_SEVENTH_DAY)) {
                Log.i(getClass().getName(), "isSeventhDayRewardAvailable" + reward.getRewardStatus().equals(STATUS_ACTIVATED));
                return reward.getRewardStatus().equals(STATUS_ACTIVATED);
            }
        }

        return false;
    }

    public boolean isFirstCommentRewardAvailable() {
        if (dailyRewards == null || dailyRewards.isEmpty()) return false;
        for (Reward reward : dailyRewards) {
            Log.i(getClass().getName(), "isFirstCommentRewardAvailable type:" + reward.getRewardType().equals(REWARD_TYPE_FIRST_COMMENT) + " and active:" + reward.getRewardStatus() + " and " + reward.getRewardType());
            if (reward.getRewardType().equals(REWARD_TYPE_FIRST_COMMENT))
                return reward.getRewardStatus().equals(STATUS_ACTIVATED);
        }

        return false;
    }

    public Reward getLoginReward() {
        for (Reward reward : dailyRewards) {
            if (reward.getRewardType().equals(REWARD_TYPE_DAILY_LOGIN))
                return reward;
        }

        return null;
    }

    public long getVideoRewardActivationTime() {
        if (dailyRewards == null || dailyRewards.isEmpty()) return 0;
        for (Reward reward : dailyRewards) {
            if (reward.getRewardType().equals(REWARD_TYPE_TEN_MINUTE_STREAM))
                return reward.getRewardActivateAfter();
        }

        return 0;
    }

    public long getVideoRewardAlertDelayTime() {
        if (dailyRewards == null || dailyRewards.isEmpty()) return 0;
        for (Reward reward : dailyRewards) {
            if (reward.getRewardType().equals(REWARD_TYPE_TEN_MINUTE_STREAM))
                return reward.getRewardActivateAfter() * 20 / 100;
        }

        return 0;
    }

    public Reward getUserStreakReward() {
        if (dailyRewards == null || dailyRewards.isEmpty()) return null;
        for (Reward reward : dailyRewards) {
            if (reward.getRewardType().equals(REWARD_TYPE_SEVENTH_DAY))
                return reward;
        }

        return null;
    }

    public Reward getAvailableReward(String[] rewardType) {
        if (dailyRewards == null || dailyRewards.isEmpty()) return null;
        for (Reward reward : dailyRewards) {
            if (reward != null && rewardType != null && Arrays.asList(rewardType).contains(reward.getRewardType()) && reward.getRewardStatus().equals(STATUS_ACTIVATED))
                return reward;
        }
        return null;
    }

    public Reward getAvailableReward() {
        for (Reward reward : dailyRewards) {
            if (reward.getRewardStatus().equals(STATUS_ACTIVATED))
                return reward;
        }
        return null;
    }

    public void setDailyLoginRewardAvailable() {
        for (Reward reward : dailyRewards) {
            if (reward.getRewardType().equals(REWARD_TYPE_DAILY_LOGIN) && reward.getRewardStatus().equals(STATUS_ACTIVATED)) {
                reward.setRewardStatus(STATUS_PENDING);
                totalCoins = String.valueOf(Integer.parseInt(totalCoins) + Integer.parseInt(reward.getCoinWon()));
                break;
            }
        }
    }

    public void setTenMinuteStreamRewardAvailable() {
        for (Reward reward : dailyRewards) {
            if (reward.getRewardType().equals(REWARD_TYPE_TEN_MINUTE_STREAM) && reward.getRewardStatus().equals(STATUS_PENDING)) {
                reward.setRewardStatus(STATUS_ACTIVATED);
                break;
            }
        }
    }

    public void addShareReward() {
        dailyRewards.add(new Reward(REWARD_TYPE_SHARE, "0", "0", "0", STATUS_PENDING));
    }

    public void setShareRewardActive() {
        for (Reward reward : dailyRewards) {
            if (reward.getRewardType().equals(REWARD_TYPE_SHARE) && reward.getRewardStatus().equals(STATUS_PENDING)) {
                reward.setRewardStatus(STATUS_ACTIVATED);
                break;
            }
        }
    }

    public void setRewardTaken(String rewardId) {
        Log.i(getClass().getName(), "setRewardTaken 1 " + rewardId + " and " + totalCoins);
        for (Reward reward : dailyRewards) {
            if (reward.getId().equals(rewardId)) {
                reward.setRewardStatus(STATUS_PENDING);
                totalCoins = String.valueOf(Integer.parseInt(totalCoins) + Integer.parseInt(reward.getCoinWon()));
                break;
            }
        }

        Log.i(getClass().getName(), "setRewardTaken 2 " + totalCoins);
    }

    public boolean shouldAskRating() {
        return shouldAskRating;
    }

    public boolean isRatingAvailable() {
        return shouldAskRating && (recentlyRewarded || recentActionTaken);
    }

    public void setShouldAskRating(boolean shouldAskRating) {
        this.shouldAskRating = shouldAskRating;
    }

    public boolean isRecentlyRewarded() {
        return recentlyRewarded;
    }

    public void setRecentlyRewarded(boolean recentlyRewarded) {
        this.recentlyRewarded = recentlyRewarded;
    }

    public boolean isActionTaken() {
        return recentActionTaken;
    }

    public void setActionTaken(boolean actionTaken) {
        this.recentActionTaken = actionTaken;
    }

    public boolean isCodaEnabled() {
        return codaEnabled;
    }

    public void setCodaEnabled(boolean codaEnabled) {
        this.codaEnabled = codaEnabled;
    }

    public void clear() {
        dailyRewards = new ArrayList<>();
        totalCoins = "0";
        shouldAskRating = false;
        recentlyRewarded = false;
        recentActionTaken = false;
        codaEnabled = false;
    }

    public void reduceCoin(int reduceAmount) {
        setTotalCoins("" + (getTotalCoin() - reduceAmount));
    }

    public void updateData(DailyRewardsResponse dailyRewardsResponse) {
        setDailyRewards(dailyRewardsResponse.getResults());
        setTotalCoins(dailyRewardsResponse.getTotalCoins());
        setCodaEnabled(dailyRewardsResponse.isCodaEnabled());
        setShouldAskRating(dailyRewardsResponse.getCanGiveFeedback());
    }

    private boolean isUserLoggedIn = false;

    public void updateNonLoggedInScratchCardShown(Context context) {
        if (CommonUtils.isUserLoggedin()) return;
        if (context == null) context = RheoTvApp.getNonUiContext();
        SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
        long lastLoginTime = sharedPrefsUtils.getLongPreference(context, LAST_LOGGED_IN_TIME, 0);
        sharedPrefsUtils.setLongPreference(context, LAST_LOGGED_IN_TIME, System.currentTimeMillis());
        Calendar lastLoggedInDate = Calendar.getInstance();
        lastLoggedInDate.setTimeInMillis(lastLoginTime);
        Calendar currentDate = Calendar.getInstance();
        if (currentDate.get(Calendar.DATE) > lastLoggedInDate.get(Calendar.DATE) || (currentDate.get(Calendar.DATE) < lastLoggedInDate.get(Calendar.DATE) && currentDate.after(lastLoggedInDate))) {
            sharedPrefsUtils.setBooleanPreference(context, IS_NON_LOGIN_SCRATCH_CARD_SHOWN, false);
        }
    }

    public void updateNonLoggedInScratchCard(Context context) {
        if (context == null) context = RheoTvApp.getNonUiContext();
        new SharedPrefsUtils().setBooleanPreference(context, IS_NON_LOGIN_SCRATCH_CARD_SHOWN, true);
    }

    public boolean isNonLoggedInScratchCardShown(Context context) {
        if (CommonUtils.isUserLoggedin()) return true;
        if (context == null) context = RheoTvApp.getNonUiContext();
        boolean isCardShown = new SharedPrefsUtils().getBooleanPreference(context, IS_NON_LOGIN_SCRATCH_CARD_SHOWN, false);
        Log.e(TAG, "isCardShown ----> " + isCardShown);
        return isCardShown;
    }
}
