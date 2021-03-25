package com.rheotv.android.ui.activities.alertInformation;

import android.content.Context;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class AlertInformationModule {

    @Provides
    AlertInformationViewModel provideAlertInformationViewModel(DataManager dataManager, SchedulerProvider schedulerProvider, Context context) {
        return new AlertInformationViewModel(dataManager, schedulerProvider, context);
    }

}
