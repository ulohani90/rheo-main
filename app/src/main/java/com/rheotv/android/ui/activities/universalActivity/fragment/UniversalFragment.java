package com.rheotv.android.ui.activities.universalActivity.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.databinding.UniversalFragmentBinding;
import com.rheotv.android.ui.activities.leaderboard.LeaderBoardActivity;
import com.rheotv.android.ui.activities.leaderboard.LeaderBoardFragment;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.tabcontainer.posts.OnFollowActionCompleteListener;
import com.rheotv.android.ui.activities.tabcontainer.profile.ProfileTabAdapter;
import com.rheotv.android.ui.activities.tabcontainer.profile.videos.VideosFragment;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivity;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppBarStateChangeListener;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.FirebaseDynamicLinkUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.PlayerHeadServiceHelper;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

public class UniversalFragment extends BaseFragment<UniversalFragmentBinding, UniversalFragmentViewModel>
        implements UniversalFragmentNavigator, PostListAdapter.BlogAdapterListener {


    Context context;
    UniversalFragmentBinding universalFragmentBinding;

    LinearLayoutManager mLayoutManager;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private UniversalFragmentViewModel universalFragmentViewModel;
    public SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
    private String gameId;
    private HashMap<String, Object> properties = new HashMap<>();

    public static UniversalFragment newInstance(String id, String gameId, String source) {
        Bundle args = new Bundle();
        args.putString(AppConstants.SEE_ALL_TYPE, id);
        args.putString(AppConstants.SEE_ALL_TYPE_ID, gameId);
        args.putString(AppConstants.SCREEN_SOURCE, source);
        UniversalFragment fragment = new UniversalFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.universal_fragment;
    }

    @Override
    public UniversalFragmentViewModel getViewModel() {
        universalFragmentViewModel = new ViewModelProvider(this, mViewModelFactory).get(UniversalFragmentViewModel.class);
        return universalFragmentViewModel;
    }

    @Override
    public void handleError(Throwable throwable) {
        if (isAdded()) {
            universalFragmentBinding.progressBar.setVisibility(View.GONE);
            universalFragmentBinding.errorView.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), "Connection Issue. Please try again later!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        universalFragmentViewModel.setNavigator(this);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        universalFragmentBinding = getViewDataBinding();
        setUp();
        subscribeToLiveData();
    }

    public void fetchData() {
        String game = getArguments() != null && getArguments().getString(AppConstants.SEE_ALL_TYPE) != null ? getArguments().getString(AppConstants.SEE_ALL_TYPE) : "";
        gameId = getArguments() != null && getArguments().getString(AppConstants.SEE_ALL_TYPE_ID) != null ? getArguments().getString(AppConstants.SEE_ALL_TYPE_ID) : "";
        if (TextUtils.isEmpty(game)) {
            Objects.requireNonNull(getActivity()).finish();
        }

        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            properties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));

        properties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_GAME_PAGE);
        properties.put("game", game);
        properties.put("gameId", gameId);

        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_GAME_PAGE, properties);
        universalFragmentViewModel.fetchGamePage(gameId);
        /*universalFragmentBinding.gameTitle.setText(game);
        universalFragmentBinding.htabToolbar.setTitle(game);*/
