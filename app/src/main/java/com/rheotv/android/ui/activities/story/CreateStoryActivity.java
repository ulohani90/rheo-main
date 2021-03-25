package com.rheotv.android.ui.activities.story;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.databinding.Observable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.rheotv.android.R;
import com.rheotv.android.databinding.ActivityCreateStoryBinding;
import com.rheotv.android.ui.activities.crop.CropImageActivity;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.activities.trim.TrimVideoActivity;
import com.rheotv.android.ui.adapters.AddedStoryAdapter;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.ui.customViews.bottomSheetMenu.BottomSheetMenuDialog;
import com.rheotv.android.ui.customViews.bottomSheetMenu.Option;
import com.rheotv.android.ui.customViews.bottomSheetMenu.OptionRequest;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.DownloadShareManager;
import com.rheotv.android.utils.FirebaseDynamicLinkUtils;
import com.rheotv.android.utils.recyclerdecorators.HorizontalSpacesItemDecoration;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.rheotv.story.Constants;
import com.rheotv.story.model.Story;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.rheotv.android.utils.AppConstants.GALLERY;
import static com.rheotv.android.utils.AppConstants.PERMISSION_REQUEST_CODE;

public class CreateStoryActivity extends BaseActivity<ActivityCreateStoryBinding, CreateStoryViewModel> implements
        HasAndroidInjector, AddedStoryAdapter.AddedStoryInteractionListener,
        StoryImageFragment.OnStoryInteractionListener, CreateStoryVideoFragment.OnVideoInteractionListener {

    private final String TAG = getClass().getSimpleName();
    private final int action_share = 100;

    private String storyUrl;

    @Inject
    DispatchingAndroidInjector<Object> fragmentDispatchingAndroidInjector;

    @Inject
    CreateStoryViewModel mViewModel;

    private ActivityCreateStoryBinding mBinding;
    private AddedStoryAdapter addedStoryAdapter;
    private StoryViewerBottomDialog storyDialog;

    private Story currentStory;
    private BottomSheetMenuDialog.Builder shareSheet;
    private HashMap<String, Object> baseProperties = new HashMap<>();
    private String sourceScreen = "";
    private ProgressDialog progressBar;

    @Override
    public AndroidInjector<Object> androidInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_story;
    }

    @Override
    public CreateStoryViewModel getViewModel() {
        mViewModel.authorId = CommonUtils.getAuthorId();
        mViewModel.loadStories();
        mViewModel.stories.observe(this, this::addMedia);

        mViewModel.publishedStatus.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (mViewModel.publishedStatus.get() == Status.SUCCESS) {
                    Toast.makeText(CreateStoryActivity.this, "Story Published!", Toast.LENGTH_SHORT).show();
                    recordPublishedEvent();
                    EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
                    finish();
                } else if (mViewModel.publishedStatus.get() == Status.ERROR) {
                    Toast.makeText(CreateStoryActivity.this, "Something went wrong, Please try Again!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mViewModel.deletedMedia.observe(this, story -> {
            Story nextStory = addedStoryAdapter.removeItem(story);
            if (nextStory == null) {
                getSupportFragmentManager().popBackStack("story", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                mViewModel.hasStories.set(false);
            } else {
                switchStories(nextStory);
            }
        });
        return mViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = getViewDataBinding();

        if (getIntent() != null && getIntent().hasExtra(AppConstants.SCREEN_SOURCE)) {
            sourceScreen = getIntent().getStringExtra(AppConstants.SCREEN_SOURCE);
            baseProperties.put(AppConstants.SCREEN_SOURCE, sourceScreen);
        }
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_CREATE_STORY);
        SegmentTracker.getInstance(this).recordScreenName(SegmentConstants.SCREEN_CREATE_STORY, baseProperties);
        mViewModel.baseProperties = baseProperties;

        setUpViews();
    }

    private void setUpViews() {
        addedStoryAdapter = new AddedStoryAdapter(this, mViewModel.storyList);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.margin_4);
        mBinding.addedStoryRv.addItemDecoration(new HorizontalSpacesItemDecoration(spacingInPixels));
        mBinding.addedStoryRv.setAdapter(addedStoryAdapter);
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
            finish();
        });
        mBinding.buttonGallery.setOnClickListener(v -> openGallery());
        mBinding.storyPlaceholderView.setOnClickListener(v -> openGallery());
        mBinding.buttonText.setOnClickListener(v -> openTemplateEditor());
        buildShareSheet();
        mViewModel.showLoading.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (mViewModel.showLoading.get()) {
                    progressBar = ProgressDialog.show(CreateStoryActivity.this, null, "Uploading Media..");
                } else {
                    if (progressBar != null && !isFinishing() && !isDestroyed())
                        progressBar.dismiss();
                }
            }
        });

        mViewModel.errorUploadStory.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (mViewModel.errorUploadStory.get() != null)
                    showErrorInUploadingStory(mViewModel.errorUploadStory.get());
            }
        });
    }

    public void showErrorInUploadingStory(Story story) {
        if (isFinishing() || isDestroyed()) return;
        new AlertDialog.Builder(this).setTitle("Error")
                .setMessage("A problem occurred in uploading media.")
                .setPositiveButton("Try again", (dialogInterface, i) -> {
                    mViewModel.uploadMedia(story);
                    dialogInterface.dismiss();
                })
                .setNegativeButton("Discard", (dialogInterface, i) -> dialogInterface.dismiss())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onAddMoreClick(Story story) {
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_ADD_MORE_STORY_CLICKED, baseProperties);
        switchStories(story);
    }

    public void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!allPermissionsGranted()) {
                requestPermission();
                return;
            }
        }

        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("*/*");
            startActivityForResult(intent, GALLERY);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "No Application found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openTemplateEditor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!allPermissionsGranted()) {
                requestPermission();
                return;
            }
        }

        Intent intent = new Intent(this, CreateStoryTemplateActivity.class);
        startActivityForResult(intent, CreateStoryTemplateActivity.ARG_TEMPLATE_CODE);
    }

    private boolean allPermissionsGranted() {
        int result2 = ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
        int result3 = ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        return result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermission() {
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 101);
        requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, 102);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            ViewUtils.showMessageOKCancel(this, getResources().getString(R.string.photo_upload_permission),
                                    (dialog, which) -> requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE));
                        } else {
                            Toast.makeText(this, getString(R.string.photo_upload_permission), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == GALLERY) {
                String[] params = CommonUtils.getMediaDetailFromIntent(data, this);
                if (params == null) {
                    Toast.makeText(CreateStoryActivity.this, "Couldn't load this media. Please try another", Toast.LENGTH_LONG).show();
                    return;
                }

                switch (params[0].toUpperCase()) {
                    case Constants.IMAGE:
                        Intent intent = new Intent(this, CropImageActivity.class);
                        intent.putExtra(CropImageActivity.ARG_IMAGE_URI, params[1]);
                        startActivityForResult(intent, CropImageActivity.CODE_CROP_IMAGE);
                        break;

                    case Constants.VIDEO:
                        Intent videoIntent = new Intent(this, TrimVideoActivity.class);
                        videoIntent.putExtra(TrimVideoActivity.ARG_VIDEO_URI, CommonUtils.getPath(CreateStoryActivity.this, data.getData()));
                        startActivityForResult(videoIntent, TrimVideoActivity.CODE_TRIM_VIDEO);
                        break;

                    default:
                        break;
                }
            } else if (requestCode == CropImageActivity.CODE_CROP_IMAGE) {
                Story story = new Story(data.getStringExtra(CropImageActivity.ARG_IMAGE_URI), Constants.IMAGE);
                addMedia(story);
            } else if (requestCode == TrimVideoActivity.CODE_TRIM_VIDEO) {
                Story story = new Story(data.getStringExtra(TrimVideoActivity.ARG_VIDEO_URI), Constants.VIDEO);
                addMedia(story);
            } else if (requestCode == CreateStoryTemplateActivity.ARG_TEMPLATE_CODE) {
                Story story = new Story(data.getStringExtra(CreateStoryTemplateActivity.ARG_TEMPLATE_URI), Constants.TEXT);
                story.setMetaData(data.getStringExtra(CreateStoryTemplateActivity.ARG_TEMPLATE_DATA));
                addMedia(story);
            }
        }
    }

    private void addMedia(ArrayList<Story> stories) {
        if (stories == null || stories.isEmpty()) return;
        Collections.sort(stories, (o1, o2) -> {
            if (o1.getCreatedAt() > o2.getCreatedAt()) {
                return 1;
            } else if (o1.getCreatedAt() < o2.getCreatedAt()) {
                return -1;
            }
            return 0;
        });
        addedStoryAdapter.addItems(stories);
        new Handler(Looper.getMainLooper()).postDelayed(() -> mBinding.addedStoryRv.smoothScrollToPosition(addedStoryAdapter.getItemCount()), 100);
        mViewModel.hasStories.set(addedStoryAdapter.getItemCount() > 0);
        switchStories(stories.get(stories.size() - 1));
    }

    private void addMedia(Story story) {
        story.addLoveCTA();
        addedStoryAdapter.addItem(story);
        new Handler(Looper.getMainLooper()).postDelayed(() -> mBinding.addedStoryRv.smoothScrollToPosition(addedStoryAdapter.getItemCount()), 100);
        mViewModel.hasStories.set(addedStoryAdapter.getItemCount() > 0);
        switchStories(story);
        mViewModel.uploadMedia(story);

        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("type", story.getType());
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_STORY_ADDED, properties);
    }

    private void switchStories(Story story) {
        switch (story.getType()) {
            case Constants.ADD_MORE:
                showCreateStoryOption();
                break;
            case Constants.TEXT:
            case Constants.IMAGE:
            case Constants.VIDEO:
                loadFragment(StoryImageFragment.getInstance(story, sourceScreen));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        EventBus.getDefault().post(EventBusModel.RefreshProfile.INSTANCE);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(mBinding.container.getId(), fragment, "story").commit();
    }

    private void showCreateStoryOption() {
        new BottomSheetMenuDialog.Builder()
                .add(R.menu.menu_create_story)
                .setListener(this::onSettingItemClicked)
                .show(getSupportFragmentManager(), "BottomSheetMenuDialog");
    }

    private void onSettingItemClicked(String s, Option option) {
        if (option.getId() == R.id.action_gallery)
            openGallery();
        else
            openTemplateEditor();
    }

    @Override
    public void onVideoMoreOptionClicked(Story story) {
        currentStory = story;
    }

    @Override
    public void onStoryShareOptionClicked(Story story) {
        currentStory = story;
        showShareSheet();
    }

    @Override
    public void onStoryDeleteOptionClicked(Story story) {
        currentStory = story;
        showDeleteDialog();

        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("type", story.getType());
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_STORY_DELETE_CLICKED, properties);
    }

    private void showShareSheet() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!allPermissionsGranted()) {
                requestPermission();
                return;
            }
        }

        shareSheet.show(getSupportFragmentManager(), "story");
    }

    private void buildShareSheet() {
        shareSheet = new BottomSheetMenuDialog.Builder()
                .header("Share via")
                .columns(3)
                .setAdjustWindow(false)
                .setListener(this::onShareItemClick);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(intent, 0);
        ArrayList<OptionRequest> optionRequestList = new ArrayList<>();
        for (ResolveInfo res : resolveInfoList) {
            OptionRequest request = new OptionRequest(
                    res.labelRes,
                    res.loadLabel(getPackageManager()).toString(),
                    res.loadIcon(getPackageManager()),
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
        if (option.getId() == -1) return;
        if (currentStory == null || currentStory.getId() == null) return;
        if (option.getTag() != null && option.getTag().equalsIgnoreCase(AppConstants.WHATSAPP_PACKAGE)) {
            shareStoryOnWhatsApp();
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put(AppConstants.BRANCH_STORY_URL_SHARE, AppUtils.getStoryShareUrl(currentStory.getId()));
            map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_STORY);
            FirebaseDynamicLinkUtils.share(this,
                    null,
                    "story_share",
                    "Hey, Did you watch this amazing Story on Rheo.\n\nFor more such Stories download the *Rheo* app now = \n",
                    "See this",
                    currentStory.getUrl(),
                    map,
                    AppUtils.getStoryShareUrl(currentStory.getId()),
                    option.getTag());
        }

        recordShareEvent(option.getTag());
    }

    private void shareStoryOnWhatsApp() {

        storyUrl = AppUtils.getStoryShareUrl(currentStory.getId());
        HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.BRANCH_STORY_URL_SHARE, AppUtils.getStoryShareUrl(currentStory.getId()));
        map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_STORY);
        FirebaseDynamicLinkUtils.FirebaseDynamicLinkData firebaseDynamicLinkData = new FirebaseDynamicLinkUtils.FirebaseDynamicLinkData();
        firebaseDynamicLinkData.setShareUrl(AppUtils.getStoryShareUrl(currentStory.getId()));
        firebaseDynamicLinkData.setTitle("Hey, Did you watch this amazing Story on Rheo.\n\nFor more such Stories download the *Rheo* app now\n");
        firebaseDynamicLinkData.setMap(map);
        firebaseDynamicLinkData.setImageUrl(currentStory.getUrl());
        firebaseDynamicLinkData.setCampaignInfo("story");
        firebaseDynamicLinkData.setIdentifier("story_share");
        firebaseDynamicLinkData.setDescription("See this");
        FirebaseDynamicLinkUtils.shareToExternal(this, firebaseDynamicLinkData, new FirebaseDynamicLinkUtils.ShareLinkGenerateListener() {
            @Override
            public void onLinkGenerationSuccess(String shareUrl) {
                new DownloadShareManager.Builder()
                        .setContext(CreateStoryActivity.this)
                        .setDirType("/rheo_stories/")
                        .setSubPath(System.currentTimeMillis() + (currentStory.getType().endsWith(Constants.IMAGE) || currentStory.getType().endsWith(Constants.TEXT) ? "_story.jpg" : "_story.mp4"))
                        .setDownloadLink(currentStory.getUrl())
                        .setShareTitle(CommonUtils.getUserName())
                        .setShareMessage("\nFor more such amazing Stories download the *Rheo* app now \n" + shareUrl + "/")
                        .build();
            }

            @Override
            public void onLinkGenerationFailure(String errorMessage) {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showDeleteDialog() {
        recordDeletedEvent();
        new AlertDialog.Builder(this).setTitle(getString(R.string.delete_this_title)).setMessage(getString(R.string.delete_content)).setPositiveButton("Yes", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            if (currentStory != null)
                mViewModel.deleteStory(currentStory);
        }).setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    private void recordShareEvent(String platform) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("type", currentStory.getType());
        properties.put("platform", platform);
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_SELF_STORY_SHARE_CLICKED, properties);
    }

    private void recordPublishedEvent() {
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_STORY_PUBLISHED, baseProperties);
    }

    private void recordDeletedEvent() {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("type", currentStory.getType());
        properties.put("state", currentStory.getState());
        SegmentTracker.getInstance(this).trackEvent(SegmentConstants.EVENT_STORY_DELETE_CLICKED, baseProperties);
    }

    @Override
    public void onWatchCountClicked(Story story) {
        storyViewer(story);
    }

    public void storyViewer(Story story) {
        if (storyDialog == null) {
            storyDialog = StoryViewerBottomDialog.getInstance(SegmentConstants.SCREEN_CREATE_STORY, story.getId(), story.getWatchCount() + "");
        }
        if (storyDialog.isAdded() || storyDialog.isVisible()) {
            return;
        }
        try {
            storyDialog.showNoAddToBackStack(this.getSupportFragmentManager(), AppConstants.LOGIN_FRAGMENT_TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startMe(Context context, String sourceScreen) {
        Intent intent = new Intent(context, CreateStoryActivity.class);
        intent.putExtra(AppConstants.SCREEN_SOURCE, sourceScreen);
        context.startActivity(intent);
    }
}
