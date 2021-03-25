package com.rheotv.android.ui.activities.story;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.databinding.LayoutStoryViewerBinding;
import com.rheotv.android.ui.activities.follower.FollowAdapter;
import com.rheotv.android.ui.activities.leaderboard.FollowListenerCallback;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.base.BaseBottomSheetDialogFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.recyclerdecorators.LinearItemDecoration;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import static com.rheotv.android.utils.segmentTracker.SegmentConstants.SCREEN_STORY_VIEWER;

public class StoryViewerBottomDialog extends BaseBottomSheetDialogFragment<LayoutStoryViewerBinding, StoryViewerViewModel> implements FollowAdapter.FollowAdapterItemListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    StoryViewerViewModel mViewModel;
    LayoutStoryViewerBinding mBinding;
    FollowAdapter mAdapter;

    private HashMap<String, Object> baseProperties = new HashMap<>();
    private boolean isLoading;
    private boolean initialLoad = true;

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_story_viewer;
    }

    @Override
    public StoryViewerViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(StoryViewerViewModel.class);
        mViewModel.authorList.observe(this, list -> {
            if (list.size() > 0) {
                isLoading = false;
                mAdapter.addItems(list);
            }

            mAdapter.setShowLoadingView(false);
            new Handler().postDelayed(() -> adjustWindow(mBinding.getRoot()), 5000);
        });
        return mViewModel;
    }

    public static StoryViewerBottomDialog getInstance(String source, String storyId, String watchCount) {
        StoryViewerBottomDialog fragmentBottomDialog = new StoryViewerBottomDialog();
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        bundle.putString(AppConstants.ARG_STORY_ID, storyId);
        bundle.putString(AppConstants.ARG_WATCH_COUNT, watchCount);
        fragmentBottomDialog.setArguments(bundle);
        return fragmentBottomDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            mViewModel.watchCount.set(getArguments().getString(AppConstants.ARG_WATCH_COUNT));
            mViewModel.storyId = getArguments().getString(AppConstants.ARG_STORY_ID);
            mViewModel.sourceScreen = getArguments().getString(AppConstants.SCREEN_SOURCE);
        }
        adjustWindow(view);
        mBinding = getViewDataBinding();
        mBinding.setViewModel(mViewModel);
        setupViews();
        mViewModel.loadViewers(true);
    }

    private void setupViews() {
        mAdapter = new FollowAdapter();
        mAdapter.setListener(this);
        int spacingInPixels = getBaseActivity().getResources().getDimensionPixelSize(R.dimen.margin_8);
        mBinding.recyclerView.addItemDecoration(new LinearItemDecoration(spacingInPixels));
        LinearLayoutManager layoutManager = (LinearLayoutManager) mBinding.recyclerView.getLayoutManager();
        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                // Load more if we have reach the end to the recyclerView
                if (!isLoading && mViewModel.nextUrl != null && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    Log.i("Next url ", mViewModel.nextUrl);
                    isLoading = true;
                    mAdapter.setShowLoadingView(true);
                    mViewModel.loadViewers(false);
                }
            }
        });
    }

    @Override
    public void onItemClick(String id) {
        if (getActivity() != null) {
            Intent intent = ProfileActivity.getCallingIntent(getContext());
            intent.putExtra("author_name", id);
            intent.putExtra(AppConstants.SCREEN_SOURCE, SCREEN_STORY_VIEWER);
            startActivity(intent);
        }
    }

    @Override
    public void onFollowClick(boolean isFollow, String author, String profileId, FollowListenerCallback callback) {
        baseProperties.put("story_id", mViewModel.storyId);
        baseProperties.put("followAction", !isFollow);
        baseProperties.put("from", SCREEN_STORY_VIEWER);
        baseProperties.put("profileId", profileId);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_SCREEN_FOLLOW_CLICK, baseProperties);
        Map<String, Object> map = new HashMap<>();
        map.put("is_first", CommonUtils.isFirstTimeFollow());
        map.put("author", author);
        map.put("source", SCREEN_STORY_VIEWER);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, map);
        CommonUtils.setFirstTimeFollow();
        mViewModel.followUnFollow(isFollow, profileId, callback);
    }
}
