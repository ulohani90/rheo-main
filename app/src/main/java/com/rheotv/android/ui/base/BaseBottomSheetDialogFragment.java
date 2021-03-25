package com.rheotv.android.ui.base;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rheotv.android.utils.ViewUtils;

import dagger.android.support.AndroidSupportInjection;

public abstract class BaseBottomSheetDialogFragment<T extends ViewDataBinding, V extends BaseViewModel> extends BottomSheetDialogFragment {

    private BaseActivity mActivity;
    private View mRootView;
    private T mViewDataBinding;
    private V mViewModel;

    // protected BottomSheetBehavior behavior;


    /**
     * Override for set binding variable
     *
     * @return variable id
     */
    public abstract int getBindingVariable();

    /**
     * @return layout resource id
     */
    public abstract
    @LayoutRes
    int getLayoutId();

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    public abstract V getViewModel();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            BaseActivity mActivity = (BaseActivity) context;
            this.mActivity = mActivity;
            mActivity.onFragmentAttached();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        performDependencyInjection();
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        mRootView = mViewDataBinding.getRoot();
        //behavior = BottomSheetBehavior.from(mRootView);
        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewDataBinding.setVariable(getBindingVariable(), mViewModel);
        mViewDataBinding.executePendingBindings();

    }

    public T getViewDataBinding() {
        return mViewDataBinding;
    }

    private void performDependencyInjection() {
        AndroidSupportInjection.inject(this);
    }


    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }


    @Override
    public void onStart() {
        super.onStart();
        View mView = getView();
        mView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float eventX = event.getRawX();
                float eventY = event.getRawY();

                int location[] = new int[2];
                mView.getLocationOnScreen(location);

                if (eventX < location[0] || eventX > (location[0] + mView.getWidth()) || eventY < location[1]
                        || eventY > location[1] + mView.getHeight()) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
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

    public void show(FragmentManager fragmentManager, String tag) {
        try {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment prevFragment = fragmentManager.findFragmentByTag(tag);
            if (prevFragment != null) {
                transaction.remove(prevFragment);
            }
//            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();
            show(transaction, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissDialog(String tag) {
        dismiss();
        getBaseActivity().onFragmentDetached(tag);
    }

    public BaseActivity getBaseActivity() {
        return mActivity;
    }

    public void hideKeyboard() {
        if (mActivity != null) {
            mActivity.hideKeyboard();
        }
    }

    public void hideLoading() {
        if (mActivity != null) {
            mActivity.hideLoading();
        }
    }

    public boolean isNetworkConnected() {
        return mActivity != null && mActivity.isNetworkConnected();
    }

    public void showLoading() {
        if (mActivity != null) {
            mActivity.showLoading();
        }
    }

    public void adjustWindow(View view) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                performActionsOnViewAdjust(view);
            }
        });
    }

    public void performActionsOnViewAdjust(View rootView) {
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            CoordinatorLayout.LayoutParams params;
            if (bottomSheet != null) {
                params = (CoordinatorLayout.LayoutParams) bottomSheet.getLayoutParams();
                params.setMargins(ViewUtils.dpToPx(80), 0, ViewUtils.dpToPx(80), 0);
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


}