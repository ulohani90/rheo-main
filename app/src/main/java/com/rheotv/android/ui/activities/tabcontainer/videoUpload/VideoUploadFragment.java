package com.rheotv.android.ui.activities.tabcontainer.videoUpload;


import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.general.GameDetails;
import com.rheotv.android.databinding.LayoutUploadPostBinding;
import com.rheotv.android.helpers.FileUploadServiceHelper;
import com.rheotv.android.helpers.GameSpinner;
import com.rheotv.android.ui.base.BaseBottomSheetDialogFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import javax.inject.Inject;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.rheotv.android.app.RheoTvApp.getNonUiContext;
import static com.rheotv.android.utils.AppConstants.CAM;
import static com.rheotv.android.utils.AppConstants.GALLERY;


public class VideoUploadFragment extends BaseBottomSheetDialogFragment<LayoutUploadPostBinding, VideoUploadViewModel> {

    public static final String TAG = "VideoUploadFragment";
    private static final int PERMISSION_REQUEST_CODE = 102;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    public static final String FILTER_ACTION_KEY = "local_event";


    private String selectedPath;
    public String signedUrl;
    private Uri uriVideo;

    private String decodableStringReceived;
    private static final String MODE_PORTRAIT = "portrait";
    private static final String MODE_LANDSCAPE = "landscape";
    private boolean isUploadClicked = false;

