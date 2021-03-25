package com.rheotv.android.ui.activities.player.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.rheotv.android.R;
import com.rheotv.android.databinding.BottomSheetPlayerGiftBinding;
import com.rheotv.android.ui.activities.gamify.RewardsActivity;
import com.rheotv.android.ui.base.BaseBottomSheetDialogFragment;
import com.rheotv.android.utils.ViewUtils;

import java.util.Stack;

import javax.inject.Inject;

public class PlayerGiftBottomSheet extends BaseBottomSheetDialogFragment<BottomSheetPlayerGiftBinding, VideoRewardViewModel> {

    public static final String TAG = "PlayerGiftBottomSheet";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private VideoRewardViewModel mViewModel;
    private boolean mIsTimerCompleted = false;
    private Stack<Runnable> mAction = new Stack<>();
    private String mSource;

    private View.OnClickListener mOnViewClickListener = (view) -> {
        RewardsActivity.startMe(getContext(), mSource);
    };

    public static PlayerGiftBottomSheet newInstance(String source) {
        Bundle bundle = new Bundle();
        PlayerGiftBottomSheet fragment = new PlayerGiftBottomSheet();
        fragment.mSource = source;
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
        return R.layout.bottom_sheet_player_gift;
    }

    @Override
    public VideoRewardViewModel getViewModel() {
        if (mViewModel == null) {
            mViewModel = new ViewModelProvider(this, mViewModelFactory).get(VideoRewardViewModel.class);
        }
        mViewModel.viewState.observe(this, viewState -> {
            if (!mIsTimerCompleted) {
                mViewModel.showWatchVideoReward.set(viewState.isShowTimer());
                getViewDataBinding().timer.setProgress(viewState.getProgress(), 0);
                mViewModel.rewardSubTitle.set(viewState.getSubTitle());
                mViewModel.rewardTimeRemaining.set(viewState.getTitle());
            }
            if (viewState instanceof TimerInactive) {
                mIsTimerCompleted = true;
            }
        });
        return mViewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adjustWindow(view);
        getViewDataBinding().setViewModel(mViewModel);
        getViewDataBinding().viewMore.setOnClickListener(mOnViewClickListener);
        getViewDataBinding().viewMoreArrow.setOnClickListener(mOnViewClickListener);
        if (!mAction.empty()) {
            mAction.pop().run();
            mAction.clear();
        }
    }

    public void updateViewOnFinish(float progress, String title, String subtitle) {
        if (mViewModel == null) {
            mAction.push(() -> {
                mViewModel.updateView(new TimerInactive(progress, false, title, subtitle));
            });
        } else {
            mViewModel.updateView(new TimerInactive(progress, false, title, subtitle));
        }
    }

    public void updateViewOnTick(float progress, String title, String subtitle) {
        if (mViewModel == null) {
            mAction.push(() -> {
                mViewModel.updateView(new TimerActive(progress, true, title, subtitle));
            });
        } else {
            mViewModel.updateView(new TimerActive(progress, true, title, subtitle));
        }
    }

    static abstract class TimerViewState {
        protected float progress;
        protected boolean showTimer;
        protected String title;
        protected String subTitle;

        public float getProgress() {
            return progress;
        }

        public void setProgress(float progress) {
            this.progress = progress;
        }

        public boolean isShowTimer() {
            return showTimer;
        }

        public void setShowTimer(boolean showTimer) {
            this.showTimer = showTimer;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubTitle() {
            return subTitle;
        }

        public void setSubTitle(String subTitle) {
            this.subTitle = subTitle;
        }
    }

    public static class TimerActive extends TimerViewState {
        public TimerActive(float progress, boolean showTimer, String title, String subTitle) {
            this.progress = progress;
            this.showTimer = showTimer;
            this.title = title;
            this.subTitle = subTitle;
        }
    }

    public static class TimerInactive extends TimerViewState {
        public TimerInactive(float progress, boolean showTimer, String title, String subtitle) {
            this.progress = progress;
            this.showTimer = showTimer;
            this.title = title;
            this.subTitle = subtitle;
        }
    }
}
