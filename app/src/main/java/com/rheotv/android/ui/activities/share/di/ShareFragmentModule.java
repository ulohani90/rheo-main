package com.rheotv.android.ui.activities.share.di;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.factories.ViewModelProviderFactoryV2;
import com.rheotv.android.ui.activities.share.ShareViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.Map;

import javax.inject.Provider;

import dagger.Module;
import dagger.Provides;

@Module
public class ShareFragmentModule {

    @Provides
    ShareViewModel viewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new ShareViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory viewModelFactory(ShareViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }
}

