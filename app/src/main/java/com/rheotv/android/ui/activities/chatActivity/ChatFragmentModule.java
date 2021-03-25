package com.rheotv.android.ui.activities.chatActivity;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;
@Module
public class ChatFragmentModule {
    @Provides
    ChatFragmentViewModel universalFragmentViewModel(DataManager dataManager,
                                                          SchedulerProvider schedulerProvider) {
        return new ChatFragmentViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ChatFragmentAdapter universalFragmentListAdapter() {
        return new ChatFragmentAdapter(new ArrayList<>());
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(ChatFragmentViewModel universalFragmentViewModel) {
        return new ViewModelProviderFactory<>(universalFragmentViewModel);
    }

    @Provides
    LinearLayoutManager provideLinearLayoutManager(ChatFragment fragment) {
        return new LinearLayoutManager(fragment.getActivity());
    }
}
