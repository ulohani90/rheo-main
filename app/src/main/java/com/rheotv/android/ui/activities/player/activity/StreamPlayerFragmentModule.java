package com.rheotv.android.ui.activities.player.activity;


import androidx.lifecycle.ViewModel;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactoryV2;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import javax.inject.Provider;

import dagger.MapKey;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;

@Module
public class StreamPlayerFragmentModule {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @MapKey
    @interface ViewModelKey {
        Class<? extends ViewModel> value();
    }

    @Provides
    ViewModelProviderFactoryV2 viewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> providerMap) {
        return new ViewModelProviderFactoryV2<>(providerMap);
    }

    @Provides
    @IntoMap
    @ViewModelKey(StreamPlayerViewModel.class)
    ViewModel viewModel1(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new StreamPlayerViewModel(dataManager, schedulerProvider);
    }
}
