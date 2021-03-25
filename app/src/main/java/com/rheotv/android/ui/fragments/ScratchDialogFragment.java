package com.rheotv.android.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.gamify.Reward;
import com.rheotv.android.databinding.ScratchDailogFragmentBinding;
import com.rheotv.android.ui.base.BaseDialog;
import com.rheotv.android.ui.customViews.ScratchView;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;
import java.util.Objects;

public class ScratchDialogFragment extends BaseDialog {
    private ScratchDailogFragmentBinding binding;
    private ScratchCardNavigator listener;
    private String[] suggestedReward = new String[]{};
    private boolean clickableRoot;
    private AnimatorSet animatorSetForward;
    private AnimatorSet animatorSetBackward;
    private final String ARG_SUGGESTION = "suggested_reward";
    private HashMap<String, Object> baseProperties = new HashMap<>();
    private int mScratchCardImage = R.drawable.ic_scratch_card_1;

    public static ScratchDialogFragment getInstance(String source, Reward reward, int cardImage, String... suggestedReward) {
        ScratchDialogFragment fragment = new ScratchDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArray("suggestedReward", suggestedReward);
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        bundle.putInt(AppConstants.ARG_SCRATCH_CARD_IMAGE, cardImage);
        if (reward != null) {
            bundle.putParcelable(AppConstants.ARG_REWARD, reward);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ScratchDialogFragment getInstance(String source, Reward reward, int cardImage) {
        ScratchDialogFragment fragment = new ScratchDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        bundle.putParcelable(AppConstants.ARG_REWARD, reward);
        bundle.putInt(AppConstants.ARG_SCRATCH_CARD_IMAGE, cardImage);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray(ARG_SUGGESTION, suggestedReward);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            suggestedReward = savedInstanceState.getStringArray(ARG_SUGGESTION);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDialog().setCanceledOnTouchOutside(false);
        setCancelable(false);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;
        binding = DataBindingUtil.inflate(inflater, R.layout.scratch_dailog_fragment, container, false);
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        setUpViews();
        return binding.getRoot();
    }

    private void setUpViews() {
        Reward available = RewardManager.getInstance().getAvailableReward(suggestedReward);
        if (getArguments() != null) {
            if (getArguments().containsKey(AppConstants.SCREEN_NAME)) {
                baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
            }
            if (getArguments().containsKey(AppConstants.ARG_REWARD)) {
                available = getArguments().getParcelable(AppConstants.ARG_REWARD);
            }
            if (getArguments().containsKey(AppConstants.ARG_SCRATCH_CARD_IMAGE)) {
                mScratchCardImage = getArguments().getInt(AppConstants.ARG_SCRATCH_CARD_IMAGE);
            }
        }
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_SCRATCH_CARD);
        if (available != null) {
            baseProperties.put("reward", Objects.requireNonNull(available).getRewardTitle());
            baseProperties.put("coins", available.getCoinWon());
        }
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_SCRATCH_CARD, baseProperties);

        binding.setAvailableReward(available);
        if (available != null) {
            binding.rewardSubtitleTextView.setText(Html.fromHtml(available.getRewardSubtitle()));
            binding.milestoneTextView.setText(Html.fromHtml("Next Milestone<br />" + available.getNextMilestone()));
        }

        binding.scratchContainerLayout.setOnClickListener((View) -> {
            if (clickableRoot) {
                dismissAllowingStateLoss();
            }
        });

        binding.scratchView.setOverlayImage(mScratchCardImage);
        Reward finalAvailable = available;
        binding.scratchView.setRevealListener(new ScratchView.IRevealListener() {
            @Override
            public void onRevealed(ScratchView scratchView) {
                if (finalAvailable != null && listener != null)
                    listener.onScratchRevealed(finalAvailable.getId());
                binding.rewardSubtitleTextView.setVisibility(View.VISIBLE);
                binding.milestoneTextView.setVisibility(View.VISIBLE);
                setCancelable(true);
                RewardManager.getInstance().setRecentlyRewarded(true);
            }

            @Override
            public void onRevealPercentChangedListener(ScratchView scratchView, float percent) {
                if (percent >= 0.28) {
                    Log.d("Reveal Percentage", "onRevealPercentChangedListener: " + percent);
                    clickableRoot = true;
                    fadeScratchView(scratchView);

                    SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_CARD_SCRATCHED, baseProperties);
                }
            }

            @Override
            public void onTouchStart(ScratchView scratchView) {

            }
        });

        binding.closeButton.setOnClickListener((View) -> dismiss());
    }

    private void fadeScratchView(ScratchView scratchView) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(scratchView, View.ALPHA, 1, 0);
        animator.setDuration(500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                scratchView.reveal();
                scratchView.setVisibility(View.GONE);
                binding.setRevealMilestone(true);
                binding.notifyChange();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }

    public void show(FragmentManager fragmentManager, String tag, ScratchCardNavigator listener, String... suggestedReward) {
        this.listener = listener;
        this.suggestedReward = suggestedReward;
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
