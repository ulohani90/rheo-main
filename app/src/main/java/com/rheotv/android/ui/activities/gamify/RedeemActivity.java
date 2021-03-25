package com.rheotv.android.ui.activities.gamify;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.rheotv.android.R;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

import static com.rheotv.android.utils.AppConstants.ARG_REWARD_META;

public class RedeemActivity extends BaseActivity implements HasAndroidInjector {
    @Inject
    DispatchingAndroidInjector<Object> fragmentDispatchingAndroidInjector;

    @Override
    public AndroidInjector<Object> androidInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_redeem;
    }

    @Override
    public BaseViewModel getViewModel() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String source = SegmentConstants.SCREEN_REDEEM_LIST;
        if (getIntent() != null && getIntent().hasExtra(AppConstants.SCREEN_SOURCE))
            source = getIntent().getStringExtra(AppConstants.SCREEN_SOURCE);

        if (getIntent().getParcelableExtra(ARG_REWARD_META) != null) {
            RedeemSummaryFragment fragment = RedeemSummaryFragment.newInstance(getIntent().getParcelableExtra(ARG_REWARD_META), source);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        } else {
            RedeemDetailFragment fragment = RedeemDetailFragment.newInstance(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }
    }
}
