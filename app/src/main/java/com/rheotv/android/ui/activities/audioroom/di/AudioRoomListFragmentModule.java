package com.rheotv.android.ui.activities.audioroom.di;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactoryV2;
import com.rheotv.android.ui.activities.audioroom.adapter.ChatRoomUserRecyclerAdapter;
import com.rheotv.android.ui.activities.audioroom.viewmodel.AudioRoomViewModel;
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
public class AudioRoomListFragmentModule {
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @MapKey
    @interface ViewModelKey {
        Class<? extends ViewModel> value();
    }

    @Provides
    ViewModelProvider.Factory provideViewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> providerMap) {
        return new ViewModelProviderFactoryV2<>(providerMap);
    }

    @Provides
    @IntoMap
    @ViewModelKey(AudioRoomViewModel.class)
    ViewModel provideViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new AudioRoomViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ChatRoomUserRecyclerAdapter provideChatRoomUserRecyclerAdapter(){
        return new ChatRoomUserRecyclerAdapter();
    }
}
