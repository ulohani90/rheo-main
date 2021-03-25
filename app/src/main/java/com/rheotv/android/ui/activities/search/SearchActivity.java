package com.rheotv.android.ui.activities.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.rheotv.android.R;
import com.rheotv.android.databinding.UniversalActivityBinding;
import com.rheotv.android.ui.activities.search.fragment.SearchFragment;
import com.rheotv.android.ui.activities.search.fragment.SearchFragmentNavigator;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivityVM;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

public class SearchActivity extends BaseActivity<UniversalActivityBinding, UniversalActivityVM>
        implements HasAndroidInjector, SearchFragmentNavigator {

    @Inject
    DispatchingAndroidInjector<Object> fragmentDispatchingAndroidInjector;
    @Inject
    UniversalActivityVM universalActivityVM;

    private UniversalActivityBinding universalActivityBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        universalActivityVM.setNavigator(this);
        universalActivityBinding = getViewDataBinding();
        loadFragment(SearchFragment.newInstance(getIntent().getStringExtra(AppConstants.SCREEN_SOURCE)), true, "Main");
    }

    public void loadFragment(Fragment fragment, boolean shouldReplace, String stackName) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!fragment.isAdded()) {
            try {
                if (shouldReplace) {
                    transaction.replace(R.id.container, fragment);
                } else {
                    // todo - a dirty hack must be removed soon.
                    transaction.add(R.id.container, fragment).addToBackStack(null);
                    transaction.commit();
                    return;
                }
                transaction.commitNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            transaction.show(fragment);
        }
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.universal_activity;
    }

    @Override
    public UniversalActivityVM getViewModel() {
        return universalActivityVM;
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Override
    public void handleError(Throwable throwable) {

    }

    @Override
    public void handleLogin() {

    }

    @Override
    public void showToast(String message) {

    }

    @Override
    public void showReportPostSuccessToast() {

    }

    public static void startMe(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
        context.startActivity(intent);
    }
}