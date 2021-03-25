package com.rheotv.android.ui.activities.tabcontainer.posts.multiviewitem.seemore;


import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.BR;
import com.rheotv.android.R;
import com.rheotv.android.databinding.SeeMoreFragmentBinding;
import com.rheotv.android.ui.base.BaseFragment;

import javax.inject.Inject;

public class SeeMoreFragment extends BaseFragment<SeeMoreFragmentBinding, SeeMoreViewModel> {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private SeeMoreViewModel seeMoreViewModel;

    public static SeeMoreFragment newInstance() {
        return new SeeMoreFragment();
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.see_more_fragment;
    }

    @Override
    public SeeMoreViewModel getViewModel() {
        return seeMoreViewModel;
    }
}
