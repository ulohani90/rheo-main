package com.rheotv.android.ui.activities.follower;

import android.os.Bundle;
import android.view.View;

import com.rheotv.android.R;
import com.rheotv.android.databinding.ActivityFollowerBinding;
import com.rheotv.android.ui.activities.gamify.RewardsTabAdapter;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.utils.AppConstants;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

public class FollowActivity extends BaseActivity<ActivityFollowerBinding, FollowViewModel> implements HasAndroidInjector {

    @Inject
    DispatchingAndroidInjector<Object> fragmentDispatchingAndroidInjector;

    @Inject
    FollowViewModel mRewardsViewModel;

    private ActivityFollowerBinding mBinding;

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_follower;
    }

    @Override
    public FollowViewModel getViewModel() {
        return mRewardsViewModel;
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = getViewDataBinding();
        setupViews();
    }

    private void setupViews() {
        boolean isFollowScreen = getIntent().getBooleanExtra(AppConstants.ARG_IS_FOLLOW_SCREEN, false);

        String username = getIntent().getStringExtra(AppConstants.ARG_USERNAME);
        String screenSource = getIntent().getStringExtra(AppConstants.SCREEN_SOURCE);

        RewardsTabAdapter tabAdapter = new RewardsTabAdapter(getSupportFragmentManager());
        if (isFollowScreen) {
            tabAdapter.addFragment(FollowerFragment.newInstance(username, AppConstants.TYPE_FOLLOWER, screenSource), getString(R.string.followers));
            tabAdapter.addFragment(FollowerFragment.newInstance(username, AppConstants.TYPE_FOLLOWING, screenSource), getString(R.string.following));
        } else {
            tabAdapter.addFragment(FollowerFragment.newInstance(username, AppConstants.TYPE_PROFILE_VIEWERS, screenSource), "Recent Viewers");
        }

        mBinding.viewpager.setAdapter(tabAdapter);
        if (isFollowScreen) {
            mBinding.tabs.setupWithViewPager(mBinding.viewpager);
            mBinding.tabs.setVisibility(View.VISIBLE);
            mBinding.toolbar.setTitle("");
            mBinding.dividerView.setVisibility(View.VISIBLE);
        } else {
            mBinding.toolbar.setTitle("Recent Viewers");
            mBinding.tabs.setVisibility(View.GONE);
            mBinding.dividerView.setVisibility(View.GONE);
        }


        mBinding.toolbar.setNavigationOnClickListener((View) -> onBackPressed());


    }


}
