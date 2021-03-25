package com.rheotv.android.ui.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.general.GameDetails;
import com.rheotv.android.databinding.LayoutDialogLiveStreamBinding;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.helpers.ErrorMessage;
import com.rheotv.android.helpers.GameSpinner;
import com.rheotv.android.ui.activities.rank.RankActivity;
import com.rheotv.android.ui.base.BaseBottomSheetDialogFragment;
import com.rheotv.android.ui.customViews.WebviewActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.DownloadShareManager;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import okhttp3.MultipartBody;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.rheotv.android.utils.AppConstants.RHEO_STUDIO_PACKAGE_NAME;

public class LiveStreamingDialogFragment extends BaseBottomSheetDialogFragment<LayoutDialogLiveStreamBinding, LiveStreamViewModel> {

    public static final String TAG = "LiveStreamingDialogFragment";
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private Uri uploadedImageUri = null;

    private static final int GALLERY = 901;
    private static final int PERMISSION_REQUEST_CODE = 111;

    private LayoutDialogLiveStreamBinding mBinding;
    private LiveStreamViewModel mViewModel;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static LiveStreamingDialogFragment getInstance(String source) {
        LiveStreamingDialogFragment fragment = new LiveStreamingDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new BottomSheetDialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                if (mViewModel.getPlatformSelected().get() && !mViewModel.getSubmitted().get()) {
                    mViewModel.setPlatformSelected(false);
                } else {
                    super.onBackPressed();
                }
            }
        };
    }

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
        return R.layout.layout_dialog_live_stream;
    }

    @Override
    public LiveStreamViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(LiveStreamViewModel.class);
        mViewModel.checkFeaturesEnabledPermission();
        mViewModel.loadGameDetails();

        mViewModel.getGameResults().observe(this, gameDetailsList -> {
            if (gameDetailsList == null || gameDetailsList.isEmpty()) {
                return;
            }
            gameDetailsList.add(0, new GameDetails(null, "Select Game", null, false, false));
            mBinding.gameSpinner.setAdapter(new GameSpinner(gameDetailsList, getContext()));
            mBinding.gameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mBinding.gameSpinner.getSelectedView() != null && ((TextView) ((LinearLayout) mBinding.gameSpinner.getSelectedView()).getChildAt(1) != null)) {
                        ((TextView) ((LinearLayout) mBinding.gameSpinner.getSelectedView()).getChildAt(1)).setTextColor(getResources().getColor(R.color.light_grey_text_color));
                    }
                    GameDetails details = (GameDetails) mBinding.gameSpinner.getSelectedItem();
                    mViewModel.setShowTakeRequest(details.canAcceptPlayRequest());
                    mViewModel.setAllowCustomRoom(details.isCustomRoomEnabled());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        });



        mViewModel.getRtmDetail().observe(this, rtmpDetails -> {
            if (rtmpDetails == null || rtmpDetails.getBase_url() == null || rtmpDetails.getBase_url().isEmpty() || rtmpDetails.getKey() == null || rtmpDetails.getKey().isEmpty()) {
                handleError(ErrorMessage.SERVER_RESPONSE_NULL);
            } else {
                mViewModel.setSubmitted(true);
            }
        });

        return mViewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_GO_LIVE_DIALOG);
        mViewModel.baseProperties = baseProperties;
        mBinding.setViewModel(mViewModel);
        SegmentTracker.getInstance(getActivity()).recordScreenName(SegmentConstants.SCREEN_NAME_GO_LIVE_DIALOG, baseProperties);

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
                    //behavior.setPeekHeight(0); // Remove this line to hide a dark background if you manually hide the dialog.
                }

            }
        });

        setupViews();
    }

    private void setupViews() {
        mBinding.termsCheckBox.setText(getSpannableText(getString(R.string.read_terms_text)));
        mBinding.termsCheckBox.setMovementMethod(LinkMovementMethod.getInstance());
        mBinding.streamTextView.setOnClickListener(v -> launchWebView("https://www.rheotv.com/streaming-steps/"));
        mBinding.doneButton.setOnClickListener(v -> dismiss());
        mBinding.cancelButton.setOnClickListener(v -> dismiss());
        mBinding.uploadThumbnailHolder.setOnClickListener(view -> openGallery());
        String learnMore = mBinding.learnMoreRheoStudioTextView.getText().toString();
        SpannableString spannableString = new SpannableString(learnMore + "Learn More");
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STUDIO_APP_LEARN_MORE_BUTTON_CLICKED, baseProperties);
                Intent intent = new Intent(getContext(), WebviewActivity.class);
                intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_GO_LIVE_DIALOG);
                intent.putExtra("URL", "https://rheotv.com/learn_more/");
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(getContext(), R.color.color_accent));
                ds.setUnderlineText(true);
            }
        }, learnMore.length(), learnMore.length() + "Learn More".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBinding.learnMoreRheoStudioTextView.setMovementMethod(LinkMovementMethod.getInstance());
        mBinding.learnMoreRheoStudioTextView.setText(spannableString);
        mBinding.submitButton.setOnClickListener(view -> {
            String livePostTitle = mViewModel.getLivePostTitle().get();
            String rheoCoinCount = mViewModel.getRheoCoinCount().get();
            if (livePostTitle == null || livePostTitle.isEmpty()) {
                mBinding.gameEditText.setError("Please enter name of the live video.");
            } else if (mViewModel.allowCustomRoom.get() != null && mViewModel.getTakeGameRequest().get() != null && mViewModel.allowCustomRoom.get() && mViewModel.getTakeGameRequest().get() && (rheoCoinCount == null || rheoCoinCount.isEmpty())) {
                Toast.makeText(getActivity(), "Enter entry price for Custom Room", Toast.LENGTH_LONG).show();
            } else {
                GameDetails gameDetails = (GameDetails) mBinding.gameSpinner.getSelectedItem();
                if (gameDetails != null && gameDetails.getId() != null) {
                    if (!mViewModel.getTermsCondition().get()) {
                        Toast.makeText(getContext(), "Please accept the Terms and Conditions", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String gameID = gameDetails.getId();
                    Bitmap bitmap;
                    MultipartBody.Part part = null;
                    if (uploadedImageUri != null) {
                        try {
                            bitmap = decodeSampledBitmapFromUri(Objects.requireNonNull(getContext()), uploadedImageUri);
                            if (bitmap != null)
                                part = CommonUtils.getMultiPartFile(getContext(), bitmap, "file");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    mViewModel.createLivePostAndGetRTMPURL(livePostTitle, gameID, gameDetails.getName(), part, (mViewModel.allowCustomRoom.get() && mViewModel.getTakeGameRequest().get() && rheoCoinCount != null && !rheoCoinCount.isEmpty()) ? Integer.parseInt(rheoCoinCount) : 0);
                } else {
                    Toast.makeText(getContext(), "Please select the game to stream.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        mBinding.mobileStreamFromOtherBtn.setOnClickListener(view -> {
            mViewModel.setPlatformSelected(true);
            mViewModel.mobileSelected.set(true);
        });

        mBinding.continueButton.setOnClickListener(view -> {
            mViewModel.setPlatformSelected(true);
            mViewModel.mobileSelected.set(false);
        });

        mBinding.openRheoStudio.setOnClickListener(view -> {
            openRheoStudio();
            dismiss();
        });
        mBinding.bannerImageView.setOnClickListener(v -> {
            RankActivity.startMe(this, getActivity(),
                    CommonUtils.getPaymentModel(getContext()),
                    CommonUtils.getUserID(getContext()),
                    CommonUtils.getLevelType(getContext()),
                    (String) baseProperties.get(AppConstants.SCREEN_SOURCE));
        });
    }

    private void openRheoStudio() {
        try {
            Map<String, Object> map = new HashMap<>(baseProperties);
            map.put("has_installed_studio_app", false);
            if (DownloadShareManager.isAppInstalled(RHEO_STUDIO_PACKAGE_NAME)) {
                Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage(RHEO_STUDIO_PACKAGE_NAME);
                if (launchIntent != null) {
                    map.put("has_installed_studio_app", true);
                    startActivity(launchIntent);//null pointer check in case package name was not found
                }
            } else {
                openStudioInPlayStore(RHEO_STUDIO_PACKAGE_NAME);
            }
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_OPEN_STUDIO_APP_BUTTON_CLICKED, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openStudioInPlayStore(String appPackageName) {
        // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private SpannableString getSpannableText(String res) {
        SpannableString builder = new SpannableString(res);
        builder.setSpan(new UnderlineSpan(), 16, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                launchWebView("https://www.rheotv.com/ugc_policy");
            }
        }, 16, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isPermissionGranted()) {
                requestPermission();
                return;
            }
        }

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Thumbnail");
        startActivityForResult(chooserIntent, GALLERY);
    }

    private void requestPermission() {
        if (getActivity() != null)
            ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(RheoTvApp.getNonUiContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
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
                            ViewUtils.showMessageOKCancel(getContext(), getResources().getString(R.string.photo_upload_permission),
                                    (dialog, which) -> requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE));
                        } else {
                            Toast.makeText(getContext(), RheoTvApp.getNonUiContext().getResources().getString(R.string.photo_thumbnail_permission), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == GALLERY) && data != null) {
            uploadedImageUri = data.getData();
            Picasso.get()
                    .load(uploadedImageUri)
                    .placeholder(this.getResources().getDrawable(R.drawable.upload_cloud_new_wt_text))
                    .into(mBinding.gameThumbnailImageView);
        }
    }

    private static Bitmap decodeSampledBitmapFromUri(Context context, Uri imageUri) throws FileNotFoundException {
        Bitmap bitmap = null;
        try {
            // Get input stream of the story
            final BitmapFactory.Options options = new BitmapFactory.Options();
            InputStream iStream = context.getContentResolver().openInputStream(imageUri);

            // First decode with inJustDecodeBounds=true to check dimensions
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(iStream, null, options);
            if (iStream != null) {
                iStream.close();
            }
            iStream = context.getContentResolver().openInputStream(imageUri);

            // Calculate inSampleSize
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeStream(iStream, null, options);
            if (iStream != null) {
                iStream.close();
            }
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void handleError(ErrorMessage.ErrorMessageItem errorMessageItem) {
        AnalyticsHelper.getInstance(getContext()).sendCreateLivePostEvent(false, errorMessageItem.getCode());
        Toast.makeText(getContext(), errorMessageItem.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void launchWebView(String url) {
        Intent intent = new Intent(getActivity(), WebviewActivity.class);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_GO_LIVE_DIALOG);
        intent.putExtra("URL", url);
        startActivity(intent);
    }


}
