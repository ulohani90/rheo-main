package com.rheotv.android.ui.activities.leaderboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.rheotv.android.BR;
import com.rheotv.android.R;
import com.rheotv.android.databinding.LayoutLeaderboardFragmentBinding;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.recyclerdecorators.LinearItemDecoration;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class LeaderBoardFragment extends BaseFragment<LayoutLeaderboardFragmentBinding, LeaderboardViewModel>
        implements LeaderBoardNavigator, LeaderboardListAdapter.LeaderBoardItemClickListener, LoginFragmentBottomDialog.LoginFragmentCallback {

    @Inject
    LeaderboardListAdapter leaderboardListAdapter;
    private LayoutLeaderboardFragmentBinding leaderboardFragmentBinding;

    private LinearLayoutManager mLayoutManager;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private LeaderboardViewModel leaderboardViewModel;
    private LoginFragmentBottomDialog loginDialogFragment;

    private boolean isLoading;
    private boolean isLoadMore = true;

    private String gameId;

    private String sortType;
    private String source = SegmentConstants.SCREEN_NAME_LEADER_BOARD;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static LeaderBoardFragment newInstance(String gameId, String sortType, String source) {
        Bundle args = new Bundle();
        args.putString("game_id", gameId);
        args.putString("sort_type", sortType);
        args.putString(AppConstants.SCREEN_SOURCE, source);
        LeaderBoardFragment fragment = new LeaderBoardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_leaderboard_fragment;
    }

    @Override
    public LeaderboardViewModel getViewModel() {
        leaderboardViewModel = ViewModelProviders.of(this, mViewModelFactory).get(LeaderboardViewModel.class);
        leaderboardViewModel.baseProperties = baseProperties;
        return leaderboardViewModel;
    }

    @Override
    public void handleError(String error) {
        if (leaderboardListAdapter.getItemCount() == 0) {
            leaderboardFragmentBinding.streamersLeaderItemsList.setVisibility(View.GONE);
        }
        isLoading = false;
        leaderboardListAdapter.setShowLoadingView(false);
        Toast.makeText(getActivity(), "Connection Issue. Please try again later!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        leaderboardFragmentBinding = getViewDataBinding();
        leaderboardViewModel.setNavigator(this);
        leaderboardListAdapter.setListener(this);
        if (getArguments() != null) {
            gameId = getArguments().getString("game_id");
            sortType = getArguments().getString("sort_type");

            if (getArguments().containsKey(AppConstants.SCREEN_SOURCE))
                source = getArguments().getString(AppConstants.SCREEN_SOURCE);
        }

        baseProperties.put(AppConstants.SCREEN_SOURCE, source);
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_LEADER_BOARD);
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_LEADER_BOARD, baseProperties);

        setUp();
        subscribeToLiveData();
    }

    private void setUp() {
        int spacingInPixels = getBaseActivity().getResources().getDimensionPixelSize(R.dimen.margin_8);
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setAutoMeasureEnabled(true);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        leaderboardFragmentBinding.streamersLeaderItemsList.setLayoutManager(mLayoutManager);
        leaderboardFragmentBinding.streamersLeaderItemsList.addItemDecoration(new LinearItemDecoration(spacingInPixels));
        leaderboardFragmentBinding.streamersLeaderItemsList.setItemAnimator(new DefaultItemAnimator());
        leaderboardFragmentBinding.streamersLeaderItemsList.setAdapter(leaderboardListAdapter);

        leaderboardFragmentBinding.streamersLeaderItemsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

                // Load more if we have reach the end to the recyclerView
                if (!isLoading && isLoadMore && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    isLoading = true;
                    leaderboardListAdapter.setShowLoadingView(true);
                    leaderboardViewModel.fetchLeaderBoardItems(gameId, totalItemCount, true, sortType);
                }
            }
        });
        leaderboardFragmentBinding.swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.main_color));
        leaderboardFragmentBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                leaderboardListAdapter.clearItems();
                leaderboardViewModel.fetchLeaderBoardItems(gameId, 0, false, sortType);
            }
        });

        loginDialogFragment = LoginFragmentBottomDialog.getInstance(source);
        loginDialogFragment.setmCallback(this);

        leaderboardViewModel.fetchLeaderBoardItems(gameId, 0, false, sortType);
    }

    public void onTabChangeCall(String sortBy) {
        sortType = sortBy;
        leaderboardListAdapter.clearItems();
        leaderboardViewModel.fetchLeaderBoardItems(gameId, 0, false, sortType);
    }

    private void subscribeToLiveData() {
        leaderboardViewModel.getLeaderboardListLiveData().observe(this, blogs -> {
            leaderboardFragmentBinding.swipeRefreshLayout.setRefreshing(false);
            if (blogs.size() > 0) {
                isLoading = false;
                leaderboardFragmentBinding.streamersLeaderItemsList.setVisibility(View.VISIBLE);
//                leaderboardFragmentBinding.sepearator.setVisibility(View.VISIBLE);
                leaderboardListAdapter.setShowLoadingView(false);
                leaderboardListAdapter.addItems(blogs);
            } else {
                isLoadMore = false;
                leaderboardListAdapter.setShowLoadingView(false);
            }
        });
    }

    @Override
    public void setRefreshing(boolean isRefreshing) {
        leaderboardFragmentBinding.swipeRefreshLayout.setRefreshing(isRefreshing);
    }

    @Override
    public void clearLeaderboardItems() {
        leaderboardListAdapter.clearItems();
    }

    @Override
    public void onItemClick(String id) {
        if (getActivity() != null) {
            //((LeaderBoardActivity) getActivity()).loadFragment(ProfileContainerFragment.newInstance(id), false, "Author");
            Intent intent = ProfileActivity.getCallingIntent(getContext());
            intent.putExtra("author_name", id);
            intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_LEADER_BOARD);
            startActivity(intent);
        }
    }

    @Override
    public void onFollowClick(boolean isFollow, String author, String profileId, FollowListenerCallback callback) {
        Map<String, Object> map = new HashMap<>();
        map.put("is_first", CommonUtils.isFirstTimeFollow());
        map.put("author", author);
        map.put("userId", profileId);
        map.put("followAction", !isFollow);
        map.put("source", SegmentConstants.SCREEN_NAME_LEADER_BOARD);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, map);
        CommonUtils.setFirstTimeFollow();
        if (CommonUtils.isUserLoggedin())
            leaderboardViewModel.followUnFollow(isFollow, profileId, callback);
        else
            askLogin();

    }

    private void askLogin() {
        try {
            if (loginDialogFragment.isAdded() || loginDialogFragment.isVisible())
                return;
            loginDialogFragment.showNoAddToBackStack(getChildFragmentManager(), AppConstants.LOGIN_FRAGMENT_TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoginSuccess() {

    }

    @Override
    public void onLoginDialogClose() {

    }

    @Override
    public String getGameId() {
        return gameId;
    }
}
