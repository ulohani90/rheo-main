package com.rheotv.android.ui.activities.clips;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.rheotv.android.R;
import com.rheotv.android.databinding.ActivityClipsLayoutBinding;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivity;
import com.rheotv.android.ui.activities.tabcontainer.clips.ClipsFragment;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import java.util.HashMap;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

public class ClipsActivity extends BaseActivity<ActivityClipsLayoutBinding, ClipsViewModel> implements ClipsNavigator, HasAndroidInjector, LoginFragmentBottomDialog.LoginFragmentCallback {

    @Inject
    ClipsViewModel clipsViewModel;

    ActivityClipsLayoutBinding mBinding;

    @Inject
    DispatchingAndroidInjector<Object> fragmentDispatchingAndroidInjector;


    LoginFragmentBottomDialog loginDialogFragment;

    boolean cameFromShare;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_clips_layout;
    }

    @Override
    public ClipsViewModel getViewModel() {
        return clipsViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mBinding = getViewDataBinding();
        clipsViewModel.setNavigator(this);
        String clipId = getIntent().getStringExtra("clip_id");
        if (clipId != null) {
            cameFromShare = true;
        }

        String source = SegmentConstants.SCREEN_NAME_CLIPS;
        if (getIntent() != null) {
            source = getIntent().getStringExtra(AppConstants.SCREEN_SOURCE);
        }
        baseProperties.put(AppConstants.SCREEN_SOURCE, source);
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_CLIPS);

        SegmentTracker.getInstance(this).recordScreenName(SegmentConstants.SCREEN_NAME_CLIPS, baseProperties);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.frame_container, ClipsFragment.newInstance(clipId, source), null).commitAllowingStateLoss();
        mBinding.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    @Override
    public AndroidInjector<Object> androidInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    public void openLoginFlow() {
        try {
            if (loginDialogFragment == null) {
                loginDialogFragment = LoginFragmentBottomDialog.getInstance(SegmentConstants.SCREEN_NAME_CLIPS);
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
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {
        if (cameFromShare) {
            startTabContainerActivity();
        } else {
            super.onBackPressed();
        }
    }

    private void startTabContainerActivity() {
        Intent intent = TabContainerActivity.newIntent(this);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_CLIPS);
        startActivity(intent);
        finish();
    }

    @Override
    public void onLoginSuccess() {
        Toast.makeText(this, "Login Successful.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginDialogClose() {

    }
}
