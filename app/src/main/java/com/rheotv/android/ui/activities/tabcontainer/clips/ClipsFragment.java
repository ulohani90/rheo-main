package com.rheotv.android.ui.activities.tabcontainer.clips;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.Observable;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.ModeratorQuestion;
import com.rheotv.android.data.ModeratorQuestionOption;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.databinding.FragmentClipsScreenLayoutBinding;
import com.rheotv.android.db.ClipItem;
import com.rheotv.android.ui.activities.clips.ClipsActivity;
import com.rheotv.android.ui.activities.home.view.HomeActivity;
import com.rheotv.android.ui.activities.player.activity.ChatMenuOptionBottomSheet;
import com.rheotv.android.ui.activities.player.activity.FollowStatusCompleteListener;
import com.rheotv.android.ui.activities.player.activity.ListOption;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.share.ClipShareBottomSheetFragment;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivity;
import com.rheotv.android.ui.adapters.ClipsListAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.customViews.bottomSheetMenu.BottomSheetMenuDialog;
import com.rheotv.android.ui.customViews.bottomSheetMenu.Option;
import com.rheotv.android.ui.customViews.bottomSheetMenu.OptionRequest;
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppUtils;
import com.rheotv.android.utils.AppUtilsKt;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.DownloadShareManager;
import com.rheotv.android.utils.ExoPlayerRecyclerView;
import com.rheotv.android.utils.FirebaseDynamicLinkUtils;
import com.rheotv.android.utils.ModeratorQuestions;
import com.rheotv.android.utils.NetworkUtils;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;
import static com.rheotv.android.ui.activities.player.activity.StreamPlayerFragment.REPORT_USER;
import static com.rheotv.android.ui.activities.player.activity.StreamPlayerFragment.VIEW_PROFILE;

public class ClipsFragment extends BaseFragment<FragmentClipsScreenLayoutBinding, ClipsFragmentViewModel> implements ClipsFragmentNavigator, ClipsListAdapter.OnClipCardItemsClick, ExoPlayerRecyclerView.ExoPlayerClickListener {
    private LoginFragmentBottomDialog loginDialogFragment;

    public static ClipsFragment newInstance(String clipId, String source) {
        ClipsFragment fragment = new ClipsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("clip_id", clipId);
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        fragment.setArguments(bundle);
        return fragment;
    }

    ClipsFragmentViewModel mClipsFragmentViewModel;

    SnapHelper snapHelper;

    FragmentClipsScreenLayoutBinding mBinding;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    ClipsListAdapter clipsListAdapter;

    int snapPosition = RecyclerView.NO_POSITION;

    boolean isFirstVideoLoad;

    boolean isLoading;

    ProgressDialog progressDialog;
    private long videoStartTime = 0;

    boolean isLoadMoreAllowed;
    private final int PERMISSION_REQUEST_CODE = 1000;
    private BottomSheetMenuDialog.Builder shareSheet;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    AlertDialog moderatorQuestionDialog;

