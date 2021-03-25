package com.rheotv.android.ui.activities.gamify.di;

import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.factories.ViewModelProviderFactory;
import com.rheotv.android.ui.activities.gamify.RedeemSummaryViewModel;
import com.rheotv.android.ui.activities.gamify.VoucherCodeAdapter;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import dagger.Module;
import dagger.Provides;

@Module
public class RedeemSummaryFragmentModule {

    @Provides
    RedeemSummaryViewModel viewModel(
            DataManager dataManager,
            SchedulerProvider schedulerProvider
    ) {
        return new RedeemSummaryViewModel(dataManager, schedulerProvider);
    }

    @Provides
    ViewModelProvider.Factory provideBlogViewModel(RedeemSummaryViewModel viewModel) {
        return new ViewModelProviderFactory<>(viewModel);
    }

    @Provides
    VoucherCodeAdapter voucherAdapter() {
        return new VoucherCodeAdapter(new ArrayList<>());
    }

}
