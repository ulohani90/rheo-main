package com.rheotv.android.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.rheotv.android.R;
import com.rheotv.android.databinding.FragmentVideoAlertDialogBinding;
import com.rheotv.android.ui.base.BaseDialog;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoAlertDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoAlertDialogFragment extends BaseDialog {
    private static final String ARG_TIME_UNTIL_FINISH = "time_until_finish";
    private long timeUntilFinish = 0;
    private FragmentVideoAlertDialogBinding mBinding;

    private VideoAlertStayClickListener mListener;
    private CountDownTimer mVideoRewardCountDownTimer;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param timeUntilFinish Parameter 1.
     * @return A new instance of fragment VideoAlertDialogFragment.
     */
    public static VideoAlertDialogFragment newInstance(long timeUntilFinish, String source) {
        VideoAlertDialogFragment fragment = new VideoAlertDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TIME_UNTIL_FINISH, timeUntilFinish);
        args.putString(AppConstants.SCREEN_SOURCE, source);
        fragment.setArguments(args);
        return fragment;
    }

    public void show(FragmentManager fragmentManager, String tag, long timeUntilFinish) {
        this.timeUntilFinish = timeUntilFinish;
        try {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment prevFragment = fragmentManager.findFragmentByTag(tag);
            if (prevFragment != null) {
                transaction.remove(prevFragment);
            }
            transaction.commitAllowingStateLoss();
            show(transaction, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            timeUntilFinish = getArguments().getLong(ARG_TIME_UNTIL_FINISH);

            if (getArguments().containsKey(AppConstants.SCREEN_SOURCE))
                baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
            baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_VIDEO_EXIT_ALERT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_alert_dialog, container, false);
        mBinding.setTimeRemaining(CommonUtils.convertToMinAndSec(timeUntilFinish));
        mBinding.exitButton.setOnClickListener(this::exitClicked);
        mBinding.stayButton.setOnClickListener(this::stayClicked);
        startCountDown();

        return mBinding.getRoot();
    }

    private void stayClicked(View view) {
        dismiss();
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_VIDEO_TIMER_ALERT_STAY_CLICKED, baseProperties);
    }

    private void exitClicked(View view) {
        dismiss();
        if (mListener != null) {
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_VIDEO_TIMER_ALERT_EXIT_CLICKED, baseProperties);
            mListener.onVideoAlertExitClicked();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VideoAlertStayClickListener) {
            mListener = (VideoAlertStayClickListener) context;
        }

//        else {
//            throw new RuntimeException(context.toString()
//                    + " must implement NotAbleToPlayDialogListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mVideoRewardCountDownTimer.cancel();
    }

    public interface VideoAlertStayClickListener {
        void onVideoAlertStayClicked();

        void onVideoAlertExitClicked();
    }

    private void startCountDown() {
        mVideoRewardCountDownTimer = new CountDownTimer(timeUntilFinish, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                mBinding.setTimeRemaining(CommonUtils.convertToMinAndSec(millisUntilFinished));
            }

            @Override
            public void onFinish() {

            }
        };
        mVideoRewardCountDownTimer.start();
    }

}