    private BroadcastReceiver videoStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(AppConstants.VIDEO_STATE)) {
                boolean isPlay = intent.getBooleanExtra(AppConstants.VIDEO_STATE, false);
                if (getContext() == null) return;
                if (isPlay)
                    mBinding.clipsRv.onHoldPlayer();
                else
                    mBinding.clipsRv.resumePlayer();
            }
        }
    };

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkConnected() && mClipsFragmentViewModel != null && mClipsFragmentViewModel.isFirstApiCalled())
                fetchOnlineData();
        }
    };


    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_clips_screen_layout;
    }

    @Override
    public ClipsFragmentViewModel getViewModel() {
        mClipsFragmentViewModel = new ViewModelProvider(this, mViewModelFactory).get(ClipsFragmentViewModel.class);
        return mClipsFragmentViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null)
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mClipsFragmentViewModel.setNavigator(this);
        loginDialogFragment = LoginFragmentBottomDialog.getInstance(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER);

        //mBlogAdapter.setListener(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();

        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE)) {
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        }
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_CLIPS);
        mClipsFragmentViewModel.properties = baseProperties;

        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_CLIPS, baseProperties);
        setUp();
        subscribeToLiveData();
        //mClipsFragmentViewModel.fetchModeratorQuestions();
        if (CommonUtils.isUserLoggedin() && CommonUtils.isUserContentModerator() && ModeratorQuestions.getInstance().getClipsQuestions() == null || ModeratorQuestions.getInstance().getClipsQuestions().size() == 0) {
            mClipsFragmentViewModel.fetchModeratorQuestions();
        }
        mBinding.clipsRv.onHoldPlayer();
    }

    public static String macAddress = null;

    private static final String TAG = "ClipsFragment";

    @Override
    public void makeViewApiCall(int duration, long timeElapsed, ClipItem result) {
        if (macAddress == null) {
            macAddress = AppUtils.getMACAddress();
        }
        if (result == null) return;
        Log.i(TAG, "duration = " + duration + "\ntime elapsed = " + timeElapsed);
        mClipsFragmentViewModel.getDataManager().postVideoView(result, CommonUtils.getDevId(getNonUiContext()),
                duration, (int) timeElapsed, macAddress, false, result.getGame(), result.getGameId())
                .enqueue(new retrofit2.Callback<ResponseBody>() {
                    @Override

                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.body() != null) {
                            try {
                                Log.i(TAG, "Response " + response.body().string());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Log.e(TAG, "Error -> " + t.getMessage());
                        t.printStackTrace();
                    }
                });
    }


    private void subscribeToLiveData() {
        mClipsFragmentViewModel.clipsLiveList.observe(getViewLifecycleOwner(), clips -> {
            mClipsFragmentViewModel.state.set(Status.SUCCESS);
            clipsListAdapter.setShowLoadingView(false);
            mBinding.clipsRv.setMediaObjects(clips);

            clipsListAdapter.setShowEndOfListFooter(mClipsFragmentViewModel.nextUrl == null);

            clipsListAdapter.setClips(clips);
            if (!isFirstVideoLoad) {
                mBinding.clipsRv.post(() -> {
                    if (mBinding.clipsRv != null) {
                        videoStartTime = System.currentTimeMillis();
                        mBinding.clipsRv.playVideo(false);
                        if (!isResumed()) {
                            Log.i("ExoPlayerRecyclerView", "video is resumed");
                            mBinding.clipsRv.onHoldPlayer();
                        }

                    }
                });
                isFirstVideoLoad = true;
            }


            isLoading = false;
            //mBinding.clipsRv.resumePlayer();
        });
    }

    public void fetchData() {
        if (NetworkUtils.isNetworkConnected(getContext())) {
            fetchOnlineData();
        } else {
            mClipsFragmentViewModel.loadOfflineData();
        }
    }

    private void fetchOnlineData() {
        String clipId = getArguments().getString("clip_id");
        String offlineClipId = mClipsFragmentViewModel.getLastClipId();
        if (clipId != null) {
            mClipsFragmentViewModel.fetchClip(clipId);
        } else if (offlineClipId != null) {
            mClipsFragmentViewModel.fetchClip(offlineClipId);
        } else {
            mClipsFragmentViewModel.fetchClips(true);
        }
    }

    private void setUp() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mBinding.clipsRv.setLayoutManager(mLayoutManager);
        mBinding.clipsRv.setListener(this);
        snapHelper = new PagerSnapHelper() {
            @Override
            public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
                int position = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
                if (position == layoutManager.getItemCount() - 1) {
                    showEndOfflineAlert();
                }
                return position;
            }
        };
        snapHelper.attachToRecyclerView(mBinding.clipsRv);
        mBinding.clipsRv.setAdapter(clipsListAdapter);
        clipsListAdapter.setListener(this);
       /* mBinding.clipsRv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                maybeNotifySnapPositionChange(recyclerView);
            }

            private void maybeNotifySnapPositionChange(RecyclerView recyclerView) {
                int snapPosition = getSnapPosition(recyclerView);
                boolean snapPositionChanged = ClipsFragment.this.snapPosition != snapPosition;
                if (snapPositionChanged) {
                    if (recyclerView.getLayoutManager() != null && ClipsFragment.this.snapPosition != RecyclerView.NO_POSITION) {
                        View view = recyclerView.getLayoutManager().findViewByPosition(ClipsFragment.this.snapPosition);
                        ((PlayerView) view.findViewById(R.id.video_view)).getPlayer().release();
                    }
                    ClipsFragment.this.snapPosition = snapPosition;
                }
            }
        });*/
        mBinding.clipsRv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int totalItemCount = mLayoutManager.getItemCount();
                    int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

                    // Load more if we have reach the end to the recyclerView
                    if (!isLoading && mClipsFragmentViewModel.nextUrl != null && (firstVisibleItemPosition + 3) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        isLoading = true;
                        clipsListAdapter.setShowLoadingView(true);
                        mClipsFragmentViewModel.fetchClips(false);
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        mBinding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetworkUtils.isNetworkConnected(getContext())) {
                    //mBinding.clipsRv.onHoldPlayer();
                    mBinding.swipeRefresh.setRefreshing(false);
                    mBinding.clipsRv.clearMediaObjects();
                    clipsListAdapter.clearClips();
                    mClipsFragmentViewModel.nextUrl = "";
                    isLoadMoreAllowed = true;
                    isFirstVideoLoad = false;
                    mClipsFragmentViewModel.fetchClips(true);
                } else {
                    mBinding.swipeRefresh.setRefreshing(false);
                    showOfflineSnackBar();
                }
            }
        });

        buildShareSheet();

        mClipsFragmentViewModel.showLoading.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (mClipsFragmentViewModel.showLoading.get()) {
                    progressDialog = ProgressDialog.show(getActivity(), null, "Submitting response. Please wait.");
                } else {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }
            }
        });

        mBinding.errorLayout.setOnClickListener(view -> fetchData());
    }

    private void showEndOfflineAlert() {
        if (NetworkUtils.isNetworkConnected(getContext())) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Offline mode")
                .setMessage(getContext().getString(R.string.offline_alert_text))
                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(R.string.go_online, (dialog, which) -> {
                    // Continue with delete operation
                    getContext().startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                })
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void startFetchingClips() {
        mClipsFragmentViewModel.fetchClips(false);
    }

    @Override
    public void setLoadMoreAllowed(boolean loadMoreAllowed) {
        isLoadMoreAllowed = loadMoreAllowed;
    }

    @Override
    public void openLoginFlow() {
        if (NetworkUtils.isNetworkConnected(getContext())) {
            if (getActivity() instanceof ClipsActivity)
                ((ClipsActivity) getActivity()).openLoginFlow();
            else if (getActivity() instanceof HomeActivity)
                askOpenLoginFlow(null);
        } else
            showOfflineSnackBar();
    }

    public void askOpenLoginFlow(String rewardMessage) {
        try {
            if (loginDialogFragment.isAdded() || loginDialogFragment.isVisible())
                return;

            loginDialogFragment.setRewardText(rewardMessage);
            loginDialogFragment.showNoAddToBackStack(this.getChildFragmentManager(), AppConstants.LOGIN_FRAGMENT_TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mBinding.clipsRv != null) {
            mBinding.clipsRv.resumePlayer();
        }
        if (mClipsFragmentViewModel != null && !mClipsFragmentViewModel.isFirstApiCalled())
            fetchData();
    }

    @Override
    public void onPause() {
        if (mBinding.clipsRv != null) {
            mBinding.clipsRv.onHoldPlayer();
        }
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        videoStartTime = System.currentTimeMillis();
        if (mBinding.clipsRv != null && isResumed()) {
            mBinding.clipsRv.resumePlayer();
        }
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(videoStateReceiver, new IntentFilter(AppConstants.FILTER_VIDEO_STATE));
            getContext().registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        mBinding.clipsRv.onHoldPlayer();
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(videoStateReceiver);
            getContext().unregisterReceiver(networkStateReceiver);
        }
    }

    @Override
    public void onDestroy() {
        mBinding.clipsRv.releasePlayer();
        super.onDestroy();
        AppUtilsKt.INSTANCE.runGC();
    }

    public int getSnapPosition(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if (layoutManager != null) {
            View snapView = snapHelper.findSnapView(layoutManager);
            return layoutManager.getPosition(snapView);
        }
        return RecyclerView.NO_POSITION;
    }


    @Override
    public void setClipsData(List<Result> clips) {

    }

    private void buildShareSheet() {
        shareSheet = new BottomSheetMenuDialog.Builder()
                .header("Share via")
                .columns(3)
                .setAdjustWindow(false)
                .setListener(this::onShareItemClick);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        List<ResolveInfo> resolveInfoList = getBaseActivity().getPackageManager().queryIntentActivities(intent, 0);
        ArrayList<OptionRequest> optionRequestList = new ArrayList<>();
        for (ResolveInfo res : resolveInfoList) {
            OptionRequest request = new OptionRequest(
                    res.labelRes,
                    res.loadLabel(getBaseActivity().getPackageManager()).toString(),
                    res.loadIcon(getBaseActivity().getPackageManager()),
                    res.activityInfo.packageName);
            optionRequestList.add(request);
        }

        Comparator<OptionRequest> comparator = (optionRequest, t1) -> {
            if (optionRequest.getTag() != null && t1.getTag() != null) {
                if (optionRequest.getTag().equalsIgnoreCase(AppConstants.WHATSAPP_PACKAGE)) {
                    return -1;
                } else if (optionRequest.getTag().equalsIgnoreCase(AppConstants.FACEBOOK_PACKAGE)) {
                    return -1;
                } else if (optionRequest.getTag().equalsIgnoreCase(AppConstants.FACEBOOK_LITE_PACKAGE)) {
                    return -1;
                } else {
                    return 1;
                }
            }
            return 0;
        };

        // todo write a better sorting
        try {
            Collections.sort(optionRequestList, comparator);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        shareSheet.addAll(optionRequestList);
        shareSheet.build();
    }

    private ClipItem post;

    @Override
    public void onClipCardShareClicked(ClipItem result) {
        if (result == null) return;
        this.post = result;
        if (NetworkUtils.isNetworkConnected(getContext())) {

        /*StringBuilder builder = new StringBuilder();
        builder.append("OMG!!! Did you see this mind blowing clip on Rheo TV app. See this\n");
        builder.append(AppUtils.getClipShareUrl(result.getId()));
        ShareTaskHelper.getNewInstance(getContext()).share(getContext(), builder.toString(), ShareTaskHelper.ShareTarget.Others);*/

//        HashMap<String, String> map = new HashMap<>();
//        map.put(AppConstants.BRANCH_CLIP_URL_SHARE, AppUtils.getClipShareUrl(result.getId()));
//        map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_CLIP);
//        BranchUtils.share(getContext(), "player_live_share", "OMG!!! Did you see this mind blowing clip on Rheo TV app. Check it out.",
//                "See this",
//                result.getThumbnailUrl(), map);

            if (isStoragePermissionGranted()) {
                loadShareSheet();
            } else {
                requestPermission();
            }
        } else {
            showOfflineSnackBar();
        }
    }

    private void loadShareSheet() {
        if (post == null) return;
        if (CommonUtils.isUserLoggedin()) {
            if (post.durationInSeconds() <= 60) {
                HashMap<String, String> map = new HashMap<>();
                map.put(AppConstants.BRANCH_CLIP_URL_SHARE, AppUtils.getClipShareUrl(post.getId()));
                map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_CLIP);
                ClipShareBottomSheetFragment.show(getParentFragmentManager(),
                        ClipShareBottomSheetFragment.Companion.build(ClipShareBottomSheetFragment.Companion.builder(new HashMap<>(baseProperties))
                                        .setClipId(post.getId())
                                        .setSource(SegmentConstants.SCREEN_NAME_CLIPS)
                                        .setVideoUrl(post.getVideoUrl())
                                        .setShareMap(map)
                                        .setCampaignInfo(post.getAuthor().getCampaignInfo())
                                        .setShareIdentifier("clips_share")
                                        .setShareTitle("See this")
                                        .setAuthorName(post.getAuthor().getUser().getUsername())
                                        .setIsLive("false")
                                        .setShareDescription("Hey, Did you watch this amazing clip on Rheo.\n\n" + "" +
                                                "Be it funny moment or a killing spree, Rheo has all of it.\n" +
                                                "There are thousands of such interesting clips here.\n" +
                                                "Check them out.\n")
                                        .setPostUrl(post.getThumbnailUrl()),
                                () -> {
                                    if (getContext() != null)
                                        mBinding.clipsRv.resumePlayer();
                                })
                );
            } else {
                shareSheet.show(getChildFragmentManager(), "BottomSheetMenuDialog");
            }
        } else {
            openLoginFlow();
        }
    }

    private void onShareItemClick(String s, Option option) {
        try {
            HashMap<String, Object> properties = new HashMap<>(baseProperties);
            properties.put("platform", option.getTitle());
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_CLIP_SHARE_PLATFORM_SELECTED, properties);
            if (option.getTag() != null && option.getTag().equalsIgnoreCase(AppConstants.WHATSAPP_PACKAGE)) {
                shareClip();
            } else {
                HashMap<String, String> map = new HashMap<>();
                map.put(AppConstants.BRANCH_CLIP_URL_SHARE, AppUtils.getClipShareUrl(post.getId()));
                map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_CLIP);
                FirebaseDynamicLinkUtils.share(getContext(), post.getAuthor().getCampaignInfo(),
                        "clip_share", "Hey, Did you watch this amazing clip on Rheo.\n\nFor more such clips download the *Rheo* app now -",
                        "See this",
                        post.getThumbnailUrl(), map, AppUtils.getClipShareUrl(post.getId()), option.getTag(), post.getAuthor().getUser().getUsername());
                SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_CLIP_SHARED, properties);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void shareClip() {
        new DownloadShareManager.Builder()
                .setContext(getContext())
                .setDownloadLink(post.getVideoUrl())
                .setShareTitle(post.getTitle())
                .setShareMessage("\nBy " + "*" + post.getAuthor().getUser().getUsername() + "*" + "\n\nFor more such clips download the *Rheo* app now\n" + post.getVideoUrl())
                .build();
    }

    @Override
    public void onClipCardClapClicked(ClipItem result) {
        if (result == null) return;
        if (NetworkUtils.isNetworkConnected(getContext()))
            mClipsFragmentViewModel.likeClicked(result.getId());
        else
            showOfflineSnackBar();
    }

    @Override
    public void onClipCardReportClicked(ClipItem result) {
        if (result == null) return;
        if (NetworkUtils.isNetworkConnected(getContext())) {
            mBinding.clipsRv.setShouldAutoScroll(false);

            new AlertDialog.Builder(getContext()).setTitle(getString(R.string.report_this_title)).setMessage(getString(R.string.report_content)).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mClipsFragmentViewModel.reportPost(result.getId());
                    mBinding.clipsRv.resumePlayer();
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mBinding.clipsRv.resumePlayer();


                }
            }).show();
        } else {
            showOfflineSnackBar();
        }
    }

    @Override
    public void onClipMoreClicked(ClipItem result) {
        if (result == null) return;
        showClipMoreBottomSheet(result);
    }

    @Override
    public void showToast() {
        if (isAdded())
            Toast.makeText(getContext(), getString(R.string.post_report_success), Toast.LENGTH_LONG).show();
    }

    @Override
    public void showLoading() {
        mClipsFragmentViewModel.state.set(Status.LOADING);
    }

    @Override
    public void hideLoading() {
        mClipsFragmentViewModel.state.set(Status.SUCCESS);
    }

    @Override
    public void showError() {
        mClipsFragmentViewModel.state.set(Status.ERROR);
    }

    @Override
    public void onFollowClicked(ClipItem result, boolean isFollowed, FollowStatusCompleteListener listener) {
        if (result == null) {
            if (listener != null)
                listener.error();
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("is_first", CommonUtils.isFirstTimeFollow());
        map.put("author", result.getAuthor().getUser().getUsername());
        map.put("source", SegmentConstants.SCREEN_NAME_CLIPS);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, map);
        CommonUtils.setFirstTimeFollow();
        if (NetworkUtils.isNetworkConnected(getContext()))
            mClipsFragmentViewModel.followClicked(result, isFollowed, listener);
        else
            showOfflineSnackBar();
    }

    @Override
    public void onProfileClicked(String authorUsername) {
        if (NetworkUtils.isNetworkConnected(getContext())) {
            Intent intent = ProfileActivity.getCallingIntent(getContext());
            // intent.putExtra("follow_action_listener", listener);
            intent.putExtra("author_name", authorUsername);
            intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_CLIPS);
            getContext().startActivity(intent);
        } else
            showOfflineSnackBar();
    }

    @Override
    public void onWatchNowClicked(String liveId) {
        if (NetworkUtils.isNetworkConnected(getContext())) {

            Intent activityIntent = new Intent(getContext(), StreamPlayerActivity.class);
            activityIntent.putExtras(new StreamPlayerContainerFragment.Builder()
                    .addPost(liveId)
                    .addFromDeepLink(false)
                    .addSourceScreenName(AppConstants.SCREEN_SOURCE)
                    .setForCustomRoom(false)
                    .addGameId(AppConstants.LIVE_GAME_ID)
                    .addLoadMore(true)
                    .buildExtras());

            getContext().startActivity(activityIntent);

        } else
            showOfflineSnackBar();
    }


    @Override
    public void onGameClicked(String gameId, String game) {
        if (NetworkUtils.isNetworkConnected(getContext())) {
            HashMap<String, Object> properties = baseProperties;
            properties.put("game", game);
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_GAME_TAG_CLICKED, properties);
            Intent intent = new Intent(getContext(), UniversalActivity.class);
            intent.putExtra(AppConstants.SEE_ALL_TYPE, game);
            intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, gameId);
            intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_CLIPS);
            startActivity(intent);
        } else
            showOfflineSnackBar();
    }

    @Override
    public void showLoginFlow() {
        openLoginFlow();
    }

    @Override
    public void stopAutoScroll() {
        mBinding.clipsRv.setShouldAutoScroll(false);
    }

    @Override
    public void onDoubleTap(ClipItem result, int playPosition) {
        if (NetworkUtils.isNetworkConnected(getContext())) {
            HashMap<String, Object> properties = baseProperties;
            properties.put("clip_id", result.getId());
            properties.put("game", result.getGame());
            properties.put("username", result.getAuthor().getUser().getUsername());
            properties.put("title", result.getTitle());
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_DOUBLE_TAP_CLIP, properties);
            mClipsFragmentViewModel.likeClicked(result.getId());
            mBinding.clapView.setVisibility(View.VISIBLE);
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.scale_fade_out_anim);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mBinding.clapView.setVisibility(View.GONE);
                    if (!result.getClap()) {
                        View view = mBinding.clipsRv.getLayoutManager().findViewByPosition(playPosition);
                        if (view != null) {
                            AppUtils.changeTopDrawable(view.findViewById(R.id.clap_btn), R.drawable.ic_like_heart_filled_48);
                            setClapCountText((TextView) view.findViewById(R.id.clap_btn), result.getClapCount() + 1);
                        }
                        clipsListAdapter.changeClapState(playPosition, true);
                        result.setClap(true);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mBinding.clapView.startAnimation(anim);
        } else
            showOfflineSnackBar();
    }

    private void authorClicked(String username) {
        Intent intent = ProfileActivity.getCallingIntent(getContext());
        intent.putExtra("author_name", username);
        startActivity(intent);
    }

    private void showClipMoreBottomSheet(ClipItem result) {
        if (result == null) return;
        ArrayList<ListOption> listOptions = new ArrayList<>();
        listOptions.add(new ListOption.Header(VIEW_PROFILE));
        listOptions.add(new ListOption.Item(VIEW_PROFILE, "View Profile", R.drawable.avd_user, null));
        listOptions.add(new ListOption.Item(REPORT_USER, "Report Clip", R.drawable.ic_flag_white_24dp, null));
        ChatMenuOptionBottomSheet bottomSheet = ChatMenuOptionBottomSheet.Companion.newInstance(
                listOptions,
                (ListOption listOption) -> {
                    if (listOption instanceof ListOption.Header) {
                        onProfileClicked(result.getAuthor().getUser().getUsername());
                    } else {
                        ListOption.Item item = (ListOption.Item) listOption;
                        if (item.getId() == REPORT_USER)
                            onClipCardReportClicked(result);
                        if (item.getId() == VIEW_PROFILE)
                            authorClicked(result.getAuthor().getUser().getUsername());

                    }

                    return null;
                }
        );
        bottomSheet.setChatMenuOptionData(mClipsFragmentViewModel.getClipOptionMenuBottomSheetData(result));
        mBinding.clipsRv.onHoldPlayer();
        bottomSheet.show(getChildFragmentManager(), ChatMenuOptionBottomSheet.TAG);
    }


    public void showInfoAlert(ClipItem result) {
        if (result == null || getActivity() == null) return;
        if (!NetworkUtils.isNetworkConnected(RheoTvApp.getNonUiContext())) {
            showErrorToast("You are offline");
            return;
        }
        ModeratorQuestion question = null;

        for (ModeratorQuestion moderatorQuestion : ModeratorQuestions.getInstance().getClipsQuestions()) {
            if (moderatorQuestion.getPertains() != null && moderatorQuestion.getPertains().equalsIgnoreCase("clip")) {
                question = moderatorQuestion;
                break;
            }
        }

        if (question != null && question.getChoiceType().equalsIgnoreCase("MULTI_CHOICE")) {

            List<String> selectedQuestionIds = new ArrayList<>();

            AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.moderator_dialog_header, null, false);
            ((TextView) view.findViewById(R.id.header)).setText(question.getQuestion());
            builderSingle.setCustomTitle(view);
            List<String> optionsList = getOptionsList(question.getOptions());
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    R.layout.checkbox_item_layout);

            arrayAdapter.addAll(optionsList);
            ModeratorQuestion finalQuestion = question;
