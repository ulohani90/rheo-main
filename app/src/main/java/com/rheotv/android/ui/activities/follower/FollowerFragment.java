package com.rheotv.android.ui.activities.follower;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.databinding.FragmentFollowerBinding;
import com.rheotv.android.ui.activities.leaderboard.FollowListenerCallback;
import com.rheotv.android.ui.activities.leaderboard.LeaderBoardActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.recyclerdecorators.LinearItemDecoration;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link FollowerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FollowerFragment extends BaseFragment<FragmentFollowerBinding, FollowerViewModel> implements FollowAdapter.FollowAdapterItemListener, RecentViewersAdapter.OnItemClickedListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;


    FollowAdapter mAdapter;


    RecentViewersAdapter recentViewersAdapter;
    private static final String ARG_TYPE = "type";

    private FragmentFollowerBinding mBinding;
    private FollowerViewModel mViewModel;
    private boolean isLoading = false;

    private HashMap<String, Object> baseProperties = new HashMap<>();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param username Parameter 1.
     * @param type     Parameter 1.
     * @return A new instance of fragment FollowerFragment.
     */
    public static FollowerFragment newInstance(String username, String type, String screenSource) {
        FollowerFragment fragment = new FollowerFragment();
        Bundle args = new Bundle();
        args.putString(AppConstants.ARG_USERNAME, username);
        args.putString(ARG_TYPE, type);
        args.putString(AppConstants.SCREEN_SOURCE, screenSource);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_follower;
    }

    @Override
    public FollowerViewModel getViewModel() {
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(FollowerViewModel.class);
        mViewModel.authorList.observe(this, list -> {
            if (list.size() > 0) {
                isLoading = false;
                mAdapter.addItems(list);
            }
            Log.i(getClass().getSimpleName(), "FollowerViewModel_nexturl: " + mViewModel.nextUrl);
            mAdapter.setShowLoadingView(false);
        });

        mViewModel.recentViewers.observe(this, recentViewers -> {
            if (recentViewers.size() > 0) {
                isLoading = false;
                recentViewersAdapter.setResults(recentViewers);
            }
        });
        return mViewModel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        if (getArguments() != null) {
            mViewModel.username = getArguments().getString(AppConstants.ARG_USERNAME);
            mViewModel.type = getArguments().getString(ARG_TYPE);
            mViewModel.sourceScreen = getArguments().getString(AppConstants.SCREEN_SOURCE);
            mViewModel.screenName = AppConstants.TYPE_FOLLOWER.equalsIgnoreCase(mViewModel.type) ? SegmentConstants.SCREEN_FOLLOWER : AppConstants.TYPE_PROFILE_VIEWERS.equalsIgnoreCase(mViewModel.type) ? SegmentConstants.SCREEN_PROFILE_VIEWERS : SegmentConstants.SCREEN_FOLLOWING;
        }
        if (mViewModel.type.equalsIgnoreCase(AppConstants.TYPE_PROFILE_VIEWERS)) {
            recentViewersAdapter = new RecentViewersAdapter(this);
            mViewModel.loadRecentViewers(true);
            mBinding.recyclerView.setAdapter(recentViewersAdapter);
        } else {
            mAdapter = new FollowAdapter();
            mViewModel.loadFollowers(true);
            mAdapter.setShouldShowFollowerButton(AppConstants.TYPE_FOLLOWER.equalsIgnoreCase(mViewModel.type));
            mAdapter.setListener(this);
            mBinding.recyclerView.setAdapter(mAdapter);
        }
        recordSegment();


        int spacingInPixels = getBaseActivity().getResources().getDimensionPixelSize(R.dimen.margin_8);
        LinearLayoutManager layoutManager = (LinearLayoutManager) mBinding.recyclerView.getLayoutManager();
        mBinding.recyclerView.addItemDecoration(new LinearItemDecoration(spacingInPixels));

        mBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mAdapter != null && !mViewModel.type.equalsIgnoreCase(AppConstants.TYPE_PROFILE_VIEWERS)) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Load more if we have reach the end to the recyclerView
                    if (!isLoading && mViewModel.nextUrl != null && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        Log.i("Next url ", mViewModel.nextUrl);
                        isLoading = true;

                        mAdapter.setShowLoadingView(true);
                        mViewModel.loadFollowers(false);
                    }
                }
            }
        });

        mBinding.streamerButton.setOnClickListener(v -> {
            Map<String, Object> properties = new HashMap<>(baseProperties);
            properties.put("is_first", CommonUtils.isFirstTimeLeaderBoardClicked());
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_LEADERBOARD_PAGE_CLICKED, properties);
            CommonUtils.setFirstTimeLeaderBoardClicked();
            Intent intent = new Intent(getActivity(), LeaderBoardActivity.class);
            intent.putExtra(AppConstants.SCREEN_SOURCE, mViewModel.sourceScreen);
            startActivity(intent);
        });
    }

    private void recordSegment() {
        baseProperties.put(AppConstants.SCREEN_NAME, mViewModel.screenName);
        baseProperties.put(AppConstants.SCREEN_SOURCE, mViewModel.sourceScreen);
        SegmentTracker.getInstance(getContext()).recordScreenName(mViewModel.screenName, baseProperties);
    }

    @Override
    public void onItemClick(String id) {
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_SCREEN_PROFILE_CLICKED, baseProperties);
        if (getActivity() != null) {
            Intent intent = ProfileActivity.getCallingIntent(getContext());
            intent.putExtra("author_name", id);
            intent.putExtra(AppConstants.SCREEN_SOURCE, mViewModel.screenName);
            startActivity(intent);
        }
    }

    @Override
    public void onFollowClick(boolean isFollow, String author, String profileId, FollowListenerCallback callback) {
        baseProperties.put("userName", mViewModel.username);
        baseProperties.put("followAction", !isFollow);
        baseProperties.put("from", mViewModel.screenName);
        baseProperties.put("profileId", profileId);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_SCREEN_FOLLOW_CLICK, baseProperties);
        Map<String, Object> map = new HashMap<>();
        map.put("is_first", CommonUtils.isFirstTimeFollow());
        map.put("author", author);
        map.put("source", mViewModel.screenName);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, map);
        CommonUtils.setFirstTimeFollow();
        mViewModel.followUnFollow(isFollow, profileId, callback);
    }

    @Override
    public void onUserClicked(String username) {
        Intent intent = ProfileActivity.getCallingIntent(getContext());
        intent.putExtra("author_name", username);
        intent.putExtra(AppConstants.SCREEN_SOURCE, mViewModel.screenName);
        startActivity(intent);
    }
}
