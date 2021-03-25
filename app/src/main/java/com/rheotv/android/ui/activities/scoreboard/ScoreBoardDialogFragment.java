package com.rheotv.android.ui.activities.scoreboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.score.ScoreboardResponse;
import com.rheotv.android.databinding.BottomSheetScoreboardBinding;
import com.rheotv.android.utils.AppConstants;

public class ScoreBoardDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ScoreBoardDialogFragment";
    BottomSheetScoreboardBinding mViewBinding;

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    public static ScoreBoardDialogFragment newInstance(String source, String postId, ScoreboardResponse scoreboardResponse) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        bundle.putString(AppConstants.KEY_POST_ID, postId);
        bundle.putParcelable(AppConstants.ARG_SCORECARD_TEAMS, scoreboardResponse);
        ScoreBoardDialogFragment fragment = new ScoreBoardDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = BottomSheetScoreboardBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ScoreFragment scoreFragment = ScoreFragment.newInstance(getArguments(), null);
        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.container, scoreFragment, ScoreFragment.TAG)
                .commit();
    }

    public void updateView(ScoreboardResponse response) {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(ScoreFragment.TAG);
        if (fragment != null) {
            ((ScoreFragment) fragment).updateView(response);
        }
    }
}