// cancel button
            builderSingle.setNegativeButton("Submit",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (finalQuestion == null) return;
                            submitModeratorResponse(result.getId(), finalQuestion.getId(), selectedQuestionIds);
                        }
                    });
            builderSingle.setAdapter(arrayAdapter, null);


            moderatorQuestionDialog = builderSingle.create();

            moderatorQuestionDialog.getListView().setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Log.i("Selected Item : ", arrayAdapter.getItem(position));
                            if (finalQuestion == null) return;
                            if (((CheckedTextView) view).isChecked()) {
                                selectedQuestionIds.remove(finalQuestion.getOptions().get(position).getId());
                                ((CheckedTextView) view).setChecked(false);
                            } else {
                                selectedQuestionIds.add(finalQuestion.getOptions().get(position).getId());
                                ((CheckedTextView) view).setChecked(true);
                            }
                        }
                    });

            moderatorQuestionDialog.show();
            if (moderatorQuestionDialog.getWindow() != null)
                moderatorQuestionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private List<String> getOptionsList(List<ModeratorQuestionOption> options) {
        List<String> optionsList = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            optionsList.add((i + 1) + ". " + options.get(i).getOption());
        }
        return optionsList;
    }

    private void submitModeratorResponse(String postId, String questionId, List<String> selectedQuestionIds) {
        mClipsFragmentViewModel.submitModeratorQuestionResponse(postId, questionId, selectedQuestionIds);
    }

    private void setClapCountText(TextView clapBtn, int clapCount) {
        if (clapCount > 0) {
            clapBtn.setText((clapCount / 1000 >= 1) ? (clapCount / 1000) + "." + ((clapCount % 1000) / 100) + "K" : clapCount + "");
        } else {
            clapBtn.setText("Clap");
        }
    }

    @Override
    public void onLongPress(ClipItem result, int playPosition) {
        if (result == null) return;
        if (NetworkUtils.isNetworkConnected(getContext())) {
            HashMap<String, Object> properties = baseProperties;
            properties.put("clip_id", result.getId());
            properties.put("game", result.getGame());
            properties.put("username", result.getAuthor().getUser().getUsername());
            properties.put("title", result.getTitle());
            stopAutoScroll();

            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_REPORT_CLIP, properties);
            onClipCardReportClicked(result);
        } else
            showOfflineSnackBar();
    }

    private boolean isStoragePermissionGranted() {
        int result = ContextCompat.checkSelfPermission(getBaseActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(getBaseActivity(), "Write External Storage permission allows us to do store data. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            this.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadShareSheet();
            } else {
                Toast.makeText(getBaseActivity(), "Permission Denied, You cannot use local drive.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStreamStart(ClipItem result, int playPosition, long bufferTime) {
        if (result == null) return;
        videoStartTime = System.currentTimeMillis();
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("clip_id", result.getId());
        properties.put("game", result.getGame());
        properties.put("username", result.getAuthor().getUser().getUsername());
        properties.put("title", result.getTitle());
        properties.put("playPosition", playPosition);
        properties.put("buffering_time", (bufferTime / 1000));

        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_CLIP_PLAY_STARTED, properties);
    }

    @Override
    public void onBottomSheetDismiss() {
        if (mBinding == null) return;
        if (!CommonUtils.isUserContentModerator())
            mBinding.clipsRv.setShouldAutoScroll(true);
        mBinding.clipsRv.resumePlayer();
    }

    @Override
    public void onStreamEnd(ClipItem result, int playPosition) {
        if (result == null) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("clip_id", result.getId());
        properties.put("game", result.getGame());
        properties.put("username", result.getAuthor().getUser().getUsername());
        properties.put("title", result.getTitle());
        properties.put("playPosition", playPosition);

        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_CLIP_ENDED, properties);
    }

    @Override
    public void onStreamBuffering(ClipItem result, int playPosition) {
        if (result == null) return;
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("clip_id", result.getId());
        properties.put("game", result.getGame());
        properties.put("username", result.getAuthor().getUser().getUsername());
        properties.put("title", result.getTitle());
        properties.put("playPosition", playPosition);

        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_CLIP_BUFFERING, properties);
    }

    @Override
    public void onClipChange(ClipItem result, int playPosition) {
        try {
            if (CommonUtils.isUserContentModerator())
                stopAutoScroll();
            HashMap<String, Object> properties = new HashMap<>(baseProperties);
            ClipItem clipItem = result;
            if (playPosition > 0 && playPosition < mBinding.clipsRv.getMediaObjects().size()) {
                clipItem = mBinding.clipsRv.getMediaObjects().get(playPosition - 1);
            }
            properties.put("clip_id", clipItem.getId());
            properties.put("game", clipItem.getGame());
            properties.put("username", CommonUtils.getUserName(getNonUiContext()));
            properties.put("title", clipItem.getTitle());
            properties.put("playPosition", playPosition > 0 ? playPosition - 1 : 0);
            properties.put("author", clipItem.getAuthor().getUser().getUsername());
            properties.put("duration", (System.currentTimeMillis() - videoStartTime) / 1000);

            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_CLIP_CHANGED, properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onThumbsDown(ClipItem result) {
        showInfoAlert(result);
    }

    @Override
    public void getFollowStatus(ClipItem clipItem, ClipsListAdapter.ClipViewHolder clipViewHolder) {
        if (clipItem == null || clipItem.getAuthor() == null || clipItem.getAuthor().getUser() == null)
            return;
        mClipsFragmentViewModel.loadUserFollowState(clipItem, isFollowed -> {
            clipItem.getAuthor().setFollowed(isFollowed);
            if (clipViewHolder != null)
                clipViewHolder.updateFollowState(clipItem);
        });
    }

    interface OnFollowStatusUpdateListener {
        void onFollowStatusUpdate(boolean isFollowed);
    }

    @Override
    public void showSuccessToast(String message) {
        if (moderatorQuestionDialog != null && moderatorQuestionDialog.isShowing()) {
            moderatorQuestionDialog.dismiss();
        }
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    public void showOfflineSnackBar() {
        Snackbar.make(mBinding.getRoot(), getString(R.string.offline_message), Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(getContext(), R.color.color_accent))
                .setAnchorView(mBinding.anchorView)
                .setAction(getString(R.string.retry), v -> {
                    if (NetworkUtils.isNetworkConnected(getContext()))
                        fetchOnlineData();
                })
                .show();
    }
}
