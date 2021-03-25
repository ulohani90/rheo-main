package com.rheotv.android.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rheotv.android.R;
import com.rheotv.android.ui.adapters.BottomSheetOptionsAdapter;

import java.util.ArrayList;
import java.util.List;

public class CommonBottomSheetDialog extends BottomSheetDialogFragment {

    List<String> options;

    View rootView;

    BottomSheetItemClickListener mListener;

    public static CommonBottomSheetDialog newInstance(@NonNull Context context, ArrayList<String> options) {
        CommonBottomSheetDialog dialog = new CommonBottomSheetDialog();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("options", options);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.common_bottom_sheet_layout, null);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        options = getArguments().getStringArrayList("options");
        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.options_rv);
        BottomSheetOptionsAdapter adapter = new BottomSheetOptionsAdapter(options, new BottomSheetOptionsAdapter.ItemClickListener() {
            @Override
            public void onItemClicked(int position) {
                mListener.onItemClicked(position);
                dismiss();
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rv.setAdapter(adapter);
    }

    public void showNoAddToBackStack(FragmentManager fragmentManager, String tag) {
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

    public interface BottomSheetItemClickListener {
        void onItemClicked(int position);
    }
}
