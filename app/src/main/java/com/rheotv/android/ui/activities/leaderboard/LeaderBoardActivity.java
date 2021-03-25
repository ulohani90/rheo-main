package com.rheotv.android.ui.activities.leaderboard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;
import com.rheotv.android.BR;
import com.rheotv.android.R;
import com.rheotv.android.databinding.LayoutLeaderboardContainerBinding;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import java.util.HashMap;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

public class LeaderBoardActivity extends BaseActivity<LayoutLeaderboardContainerBinding, LeaderBoardActivityVM>
        implements HasAndroidInjector, LeaderBoardNavigator {

    @Inject
    DispatchingAndroidInjector<Object> fragmentDispatchingAndroidInjector;
    @Inject
    LeaderBoardActivityVM leaderboardViewModel;

    private LayoutLeaderboardContainerBinding leaderboardContainerBinding;
    private LeaderBoardFragment leaderBoardFragment;
    private static final String SORT_BY_DAY = "daily";
    private static final String SORT_BY_WEEK = "weekly";
    public static final String SORT_BY_MONTH = "monthly";
    private HashMap<String, Object> baseProperties = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        leaderboardViewModel.setNavigator(this);
        leaderboardContainerBinding = getViewDataBinding();
        setupViews();
    }

    private void setupViews() {
        leaderboardContainerBinding.toolbar.setNavigationOnClickListener(view -> finish());
        leaderboardContainerBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int selectedPosition = tab.getPosition();
                switch (selectedPosition) {
                    case 0:
                        refreshContent(SORT_BY_DAY);
                        SegmentTracker.getInstance(LeaderBoardActivity.this).trackEvent(SegmentConstants.EVENT_LEADER_BOARD_TAB_CHANGED, baseProperties);
                        break;

                    case 1:
                        refreshContent(SORT_BY_WEEK);
                        break;

                    case 2:
                        refreshContent(SORT_BY_MONTH);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        String source;
        if (getIntent() != null && getIntent().hasExtra(AppConstants.SCREEN_SOURCE))
            source = getIntent().getStringExtra(AppConstants.SCREEN_SOURCE);
        else
            source = SegmentConstants.SCREEN_NAME_LEADER_BOARD;
        baseProperties.put(AppConstants.SCREEN_SOURCE, source);
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_LEADER_BOARD);

        leaderBoardFragment = LeaderBoardFragment.newInstance(null, SORT_BY_DAY, source);
        loadFragment(leaderBoardFragment, true, "Main");
    }

    private void refreshContent(String filter) {
        if (leaderBoardFragment == null || !leaderBoardFragment.isAdded()) return;
        leaderBoardFragment.onTabChangeCall(filter);
    }

    public void loadFragment(Fragment fragment, boolean shouldReplace, String stackName) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!fragment.isAdded()) {
            try {
                if (shouldReplace) {
                    transaction.replace(R.id.container, fragment);
                } else {
                    // todo - a dirty hack must be removed soon.
                    transaction.add(R.id.container, fragment).addToBackStack(null);
                    transaction.commit();
                    return;
                }
                transaction.commitNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            transaction.show(fragment);
        }
    }

    @Override
    public void handleError(String error) {

    }

    @Override
    public String getGameId() {
        return null;
    }

    @Override
    public void clearLeaderboardItems() {

    }

    @Override
    public void setRefreshing(boolean isRefreshing) {

    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_leaderboard_container;
    }

    @Override
    public LeaderBoardActivityVM getViewModel() {
        return leaderboardViewModel;
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return fragmentDispatchingAndroidInjector;
    }
}
