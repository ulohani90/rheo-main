package com.rheotv.android.ui.activities.rank;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.useProfile.responses.StreamerLevel;
import com.rheotv.android.data.network.models.useProfile.responses.StreamerLevelResponseBody;
import com.rheotv.android.databinding.ActivityRankBinding;
import com.rheotv.android.factories.ViewModelProviderFactoryV2;
import com.rheotv.android.ui.adapters.LevelType;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.ui.fragments.LiveStreamingDialogFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

public class RankActivity extends BaseActivity<ActivityRankBinding, RankActivityViewModel> implements
        RankActivityNavigator, HasAndroidInjector {

    @Inject
    DispatchingAndroidInjector<Object> fragmentDispatchingAndroidInjector;

    @Inject
    ViewModelProviderFactoryV2 mViewModelFactory;

    RankActivityViewModel mViewModel;

    private ActivityRankBinding mBinding;

    private int userId;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static void startMe(Fragment fragment, Activity activity, int paymentModel, int userId, LevelType levelType, String source) {
        if (fragment != null) {
            Intent intent = new Intent(fragment.getContext(), RankActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("level_type", levelType);
            intent.putExtra("payment_model", paymentModel);
            intent.putExtra(AppConstants.SCREEN_SOURCE, source);
            fragment.startActivity(intent);
            return;
        }
        Intent intent = new Intent(activity, RankActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_rank;
    }

    @Override
    public RankActivityViewModel getViewModel() {
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(RankActivityViewModel.class);
        return mViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = getViewDataBinding();
        userId = getIntent().getIntExtra("user_id", 0);
        if (getIntent().hasExtra("level_type")) {
            mViewModel.setLevelType(getIntent().getParcelableExtra("level_type"));
        }
        if (getIntent().hasExtra("payment_model")) {
            mViewModel.setPaymentModel(getIntent().getIntExtra("payment_model", mViewModel.getPaymentModel()));
        }

        if (getIntent() != null && getIntent().hasExtra(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getIntent().getStringExtra(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_MY_RANK);
        SegmentTracker.getInstance(this).recordScreenName(SegmentConstants.SCREEN_NAME_MY_RANK, baseProperties);

        setSupportActionBar(mBinding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        setUpViews();
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    private void setUpViews() {
        mViewModel.setNavigator(this);
        mBinding.toolbar.setNavigationOnClickListener((View) -> onBackPressed());
        mBinding.appbar.setVisibility(View.GONE);
        mBinding.loadingView.setVisibility(View.VISIBLE);
        mBinding.viewpager.setVisibility(View.GONE);
        mViewModel.fetchStreamerLevelInfo(userId);
        mBinding.goLiveButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString(AppConstants.SCREEN_SOURCE, ((String) baseProperties.get(AppConstants.SCREEN_SOURCE)));
            LiveStreamingDialogFragment liveStreamingDialogFragment = LiveStreamingDialogFragment.getInstance(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
            liveStreamingDialogFragment.setArguments(args);
            liveStreamingDialogFragment.show(getSupportFragmentManager(), null);
        });
    }

    private int[] medalColors = new int[]{R.color.white_text_color, R.color.white_text_color, R.color.white_text_color};
    private int[] medalDrawable = new int[]{R.drawable.nonselected_item_dot, R.drawable.nonselected_item_dot, R.drawable.nonselected_item_dot};
    private int[] medalSelectedDrawable = new int[]{R.drawable.selected_white_item_dot, R.drawable.selected_white_item_dot, R.drawable.selected_white_item_dot};
    private int currentRankLevel = 0;

    @Override
    public void setStreamerLevelInfo(StreamerLevelResponseBody response) {
        try {
            mBinding.appbar.setVisibility(View.VISIBLE);
            mBinding.loadingView.setVisibility(View.GONE);
            mBinding.goLiveButton.setVisibility(View.VISIBLE);
            mBinding.viewpager.setVisibility(View.VISIBLE);

            RankTabAdapter tabAdapter = new RankTabAdapter(getSupportFragmentManager());
            tabAdapter.addFragment(RankListFragment.newInstance(mViewModel.getPaymentModel(), getDataForStreamerLevel(response, "Bronze"), mViewModel.getRewardDefinition(), SegmentConstants.SCREEN_NAME_MY_RANK), getString(R.string.bronze));
            tabAdapter.addFragment(RankListFragment.newInstance(mViewModel.getPaymentModel(), getDataForStreamerLevel(response, "Silver"), mViewModel.getRewardDefinition(), SegmentConstants.SCREEN_NAME_MY_RANK), getString(R.string.silver));
            tabAdapter.addFragment(RankListFragment.newInstance(mViewModel.getPaymentModel(), getDataForStreamerLevel(response, "Gold"), mViewModel.getRewardDefinition(), SegmentConstants.SCREEN_NAME_MY_RANK), getString(R.string.gold));
            mBinding.viewpager.setAdapter(tabAdapter);
            mBinding.viewpager.setOffscreenPageLimit(3);
            mBinding.tabs.setupWithViewPager(mBinding.viewpager);

            if (response.getData().getCurrentLevel() == null) {
                mBinding.viewpager.setCurrentItem(0);
                currentRankLevel = -1;
            } else if (response.getData().getCurrentLevel().equalsIgnoreCase("bronze")) {
                mBinding.viewpager.setCurrentItem(0);
                currentRankLevel = 0;
                medalDrawable[0] = R.drawable.avd_correct;
                medalSelectedDrawable[0] = R.drawable.avd_correct;

            } else if (response.getData().getCurrentLevel().equalsIgnoreCase("silver")) {
                mBinding.viewpager.setCurrentItem(1);
                medalDrawable[0] = R.drawable.avd_correct;
                medalSelectedDrawable[0] = R.drawable.avd_correct;

                medalDrawable[1] = R.drawable.avd_correct;
                medalSelectedDrawable[1] = R.drawable.avd_correct;
                currentRankLevel = 1;
            } else {
                mBinding.viewpager.setCurrentItem(2);
                currentRankLevel = 2;
                medalDrawable[0] = R.drawable.avd_correct;
                medalDrawable[1] = R.drawable.avd_correct;
                medalDrawable[2] = R.drawable.avd_correct;

                medalSelectedDrawable[0] = R.drawable.avd_correct;
                medalSelectedDrawable[1] = R.drawable.avd_correct;
                medalSelectedDrawable[2] = R.drawable.avd_correct;
            }

            scale(mBinding.medalImageView);
            onTabUpdate(mBinding.viewpager.getCurrentItem());

            mBinding.viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    onTabUpdate(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private StreamerLevel getDataForStreamerLevel(StreamerLevelResponseBody response, String levelName) {
        for (StreamerLevel level : response.getData().getLevelData()) {
            if (level.getLevel().equalsIgnoreCase(levelName)) {
                mViewModel.streamerLevelHashMap.put(levelName, level);
                return level;
            }
        }
        return null;
    }

    @Override
    public void showErrorToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void onTabUpdate(int position) {
        for (int i = 0; i < mBinding.tabs.getTabCount(); i++) {
            if (i != position) {
                mBinding.tabs.setTextColor(i, R.color.color_unselected_rank);
                mBinding.tabs.setTabIndicator(i, medalDrawable[i]);
            } else {
                mBinding.tabs.setTextColor(position, medalColors[i]);
                mBinding.tabs.setTabIndicator(i, medalSelectedDrawable[i]);
            }
        }

        if (mViewModel.getPaymentModel() == 2) {
            StreamerLevel streamerLevel = null;
            if (position == 0) {
                streamerLevel = mViewModel.streamerLevelHashMap.get("Bronze");
            } else if (position == 1) {
                streamerLevel = mViewModel.streamerLevelHashMap.get("Silver");
            } else {
                streamerLevel = mViewModel.streamerLevelHashMap.get("Gold");
            }

            if (streamerLevel != null) {
                mBinding.rankTitleTextView.setText(streamerLevel.getCriteria().get(0).getTitle());
            }
        } else {
            if (position == currentRankLevel) {
                mBinding.rankTitleTextView.setText(getString(R.string.my_current_rank));
                scaleUp(mBinding.rankTitleTextView);
            } else if (position < currentRankLevel) {
                mBinding.rankTitleTextView.setText(getString(R.string.rank_achieved));
                scaleDown(mBinding.rankTitleTextView);
            } else {
                mBinding.rankTitleTextView.setText(getString(R.string.aim_high));
                scaleDown(mBinding.rankTitleTextView);
            }
        }

        switch (position) {
            case 0:
                if (mViewModel.getLevelType() instanceof LevelType.Gold
                        || mViewModel.getLevelType() instanceof LevelType.Silver
                        || mViewModel.getLevelType() instanceof LevelType.Bronze) {
                    mBinding.medalImageView.setImageResource(R.drawable.ic_bronze_selected);
                } else {
                    mBinding.medalImageView.setImageResource(R.drawable.ic_bronze_unselected);
                }
                mBinding.medalTitleTextView.setText("Bronze");
                break;
            case 1:
                if (mViewModel.getLevelType() instanceof LevelType.Gold || mViewModel.getLevelType() instanceof LevelType.Silver) {
                    mBinding.medalImageView.setImageResource(R.drawable.ic_silver_selected);
                } else {
                    mBinding.medalImageView.setImageResource(R.drawable.ic_silver_unselected);
                }
                mBinding.medalTitleTextView.setText("Silver");
                break;
            case 2:
                if (mViewModel.getLevelType() instanceof LevelType.Gold) {
                    mBinding.medalImageView.setImageResource(R.drawable.ic_gold_selected);
                } else {
                    mBinding.medalImageView.setImageResource(R.drawable.ic_gold_unselected);
                }
                mBinding.medalTitleTextView.setText("Gold");
                break;
        }
    }

    private void scaleUp(View view) {
        ObjectAnimator scaleUp = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.5f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.5f));
        scaleUp.setDuration(250);
        scaleUp.start();
    }

    private void scaleDown(View view) {
        ObjectAnimator scaleUp = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1.5f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1.5f, 1f));
        scaleUp.setDuration(250);
        scaleUp.start();
    }

    private void scale(View view) {
        ObjectAnimator scaleUp = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.2f));
        scaleUp.setDuration(250);
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1.2f, 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.2f, 1.0f));
        scaleDown.setDuration(250);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleDown).after(scaleUp);
        animatorSet.start();
    }

}
