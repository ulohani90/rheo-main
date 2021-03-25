package com.rheotv.android.ui.activities.player.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.rheotv.android.R;
import com.rheotv.android.databinding.FragmentNotAbleToPlatDialogBinding;
import com.rheotv.android.ui.base.BaseDialog;
import com.rheotv.android.utils.AppConstants;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotAbleToPlayDialogListener} interface
 * to handle interaction events.
 * Use the {@link NotAbleToPlayDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotAbleToPlayDialogFragment extends BaseDialog {
    private static final String ARG_PLAYER_NAME = "arg_player_name";
    private static final String ARG_REQUEST_ID = "arg_request_id";
    private static final String ARG_PROFILE_URL = "arg_profile_url";

    private String mPlayerName;
    private String mRequestId;
    private String mProfileUrl;

    private NotAbleToPlayDialogListener mListener;
    private FragmentNotAbleToPlatDialogBinding mBinding;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param requestId  Parameter 1.
     * @param playerName Parameter 2.
     * @return A new instance of fragment NotAbleToPlayDialogFragment.
     */
    public static NotAbleToPlayDialogFragment newInstance(String requestId, String playerName, String profileUrl) {
        NotAbleToPlayDialogFragment fragment = new NotAbleToPlayDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST_ID, requestId);
        args.putString(ARG_PLAYER_NAME, playerName);
        args.putString(ARG_PROFILE_URL, profileUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlayerName = getArguments().getString(ARG_PLAYER_NAME);
            mRequestId = getArguments().getString(ARG_REQUEST_ID);
            mProfileUrl = getArguments().getString(ARG_PROFILE_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_not_able_to_plat_dialog, container, false);
        setUpViews();
        return mBinding.getRoot();
    }

    private void setUpViews() {
        mBinding.setUserName(mPlayerName);
        mBinding.setProfileUrl(mProfileUrl);
        mBinding.noButton.setOnClickListener(view -> dismiss());
        mBinding.yesButton.setOnClickListener(view -> {
            mListener.onRefundAction(mRequestId, AppConstants.PLAY_REQUEST_REFUND);
            dismiss();
        });
    }

    public void show(FragmentManager fragmentManager, String tag,
                     NotAbleToPlayDialogListener listener) {
        try {
            this.mListener = listener;
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
    public interface NotAbleToPlayDialogListener {
        void onRefundAction(String requestId, String action);
    }
}
