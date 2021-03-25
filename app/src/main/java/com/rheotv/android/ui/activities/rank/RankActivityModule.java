package com.rheotv.android.ui.activities.rank;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactoryV2;
import com.rheotv.android.utils.annotaion.ViewModelKey;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.Map;

import javax.inject.Provider;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;

@Module
public class RankActivityModule {
    @Provides
    @IntoMap
    @ViewModelKey(RankActivityViewModel.class)
    ViewModel provideViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new RankActivityViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProviderFactoryV2 provideViewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> providerMap) {
        return new ViewModelProviderFactoryV2<>(providerMap);
    }

}
