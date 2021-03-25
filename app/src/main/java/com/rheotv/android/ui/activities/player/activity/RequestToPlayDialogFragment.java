package com.rheotv.android.ui.activities.player.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.rheotv.android.R;
import com.rheotv.android.databinding.FragmentRequestToPlayDialogBinding;
import com.rheotv.android.ui.base.BaseDialog;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.BindingUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RequestToPlayDialogFragment.OnRequestToPlayInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RequestToPlayDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestToPlayDialogFragment extends BaseDialog {
    private static final String ARG_REQUEST_ID = "arg_request_id";
    private static final String ARG_PLAYER_NAME = "arg_player_name";
    private static final String ARG_GAME_NAME = "arg_game_name";
    private static final String ARG_GAMER_PLAYER_ID = "arg_game_player_id";
    private static final String ARG_PROFILE_URL = "arg_profile_url";
    private static final String ARG_IS_VIEW_ONLY = "arg_is_view_only";
    private static final String ARG_CUSTOM_ROOM_ENABLED = "arg_is_custom_room_enabled";
    private static final String ARG_REQUEST_STATUS_ACCEPTED = "arg_request_status_accepted";

    // TODO: Rename and change types of parameters
    private String mRequestId;
    private String mPlayerName;
    private String mGameName;
    private String mGamePlayerId;
    private String mProfileUrl;
    private boolean mIsViewOnly;
    private boolean mIsCustomRoomEnabled;
    private boolean mIsRequestAccepted;

    private OnRequestToPlayInteractionListener mListener;
    private FragmentRequestToPlayDialogBinding mBinding;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param requestId    Parameter 1.
     * @param playerName   Parameter 2.
     * @param gameName     Parameter 2.
     * @param gamePlayerId Parameter 2.
     * @return A new instance of fragment RequestToPlayDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestToPlayDialogFragment newInstance(
            String requestId,
            String playerName,
            String gameName,
            String gamePlayerId,
            String profileUrl,
            boolean isViewOnly, boolean isCustomRoomEnabled, boolean requestAccepted
    ) {
        RequestToPlayDialogFragment fragment = new RequestToPlayDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST_ID, requestId);
        args.putString(ARG_PLAYER_NAME, playerName);
        args.putString(ARG_GAME_NAME, gameName);
        args.putString(ARG_GAMER_PLAYER_ID, gamePlayerId);
        args.putString(ARG_PROFILE_URL, profileUrl);
        args.putBoolean(ARG_IS_VIEW_ONLY, isViewOnly);
        args.putBoolean(ARG_CUSTOM_ROOM_ENABLED, isCustomRoomEnabled);
        args.putBoolean(ARG_REQUEST_STATUS_ACCEPTED, requestAccepted);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRequestId = getArguments().getString(ARG_REQUEST_ID);
            mPlayerName = getArguments().getString(ARG_PLAYER_NAME);
            mGameName = getArguments().getString(ARG_GAME_NAME);
            mGamePlayerId = getArguments().getString(ARG_GAMER_PLAYER_ID);
            mProfileUrl = getArguments().getString(ARG_PROFILE_URL);
            mIsViewOnly = getArguments().getBoolean(ARG_IS_VIEW_ONLY);
            mIsCustomRoomEnabled = getArguments().getBoolean(ARG_CUSTOM_ROOM_ENABLED);
            mIsRequestAccepted = getArguments().getBoolean(ARG_REQUEST_STATUS_ACCEPTED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_request_to_play_dialog, container, false);
        setUpViews();
        return mBinding.getRoot();
    }

    private void setUpViews() {
        mBinding.setCustomRoomEnabled(mIsCustomRoomEnabled);
        mBinding.setGameName(mGameName);
        mBinding.setGamerId(mGamePlayerId);
        mBinding.setUserName(mPlayerName);
        mBinding.setRequestAccepted(mIsRequestAccepted);
        //mBinding.setProfileUrl(mProfileUrl);
        BindingUtils.setProfileImageUrlFromCache(mBinding.profileImageView, mProfileUrl, true);
        mBinding.setIsViewOnly(mIsViewOnly);
        mBinding.cancelButton.setOnClickListener(view -> dismiss());
        mBinding.closeButton.setOnClickListener(view -> dismiss());
        mBinding.acceptButton.setOnClickListener(view -> {
            mListener.onPlayRequestAccept(mRequestId, AppConstants.PLAY_REQUEST_ACCEPT);
            dismiss();
        });
        mBinding.copyImageViw.setOnClickListener(view -> {
            if (getContext() != null) {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("PlayRequest", mBinding.usernameTextView.getText().toString());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getContext(), "Copied to Clipborad", Toast.LENGTH_SHORT).show();
            }
        });
        mBinding.winnerButton.setOnClickListener(view -> {
            mListener.onWinnerSelected(mRequestId);
            dismiss();
        });
    }

    public void show(FragmentManager fragmentManager, String tag,
                     OnRequestToPlayInteractionListener listener) {
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
    public interface OnRequestToPlayInteractionListener {

        void onPlayRequestAccept(String requestId, String action);

        void onWinnerSelected(String userId);
    }
}
