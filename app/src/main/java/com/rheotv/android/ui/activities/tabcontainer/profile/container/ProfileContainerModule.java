package com.rheotv.android.ui.activities.tabcontainer.profile.container;



import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class ProfileContainerModule {
    @Provides
    ProfileContainerViewModel profileContainerViewModel(DataManager dataManager,
                                                        SchedulerProvider schedulerProvider) {
        return new ProfileContainerViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideProfileContainerViewModel(ProfileContainerViewModel profileViewModel) {
        return new ViewModelProviderFactory<>(profileViewModel);
    }

    @Provides
    PostListAdapter providePostListAdapter() {
        return new PostListAdapter(new ArrayList<>());
    }
}
