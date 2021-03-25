package com.rheotv.android.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.rheotv.android.R;
import com.rheotv.android.databinding.FragmentRecentlyRedeemedLayoutBinding;
import com.rheotv.android.ui.adapters.RecentlyRedeemedListAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.recyclerdecorators.LinearItemDecoration;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;

import javax.inject.Inject;

public class RecentlyRedeemedFragment extends BaseFragment<FragmentRecentlyRedeemedLayoutBinding, RecentlyRedeemedFragmentViewModel> implements RecentlyRedeemedNavigator {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    RecentlyRedeemedListAdapter adapter;

    RecentlyRedeemedFragmentViewModel mViewModel;

    FragmentRecentlyRedeemedLayoutBinding mBinding;


    private HashMap<String, Object> baseProperties = new HashMap<>();


    public static RecentlyRedeemedFragment newInstance(String source) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        RecentlyRedeemedFragment fragment = new RecentlyRedeemedFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_recently_redeemed_layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        mBinding.setViewModel(mViewModel);

        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_RECENTLY_REDEEMED);
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_RECENTLY_REDEEMED, baseProperties);

        setupViews();
    }

    private void setupViews() {
        int spacingInPixels = mBinding.recentlyRedeemedRecyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.margin_12);
        mBinding.recentlyRedeemedRecyclerView.addItemDecoration(new LinearItemDecoration(spacingInPixels));
        mBinding.recentlyRedeemedRecyclerView.setAdapter(adapter);
        mViewModel.results.observe(getViewLifecycleOwner(), recentlyRedeemedObjects -> {
            adapter.setRecentlyRedeemedObjects(recentlyRedeemedObjects);
        });
        mViewModel.loadRecentlyRedeemedList();
    }


    @Override
    public RecentlyRedeemedFragmentViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(RecentlyRedeemedFragmentViewModel.class);
        mViewModel.setNavigator(this);
        return mViewModel;
    }

    @Override
    public void onErrorReceived(String message) {

    }
}
