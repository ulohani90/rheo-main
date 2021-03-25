package com.rheotv.android.ui.activities.scoreboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.score.ScoreboardResponse;
import com.rheotv.android.databinding.ScoreboardFragmentBinding;
import com.rheotv.android.ui.activities.scoreboard.adapter.ScoreboardTeamAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.SearchItemLinearDecorator;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;

import javax.inject.Inject;

import static com.rheotv.android.utils.AppConstants.MSG_SCORE;

public class ScoreboardFragment extends BaseFragment<ScoreboardFragmentBinding, ScoreboardViewModel> {

    public static final String TAG = "ScoreboardFragment";
    private ScoreboardViewModel mViewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    ScoreboardTeamAdapter teamAdapter;

    ScoreboardFragmentBinding mBinding;

    public static ScoreboardFragment newInstance(String source, String postId, ScoreboardResponse score) {
        ScoreboardFragment fragment = new ScoreboardFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.KEY_POST_ID, postId);
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        bundle.putParcelable(AppConstants.ARG_SCORECARD_TEAMS, score);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.scoreboard_fragment;
    }

    @Override
    public ScoreboardViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(ScoreboardViewModel.class);
        mViewModel.list.observe(this, teams -> teamAdapter.addItems(teams));
        mViewModel.unit.observe(this, unit -> teamAdapter.setScoreUnit(unit));
        return mViewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        setupViews();
    }

    private void setupViews() {
        int spacingInPixels = getContext().getResources().getDimensionPixelSize(R.dimen.margin_20);
        mBinding.rvScoreboard.addItemDecoration(new SearchItemLinearDecorator(spacingInPixels, 2));
        mBinding.rvScoreboard.setAdapter(teamAdapter);

        if (getArguments() != null && getArguments().containsKey(AppConstants.ARG_SCORECARD_TEAMS))
            mViewModel.list.setValue(((ScoreboardResponse) getArguments().getParcelable(AppConstants.ARG_SCORECARD_TEAMS)).getTeamsList());

        HashMap<String, Object> properties = new HashMap<>();
        properties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_SCOREBOARD);
        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            properties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_SCOREBOARD, properties);
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(chatBoardCast, new IntentFilter(MSG_SCORE));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(chatBoardCast);
    }

    private BroadcastReceiver chatBoardCast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra(AppConstants.ARG_SCORECARD_TEAMS)) {
                ScoreboardResponse response = intent.getParcelableExtra(AppConstants.ARG_SCORECARD_TEAMS);
                mViewModel.list.setValue(response.getTeamsList());
                mViewModel.unit.setValue(response.getScoreUnit());
            }
        }
    };

    void updateView(ScoreboardResponse scoreboardResponse) {
        mViewModel.list.setValue(scoreboardResponse.getTeamsList());
        mViewModel.unit.setValue(scoreboardResponse.getScoreUnit());
    }
}
