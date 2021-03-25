package com.rheotv.android.ui.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.gamify.Reward;
import com.rheotv.android.databinding.FragmentRewardProgressDialogBinding;
import com.rheotv.android.ui.activities.gamify.RewardsActivity;
import com.rheotv.android.ui.base.BaseDialog;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import java.util.HashMap;
import java.util.Objects;

public class RewardProgressDialogFragment extends BottomSheetDialogFragment {
    private FragmentRewardProgressDialogBinding binding;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static RewardProgressDialogFragment getInstance(String source) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        RewardProgressDialogFragment fragment = new RewardProgressDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reward_progress_dialog, container, false);
        binding.cancelButton.setOnClickListener(v -> dismiss());
        Reward reward = RewardManager.getInstance().getUserStreakReward();
        binding.setReward(reward);
        if (reward != null && reward.getDescription() != null)
            binding.streakSubtitleTextView.setText(Html.fromHtml(reward.getDescription()));

        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_LOGIN_STREAK);
        if (reward != null) {
            baseProperties.put("currentStreak", Objects.requireNonNull(reward).getDaysCompleted());
            baseProperties.put("totalDays", reward.getTotalDays());
        }
        binding.viewDetailsButton.setOnClickListener(v -> {
            RewardsActivity.startMe(binding.getRoot().getContext(), getArguments().getString(AppConstants.SCREEN_SOURCE));
            if (!isStateSaved())
                dismiss();
        });
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_LOGIN_STREAK, baseProperties);
        return binding.getRoot();
    }

    public void show(FragmentManager fragmentManager, String tag) {
        try {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment prevFragment = fragmentManager.findFragmentByTag(tag);
            if (prevFragment != null) {
                transaction.remove(prevFragment);
            }
            transaction.commitAllowingStateLoss();
            show(transaction, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
