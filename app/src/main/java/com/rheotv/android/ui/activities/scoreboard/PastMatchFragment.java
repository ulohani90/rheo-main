package com.rheotv.android.ui.activities.scoreboard;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.rheotv.android.R;
import com.rheotv.android.databinding.PastMatchFragmentBinding;
import com.rheotv.android.ui.activities.scoreboard.adapter.PastMatchAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.SearchItemLinearDecorator;

import javax.inject.Inject;

public class PastMatchFragment extends BaseFragment<PastMatchFragmentBinding, PastMatchViewModel> {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    PastMatchAdapter matchAdapter;

    private PastMatchViewModel mViewModel;

    private PastMatchFragmentBinding mBinding;

    public static PastMatchFragment newInstance() {
        return new PastMatchFragment();
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.past_match_fragment;
    }

    @Override
    public PastMatchViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(PastMatchViewModel.class);
        mViewModel.loadPastMatches();
        mViewModel.list.observe(this, matches -> matchAdapter.addItems(matches));
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
        mBinding.rvPastMatch.addItemDecoration(new SearchItemLinearDecorator(spacingInPixels, 2));
        mBinding.rvPastMatch.setAdapter(matchAdapter);
    }

}
