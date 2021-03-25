package com.rheotv.android.ui.activities.alertInformation;

import android.content.Context;

import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import androidx.databinding.ObservableField;

import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.helpers.ShareTaskHelper;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.TimeUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;

public class AlertInformationViewModel extends BaseViewModel<AlertInformationNavigator> {

    public ObservableField<String> title = new ObservableField<>();
    public ObservableField<String> description = new ObservableField<>();
    public ObservableField<String> bannerImageUrl = new ObservableField<>();
    public ObservableField<String> startsIn = new ObservableField<>();
    Context mContext;

    Result result;

    public AlertInformationViewModel(DataManager dataManager, SchedulerProvider schedulerProvider, Context context) {
        super(dataManager, schedulerProvider);
        this.mContext = context;
    }

    public void setData(Result result) {
        this.title.set(result.getTitle());
        this.description.set(result.getDescription());
        this.bannerImageUrl.set(result.getBannerImageUrl());
        this.result = result;
        setCallToAction(result.getStartDate(), result.getEndDate());
    }

    private void setCallToAction(String startDate, String endDate) {
        int state = TimeUtils.getContestDateState(startDate, endDate);
        if (state == AppConstants.CONTEST_DATE_STATE_BEFORE_1_DAY) {
            startsIn.set(TimeUtils.getFormattedDateForContestStart(startDate));
            return;
        }
        if (state == AppConstants.CONTEST_DATE_STATE_END || state == AppConstants.CONTEST_DATE_STATE_LIVE) {
            startsIn.set(mContext.getString(R.string.view_leaderboard));
            return;
        }
        addTimerToCallToAction(startDate);

    }

    private void addTimerToCallToAction(String startDate) {
        final long[] timeLeft = {TimeUtils.getContestStartIn(startDate)};
        if (timeLeft[0] == -1) {
            startsIn.set(TimeUtils.getFormattedDateForContestStart(startDate));
        }
        new CountDownTimer(timeLeft[0] * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                startsIn.set("Starts In " + TimeUtils.getTimeForLong(timeLeft[0]));
                timeLeft[0]--;
            }

            public void onFinish() {
                startsIn.set(mContext.getString(R.string.view_leaderboard));
            }

        }.start();

    }

    public void onCloseClicked() {
        getNavigator().closeActivity();
    }

    public void onViewLeaderboardClick(View v) {
        if (((TextView) v).getText().equals(v.getContext().getString(R.string.view_leaderboard))) {
            getNavigator().startLeaderboardActivity();
        }
    }

    public void onShareClicked(View view) {
        String competitionUrl = "https://www.rheotv.com/competition/" + result.getId();
        AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext()).sendClick("competition share");
        String text = "Hey Mate! Did you hear about the gaming tournament that Rheo TV is hosting.\nCheck this out. \n" + competitionUrl;
        new ShareTaskHelper().share(view.getContext(), text, ShareTaskHelper.ShareTarget.Others);
    }
}
