package com.rheotv.android.ui.activities.story;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.databinding.Observable;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.gson.Gson;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.story.StoryResult;
import com.rheotv.android.databinding.FragmentStoryPagerBinding;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.helpers.ShareTaskHelper;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivity;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.customViews.bottomSheetMenu.BottomSheetMenuDialog;
import com.rheotv.android.ui.customViews.bottomSheetMenu.Option;
import com.rheotv.android.ui.customViews.bottomSheetMenu.OptionRequest;
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppUtils;
import com.rheotv.android.utils.FirebaseDynamicLinkUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.DownloadShareManager;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.rheotv.story.Constants;
import com.rheotv.story.StoryCallback;
import com.rheotv.story.StoryLayout;
import com.rheotv.story.model.Author;
import com.rheotv.story.model.Story;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.rheotv.android.utils.AppConstants.PERMISSION_REQUEST_CODE;

public class StoryPagerFragment extends BaseFragment<FragmentStoryPagerBinding, StoryViewModel> implements StoryCallback {

    private static final String TAG = "StoryPagerFragment";
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private StoryViewModel mViewModel;

    private FragmentStoryPagerBinding mBinding;
    private StoryLayout storyLayout;
    private Story currentStory;
    private BottomSheetMenuDialog.Builder shareSheet;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    private final int action_share = 100;
    private final int action_view_profile = 101;
    private final int action_report = 102;
    private LoginFragmentBottomDialog loginDialogFragment;
    private boolean mIsFragmentReCreated = false;
    private String mAuthorId;
    private String storyUrl;
    private StoryResult mStoryResult;
    private boolean mFragmentSelected = false;
    private HashSet<Runnable> mHashSet = new HashSet<>();
    private Runnable mStartRunnable = this::resumeCurrentStory;
    private Runnable mStopRunnable = () -> {
        pauseCurrentStory();
        if (storyLayout != null) {
            storyLayout.resetCurrentStory();
        }
    };

    public static StoryPagerFragment newInstance(String authorId) {
        Bundle bundle = new Bundle();
        bundle.putString(StoryActivity.ARG_AUTHOR_ID, authorId);
        StoryPagerFragment fragment = new StoryPagerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_story_pager;
    }

    @Override
    public StoryViewModel getViewModel() {
        // note: check if it produce NPE
        if (getActivity() != null) {
            mViewModel = new ViewModelProvider(getActivity(), mViewModelFactory).get(StoryViewModel.class);
        }
        if (mViewModel != null) {
            if (getArguments() != null) {
                if (getArguments().containsKey(StoryActivity.ARG_AUTHOR_ID)) {
                    mAuthorId = getArguments().getString(StoryActivity.ARG_AUTHOR_ID);
                }
                if (mAuthorId == null) {
                    mAuthorId = "";
                }
            }
            mViewModel.stories.observe(this, item -> {
                if (item == null || item.getUserProfile() == null) return;
                if (mStoryResult == null && mAuthorId.equalsIgnoreCase(item.getUserProfile().getId())) {
                    mStoryResult = item;
                    loadStories(item.getUserStories());
                }
            });

            mViewModel.shouldResume.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    try {
                        if (storyLayout != null)
                            storyLayout.resume(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            mViewModel.followStatus.observe(this, this::updateFollowingStoryView);
            mViewModel.interestedStatus.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    updateInterestedStoryView();
                }
            });

            mViewModel.loadingStatus.observe(this, status -> {
                if (status == Status.ERROR && storyLayout != null) {
                    storyLayout.next();
                }
            });
            mViewModel.loadStories(mAuthorId);
        }
        return mViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mIsFragmentReCreated = true;
            mAuthorId = savedInstanceState.getString(AppConstants.ARG_AUTHOR_ID);
            try {
                mStoryResult = new Gson().fromJson(savedInstanceState.getString(AppConstants.ARG_STORIES), StoryResult.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        if (mStoryResult != null && mIsFragmentReCreated) {
            loadStories(mStoryResult.getUserStories());
        }
        buildShareSheet();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (storyLayout != null) {
            pauseCurrentStory();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (storyLayout != null)
            resumeCurrentStory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (storyLayout != null)
            storyLayout.checkAndReleasePlayer();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(AppConstants.ARG_STORIES, new Gson().toJson(mStoryResult));
        outState.putString(AppConstants.ARG_AUTHOR_ID, mAuthorId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showShareSheet();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            ViewUtils.showMessageOKCancel(getContext(), getResources().getString(R.string.photo_upload_permission),
                                    (dialog, which) -> requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE));
                        } else {
                            Toast.makeText(getContext(), getString(R.string.photo_upload_permission), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void done() {
        mViewModel.updateStoryAction(StoryAction.NEXT);
    }

    @Override
    public void backward() {
        mViewModel.updateStoryAction(StoryAction.PREVIOUS);
    }

    @Override
    public void dismiss() {
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(AppConstants.UPDATE_STORY_BROADCAST_FILTER));
        }
        onFinish();
    }

    @Override
    public void watched(Story story) {
        this.currentStory = story;
        if (!story.isWatched()) {
            mViewModel.markStoryWatched(story);
            recordSeenEvent(story);
        }
    }

    @Override
    public void moreOption(Story story) {
        HashMap<String, Object> properties = baseProperties;
        properties.put("story_id", story.getId());
        properties.put("author", story.getAuthor());
        properties.put("type", story.getType());
        properties.put("created_at", story.getCreatedAt());
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STORY_SHOW_MORE_CLICKED, properties);
        showCreateStoryOption(story);
    }

    @Override
    public void viewAuthorProfile(String authorName) {
        HashMap<String, Object> properties = baseProperties;
        properties.put("author", authorName);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STORY_VIEW_AUTHOR_PROFILE_CLICKED, properties);
        viewProfile(authorName);
    }

