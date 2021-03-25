package com.rheotv.android.ui.fragments;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.adapters.RecentlyRedeemedListAdapter;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class RecentlyRedeemedFragmentModule {
    @Provides
    RecentlyRedeemedFragmentViewModel recentlyRedeemFragmentViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new RecentlyRedeemedFragmentViewModel(dataManager, schedulerProvider);
    }

    @Provides
    RecentlyRedeemedListAdapter recentlyRedeemAdapter() {
        return new RecentlyRedeemedListAdapter(new ArrayList<>());
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(RecentlyRedeemedFragmentViewModel recentlyRedeemViewModel) {
        return new ViewModelProviderFactory<>(recentlyRedeemViewModel);
    }
}
