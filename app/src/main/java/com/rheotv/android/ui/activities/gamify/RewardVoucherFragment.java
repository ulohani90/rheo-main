package com.rheotv.android.ui.activities.gamify;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.R;
import com.rheotv.android.databinding.RewardVoucherFragmentBinding;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.fragments.ScratchDialogFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.recyclerdecorators.GridItemDecoration;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;

import javax.inject.Inject;

public class RewardVoucherFragment extends BaseFragment<RewardVoucherFragmentBinding, RewardVoucherViewModel> {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    RewardVoucherAdapter mAdapter;

    private RewardVoucherViewModel mViewModel;
    private ScratchCardRevealedListener mScratchCardRevealedListener;
    private HashMap<String, Object> baseProperties = new HashMap<>();
    private String source = SegmentConstants.SCREEN_NAME_REWARD_DETAILS;

    public static RewardVoucherFragment newInstance(String source, ScratchCardRevealedListener listener) {
        RewardVoucherFragment fragment = new RewardVoucherFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        fragment.mScratchCardRevealedListener = listener;
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.reward_voucher_fragment;
    }

    @Override
    public RewardVoucherViewModel getViewModel() {
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(RewardVoucherViewModel.class);
        return mViewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(AppConstants.SCREEN_SOURCE))
                source = getArguments().getString(AppConstants.SCREEN_SOURCE);
        }
        baseProperties.put(AppConstants.SCREEN_SOURCE, source);
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_REDEEM_VOUCHER);
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_REDEEM_VOUCHER, baseProperties);
        setupViews();
    }

    private void setupViews() {
        RewardVoucherFragmentBinding binding = getViewDataBinding();
        if (binding != null) {
            binding.voucherRecyclerView.addItemDecoration(new GridItemDecoration((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics()), 3));
            mAdapter.setListener((card, cardImage) -> {
                HashMap<String, Object> properties = new HashMap<>(baseProperties);
                properties.put("username", CommonUtils.getUserName(getContext()));
                properties.put("milestone_type", card.getRewardType());
                properties.put("earn_source", card.getRewardTitle());
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_REDEEM_SCRATCH_CARD_CLICK, properties);

                ScratchDialogFragment scratchDialogFragment = ScratchDialogFragment.getInstance(SegmentConstants.SCREEN_REDEEM_VOUCHER, card, cardImage);
                scratchDialogFragment.show(this.getChildFragmentManager(), AppConstants.SCRATCH_FRAGMENT_TAG, rewardId -> {
                    mViewModel.updateScratchCard(rewardId);
                }, card.getRewardType());
            });
            binding.voucherRecyclerView.setAdapter(mAdapter);
        }
        if (mViewModel != null) {
            mViewModel.loadAvailableScratchCard();
            mViewModel.getAvailableScratchCardList().observe(getViewLifecycleOwner(), list -> {
                if (binding != null) {
                    binding.progressBar.setVisibility(View.GONE);
                    if (list.isEmpty()) {
                        binding.voucherRecyclerView.setVisibility(View.GONE);
                        binding.emptyListMessage.setVisibility(View.VISIBLE);
                    }
                }
                mAdapter.submitList(list);
                if (mScratchCardRevealedListener != null) {
                    mScratchCardRevealedListener.onScratchCardRevealed(list.size());
                }
            });
        }
    }

    interface ScratchCardRevealedListener {
        void onScratchCardRevealed(int voucherCount);
    }
}