    @Override
    public void viewMentionedProfile(String username) {
        HashMap<String, Object> properties = baseProperties;
        properties.put("username", username);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STORY_MENTIONED_USER_CLICKED, properties);
        viewProfile(username);
    }

    private void viewProfile(String username) {
        Intent intent = ProfileActivity.getCallingIntent(getContext());
        intent.putExtra("author_name", username);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_VIEW_STORY);
        startActivity(intent);
    }

    @Override
    public void onInterest(Story story) {
        currentStory = story;
        if (CommonUtils.isUserLoggedin()) {
            HashMap<String, Object> properties = baseProperties;
            properties.put("story_id", story.getId());
            properties.put("author", story.getAuthor());
            properties.put("type", story.getType());
            properties.put("created_at", story.getCreatedAt());
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STORY_SHOW_INTERESTED_CLICKED, properties);
            mViewModel.interestedStory(story);
        } else {
            openLoginFlow();
        }
    }

    @Override
    public void onMentionFollow(Story story, String author, String authorId, boolean isFollowing) {
        this.currentStory = story;
        if (CommonUtils.isUserLoggedin()) {
            mViewModel.followUnFollow(authorId, isFollowing);
            HashMap<String, Object> properties = baseProperties;
            properties.put("author_id", authorId);
            properties.put("is_following", isFollowing);
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STORY_MENTIONED_USER_FOLLOW_CLICKED, properties);
            Map<String, Object> map = new HashMap<>();
            map.put("is_first", CommonUtils.isFirstTimeFollow());
            map.put("author", author);
            map.put("source", SegmentConstants.SCREEN_VIEW_STORY);
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_CLICKED, map);
            CommonUtils.setFirstTimeFollow();
        } else {
            openLoginFlow();
        }
    }

    private void loadStories(List<Story> stories) {
        if (mStoryResult == null || mStoryResult.getUserProfile() == null) return;
        mBinding.loaderProgressbar.setVisibility(View.GONE);
        Collections.sort(stories, (o1, o2) -> {
            if (o1.getCreatedAt() > o2.getCreatedAt()) {
                return 1;
            } else if (o1.getCreatedAt() < o2.getCreatedAt()) {
                return -1;
            }
            return 0;
        });
        Author author = new Author(mStoryResult.getUserProfile().getUser().getProfileId(), mStoryResult.getUserProfile().getUser().getUsername(), mStoryResult.getUserProfile().getProfilePic(), "2h ago", stories);
        storyLayout = new StoryLayout(getContext(), author, mBinding.containerLayout, this, mViewModel.index != 0, CommonUtils.getAuthorId());
        storyLayout.start();
        if (!mFragmentSelected) {
            pauseCurrentStory(true);
        }
        try {
            while (mHashSet.iterator().hasNext()) {
                Runnable runnable = mHashSet.iterator().next();
                runnable.run();
                mHashSet.remove(runnable);
                printLog("loadStories -----> iterating ------> " + mStoryResult.getUserProfile().getUser().getUsername());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resumeCurrentStory() {
        if (storyLayout != null && !storyLayout.isPlaying())
            storyLayout.callPause(false, false, false);
    }

    private void pauseCurrentStory() {
        pauseCurrentStory(false);
    }

    void onFragmentSelected(boolean selected) {
        mFragmentSelected = selected;
        if (getView() != null) {
            getView().post(() -> {
                if (selected) {
                    if (mStoryResult == null) {
                        mHashSet.add(mStartRunnable);
                    } else {
                        mStartRunnable.run();
                    }
                } else {
                    if (mStoryResult == null) {
                        mHashSet.add(mStopRunnable);
                    } else {
                        mStopRunnable.run();
                    }
                }
            });
        }
    }

    public static void printLog(String text) {
        Log.d(StoryLayout.class.getSimpleName(), text);
    }

    private void pauseCurrentStory(boolean explicitPause) {
        if (storyLayout != null)
            storyLayout.callPause(true, false, explicitPause);
    }

    private void onFinish() {
        if (mViewModel.isFromDeeplink) {
            Intent intent = TabContainerActivity.newIntent(getContext());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_VIEW_STORY);
            startActivity(intent);
        }
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void updateFollowingStoryView(boolean flag) {
        if (storyLayout != null) {
            storyLayout.setFollowing(flag);
            storyLayout.checkAndUpdateFollowing(flag);
        }
    }

    private void updateInterestedStoryView() {
        if (storyLayout != null) {
            storyLayout.addInterestedCTA();
            storyLayout.checkAndUpdateInterest();
        }
    }

    private void buildShareSheet() {
        shareSheet = new BottomSheetMenuDialog.Builder()
                .header("Share via")
                .columns(3)
                .setAdjustWindow(false)
                .setListener(this::onShareItemClick);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        List<ResolveInfo> resolveInfoList = getActivity().getPackageManager().queryIntentActivities(intent, 0);
        ArrayList<OptionRequest> optionRequestList = new ArrayList<>();
        for (ResolveInfo res : resolveInfoList) {
            OptionRequest request = new OptionRequest(
                    res.labelRes,
                    res.loadLabel(getActivity().getPackageManager()).toString(),
                    res.loadIcon(getActivity().getPackageManager()),
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

        try {
            Collections.sort(optionRequestList, comparator);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        shareSheet.addAll(optionRequestList);
        shareSheet.build();
    }

    private void onShareItemClick(String s, Option option) {
        if (currentStory == null || currentStory.getId() == null) return;
        if (option.getTag() != null && option.getTag().equalsIgnoreCase(AppConstants.WHATSAPP_PACKAGE)) {
            shareStoryOnWhatsApp();
        } else if (option.getId() == -1) {
            resumeCurrentStory();
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(AppConstants.BRANCH_STORY_URL_SHARE, AppUtils.getStoryShareUrl(currentStory.getId()));
            map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_STORY);
            FirebaseDynamicLinkUtils.share(getContext(),
                    "story",
                    "story_share",
                    "Hey, Did you watch this amazing Story on Rheo.\n\nFor more such Stories download the *Rheo* app now\n",
                    "See this",
                    currentStory.getUrl(),
                    map,
                    AppUtils.getStoryShareUrl(currentStory.getId()),
                    option.getTag());
        }

        recordShareEvent(option.getTag());
    }

    private void shareStoryOnWhatsApp() {
        if (mStoryResult == null || mStoryResult.getUserProfile() == null) return;
        storyUrl = AppUtils.getStoryShareUrl(currentStory.getId());
        HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.BRANCH_STORY_URL_SHARE, AppUtils.getStoryShareUrl(currentStory.getId()));
        map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_STORY);
        FirebaseDynamicLinkUtils.FirebaseDynamicLinkData firebaseDynamicLinkData = new FirebaseDynamicLinkUtils.FirebaseDynamicLinkData();
        firebaseDynamicLinkData.setShareUrl(AppUtils.getStoryShareUrl(currentStory.getId()));
        firebaseDynamicLinkData.setTitle("Hey, Did you watch this amazing Story on Rheo.\n\nFor more such Stories download the *Rheo* app now\n");
        firebaseDynamicLinkData.setMap(map);
        firebaseDynamicLinkData.setImageUrl(currentStory.getUrl());
        firebaseDynamicLinkData.setCampaignInfo(currentStory.getAuthor());
        firebaseDynamicLinkData.setAuthorName(mStoryResult.getUserProfile().getUser().getUsername());
        firebaseDynamicLinkData.setIdentifier("story_share");
        firebaseDynamicLinkData.setDescription("See this");
        FirebaseDynamicLinkUtils.shareToExternal(getContext(), firebaseDynamicLinkData, new FirebaseDynamicLinkUtils.ShareLinkGenerateListener() {
            @Override
            public void onLinkGenerationSuccess(String shareUrl) {
                new DownloadShareManager.Builder()
                        .setContext(getContext())
                        .setDirType("/rheo_stories/")
                        .setSubPath(System.currentTimeMillis() + (currentStory.getType().endsWith(Constants.IMAGE) || currentStory.getType().endsWith(Constants.TEXT) ? "_story.jpg" : "_story.mp4"))
                        .setDownloadLink(currentStory.getUrl())
                        .setShareTitle(mStoryResult.getUserProfile().getUser().getUsername())
                        .setShareMessage("\nFor more such amazing Stories download the *Rheo* app now \n" + shareUrl + "/")
                        .build();
            }

            @Override
            public void onLinkGenerationFailure(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void recordShareEvent(String platform) {
        if (currentStory == null) return;
        HashMap<String, Object> properties = baseProperties;
        properties.put("story_id", currentStory.getId());
        properties.put("author", currentStory.getAuthor());
        properties.put("type", currentStory.getType());
        properties.put("created_at", currentStory.getCreatedAt());
        properties.put("platform", platform);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STORY_SHARE_CLICKED, properties);
    }

    private void recordSeenEvent(Story story) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("story_id", story.getId());
        properties.put("author", story.getAuthor());
        properties.put("type", story.getType());
        properties.put("created_at", story.getCreatedAt());
        properties.put("is_first", CommonUtils.isFirstStorySeen());
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STORY_SEEN, properties);
        CommonUtils.setFirstStorySeen();
    }

    private void showCreateStoryOption(Story story) {
        pauseCurrentStory();
        this.currentStory = story;
        new BottomSheetMenuDialog.Builder()
                .addAll(getOptions())
                .setListener(this::onCreateStoryItemClicked)
                .show(getChildFragmentManager(), "BottomSheetMenuDialog");
    }

    private ArrayList<OptionRequest> getOptions() {
        ArrayList<OptionRequest> options = new ArrayList<>();
        options.add(new OptionRequest(action_share, getString(R.string.share_only), R.drawable.ic_share_white_24dp));
        options.add(new OptionRequest(action_view_profile, getString(R.string.view_profile), R.drawable.ic_info_outline_black_24dp));
        options.add(new OptionRequest(action_report, getString(R.string.report), R.drawable.avd_report));
        return options;
    }

    private void onCreateStoryItemClicked(String s, Option option) {
        switch (option.getId()) {
            case action_share:
                showShareSheet();
                break;

            case action_view_profile:
                if (mStoryResult == null || mStoryResult.getUserProfile() == null) return;
                viewProfile(mStoryResult.getUserProfile().getUser().getUsername());
                break;

            case action_report:
                showReportDialog();
                break;

            default:
                resumeCurrentStory();
                break;

        }
    }

    private void showShareSheet() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!allPermissionsGranted()) {
                requestPermission();
                return;
            }
        }

        shareSheet.show(getChildFragmentManager(), "story");
    }

    private boolean allPermissionsGranted() {
        if (getContext() == null) return false;
        int result2 = ActivityCompat.checkSelfPermission(getContext(), READ_EXTERNAL_STORAGE);
        int result3 = ActivityCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE);
        return result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermission() {
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 101);
        requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, 102);
    }

    private void showReportDialog() {
        if (getContext() == null) return;
        recordReportClickEvent();
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.report_this_title))
                .setMessage(getString(R.string.report_content))
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    recordReportedEvent();
                    resumeCurrentStory();
                    if (currentStory != null)
                        mViewModel.reportStory(currentStory);
                })
                .setNegativeButton("No", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    resumeCurrentStory();
                }).show();
        pauseCurrentStory();
    }

    private void recordReportClickEvent() {
        if (currentStory == null) return;
        HashMap<String, Object> properties = baseProperties;
        properties.put("story_id", currentStory.getId());
        properties.put("author", currentStory.getAuthor());
        properties.put("type", currentStory.getType());
        properties.put("created_at", currentStory.getCreatedAt());
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_REPORT_STORY_CLICKED, properties);
    }

    private void recordReportedEvent() {
        if (currentStory == null) return;
        HashMap<String, Object> properties = baseProperties;
        properties.put("story_id", currentStory.getId());
        properties.put("author", currentStory.getAuthor());
        properties.put("type", currentStory.getType());
        properties.put("created_at", currentStory.getCreatedAt());
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STORY_REPORTED, properties);
    }

    private void openLoginFlow() {
        try {
            loginDialogFragment = LoginFragmentBottomDialog.getInstance(SegmentConstants.SCREEN_VIEW_STORY);
            if (loginDialogFragment.isAdded()) {
                return;
            }
            pauseCurrentStory();
            loginDialogFragment.setmCallback(new LoginFragmentBottomDialog.LoginFragmentCallback() {
                @Override
                public void onLoginSuccess() {
                    Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                    resumeCurrentStory();
                    if (getContext() == null) return;
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(AppConstants.UPDATE_STORY_BROADCAST_FILTER));
                }

                @Override
                public void onLoginDialogClose() {
                    resumeCurrentStory();
                }
            });
            loginDialogFragment.show(this.getChildFragmentManager(), AppConstants.LOGIN_FRAGMENT_TAG);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}

