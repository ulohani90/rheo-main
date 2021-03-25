package com.rheotv.android.ui.activities.gamify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rheotv.android.R;
import com.rheotv.android.databinding.LayoutBottomSheetAlertBinding;

public class ConfirmationBottomSheetDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    public final String TAG = getClass().getSimpleName();
    public static final String ARG_GAME_NAME = "alert_game_name";
    public static final String ARG_USER_NAME = "alert_user_name";

    private static ItemClickListener mListener;
    private LayoutBottomSheetAlertBinding mBinding;

    public static ConfirmationBottomSheetDialog newInstance(ItemClickListener listener, String gameName, String userName) {
        mListener = listener;
        ConfirmationBottomSheetDialog fragment = new ConfirmationBottomSheetDialog();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_GAME_NAME, gameName);
        bundle.putString(ARG_USER_NAME, userName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.layout_bottom_sheet_alert, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            mBinding.setGameName(getArguments().getString(ARG_GAME_NAME));
            mBinding.setUserName(getArguments().getString(ARG_USER_NAME));
        }
        mBinding.negativeButton.setOnClickListener(v -> {
            if (mListener != null)
                mListener.onNegativeButtonClick();
            dismiss();
        });
        mBinding.positiveButton.setOnClickListener(v -> {
            if (mListener != null)
                mListener.onPositiveButtonClick();
            dismiss();
        });
    }

    @Override
    public void onClick(View view) {
        mListener.onPositiveButtonClick();
        dismiss();
    }

    public interface ItemClickListener {
        void onPositiveButtonClick();

        void onNegativeButtonClick();
    }

}