    private LayoutUploadPostBinding mBinding;
    private VideoUploadViewModel mViewModel;
    private HashMap<String, Object> baseProperties = new HashMap<>();

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
        return R.layout.layout_upload_post;
    }

    public static VideoUploadFragment newInstance(String source) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        VideoUploadFragment fragment = new VideoUploadFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public VideoUploadViewModel getViewModel() {
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(VideoUploadViewModel.class);
        mViewModel.loadGameDetails();
        mViewModel.getGameResults().observe(this, list -> {
            if (list == null || list.isEmpty()) {
                return;
            }
            list.add(0, new GameDetails(null, "Select Game", null, false, false));

            mBinding.gameSpinner.setAdapter(new GameSpinner(list, getContext()));

        });

        mViewModel.getSignedUrl().observe(this, url -> {
            if (isUploadClicked && url != null && !url.isEmpty())
                uploadOnInternet();
        });

        return mViewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        mBinding.setViewModel(mViewModel);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
                FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior behavior;
                if (bottomSheet != null) {
                    behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        setupViews();

        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_UPLOAD_VIDEO);
        mViewModel.baseProperties = baseProperties;

        SegmentTracker.getInstance(getNonUiContext()).recordScreenName(SegmentConstants.SCREEN_UPLOAD_VIDEO, baseProperties);
        SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_UPLOAD_VIDEO_OPEN, baseProperties);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            ViewUtils.showMessageOKCancel(getContext(), getResources().getString(R.string.photo_upload_permission),
                                    (dialog, which) -> requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE));
                        } else {
                            Toast.makeText(getContext(), getNonUiContext().getResources().getString(R.string.photo_upload_permission), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    private void setupViews() {
        mBinding.doneButton.setOnClickListener(v -> dismiss());
        mBinding.cancelButton.setOnClickListener(v -> dismiss());
        mBinding.uploadThumbnailHolder.setOnClickListener(view -> openGallery());
        mBinding.thumbnailLayout.setOnClickListener(view -> openGallery());
        mBinding.submitButton.setOnClickListener(v -> {
            String livePostTitle = mViewModel.getPostTitle().get();
            isUploadClicked = true;
            if (livePostTitle == null || livePostTitle.isEmpty()) {
                isUploadClicked = false;
                mBinding.gameEditText.setError("Please enter name of the video.");
            } else {
                if (mViewModel.getGameId() != null) {
                    if (!mViewModel.getTermsCondition().get()) {
                        isUploadClicked = false;
                        Toast.makeText(getContext(), "Please accept the Terms and Conditions", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        if (decodableStringReceived != null && decodableStringReceived.length() > 0) {
                            if (mViewModel.getSignedUrl().getValue() != null) {
                                uploadOnInternet();
                            } else {
                                mViewModel.setUploadProgress(0);
                                if (!mViewModel.isSignedUrlApiCalled()) {
                                    mViewModel.buildSignedUrl();
                                }
                            }
                        } else {
                            isUploadClicked = false;
                            Toast.makeText(getContext(), "Please attach a video", Toast.LENGTH_SHORT).show();
                        }
                    }

                } else {
                    isUploadClicked = false;
                    Toast.makeText(getContext(), "Please select the game", Toast.LENGTH_SHORT).show();
                }
            }

            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_UPLOAD_VIDEO_SUBMIT_CLICK, baseProperties);
        });

        mBinding.gameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (((LinearLayout) mBinding.gameSpinner.getSelectedView()).getChildCount() < 1) return;
                ((TextView) ((LinearLayout) mBinding.gameSpinner.getSelectedView()).getChildAt(1)).setTextColor(getResources().getColor(R.color.light_grey_text_color));
                GameDetails details = (GameDetails) mBinding.gameSpinner.getSelectedItem();
                mViewModel.setGameId(details.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void uploadOnInternet() {
        isUploadClicked = false;
        if (decodableStringReceived != null && decodableStringReceived.length() > 0) {
            Intent videoUploadServiceIntent = new Intent(getContext(), FileUploadServiceHelper.class);
            videoUploadServiceIntent.putExtra(AppConstants.VIDEO_FILE_NAME, decodableStringReceived);
            videoUploadServiceIntent.putExtra(AppConstants.UPLOAD_URL_VIDEO, mViewModel.getSignedUrl().getValue());
            videoUploadServiceIntent.setAction(FileUploadServiceHelper.ACTION_START_FOREGROUND_SERVICE);
            getBaseActivity().startService(videoUploadServiceIntent);
            mViewModel.setUploadProgress(0);
        } else {
            Toast.makeText(getContext(), "Please attach a video", Toast.LENGTH_SHORT).show();
        }
    }

    public void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!allPermissionsGranted()) {
                requestPermission();
                return;
            }
        }

        try {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setType("video/*");
            startActivityForResult(galleryIntent, GALLERY);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "No Application found", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean allPermissionsGranted() {
        int result2 = ContextCompat.checkSelfPermission(getContext(), READ_EXTERNAL_STORAGE);
        int result3 = ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE);
        return result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermission() {
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == GALLERY || requestCode == CAM) && data != null) {
            try {
                mViewModel.setClipAdded(true);
                Uri contentURI = data.getData();

                String[] filePathColumn = {MediaStore.Video.Media.DATA};
                assert contentURI != null;
                Cursor cursor = getNonUiContext().getContentResolver().query(contentURI, filePathColumn, null, null, null);
                if (cursor == null) return;
                cursor.moveToFirst();
                decodableStringReceived = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                cursor.close();

                uriVideo = contentURI;
                selectedPath = getPath(contentURI);
                if (selectedPath == null) {
                    Toast.makeText(getContext(), "Couldn't find the video", Toast.LENGTH_LONG).show();
                    return;
                }

                MediaPlayer mp = MediaPlayer.create(getNonUiContext(), Uri.parse(selectedPath));
                if (mp == null) return;
                mViewModel.setVideoDuration(mp.getDuration());
                mViewModel.setVideoMode(mp.getVideoWidth() > mp.getVideoHeight() ? MODE_LANDSCAPE : MODE_PORTRAIT);
                mp.release();
                startVideo(contentURI);
                mViewModel.buildSignedUrl();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Couldn't find the video", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startVideo(Uri uri) {
        mBinding.gameVideoView.setVideoURI(uri);
        mBinding.gameVideoView.requestFocus();
        mBinding.gameVideoView.start();
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getBaseActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        IntentFilter filter = new IntentFilter(FILTER_ACTION_KEY);
        LocalBroadcastManager.getInstance(context).registerReceiver(onEvent, filter);
    }

    private BroadcastReceiver onEvent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getExtras().getInt("contentData");
            Log.i(getClass().getSimpleName(), "onEvent_called " + progress);
            mViewModel.updateProgress(progress);
            if (progress == 200) {
                mViewModel.createStory();
                if (mBinding != null) mBinding.progressText.setVisibility(View.GONE);
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_UPLOAD_VIDEO_COMPLETE, baseProperties);
                unregisterReceiver(context);
            } else if (progress >= 0 && progress <= 100) {
                if (mBinding != null) {
                    mBinding.progressText.setText(progress + "% uploaded");
                    mBinding.progressText.setVisibility(View.VISIBLE);
                }
            }

        }
    };

    private void unregisterReceiver(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(onEvent);
    }

}
