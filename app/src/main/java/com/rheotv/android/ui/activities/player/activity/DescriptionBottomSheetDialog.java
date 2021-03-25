package com.rheotv.android.ui.activities.player.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rheotv.android.R;
import com.rheotv.android.databinding.LayoutPostDescriptionBinding;
import com.rheotv.android.utils.AppConstants;

public class DescriptionBottomSheetDialog extends BottomSheetDialogFragment {

    private LayoutPostDescriptionBinding mBinding;

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    private static DescriptionBottomSheetDialog getInstance(String source, String description, String title, String game, String duration) {
        DescriptionBottomSheetDialog dialog = new DescriptionBottomSheetDialog();
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        bundle.putString(AppConstants.ARG_DESCRIPTION, description);
        bundle.putString(AppConstants.ARG_TITLE, title);
        bundle.putString(AppConstants.ARG_GAME_NAME, game);
        bundle.putString(AppConstants.ARG_DURATION, duration);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.layout_post_description, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adjustWindow(view);
        if (getArguments() != null && getArguments().containsKey(AppConstants.ARG_DESCRIPTION))
            mBinding.setDescription(getArguments().getString(AppConstants.ARG_DESCRIPTION));
        if (getArguments() != null && getArguments().containsKey(AppConstants.ARG_TITLE))
            mBinding.setTitle(getArguments().getString(AppConstants.ARG_TITLE));
        if (getArguments() != null && getArguments().containsKey(AppConstants.ARG_GAME_NAME))
            mBinding.setGame(getArguments().getString(AppConstants.ARG_GAME_NAME));
        if (getArguments() != null && getArguments().containsKey(AppConstants.ARG_DURATION))
            mBinding.setStreamingDuration(getArguments().getString(AppConstants.ARG_DURATION));
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
                        params.setMargins(220, 0, 220, 0);
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


    public static class Builder {
        private String title;
        private String game;
        private String streamDuration;
        private String description;
        private String source;

        public Builder addTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder addGame(String game) {
            this.game = game;
            return this;
        }

        public Builder addStreamDuration(String streamDuration) {
            this.streamDuration = streamDuration;
            return this;
        }

        public Builder addDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder addSource(String source) {
            this.source = source;
            return this;
        }

        public DescriptionBottomSheetDialog build() {
            return DescriptionBottomSheetDialog.getInstance(source, description, title, game, streamDuration);
        }

        /**
         * Build and show the [DescriptionBottomSheetDialog]
         */
        public DescriptionBottomSheetDialog show(FragmentManager fragmentManager, String tag) {
            DescriptionBottomSheetDialog dialog = build();
            dialog.show(fragmentManager, tag);
            return dialog;
        }
    }
}
