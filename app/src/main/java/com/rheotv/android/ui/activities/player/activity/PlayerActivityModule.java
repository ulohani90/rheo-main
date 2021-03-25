package com.rheotv.android.ui.activities.player.activity;

import android.content.Context;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.adapters.PlayerListAdapter;
import com.rheotv.android.ui.adapters.ScorecardAdapter;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class PlayerActivityModule {

    @Provides
    PlayerViewModel providePlayerViewModel(DataManager dataManager, SchedulerProvider schedulerProvider, Context context) {
        return new PlayerViewModel(dataManager, schedulerProvider, context);
    }

    @Provides
    PlayerListAdapter provideBlogAdapter() {
        return new PlayerListAdapter(new ArrayList<>());
    }

    @Provides
    ScorecardAdapter provideScorecardAdapter() {
        return new ScorecardAdapter(new ArrayList<>());
    }

}
