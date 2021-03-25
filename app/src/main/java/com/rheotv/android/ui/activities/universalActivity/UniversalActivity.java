package com.rheotv.android.ui.activities.universalActivity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.rheotv.android.R;
import com.rheotv.android.databinding.UniversalActivityBinding;
import com.rheotv.android.ui.activities.chatActivity.ChatFragment;
import com.rheotv.android.ui.activities.universalActivity.fragment.UniversalFragment;
import com.rheotv.android.ui.activities.universalActivity.fragment.UniversalFragmentNavigator;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

import static com.rheotv.android.utils.AppConstants.SEE_ALL_TYPE_CHAT;

public class UniversalActivity extends BaseActivity<UniversalActivityBinding, UniversalActivityVM>
        implements HasAndroidInjector, UniversalFragmentNavigator, LoginFragmentBottomDialog.LoginFragmentCallback {

    @Inject
    DispatchingAndroidInjector<Object> fragmentDispatchingAndroidInjector;
    @Inject
    UniversalActivityVM universalActivityVM;

    private UniversalActivityBinding universalActivityBinding;

    LoginFragmentBottomDialog loginDialogFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        universalActivityVM.setNavigator(this);
        universalActivityBinding = getViewDataBinding();
        if (getIntent().getExtras() == null || getIntent().getExtras().getString(AppConstants.SEE_ALL_TYPE) == null || getIntent().getExtras().getString(AppConstants.SEE_ALL_TYPE).isEmpty()) {
            finish();
        }

        String source = SegmentConstants.SCREEN_NAME_GAME_PAGE;
        if (getIntent() != null && getIntent().hasExtra(AppConstants.SCREEN_SOURCE))
            source = getIntent().getStringExtra(AppConstants.SCREEN_SOURCE);

        if (SEE_ALL_TYPE_CHAT.contentEquals(getIntent().getExtras().getString(AppConstants.SEE_ALL_TYPE))) {
            loadFragment(ChatFragment.newInstance(
                    getIntent().getExtras().getString(AppConstants.SEE_ALL_TYPE),
                    getIntent().getExtras().getString(AppConstants.SEE_ALL_TYPE_ID)
            ), true, "Main");
        } else {
            loadFragment(UniversalFragment.newInstance(
                    getIntent().getExtras().getString(AppConstants.SEE_ALL_TYPE),
                    getIntent().getExtras().getString(AppConstants.SEE_ALL_TYPE_ID),
                    source
            ), true, "Main");
        }
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
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showReportPostSuccessToast() {

    }

    @Override
    public void handleError(Throwable throwable) {

    }

    @Override
    public void handleLogin() {

    }

    public void launchLogInFragment() {
        if (getSupportFragmentManager().findFragmentByTag(AppConstants.LOGIN_FRAGMENT_TAG) != null) {
            loginDialogFragment = (LoginFragmentBottomDialog) getSupportFragmentManager().findFragmentByTag(AppConstants.LOGIN_FRAGMENT_TAG);
        } else {
            loginDialogFragment = LoginFragmentBottomDialog.getInstance(SegmentConstants.SCREEN_NAME_GAME_PAGE);
        }
        if (loginDialogFragment != null) {
            loginDialogFragment.setmCallback(this);
        }
        if (loginDialogFragment != null && (loginDialogFragment.isAdded() || loginDialogFragment.isVisible())) {
            return;
        }
        try {
            if (loginDialogFragment != null) {
                loginDialogFragment.showNoAddToBackStack(this.getSupportFragmentManager(), AppConstants.LOGIN_FRAGMENT_TAG);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onLoginSuccess() {
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginDialogClose() {

    }
}
