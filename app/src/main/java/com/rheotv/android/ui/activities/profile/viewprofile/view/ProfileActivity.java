package com.rheotv.android.ui.activities.profile.viewprofile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.rheotv.android.BR;
import com.rheotv.android.R;
import com.rheotv.android.databinding.ActivityProfileBinding;
import com.rheotv.android.ui.activities.profile.viewprofile.utils.ProfileNavigator;
import com.rheotv.android.ui.activities.profile.viewprofile.viewmodel.ProfileViewModel;
import com.rheotv.android.ui.activities.profile.view.ProfileContainerFragment;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivity;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

import static com.rheotv.android.utils.AppConstants.REQUEST_CODE_EDIT_PROFILE;

public class ProfileActivity extends BaseActivity<ActivityProfileBinding, ProfileViewModel> implements ProfileNavigator, HasAndroidInjector, LoginFragmentBottomDialog.LoginFragmentCallback {


    @Inject
    ProfileViewModel profileViewModel;

    @Inject
    DispatchingAndroidInjector<Object> fragmentDispatchingAndroidInjector;

    ActivityProfileBinding mBinding;

    String creatorUsername;

    boolean isDeepLinkActivity;

    private LoginFragmentBottomDialog loginDialogFragment;

    boolean refreshProfile;
    private String source;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupTransparentToolbar();
        mBinding = getViewDataBinding();
        profileViewModel.setNavigator(this);

        if (getIntent() == null || !getIntent().hasExtra("author_name")) {
            finish();
            return;
        }
        creatorUsername = getIntent().getStringExtra("author_name");
        isDeepLinkActivity = getIntent().getBooleanExtra("is_deeplink", false);

        if (getIntent() != null && getIntent().hasExtra(AppConstants.SCREEN_SOURCE))
            source = getIntent().getStringExtra(AppConstants.SCREEN_SOURCE);
        else
            source = SegmentConstants.SCREEN_NAME_USER_PROFILE;
        baseProperties.put(AppConstants.SCREEN_SOURCE, source);
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_USER_PROFILE);
        baseProperties.put("username", creatorUsername);
        profileViewModel.baseProperties = baseProperties;

        mBinding.toolbarTitle.setText(creatorUsername);
        loadFragment(ProfileContainerFragment.Companion.newInstance(creatorUsername, source,isDeepLinkActivity), true);
        if (creatorUsername.equalsIgnoreCase("me") || creatorUsername.equalsIgnoreCase(CommonUtils.getUserName(this))) {
            mBinding.shareBtn.setVisibility(View.GONE);
        }
        SegmentTracker.getInstance(this).recordScreenName(SegmentConstants.SCREEN_NAME_USER_PROFILE, baseProperties);
    }

    public ProfileViewModel getProfileViewModel() {
        return profileViewModel;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setupTransparentToolbar();
    }

    private void setupTransparentToolbar() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (refreshProfile) {
            refreshProfile = false;
            loadFragment(ProfileContainerFragment.Companion.newInstance(creatorUsername, source,isDeepLinkActivity), true);
        }

    }

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, ProfileActivity.class);
    }

    public void loadFragment(Fragment fragment, boolean shouldReplace) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_profile;
    }

    @Override
    public ProfileViewModel getViewModel() {
        return profileViewModel;
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    public void setPrimeShowHostTagVisibility() {
        //mBinding.primeShowHostTag.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (isDeepLinkActivity) {
            startTabContainerActivity();
        }
        super.onBackPressed();
    }

    private void startTabContainerActivity() {
        Intent intent = TabContainerActivity.newIntent(this);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_USER_PROFILE);
        startActivity(intent);
    }

    @Override
    public void openLoginFlow() {
        try {
            if (loginDialogFragment == null) {
                loginDialogFragment = LoginFragmentBottomDialog.getInstance("profile");
                loginDialogFragment.setmCallback(this);
            }
            if (loginDialogFragment.isAdded() || loginDialogFragment.isVisible()) {
                return;
            }
            loginDialogFragment.showNoAddToBackStack(this.getSupportFragmentManager(), AppConstants.LOGIN_FRAGMENT_TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onToolbarBackPressed() {
        onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                refreshProfile = true;
            }
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginSuccess() {
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(AppConstants.UPDATE_STORY_BROADCAST_FILTER));
    }

    @Override
    public void onLoginDialogClose() {

    }

    public static void startMe(Context context, String screenSource, String username) {
        Intent intent = new Intent(context, ProfileActivity.class);
        if (screenSource != null && !screenSource.isEmpty()) {
            intent.putExtra(AppConstants.SCREEN_SOURCE, screenSource);
        }
        if (username != null && !username.isEmpty()) {
            intent.putExtra("author_name", username);
        }
        context.startActivity(intent);
    }
}
