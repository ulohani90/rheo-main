package com.rheotv.android.ui.activities.gamify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.gamify.CodaShopGame;
import com.rheotv.android.databinding.RewardRedeemFragmentBinding;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.recyclerdecorators.LinearItemDecoration;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;

import javax.inject.Inject;

public class RewardRedeemFragment extends BaseFragment<RewardRedeemFragmentBinding, RewardRedeemViewModel> {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    RewardRedeemAdapter adapter;

    private RewardRedeemFragmentBinding mBinding;
    private RewardRedeemViewModel mViewModel;
    public static final String ARG_GAME = "game_detail";
    private HashMap<String, Object> baseProperties = new HashMap<>();
    private String source = SegmentConstants.SCREEN_NAME_REWARD_DETAILS;

    public static RewardRedeemFragment newInstance(String source) {
        RewardRedeemFragment fragment = new RewardRedeemFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.reward_redeem_fragment;
    }

    @Override
    public RewardRedeemViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(RewardRedeemViewModel.class);
        return mViewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();

        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            source = getArguments().getString(AppConstants.SCREEN_SOURCE);
        baseProperties.put(AppConstants.SCREEN_SOURCE, source);
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_REDEEM_LIST);
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_REDEEM_LIST, baseProperties);

        setupViews();
    }

    private void setupViews() {
        adapter.setListener(this::onGameSelected);
        int spacingInPixels = getContext().getResources().getDimensionPixelSize(R.dimen.margin_8);
        mBinding.redeemRecyclerView.setPadding(ViewUtils.dpToPx(8), 0, ViewUtils.dpToPx(8), 0);
        mBinding.redeemRecyclerView.addItemDecoration(new LinearItemDecoration(spacingInPixels));
        mBinding.redeemRecyclerView.setAdapter(adapter);
        mViewModel.loadGames();
        mViewModel.getGameListResult().observe(this, list -> adapter.submitList(list));
    }

    private void onGameSelected(CodaShopGame game) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("username", CommonUtils.getUserName(getContext()));
        properties.put("game", game.getName());
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_REDEEM_GAME_CLICK, properties);

        Intent intent = new Intent(getContext(), RedeemActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_GAME, game);
        intent.putExtras(bundle);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_REDEEM_LIST);
        startActivity(intent);
    }
}
