package com.rheotv.android.ui.activities.scoreboard;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.score.ScoreboardResponse;
import com.rheotv.android.databinding.FragmentScoreBinding;
import com.rheotv.android.ui.activities.gamify.RewardsTabAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;

import javax.inject.Inject;

public class ScoreFragment extends BaseFragment<FragmentScoreBinding, ScoreViewModel> {

    public static final String TAG = "ScoreActivity";
    private FragmentScoreBinding mBinding;

    @Inject
    ScoreViewModel mViewModel;
    private View.OnClickListener mCloseClickListener;

    public static ScoreFragment newInstance(String source, String postId, ScoreboardResponse scoreboardResponse, View.OnClickListener listener) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        bundle.putString(AppConstants.KEY_POST_ID, postId);
        bundle.putParcelable(AppConstants.ARG_SCORECARD_TEAMS, scoreboardResponse);
        ScoreFragment fragment = new ScoreFragment();
        fragment.mCloseClickListener = listener;
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ScoreFragment newInstance(Bundle bundle, View.OnClickListener listener) {
        ScoreFragment fragment = new ScoreFragment();
        fragment.mCloseClickListener = listener;
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_score;
    }

    @Override
    public ScoreViewModel getViewModel() {
        return mViewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        setUpViews();
    }

    private void setUpViews() {
        RewardsTabAdapter tabAdapter = new RewardsTabAdapter(getChildFragmentManager());
        tabAdapter.addFragment(ScoreboardFragment.newInstance(
                getArguments().getString(AppConstants.SCREEN_SOURCE, ""),
                getArguments().getString(AppConstants.KEY_POST_ID),
                getArguments().getParcelable(AppConstants.ARG_SCORECARD_TEAMS)),
                getString(R.string.scoreboard));
        mBinding.viewpager.setAdapter(tabAdapter);
        mBinding.tabs.setupWithViewPager(mBinding.viewpager);
        if (mCloseClickListener == null) {
            mBinding.closeButton.setVisibility(View.GONE);
        } else {
            mBinding.closeButton.setOnClickListener(v -> {
                mCloseClickListener.onClick(v);
            });
        }
    }

    public void updateView(ScoreboardResponse scoreboardResponse) {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(ScoreboardFragment.TAG);
        if (fragment instanceof ScoreboardFragment) {
            ((ScoreboardFragment) fragment).updateView(scoreboardResponse);
        }
    }
}
