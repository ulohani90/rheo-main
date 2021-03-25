package com.rheotv.android.ui.activities.selectGame.di;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.activities.selectGame.GameActivityViewModel;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class GameSelectionActivityModule {
    @Provides
    GameActivityViewModel provideGameActivityViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        return new GameActivityViewModel(dataManager, schedulerProvider);
    }
}
