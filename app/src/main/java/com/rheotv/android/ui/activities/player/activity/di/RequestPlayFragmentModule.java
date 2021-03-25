package com.rheotv.android.ui.activities.player.activity.di;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.player.activity.PlayRequestAdapter;
import com.rheotv.android.ui.activities.player.activity.RequestPlayViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class RequestPlayFragmentModule {
    @Provides
    RequestPlayViewModel requestToPlatViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new RequestPlayViewModel(dataManager, schedulerProvider);
    }

    @Provides
    PlayRequestAdapter playRequestAdapter() {
        return new PlayRequestAdapter();
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(RequestPlayViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}
