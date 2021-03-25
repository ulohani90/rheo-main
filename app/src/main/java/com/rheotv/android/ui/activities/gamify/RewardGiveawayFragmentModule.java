package com.rheotv.android.ui.activities.gamify;

import android.content.Context;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class RewardGiveawayFragmentModule {

    @Provides
    RewardGiveawayViewModel rewardRedeemViewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new RewardGiveawayViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(RewardGiveawayViewModel rewardVoucherViewModel) {
        return new ViewModelProviderFactory<>(rewardVoucherViewModel);
    }

    @Provides
    PostListAdapter provideRewardVoucherAdapter(Context context) {
        return new PostListAdapter(context, new ArrayList<>());
    }

}
