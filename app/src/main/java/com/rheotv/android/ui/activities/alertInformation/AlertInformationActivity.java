package com.rheotv.android.ui.activities.alertInformation;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.rheotv.android.BR;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.databinding.ActivityAlertInformationBinding;
import com.rheotv.android.ui.activities.leaderboard.LeaderBoardActivity;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivity;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.ListHolder;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class AlertInformationActivity extends BaseActivity<ActivityAlertInformationBinding, AlertInformationViewModel> implements AlertInformationNavigator {

    @Inject
    AlertInformationViewModel alertInformationViewModel;

    ActivityAlertInformationBinding activityAlertInformationBinding;

    boolean isDeepLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityAlertInformationBinding = getViewDataBinding();
        alertInformationViewModel.setNavigator(this);
        Result result = ListHolder.getInstance().getAlertInfoObject();
        alertInformationViewModel.setData(result);
        BindingUtils.setImageUrlUsingCache(activityAlertInformationBinding.bannerIv, result.getBannerImageUrl(), true);
        setPrizes(result.getPrizes());
        setRules(result.getRules());
        if (getIntent() != null && getIntent().hasExtra("is_deep_link"))
            isDeepLink = getIntent().getBooleanExtra("is_deep_link", false);
    }


    private void setPrizes(List<Result.Prize> prizes) {
        activityAlertInformationBinding.prizeValue1.setText(prizes.get(0).getValue());
        activityAlertInformationBinding.prizeValue2.setText(prizes.get(1).getValue());
        activityAlertInformationBinding.prizeValue3.setText(prizes.get(2).getValue());
    }


    public void setRules(List<String> rules) {
        activityAlertInformationBinding.rulesContainer.removeAllViews();
        for (String rule : rules) {
            TextView textView = new TextView(this);
            textView.setText(rule);
            textView.setTextColor(Color.WHITE);
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.raleway_bold));
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            textView.setPadding(0, 0, 0, padding);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            activityAlertInformationBinding.rulesContainer.addView(textView, lp);
        }
    }

    private void setUpPrizeLayout(LinearLayout prizeLayout1, TextView prizeName1, TextView prizeValue1, Result.Prize prize) {
        prizeLayout1.setVisibility(View.VISIBLE);
        prizeName1.setText(prize.getPrizeName());
        prizeValue1.setText(prize.getValue());
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_alert_information;
    }

    @Override
    public AlertInformationViewModel getViewModel() {
        return alertInformationViewModel;
    }


    @Override
    public void closeActivity() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (isDeepLink) {
            startTabContainerActivity();
        }
        super.onBackPressed();
    }

    private void startTabContainerActivity() {
        Intent intent = TabContainerActivity.newIntent(this);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public void startLeaderboardActivity() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("is_first", CommonUtils.isFirstTimeLeaderBoardClicked());
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_LEADERBOARD_PAGE_CLICKED, properties);
        CommonUtils.setFirstTimeLeaderBoardClicked();
        Intent intent = new Intent(AlertInformationActivity.this, LeaderBoardActivity.class);
        startActivity(intent);
    }
}
