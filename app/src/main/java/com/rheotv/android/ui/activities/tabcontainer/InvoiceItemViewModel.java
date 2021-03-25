package com.rheotv.android.ui.activities.tabcontainer;


import androidx.databinding.ObservableField;

import com.rheotv.android.helpers.AnalyticsHelper;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class InvoiceItemViewModel {
    public ObservableField<String> title = new ObservableField<String>();
    public ObservableField<String> description = new ObservableField<String>();

    public void onCardClicked() {
        AnalyticsHelper.getInstance(getNonUiContext()).sendLeaderboardClicked();
    }

    public void setData(String titleText, String description) {
        title.set(titleText);
        this.description.set(description);
    }
}
