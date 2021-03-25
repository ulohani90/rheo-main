package com.rheotv.android.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.rheotv.android.R;
import com.rheotv.android.databinding.FragmentRateUsBinding;
import com.rheotv.android.ui.base.BaseDialog;
import com.rheotv.android.ui.customViews.smileRating.SmileRating;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RateUsFragment.RateUsListener} interface
 * to handle interaction events.
 * Use the {@link RateUsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RateUsFragment extends BaseDialog {

    private RateUsListener mListener;
    private FragmentRateUsBinding mBinding;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static RateUsFragment newInstance(String source) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        RateUsFragment fragment = new RateUsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_rate_us, container, false);
        getDialog().setCanceledOnTouchOutside(false);
        setCancelable(false);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;
        setUpViews();

        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_RATE_US);
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_RATE_US, baseProperties);

        return mBinding.getRoot();
    }

    private void setUpViews() {
        mBinding.smileRating.setSelectedSmile(SmileRating.GREAT);
        mBinding.containerLayout.setOnClickListener(view -> {
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_RATE_US_CANCEL, new HashMap<>());
            dismiss();
        });
        mBinding.submitButton.setOnClickListener(view -> {
            HashMap<String, Object> properties = new HashMap<>(baseProperties);
            properties.put("rating", mBinding.smileRating.getRating());
            properties.put("feedback", mBinding.feedbackEditText.getText().toString());
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_APP_RATED, properties);
            mListener.onSubmitClick(mBinding.smileRating.getRating(), mBinding.feedbackEditText.getText().toString());
            dismiss();
        });

        mBinding.playStoreButton.setOnClickListener(view -> {
            HashMap<String, Object> properties = new HashMap<>(baseProperties);
            properties.put("rating", mBinding.smileRating.getRating());
            properties.put("feedback", mBinding.feedbackEditText.getText().toString());
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_APP_RATED, properties);
            mListener.onPlayStoreClick(mBinding.smileRating.getRating(), mBinding.feedbackEditText.getText().toString());
            dismiss();
        });

        mBinding.cancelButton.setOnClickListener(view -> {
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_RATE_US_CANCEL, baseProperties);
            mListener.onRatingCancelClick();
            dismiss();
        });

        mBinding.smileRating.setOnSmileySelectionListener((smiley, reselected) -> {
            switch (smiley) {
                case SmileRating.TERRIBLE:
                case SmileRating.BAD:
                case SmileRating.OKAY:
                case SmileRating.GOOD:
                    mBinding.feedbackEditText.setVisibility(View.VISIBLE);
                    mBinding.submitButton.setVisibility(View.VISIBLE);
                    mBinding.playStoreButton.setVisibility(View.GONE);
                    break;
                case SmileRating.GREAT:
                    mBinding.feedbackEditText.setVisibility(View.GONE);
                    mBinding.submitButton.setVisibility(View.GONE);
                    mBinding.playStoreButton.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RateUsListener) {
            mListener = (RateUsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement RateUsListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface RateUsListener {

        void onSubmitClick(int rating, String feedback);

        void onPlayStoreClick(int rating, String feedback);

        void onRatingCancelClick();
    }

    public void show(FragmentManager fragmentManager, String tag) {
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
}
