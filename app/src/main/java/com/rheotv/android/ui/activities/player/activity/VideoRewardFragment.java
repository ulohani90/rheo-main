package com.rheotv.android.ui.activities.player.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.rheotv.android.R;
import com.rheotv.android.databinding.FragmentVideoRewardBinding;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import java.util.HashMap;

import javax.inject.Inject;

import static com.rheotv.android.utils.AppConstants.ARG_GLOBAL_VIDEO_REWARD_TIME;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VideoRewardFragment.OnVideoRewardFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VideoRewardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoRewardFragment extends BaseFragment<FragmentVideoRewardBinding, VideoRewardViewModel> {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private FragmentVideoRewardBinding mBinding;
    private VideoRewardViewModel mViewModel;

    private long TOTAL_PROGRESS_TIME = 0;
    public long TIME_UNTIL_FINISH = 0;
    private long TIME_DELAY_TO_SHOW_VIDEO_ALERT = 0;
    private CountDownTimer mVideoRewardCountDownTimer;
    private boolean rewardTimerComplete = false;
    private boolean isPostLive = false;

    private OnVideoRewardFragmentInteractionListener mListener;

    private long globalVideoRewardTime = 0;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static VideoRewardFragment newInstance() {
        return new VideoRewardFragment();
    }

    public static VideoRewardFragment newInstance(long globalVideoTime, String source) {
        VideoRewardFragment fragment = new VideoRewardFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(ARG_GLOBAL_VIDEO_REWARD_TIME, globalVideoTime);
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        mBinding.setViewModel(mViewModel);
        if (getArguments() != null) {
            globalVideoRewardTime = getArguments().getLong(ARG_GLOBAL_VIDEO_REWARD_TIME, 0);
            if (getArguments().containsKey(AppConstants.SCREEN_SOURCE))
                baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
            baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_VIDEO_WATCH_REWARD);
        }

        setRewards();
    }

    void setRewards() {
        TOTAL_PROGRESS_TIME = RewardManager.getInstance().getVideoRewardActivationTime();
        TIME_DELAY_TO_SHOW_VIDEO_ALERT = RewardManager.getInstance().getVideoRewardAlertDelayTime();
        if (mListener != null)
            isPostLive = mListener.isPostLive();

        TIME_UNTIL_FINISH = 0;
        if (RewardManager.getInstance().isTenMinuteStreamRewardAvailable()) {
            if (globalVideoRewardTime == -1) {
                TOTAL_PROGRESS_TIME = 0;
                Log.i(getClass().getSimpleName(), "checking_setRewards_from_service " + globalVideoRewardTime);
            } else if (globalVideoRewardTime > 0)
                TOTAL_PROGRESS_TIME = globalVideoRewardTime;
        }

        Log.i(getClass().getSimpleName(), "setRewards_globalVideoRewardTime " + globalVideoRewardTime);

        mBinding.rewardProgressBar.setShowTimer(false);
        mBinding.rewardProgressBar.setOnClickListener((view) -> recordRewardTimeClickEvent());
        mViewModel.showWatchVideoReward.set(RewardManager.getInstance().isTenMinuteStreamRewardAvailable());
        mVideoRewardCountDownTimer = new CountDownTimer(TOTAL_PROGRESS_TIME, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                TIME_UNTIL_FINISH = millisUntilFinished;
                mViewModel.rewardTimeRemaining.set(CommonUtils.convertToMinAndSec(millisUntilFinished));
                mBinding.rewardProgressBar.setProgress(((float) TOTAL_PROGRESS_TIME - millisUntilFinished) / TOTAL_PROGRESS_TIME, millisUntilFinished);
                if (mListener != null) {
                    mListener.updateVideoTimer(TIME_UNTIL_FINISH);
                }
            }

            @Override
            public void onFinish() {
                if (!rewardTimerComplete) {
                    rewardTimerComplete = true;
                    mBinding.rewardProgressBar.setProgress(1f, TOTAL_PROGRESS_TIME);
                    mViewModel.showWatchVideoReward.set(false);
                    if (mListener != null) {
                        mListener.updateVideoWatchAlert(false);
                        mListener.checkVideoReward();
                        mListener.updateVideoTimer(0);
                    }
                }
            }
        };

        Log.i(getClass().getSimpleName(), "isTenMinuteStreamRewardAvailable: " + RewardManager.getInstance().isTenMinuteStreamRewardAvailable() + " isLive " + isPostLive);
        mViewModel.isLoggedIn.set(CommonUtils.isUserLoggedin());
        if (CommonUtils.isUserLoggedin()) {
            if (RewardManager.getInstance().isTenMinuteStreamRewardAvailable()) {
                if (isPostLive) {
                    mVideoRewardCountDownTimer.start();
                    activateExitAlert();
                    mViewModel.rewardSubTitle.set(getString(R.string.keep_watching_video_message));
                    mBinding.rewardImageView.setImageResource(R.drawable.avd_gift_box);
                } else {
                    mBinding.rewardProgressBar.setProgress(0, 0);
                    mViewModel.rewardTimeRemaining.set("Watch Live");
                    mViewModel.rewardSubTitle.set(getString(R.string.reward_live_stream_message));
                    mViewModel.showWatchVideoReward.set(false);
                    mBinding.rewardImageView.setImageResource(R.drawable.avd_gift_box_color);
                }
            } else {
                mBinding.rewardProgressBar.setProgress(100, 0);
                mViewModel.rewardTimeRemaining.set("You're Rewarded");
                mViewModel.rewardSubTitle.set(getString(R.string.after_video_rewarded_subtitle));
                mViewModel.showWatchVideoReward.set(false);
                mBinding.rewardProgressBar.setVisibility(View.INVISIBLE);
                mBinding.rewardImageView.setImageResource(R.drawable.ic_confetti);
            }
        } else {
            mBinding.rewardProgressBar.setProgress(100, 0);
            mViewModel.rewardTimeRemaining.set(getString(R.string.login));
            mViewModel.rewardSubTitle.set(getString(R.string.login_to_get_reward_message));
            mViewModel.showWatchVideoReward.set(false);
            mBinding.rewardProgressBar.setVisibility(View.INVISIBLE);
        }

        mBinding.cardView.setOnClickListener(v -> {
            if (getActivity() instanceof PlayerActivity && !CommonUtils.isUserLoggedin())
                ((PlayerActivity) getActivity()).openLoginFlow();
        });
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_video_reward;
    }

    @Override
    public VideoRewardViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(VideoRewardViewModel.class);
        return mViewModel;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnVideoRewardFragmentInteractionListener) {
            mListener = (OnVideoRewardFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnVideoRewardFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mVideoRewardCountDownTimer != null && CommonUtils.isUserLoggedin() && RewardManager.getInstance().isTenMinuteStreamRewardAvailable() && isPostLive)
            mVideoRewardCountDownTimer.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mVideoRewardCountDownTimer != null)
            mVideoRewardCountDownTimer.cancel();
    }

    private void recordRewardTimeClickEvent() {
        HashMap<String, Object> property = baseProperties;
        property.put("postId", mListener.getCurrentPostId());
        property.put("userName", CommonUtils.getUserName(getContext()));
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_VIDEO_PROGRESS_CLICKED, property);
    }

    private void activateExitAlert() {
        new Handler().postDelayed(() -> {
            if (CommonUtils.isUserLoggedin() && mListener != null) {
                mListener.updateVideoWatchAlert(true);
            }
        }, TIME_DELAY_TO_SHOW_VIDEO_ALERT);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnVideoRewardFragmentInteractionListener {

        void checkVideoReward();

        String getCurrentPostId();

        void updateVideoWatchAlert(boolean flag);

        boolean isPostLive();

        void updateVideoTimer(long ttl);
    }
}
