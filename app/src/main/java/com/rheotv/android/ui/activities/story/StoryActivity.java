package com.rheotv.android.ui.activities.story;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.story.StoryResult;
import com.rheotv.android.databinding.ActivityStoryBinding;
import com.rheotv.android.factories.ViewModelProviderFactoryV2;
import com.rheotv.android.ui.activities.home.view.HomeActivity;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivity;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

public class StoryActivity extends BaseActivity<ActivityStoryBinding, StoryViewModel> implements HasAndroidInjector {

    public static final String TAG = "StoryActivity";
    @Inject
    DispatchingAndroidInjector<Object> fragmentDispatchingAndroidInjector;

    @Inject
    ViewModelProviderFactoryV2 mViewModelFactory;
    StoryViewModel mViewModel;

    public static final String ARG_AUTHOR_INDEX = "author_index";
    public static final String ARG_AUTHOR_LIST = "author_list";
    public static final String ARG_AUTHOR_ID = "author_id";
    public static final String ARG_STORY_ID = "story_id";
    public static final String ARG_IS_FROM_DEEPLINK = "is_from_deeplink";

    private ActivityStoryBinding mBinding;
    private StoryResult mSingleAuthor;

    private HashMap<String, Object> baseProperties = new HashMap<>();

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_story;
    }

    @Override
    public StoryViewModel getViewModel() {
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(StoryViewModel.class);
        if (getIntent() != null) {
            mViewModel.index = getIntent().getIntExtra(ARG_AUTHOR_INDEX, 0);

            if (getIntent().hasExtra(ARG_AUTHOR_LIST))
                mViewModel.profiles = getIntent().getParcelableArrayListExtra(ARG_AUTHOR_LIST);
            else
                mViewModel.profiles = new ArrayList<>();

            if (getIntent().hasExtra(ARG_STORY_ID))
                mViewModel.storyId = getIntent().getStringExtra(ARG_STORY_ID);

            if (getIntent().hasExtra(ARG_IS_FROM_DEEPLINK))
                mViewModel.isFromDeeplink = true;

            if (getIntent().hasExtra(AppConstants.ARG_NEXT_AUTHOR_URL))
                mViewModel.nextStoryAuthorUrl = getIntent().getStringExtra(AppConstants.ARG_NEXT_AUTHOR_URL);

            if (getIntent().hasExtra(StoryActivity.ARG_AUTHOR_ID))
                mViewModel.singleAuthorId = getIntent().getStringExtra(StoryActivity.ARG_AUTHOR_ID);
        }

        if (getIntent() != null && getIntent().hasExtra(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getIntent().getStringExtra(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_VIEW_STORY);
        baseProperties.put("isFromDeepLink", mViewModel.isFromDeeplink);
        SegmentTracker.getInstance(this).recordScreenName(AppConstants.SCREEN_NAME, baseProperties);

        mViewModel.storyAction.observe(this, action -> {
            int currentIndex = mBinding.viewPager.getCurrentItem();
            if (mBinding.viewPager.getAdapter() == null) {
                return;
            }
            if (action == StoryAction.NEXT) {
                if (currentIndex < mBinding.tabLayout.getTabCount() - 1) {
                    mBinding.tabLayout.selectTab(mBinding.tabLayout.getTabAt(currentIndex + 1));
                } else {
                    mViewModel.markCurrentProfileAsViewed();
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(AppConstants.UPDATE_STORY_BROADCAST_FILTER));
                    onFinish();
                }
            } else if (action == StoryAction.PREVIOUS) {
                if (currentIndex > 0) {
                    mBinding.tabLayout.selectTab(mBinding.tabLayout.getTabAt(currentIndex - 1));
                }
            }
        });

        mViewModel.stories.observe(this, item -> {
            if (item == null || item.getUserProfile() == null) return;
            if (mSingleAuthor == null && mViewModel.profiles != null && mViewModel.profiles.isEmpty()) {
                mSingleAuthor = item;
                mViewModel.profiles.add(mSingleAuthor.getUserProfile());
                setupViewPager();
            }
        });

        mViewModel.newStories.observe(this, list -> {
            if (list == null || list.isEmpty()) return;
            if (mBinding.viewPager.getAdapter() instanceof StoryViewPagerAdapter) {
                StoryViewPagerAdapter storyViewPagerAdapter = (StoryViewPagerAdapter) mBinding.viewPager.getAdapter();
                storyViewPagerAdapter.addListItem(list);
            }
        });

        return mViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = getViewDataBinding();
        if (mViewModel.profiles != null && !mViewModel.profiles.isEmpty()) {
            setupViewPager();
        } else {
            mViewModel.loadStories(mViewModel.singleAuthorId);
        }
    }

    @Override
    public void onBackPressed() {
        if (mViewModel.isFromDeeplink) {
            onFinish();
            return;
        }
        super.onBackPressed();

    }

    private void setupViewPager() {
        StoryViewPagerAdapter adapter = new StoryViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), mViewModel.profiles);
        mBinding.viewPager.setAdapter(adapter);
        mBinding.viewPager.setPageTransformer(new CubeOutViewPagerTransformer());
        new TabLayoutMediator(mBinding.tabLayout,
                mBinding.viewPager,
                (tab, position) -> {
                })
                .attach();
        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                onTabSelectionChange(position, true);
                if (position == mBinding.tabLayout.getTabCount() - 5) {
                    mViewModel.loadStoryAuthor();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                onTabSelectionChange(tab.getPosition(), false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelectionChange(tab.getPosition(), true);
            }

            private void onTabSelectionChange(int position, boolean selected) {
                if (mBinding.viewPager.getAdapter() instanceof StoryViewPagerAdapter) {
                    StoryViewPagerAdapter storyViewPagerAdapter = (StoryViewPagerAdapter) mBinding.viewPager.getAdapter();
                    if (position < storyViewPagerAdapter.getItemCount()) {
                        StoryPagerFragment pagerFragment = storyViewPagerAdapter.getItem(position);
                        if (pagerFragment != null) {
                            pagerFragment.onFragmentSelected(selected);
                        }
                    }
                }
            }
        });
        mBinding.viewPager.post(() -> {
            mBinding.tabLayout.selectTab(mBinding.tabLayout.getTabAt(mViewModel.index));
        });
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    public void onFinish() {
        if (mViewModel.isFromDeeplink) {
            Intent intent = TabContainerActivity.newIntent(this);
            Bundle bundle = new Bundle();
            List<Integer> list = new ArrayList<>();
            list.add(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            list.add(Intent.FLAG_ACTIVITY_NEW_TASK);
            list.add(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            bundle.putString(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_VIEW_STORY);
//            startActivity(intent);
            HomeActivity.Companion.startActivity(this, bundle, list);
        }
        super.finish();
    }
}
