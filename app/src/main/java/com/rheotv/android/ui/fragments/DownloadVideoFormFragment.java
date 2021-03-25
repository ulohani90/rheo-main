package com.rheotv.android.ui.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.rheotv.android.R;
import com.rheotv.android.databinding.DownloadVideoFragmentLayoutBinding;
import com.rheotv.android.ui.base.BaseBottomSheetDialogFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import java.util.HashMap;

import javax.inject.Inject;

public class DownloadVideoFormFragment extends BaseBottomSheetDialogFragment<DownloadVideoFragmentLayoutBinding, DownloadVideoFragmentViewModel> implements DownloadVideoFragmentNavigator {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private String postId;

    private DownloadVideoFragmentViewModel mViewModel;
    private DownloadVideoFragmentLayoutBinding mBinding;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static DownloadVideoFormFragment newInstance(String postId, String source) {
        DownloadVideoFormFragment fragment = new DownloadVideoFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString("post_id", postId);
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        fragment.setArguments(bundle);
        return fragment;
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
        return R.layout.download_video_fragment_layout;
    }

    @Override
    public DownloadVideoFragmentViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(DownloadVideoFragmentViewModel.class);
        mViewModel.downloadComplete.observe(this, status -> {
            if (status == Status.SUCCESS)
                onDownloadVideoRequestSuccess();
            else if (status == Status.ERROR)
                onDownloadVideoRequestFailure();
        });
        return mViewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mBinding = getViewDataBinding();
        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_DOWNLOAD_VIDEO_FORM);

        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_DOWNLOAD_VIDEO_FORM, baseProperties);

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
                    //behavior.setPeekHeight(0); // Remove this line to hide a dark background if you manually hide the dialog.
                }

            }
        });
        postId = getArguments().getString("post_id");
        if (postId == null || postId.length() == 0) {
            dismiss();
        }

        setupViews();
    }

    private void setupViews() {
        SpannableString downloadNoteText = new SpannableString(getString(R.string.download_note_text));
        downloadNoteText.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //downloadNoteText.setSpan(new ForegroundColorSpan(Color.parseColor("#f1000c")), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBinding.downloadNote.setText(downloadNoteText.toString());
        mBinding.submitButton.setOnClickListener(v -> submitVideoDownloadRequest());
        mBinding.cancelButton.setOnClickListener(v -> dismiss());
    }

    private void submitVideoDownloadRequest() {
        int checkID = mBinding.qualityChipGroup.getCheckedChipId();
        String quality = checkID == R.id.video_quality_high ? "high" : (checkID == R.id.video_quality_low ? "low" : "medium");
        mViewModel.submitDownloadVideoRequest(postId, quality);
    }

    public void onDownloadVideoRequestSuccess() {
        Toast.makeText(getContext(), "You will receive an email with the download link shortly", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    public void onDownloadVideoRequestFailure() {
        Toast.makeText(getContext(), "Something went wrong. Please try again in some time.", Toast.LENGTH_SHORT).show();
    }
}
