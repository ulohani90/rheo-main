package com.rheotv.android.ui.activities.gamify.di;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.gamify.RedeemDetailViewModel;
import com.rheotv.android.ui.activities.gamify.SkuAdapter;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class RedeemDetailFragmentModule {

    @Provides
    RedeemDetailViewModel viewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new RedeemDetailViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(RedeemDetailViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }

    @Provides
    SkuAdapter skuAdapter() {
        return new SkuAdapter(new ArrayList<>());
    }
}
