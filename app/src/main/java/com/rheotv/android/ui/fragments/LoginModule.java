package com.rheotv.android.ui.fragments;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class LoginModule {

    @Provides
    LoginViewModel loginViewModel(DataManager dataManager,
                                  SchedulerProvider schedulerProvider) {
        return new LoginViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideLoginViewModel(LoginViewModel loginViewModel) {
        return new ViewModelProviderFactory<>(loginViewModel);
    }

}
