package com.rheotv.android.ui.activities.player.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.rheotv.android.R;
import com.rheotv.android.databinding.BottomSheetVideoQualityBinding;
import com.rheotv.android.model.VideoQuality;
import com.rheotv.android.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class PlayerVideoQualityBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "PlayerVideoQualityBottomSheet";
    private BottomSheetVideoQualityBinding mViewBinding;
    private VideoQualityChangeListener mVideoQualityChangeListener;
    private String mCheckedVideoQuality;
    private List<VideoQuality> qualityFormats;

    public static PlayerVideoQualityBottomSheet newInstance(List<VideoQuality> qualityFormats, String videoQuality) {
        Bundle bundle = new Bundle();
        PlayerVideoQualityBottomSheet fragment = new PlayerVideoQualityBottomSheet();
        fragment.mCheckedVideoQuality = videoQuality;
        fragment.qualityFormats = qualityFormats;
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = BottomSheetVideoQualityBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adjustWindow(view);
        setUpView();

        mViewBinding.chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (mVideoQualityChangeListener == null) return;
            switch (checkedId) {
                case R.id.quality_1:
                    mVideoQualityChangeListener.onVideoQualityChanged(qualityFormats.get(0).toString());
                    break;
                case R.id.quality_2:
                    mVideoQualityChangeListener.onVideoQualityChanged(qualityFormats.get(1).toString());
                    break;
                case R.id.quality_3:
                    mVideoQualityChangeListener.onVideoQualityChanged(qualityFormats.get(2).toString());
                    break;
                case R.id.quality_4:
                    mVideoQualityChangeListener.onVideoQualityChanged(qualityFormats.get(3).toString());
                    break;
                default:
                    try {
                        mVideoQualityChangeListener.onVideoQualityChanged(qualityFormats.get(4).toString());
                    } catch (IndexOutOfBoundsException e) {
                        mVideoQualityChangeListener.onVideoQualityChanged(qualityFormats.get(qualityFormats.size() - 1).toString());
                    }
                    break;
            }
            dismiss();
        });
    }

    public void setUpView() {
        if (qualityFormats == null) return;
        if (qualityFormats.size() > 4) {
            checkVideoQuality(mViewBinding.quality1, qualityFormats.get(0).toString());
            checkVideoQuality(mViewBinding.quality2, qualityFormats.get(1).toString());
            checkVideoQuality(mViewBinding.quality3, qualityFormats.get(2).toString());
            checkVideoQuality(mViewBinding.quality4, qualityFormats.get(3).toString());
            checkVideoQuality(mViewBinding.quality5, qualityFormats.get(4).toString());
        } else if (qualityFormats.size() > 3) {
            checkVideoQuality(mViewBinding.quality1, qualityFormats.get(0).toString());
            checkVideoQuality(mViewBinding.quality2, qualityFormats.get(1).toString());
            checkVideoQuality(mViewBinding.quality3, qualityFormats.get(2).toString());
            checkVideoQuality(mViewBinding.quality4, qualityFormats.get(3).toString());
            mViewBinding.quality5.setVisibility(View.GONE);
        } else if (qualityFormats.size() > 2) {
            checkVideoQuality(mViewBinding.quality1, qualityFormats.get(0).toString());
            checkVideoQuality(mViewBinding.quality2, qualityFormats.get(1).toString());
            checkVideoQuality(mViewBinding.quality3, qualityFormats.get(2).toString());
            mViewBinding.quality4.setVisibility(View.GONE);
            mViewBinding.quality5.setVisibility(View.GONE);
        } else if (qualityFormats.size() > 1) {
            checkVideoQuality(mViewBinding.quality1, qualityFormats.get(0).toString());
            checkVideoQuality(mViewBinding.quality2, qualityFormats.get(1).toString());
            mViewBinding.quality3.setVisibility(View.GONE);
            mViewBinding.quality4.setVisibility(View.GONE);
            mViewBinding.quality5.setVisibility(View.GONE);
        } else if (!qualityFormats.isEmpty()) {
            checkVideoQuality(mViewBinding.quality1, qualityFormats.get(0).toString());
            mViewBinding.quality2.setVisibility(View.GONE);
            mViewBinding.quality3.setVisibility(View.GONE);
            mViewBinding.quality4.setVisibility(View.GONE);
            mViewBinding.quality5.setVisibility(View.GONE);
        } else {
            Toast.makeText(getContext(), "Only one video format is available!", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    private void checkVideoQuality(Chip chip, String text) {
        chip.setVisibility(View.VISIBLE);
        chip.setText(text);
        if (text.equalsIgnoreCase(mCheckedVideoQuality)) {
            chip.setChecked(true);
        }
    }

    private void adjustWindow(View view) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
                FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    CoordinatorLayout.LayoutParams params;
                    if (bottomSheet != null) {
                        params = (CoordinatorLayout.LayoutParams) bottomSheet.getLayoutParams();
                        params.setMargins(ViewUtils.dpToPx(80), 0, ViewUtils.dpToPx(80), 0);
                        bottomSheet.setLayoutParams(params);
                        if (dialog.getWindow() != null) {
                            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                        }

                    }
                }

                BottomSheetBehavior behavior;
                if (bottomSheet != null) {
                    behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
    }

    void setCheckedVideoQuality(String videoQuality) {
        mCheckedVideoQuality = videoQuality;
    }

    public void setVideoQualitySelectionListener(VideoQualityChangeListener videoQualitySelectionListener) {
        mVideoQualityChangeListener = videoQualitySelectionListener;
    }

    public interface VideoQualityChangeListener {
        void onVideoQualityChanged(String videoQuality);
    }
}
