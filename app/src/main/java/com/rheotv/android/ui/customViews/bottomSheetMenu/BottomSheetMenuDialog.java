package com.rheotv.android.ui.customViews.bottomSheetMenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rheotv.android.R;
import com.rheotv.android.databinding.BottomSheetDialogFragmentBinding;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetMenuDialog extends BottomSheetDialogFragment {

    private static String KEY_OPTIONS = "options";
    private static String KEY_LAYOUT = "layout";
    private static String KEY_COLUMNS = "columns";
    private static String KEY_HEADER = "header";
    private static String KEY_HEADER_LAYOUT_RES = "header_layout_res";
    private static String KEY_IS_CHECKABLE = "is_checkable";
    private static String KEY_CHECKED_ID = "check_id";
    private static String KEY_APPLY_TINT = "apply_tint";
    private static String KEY_ADJUST_WINDOW = "adjust_window";
    private static String KEY_TEXT_SPANNER = "text_sp";

    private BottomSheetDialogFragmentBinding binding;
    private MenuInflater menuInflater;
    private BottomSheetMenuAdapter adapter;
    private static BottomSheetMenuListener listener;
    private boolean hasOptionSelected = false;

    private static BottomSheetMenuDialog newInstance(Builder builder, BottomSheetMenuListener callback) {
        BottomSheetMenuDialog fragment = new BottomSheetMenuDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList(KEY_OPTIONS, builder.options);
        args.putInt(KEY_LAYOUT, builder.layoutRes);
        args.putInt(KEY_COLUMNS, builder.columns);
        args.putString(KEY_HEADER, builder.header);
        args.putInt(KEY_HEADER_LAYOUT_RES, builder.headerLayoutRes);
        args.putBoolean(KEY_IS_CHECKABLE, builder.isCheckable);
        args.putInt(KEY_CHECKED_ID, builder.checkedId);
        args.putBoolean(KEY_APPLY_TINT, builder.applyTint);
        args.putBoolean(KEY_ADJUST_WINDOW, builder.adjustWindow);
        args.putInt(KEY_TEXT_SPANNER, builder.spanner);
        listener = callback;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    public static DisplayMetrics getDeviceMetrics(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getMetrics(metrics);
        return metrics;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_dialog_fragment, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (!hasOptionSelected && listener != null)
            listener.onModalOptionSelected(getTag(), new Option(-1, null, null));
        super.onDismiss(dialog);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assert getArguments() != null;
        if (getArguments().getBoolean(KEY_ADJUST_WINDOW))
            adjustWindow(view);
        ArrayList<OptionHolder> holder = getArguments().getParcelableArrayList(KEY_OPTIONS);
        ArrayList<Option> options = new ArrayList<>();
        menuInflater = new MenuInflater(getContext());

        assert holder != null;
        for (OptionHolder h : holder) {
            Integer resource = h.getResource();
            OptionRequest optionRequest = h.getOptionRequest();
            if (resource != null) {
                inflate(resource, options);
            }
            if (optionRequest != null) {
                options.add(optionRequest.toOption(getContext()));
            }
        }

        adapter = new BottomSheetMenuAdapter(
                getArguments().getString(KEY_HEADER),
                this::onMenuClick, getArguments().getBoolean(KEY_IS_CHECKABLE),
                getArguments().getInt(KEY_CHECKED_ID),
                getArguments().getInt(KEY_COLUMNS) > 1,
                getArguments().getBoolean(KEY_APPLY_TINT),
                getArguments().getInt(KEY_TEXT_SPANNER)
        );
        binding.rvList.setAdapter(adapter);

        int columns = getArguments().getInt(KEY_COLUMNS);
        if (columns == 1) {
            binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), columns);
            layoutManager.setSpanSizeLookup(
                    new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
                            return adapter.getHeader() != null && position == 0 ? columns : 1;
                        }
                    });
            binding.rvList.setLayoutManager(layoutManager);
        }

        adapter.set(options);
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

    @SuppressLint("RestrictedApi")
    private void inflate(Integer menuRes, ArrayList<Option> options) {
        if (getContext() == null) return;
        MenuBuilder menu = new MenuBuilder(getContext());
        menuInflater.inflate(menuRes, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            Option option = new Option(item.getItemId(), item.getTitle(), item.getIcon());
            options.add(option);
        }
    }

    private void onMenuClick(Option option) {
        hasOptionSelected = true;
        if (listener != null)
            listener.onModalOptionSelected(getTag(), option);
        dismiss();
    }

    /**
     * Used to build a [BottomSheetMenuDialog]
     */
    public static class Builder {

        private ArrayList<OptionHolder> options = new ArrayList<>();
        @LayoutRes
        private int layoutRes = R.layout.bottom_sheet_dialog_fragment_item;
        private int columns = 1;
        private String header = null;
        private int headerLayoutRes = R.layout.bottom_sheet_dialog_fragment_header;
        private BottomSheetMenuListener listener;
        private boolean isCheckable = false;
        private int checkedId = -1;
        private boolean applyTint = true;
        private boolean adjustWindow = true;
        private int spanner = -1;
        public static int SPANNER_BRACKET_ROUND = 0;

        /**
         * Inflate the given menu resource to the options
         */
        public Builder add(@MenuRes Integer menuRes) {
            options.add(new OptionHolder(menuRes, null));
            return this;
        }

        /**
         * Add an option to the sheet
         */
        public Builder add(OptionRequest option) {
            options.add(new OptionHolder(null, option));
            return this;
        }

        public Builder addAll(List<OptionRequest> options) {
            for (OptionRequest option : options) {
                this.options.add(new OptionHolder(null, option));
            }
            return this;
        }

        public Builder addCancel(OptionRequest option) {
            options.add(new OptionHolder(null, option));
            return this;
        }

        /**
         * Set the custom layout resource to inflate for each option. Note that you need to have a
         * TextView with a resource id of @android:id/text1 if your option has a title and an ImageView
         * with a resource id of @android:id/icon if your option has a drawable associated
         */
        private Builder layout(@LayoutRes Integer layoutRes) {
            this.layoutRes = layoutRes;
            return this;
        }

        /**
         * Set the number of columns you want for your options
         */
        public Builder columns(int columns) {
            this.columns = columns;
            return this;
        }

        /**
         * Add a custom header to the modal, using the custom layout if provided
         */
        public Builder header(String header, @LayoutRes Integer layoutRes) {
            if (layoutRes == null)
                layoutRes = R.layout.bottom_sheet_dialog_fragment_header;
            this.header = header;
            this.headerLayoutRes = layoutRes;
            return this;
        }

        public Builder header(String header) {
            this.header = header;
            return this;
        }

        public Builder setCheckable(boolean isCheckable) {
            this.isCheckable = isCheckable;
            return this;
        }

        public Builder setCheckedId(int id) {
            this.checkedId = id;
            return this;
        }

        public Builder setViewTint(boolean applyTint) {
            this.applyTint = applyTint;
            return this;
        }

        public Builder setAdjustWindow(boolean adjustWindow) {
            this.adjustWindow = adjustWindow;
            return this;
        }

        /**
         * Build the [BottomSheetMenuDialog]. You still need to call [BottomSheetMenuDialog.show] when you want it to show
         */
        public BottomSheetMenuDialog build() {
            return newInstance(this, listener);
        }

        public Builder setListener(BottomSheetMenuListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setSpanner(int spanner) {
            this.spanner = spanner;
            return this;
        }

        /**
         * Build and show the [BottomSheetMenuDialog]
         */
        public BottomSheetMenuDialog show(FragmentManager fragmentManager, String tag) {
            BottomSheetMenuDialog dialog = build();
            dialog.show(fragmentManager, tag);
            return dialog;
        }
    }

}
