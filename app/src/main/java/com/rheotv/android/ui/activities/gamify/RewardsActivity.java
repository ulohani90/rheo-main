package com.rheotv.android.ui.activities.gamify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.rheotv.android.BR;
import com.rheotv.android.R;
import com.rheotv.android.databinding.ActivityRewardsBinding;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivity;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.ui.customViews.WebviewActivity;
import com.rheotv.android.ui.fragments.WebViewFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

public class RewardsActivity extends BaseActivity<ActivityRewardsBinding, RewardsViewModel> implements RewardsNavigator, HasAndroidInjector {
    @Inject
    DispatchingAndroidInjector<Object> fragmentDispatchingAndroidInjector;

    @Inject
    RewardsViewModel mRewardsViewModel;

    private ActivityRewardsBinding mActivityRewardsBinding;

    @Override
    public AndroidInjector<Object> androidInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_rewards;
    }

    @Override
    public RewardsViewModel getViewModel() {
        return mRewardsViewModel;
    }

    private HashMap<String, Object> baseProperties = new HashMap<>();
    private String source = SegmentConstants.SCREEN_NAME_REWARD_DETAILS;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setupViews();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRewardsViewModel.setNavigator(this);
        mActivityRewardsBinding = getViewDataBinding();
        if (getIntent() != null && getIntent().hasExtra(AppConstants.SCREEN_SOURCE)) {
            source = getIntent().getStringExtra(AppConstants.SCREEN_SOURCE);
            baseProperties.put(AppConstants.SCREEN_SOURCE, source);
        }
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_REWARD_DETAILS);

        SegmentTracker.getInstance(this).recordScreenName(SegmentConstants.SCREEN_NAME_REWARD_DETAILS, baseProperties);
        setSupportActionBar(mActivityRewardsBinding.toolbar);
        setupViews();
        mRewardsViewModel.loadRewardsPage();
    }

    private void setupViews() {
        mRewardsViewModel.totalCoins.set(RewardManager.getInstance().getTotalCoins());
        mActivityRewardsBinding.setViewModel(mRewardsViewModel);

        mActivityRewardsBinding.toolbar.setNavigationOnClickListener((View) -> onBackPressed());
    }


    @Override
    public void onBackPressed() {
        if (getIntent() != null && getIntent().hasExtra("from") && "share".equalsIgnoreCase(getIntent().getStringExtra("from")))
            startTabContainerActivity();
        else
            super.onBackPressed();
    }

    private void startTabContainerActivity() {
        Intent intent = TabContainerActivity.newIntent(this);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_REWARD_DETAILS);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            mRewardsViewModel.trackRuleInfo(this);
            Intent intent = new Intent(RewardsActivity.this, WebviewActivity.class);
            intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_REWARD_DETAILS);
            intent.putExtra("URL", "https://www.rheotv.com/reward-rules/");
            startActivity(intent);
        }
        return true;
    }

    public static void startMe(Context context, String sourceName) {
        Intent intent = new Intent(context, RewardsActivity.class);
        intent.putExtra(AppConstants.SCREEN_SOURCE, sourceName);
        context.startActivity(intent);
    }

    @Override
    public void loadRewardsWebviewSuccess(String pageTitle, String url) {
        setUpAdapter(pageTitle, url);
    }

    @Override
    public void loadRewardsWebviewFailure() {
        setUpAdapter(null, null);
    }


    public void setUpAdapter(String pageTitle, String url) {
        try {
            boolean codaEnable = RewardManager.getInstance().isCodaEnabled();
            RewardsTabAdapter tabAdapter = new RewardsTabAdapter(getSupportFragmentManager());

            if (pageTitle != null && !pageTitle.isEmpty() && url != null && !url.isEmpty()) {
                tabAdapter.addFragment(WebViewFragment.newInstance(url), pageTitle);
            }

            //tabAdapter.addFragment(RecentlyRedeemedFragment.newInstance(source), getString(R.string.recent_redeems));
            if (codaEnable) {
                tabAdapter.addFragment(RewardVoucherFragment.newInstance(source, (voucherCount) -> {
                    mRewardsViewModel.totalCoins.set(RewardManager.getInstance().getTotalCoins());
                    mActivityRewardsBinding.setViewModel(mRewardsViewModel);
                    for (int index = 0; index < tabAdapter.getCount(); ++index) {
                        if (tabAdapter.getItem(index) instanceof RewardVoucherFragment) {
                            TabLayout.Tab tab = mActivityRewardsBinding.tabs.getTabAt(index);
                            if (tab != null) {
                                tab.setText(getString(R.string.voucher, "" + voucherCount).trim());
                            }
                        }
                    }
                }), getString(R.string.voucher, "").trim());
                //tabAdapter.addFragment(RewardPagerFragment.newInstance(source), getString(R.string.redeem));
            } else {
                mActivityRewardsBinding.tabs.setVisibility(View.GONE);
                mActivityRewardsBinding.dividerView.setVisibility(View.GONE);
            }
            tabAdapter.addFragment(RewardHistoryFragment.newInstance(source), getString(R.string.history));

            mActivityRewardsBinding.viewpager.setAdapter(tabAdapter);
            mActivityRewardsBinding.tabs.setupWithViewPager(mActivityRewardsBinding.viewpager);
            if (codaEnable && getIntent().hasExtra("from") && "share".equalsIgnoreCase(getIntent().getStringExtra("from"))) {
                mActivityRewardsBinding.tabs.getTabAt(2).select();
                mActivityRewardsBinding.viewpager.setCurrentItem(2);
            }
        } catch (IllegalStateException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
    }


}