//            universalFragmentBinding.toolbarTitle.setText(game);

    }

    private void setUp() {
        fetchData();
//        universalFragmentBinding.backIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (getActivity() != null) {
//                    getActivity().onBackPressed();
//                }
//            }
//        });

//            universalFragmentBinding.htabToolbar.getViewTreeObserver().addOnDrawListener(() -> {
//                if (universalFragmentBinding.htabToolbar.getChildAt(1) != null) {
//                    if (universalFragmentBinding.htabToolbar.getChildAt(1).getVisibility() == View.VISIBLE) {
//                        universalFragmentBinding.htabToolbar.getChildAt(1).setVisibility(View.GONE);
//                    }
//                }
//            });
        universalFragmentBinding.htabToolbar.setNavigationOnClickListener(view -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        universalFragmentBinding.htabAppbar.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state == State.COLLAPSED) {
                    if (universalFragmentBinding.htabToolbar.getChildAt(1) != null)
                        universalFragmentBinding.htabToolbar.getChildAt(1).setVisibility(View.VISIBLE);
                } else {
                    if (universalFragmentBinding.htabToolbar.getChildAt(1) != null)
                        universalFragmentBinding.htabToolbar.getChildAt(1).setVisibility(View.GONE);
                }
            }
        });

        ProfileTabAdapter tabAdapter = new ProfileTabAdapter(getChildFragmentManager());
        tabAdapter.addFragment(VideosFragment.newInstance(gameId, true, true, SegmentConstants.SCREEN_NAME_GAME_PAGE), "Live");

        if (!gameId.equalsIgnoreCase("is_live")) {
            tabAdapter.addFragment(VideosFragment.newInstance(gameId, true, false, SegmentConstants.SCREEN_NAME_GAME_PAGE), "Videos");
            tabAdapter.addFragment(LeaderBoardFragment.newInstance(gameId, LeaderBoardActivity.SORT_BY_MONTH, SegmentConstants.SCREEN_NAME_GAME_PAGE), "Top Streamers");
        }

        universalFragmentBinding.htabViewpager.setAdapter(tabAdapter);
        universalFragmentBinding.errorView.setOnClickListener(v -> {
            universalFragmentBinding.errorView.setVisibility(View.GONE);
            universalFragmentViewModel.fetchGamePage(gameId);
        });

        universalFragmentBinding.htabViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                HashMap<String, Object> tabPropertied = new HashMap<>(properties);
                tabPropertied.put("tab", tabNames[position]);
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_GAME_TAB_CHANGED, tabPropertied);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private String[] tabNames = new String[]{"Live", "Videos", "Top Streamers"};

    private void setupPagerAdapter() {
        universalFragmentBinding.htabViewpager.setVisibility(View.VISIBLE);
        if (!gameId.equalsIgnoreCase("is_live")) {
            universalFragmentBinding.tabLayout.setupWithViewPager(universalFragmentBinding.htabViewpager);
            universalFragmentBinding.tabLayout.setVisibility(View.VISIBLE);
            universalFragmentBinding.tabsSeparator.setVisibility(View.VISIBLE);
        } else {
            universalFragmentBinding.tabLayout.setVisibility(View.GONE);
            universalFragmentBinding.tabsSeparator.setVisibility(View.GONE);
        }
    }

    private void subscribeToLiveData() {
        universalFragmentViewModel.getGameobjectLiveData().observe(getViewLifecycleOwner(), gameObject -> {
            universalFragmentBinding.progressBar.setVisibility(View.GONE);
            if (gameObject != null) {
//                Log.i(getClass().getSimpleName(), "live_Image " + gameObject.getCoverPic());
                BindingUtils.setImageUrlUsingCache(universalFragmentBinding.htabHeader, gameObject.getCoverPic(), true);
                universalFragmentBinding.errorView.setVisibility(View.GONE);
                setupPagerAdapter();
            } else {
                universalFragmentBinding.errorView.setVisibility(View.VISIBLE);
                universalFragmentBinding.tabLayout.setVisibility(View.GONE);
                universalFragmentBinding.tabsSeparator.setVisibility(View.GONE);
                universalFragmentBinding.htabViewpager.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public void onItemClick(String id, PostObject post) {
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
        if (getContext() == null) return;
        StreamPlayerActivity.Companion.startActivity(getContext(),
                new StreamPlayerContainerFragment.Builder()
                        .addPost(post)
                        .addSourceScreenName(SegmentConstants.SCREEN_NAME_GAME_PAGE)
                        .buildExtras());
    }

    @Override
    public void onRetryClick() {

    }

    @Override
    public void onLikeButtonClicked(String body, Result post) {

    }

    @Override
    public void onShareButtonClicked(PostObject post) {
        shareBranchLink(post);
    }

    private void shareBranchLink(PostObject post) {
        if (post == null || post.getAuthor() == null || post.getAuthor().getUser() == null || post.getAuthor().getUser().getUsername() == null)
            return;
        HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.BRANCH_POST_SOURCE_URL, post.getShareUrl());
        map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_LIVE_STREAM);
        FirebaseDynamicLinkUtils.share(context, post.getAuthor().getCampaignInfo(), "player_live_share", post.getAuthor().getUser().getUsername() + " is Live on Rheo TV",
                "Watch " + post.getAuthor().getUser().getUsername() + "playing " + post.getGame() + " live on Rheo TV",
                post.getThumbnail(), map, post.getShareUrl(), true,post.isLive(),post.getAuthor().getUser().getUsername());
    }


    @Override
    public void onAuthorClicked(String userName) {
        Intent intent = ProfileActivity.getCallingIntent(context);
        intent.putExtra("author_name", userName);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_GAME_PAGE);
        context.startActivity(intent);

        //((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().add(R.id.container, ProfileContainerFragment.newInstance(userName)).addToBackStack("Author").commit();
    }

    @Override

    public void onCarouselItemClicked(String id, List<PostObject> results, PostObject post, String carouselTitle) {


    }

    @Override
    public void onMultiViewItemClicked(String id, List<PostObject> results) {

    }

    @Override
    public void onSeeMoreClicked(List<PostObject> result) {

    }

    @Override
    public void onLeaderboardClicked(String id) {

    }

    @Override
    public void onSeeAllClicked(String game, String id) {

    }

    @Override
    public void onAlertCardClicked() {

    }

    @Override
    public void onFollowBtnClicked(String author, int id, boolean isFollowed, OnFollowActionCompleteListener listener) {
        Map<String, Object> map = new HashMap<>();
        map.put("is_first", CommonUtils.isFirstTimeFollow());
        map.put("author", author);
        map.put("source", SegmentConstants.SCREEN_NAME_GAME_PAGE);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, map);
        CommonUtils.setFirstTimeFollow();
        universalFragmentViewModel.onFollowClicked(id, isFollowed, listener);
    }

    @Override
    public void onProfileViewAction(String authorUsername, OnFollowActionCompleteListener listener) {
        Intent intent = ProfileActivity.getCallingIntent(context);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_GAME_PAGE);
        intent.putExtra("author_name", authorUsername);
        context.startActivity(intent);
    }

    @Override
    public void onMoreOptionsBtnClick(String id) {
        new AlertDialog.Builder(getContext()).setTitle(getString(R.string.report_this_title))
                .setMessage(getString(R.string.report_content)).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                universalFragmentViewModel.reportPost(id);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    @Override
    public void onSuperPrimeReminderListener(PostObject result) {

    }

    @Override
    public void onSuperStreamerCardClick(String id) {

    }

    @Override
    public void handleLogin() {
        try {
            ((UniversalActivity) getBaseActivity()).launchLogInFragment();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showToast(String message) {

    }

    @Override
    public void showReportPostSuccessToast() {
        if (isAdded())
            Toast.makeText(getContext(), getString(R.string.post_report_success), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGameClicked(String game, String gameId) {
        HashMap<String, Object> gameProp = new HashMap<>(properties);
        gameProp.put("game", game);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_GAME_TAG_CLICKED, gameProp);

        Intent intent = new Intent(getActivity(), UniversalActivity.class);
        intent.putExtra(AppConstants.SEE_ALL_TYPE, game);
        intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, gameId);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_GAME_PAGE);
        startActivity(intent);
    }

    @Override
    public void onDeleteVideoClicked(String id, int position) {

    }

    @Override
    public void onDownloadVideoClicked(String id, int position) {

    }

}
