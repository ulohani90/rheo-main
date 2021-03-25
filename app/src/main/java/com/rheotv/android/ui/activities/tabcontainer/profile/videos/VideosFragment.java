package com.rheotv.android.ui.activities.tabcontainer.profile.videos;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.databinding.VideosFragmentBinding;
import com.rheotv.android.helpers.AlarmReceiver;
import com.rheotv.android.ui.activities.player.activity.PlayerActivity;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivity;
import com.rheotv.android.ui.activities.tabcontainer.posts.OnFollowActionCompleteListener;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivity;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.decorators.PostItemDecorator;
import com.rheotv.android.ui.fragments.DownloadVideoFormFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.FirebaseDynamicLinkUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.ListHolder;
import com.rheotv.android.utils.PlayerHeadServiceHelper;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.TimeUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class VideosFragment extends BaseFragment<VideosFragmentBinding, VideosFragmentViewModel>
        implements VideosFragmentNavigator, PostListAdapter.BlogAdapterListener {

    @Inject
    VideoFragmentAdapter universalFragmentListAdapter;
    Context context;
    VideosFragmentBinding universalFragmentBinding;

    LinearLayoutManager mLayoutManager;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private VideosFragmentViewModel universalFragmentViewModel;

    DownloadVideoFormFragment downloadVideoFormFragment;
    ProgressDialog progressDialog;

    boolean isLoading;
    private boolean showingSelfVideos;

    public SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
    private HashMap<String, Object> baseProperties = new HashMap<>();
    private String source = SegmentConstants.SCREEN_NAME_VIDEO_LIST;

    public static VideosFragment newInstance(int userId, String source) {
        Bundle args = new Bundle();
        args.putInt(AppConstants.USER_ID, userId);
        args.putBoolean(AppConstants.IS_LITE, false);
        args.putBoolean(AppConstants.IS_LIVE, false);
        args.putString(AppConstants.GAME_ID, null);
        args.putString(AppConstants.SCREEN_SOURCE, source);
        VideosFragment fragment = new VideosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static VideosFragment newInstance(String gameId, boolean isLite, boolean isLive, String source) {
        Bundle args = new Bundle();
        args.putBoolean(AppConstants.IS_LITE, isLite);
        args.putBoolean(AppConstants.IS_LIVE, isLive);
        args.putString(AppConstants.GAME_ID, gameId);
        args.putString(AppConstants.USER_ID, null);
        args.putString(AppConstants.SCREEN_SOURCE, source);
        VideosFragment fragment = new VideosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.videos_fragment;
    }

    @Override
    public VideosFragmentViewModel getViewModel() {
        universalFragmentViewModel = ViewModelProviders.of(this, mViewModelFactory).get(VideosFragmentViewModel.class);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            int userId = bundle.getInt(AppConstants.USER_ID);
            universalFragmentViewModel.setParams(
                    userId,
                    bundle.getString(AppConstants.GAME_ID),
                    bundle.getBoolean(AppConstants.IS_LITE),
                    bundle.getBoolean(AppConstants.IS_LIVE)
            );
            int currentUserId = CommonUtils.getUserID(getContext());
            showingSelfVideos = currentUserId == userId;
        }

        return universalFragmentViewModel;
    }

    @Override
    public void handleError(Throwable throwable) {
        if (universalFragmentViewModel.getBlogListLiveData() != null && universalFragmentViewModel.blogObservableArrayList.size() > 0) {

        } else {
            universalFragmentBinding.blogRecyclerView.setVisibility(View.GONE);
            universalFragmentBinding.errorView.setVisibility(View.VISIBLE);
        }
        isLoading = false;
        universalFragmentListAdapter.setShowLoadingView(false);
        Log.e(getClass().getSimpleName(), "handle_error: " + throwable.getMessage());
    }

    @Override
    public void showNullView() {
        universalFragmentBinding.blogRecyclerView.setVisibility(View.GONE);
        universalFragmentBinding.errorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showReportPostSuccessToast() {
        if (isAdded())
            Toast.makeText(getContext(), getString(R.string.post_report_success), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeleteVideoSuccess(int position) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        Toast.makeText(getContext(), "Video deleted successfully", Toast.LENGTH_SHORT).show();
        universalFragmentListAdapter.removeItemAtPos(position);
    }

    @Override
    public void onDeleteVideoFailure() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (isAdded())
            Toast.makeText(getContext(), "Could not delete video. Please try again.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        universalFragmentViewModel.setNavigator(this);
        universalFragmentListAdapter.setListener(this);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        universalFragmentBinding = getViewDataBinding();
        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE)) {
            source = getArguments().getString(AppConstants.SCREEN_SOURCE);
        }

        baseProperties.put(AppConstants.SCREEN_SOURCE, source);
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_VIDEO_LIST);
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_VIDEO_LIST, baseProperties);

        setUp();
        subscribeToLiveData();
    }

    private void setUp() {
        mLayoutManager = new LinearLayoutManager(context);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        universalFragmentBinding.blogRecyclerView.setLayoutManager(mLayoutManager);
        universalFragmentBinding.blogRecyclerView.setItemAnimator(new DefaultItemAnimator());
        universalFragmentBinding.blogRecyclerView.addItemDecoration(new PostItemDecorator((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics())));
        universalFragmentBinding.blogRecyclerView.setAdapter(universalFragmentListAdapter);

        if (getArguments() != null) {
            if (CommonUtils.isUserLoggedin()) {
                universalFragmentListAdapter.setShowingSelfVideos(showingSelfVideos);
            }

            universalFragmentViewModel.fetchUserVideos(0);
            universalFragmentBinding.blogRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                    if (!isLoading && universalFragmentViewModel.nextUrl != null && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        isLoading = true;
                        universalFragmentListAdapter.setShowLoadingView(true);
                        universalFragmentViewModel.fetchUserVideos(totalItemCount);
                    }
                }
            });
            /*universalFragmentBinding.blogRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(mLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    if (totalItemsCount >= 10) {
                        universalFragmentViewModel.fetchUserVideos(totalItemsCount, userName);
                    }
                }
            });*/
        }
    }

    private void subscribeToLiveData() {
        universalFragmentViewModel.getBlogListLiveData().observe(getViewLifecycleOwner(), blogs -> {
            if (blogs != null && blogs.size() > 0) {
                universalFragmentListAdapter.setShowLoadingView(false);
                universalFragmentViewModel.addBlogItemsToList(blogs);
                universalFragmentBinding.errorView.setVisibility(View.GONE);
            } else {
                universalFragmentBinding.blogRecyclerView.setVisibility(View.GONE);
                universalFragmentBinding.errorView.setVisibility(View.VISIBLE);
            }
            isLoading = false;
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
        ListHolder.getInstance().extractPostIds(universalFragmentListAdapter.getmPostList());
        if (getContext() == null) return;
        StreamPlayerActivity.Companion.startActivity(getContext(),
                new StreamPlayerContainerFragment.Builder()
                        .addPostList(new ArrayList<>(universalFragmentListAdapter.getmPostList()))
                        .addPost(post)
                        .addPaginationUrl(universalFragmentViewModel.nextUrl)
                        .addSourceScreenName(SegmentConstants.SCREEN_NAME_VIDEO_LIST)
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
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_VIDEO_LIST);
        intent.putExtra("author_name", userName);
        context.startActivity(intent);
    }

    @Override

    public void onCarouselItemClicked(String id, List<PostObject> results, PostObject post, String carouselTitle) {
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
        Bundle bundle = new Bundle();
        if (getContext() == null) return;
        StreamPlayerActivity.Companion.startActivity(getContext(),
                new StreamPlayerContainerFragment.Builder()
                        .addPostList(new ArrayList<>(results))
                        .addPost(post)
                        .buildExtras());
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
        map.put("userId", id);
        map.put("followAction", !isFollowed);
        map.put("source", SegmentConstants.SCREEN_NAME_VIDEO_LIST);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, map);
        CommonUtils.setFirstTimeFollow();
        universalFragmentViewModel.onFollowClicked(id, isFollowed, listener);
    }

    @Override
    public void onProfileViewAction(String authorUsername, OnFollowActionCompleteListener listener) {

    }

    @Override
    public void onMoreOptionsBtnClick(String id) {
        new AlertDialog.Builder(getContext()).setTitle(getString(R.string.report_this_title)).setMessage(getString(R.string.report_content)).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("title", result.getAuthor().getUser().getUsername() + " will be live in 15 mins");
        intent.putExtra("body", "Watch " + result.getAuthor().getUser().getUsername() + "streaming " + result.getGame() + " live");
        intent.putExtra("image_url", result.getThumbnail());
        intent.putExtra("post_id", result.getId());
        intent.putExtra("target_url", result.getShareUrl());
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, (result.getStartFrom() - (15 * TimeUtils.MILLIS_AN_HOUR)), alarmIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, (result.getStartFrom() - (5 * TimeUtils.MILLIS_IN_A_MIN)), alarmIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, (result.getStartFrom() - (5 * TimeUtils.MILLIS_IN_A_MIN)), alarmIntent);
        } else {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, (result.getStartFrom() - (5 * TimeUtils.MILLIS_IN_A_MIN)), alarmIntent);
        }
        //  alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() +
        //         60 * 1000, alarmIntent);
        Toast.makeText(getContext(), "You will be notified 5 mins before the stream starts", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuperStreamerCardClick(String id) {

    }

    @Override
    public void onGameClicked(String game, String gameId) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("game", game);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_GAME_TAG_CLICKED, properties);

        Intent intent = new Intent(getActivity(), UniversalActivity.class);
        intent.putExtra(AppConstants.SEE_ALL_TYPE, game);
        intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, gameId);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_VIDEO_LIST);
        startActivity(intent);
    }

    @Override
    public void onDeleteVideoClicked(String id, int position) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("postId", id);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_DELETE_VIDEO_CLICKED, properties);
        new AlertDialog.Builder(getContext()).setTitle("Delete Video?").setMessage("Are you sure you want to delete this video?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                universalFragmentViewModel.deleteVideo(id, position);
                progressDialog = ProgressDialog.show(getContext(), null, "Deleting video. Please wait");
                properties.put("postId", id);
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_DELETE_VIDEO_CONFIRMED, properties);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                properties.put("postId", id);
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_DELETE_VIDEO_CANCELLED, properties);
            }
        }).show();
    }

    @Override
    public void onDownloadVideoClicked(String id, int position) {
        if (getActivity().getSupportFragmentManager().findFragmentByTag(AppConstants.DOWNLOAD_VIDEO_FORM_TAG) != null) {
            downloadVideoFormFragment = (DownloadVideoFormFragment) getActivity().getSupportFragmentManager().findFragmentByTag(AppConstants.DOWNLOAD_VIDEO_FORM_TAG);
        } else {
            downloadVideoFormFragment = DownloadVideoFormFragment.newInstance(id, SegmentConstants.SCREEN_NAME_VIDEO_LIST);
        }
        if (downloadVideoFormFragment != null && (downloadVideoFormFragment.isAdded() || downloadVideoFormFragment.isVisible())) {
            return;
        }
        downloadVideoFormFragment.show(getActivity().getSupportFragmentManager(), AppConstants.DOWNLOAD_VIDEO_FORM_TAG);
    }

    @Override
    public void handleLogin() {
        try {
            if (getActivity() instanceof TabContainerActivity) {
                ((TabContainerActivity) getBaseActivity()).launchLogInFragment();
            } else if (getActivity() instanceof PlayerActivity) {
                ((PlayerActivity) getActivity()).openLoginFlow();
            } else if (getActivity() instanceof ProfileActivity) {
                ((ProfileActivity) getActivity()).openLoginFlow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}