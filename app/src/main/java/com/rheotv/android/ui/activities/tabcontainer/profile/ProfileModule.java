package com.rheotv.android.ui.activities.tabcontainer.profile;



import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class ProfileModule {
    @Provides
    ProfileViewModel profileViewModel(DataManager dataManager,
                                      SchedulerProvider schedulerProvider) {
        return new ProfileViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideProfileViewModel(ProfileViewModel profileViewModel) {
        return new ViewModelProviderFactory<>(profileViewModel);
    }

    @Provides
    PostListAdapter provideProfileFragmentAdapter() {
        return new PostListAdapter(new ArrayList<>());
    }

}
