package com.rheotv.android.ui.activities.gamify;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.facebook.internal.Mutable;
import com.rheotv.android.R;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.gamify.RewardMeta;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.ui.customViews.bottomSheetMenu.BottomSheetMenuDialog;
import com.rheotv.android.ui.customViews.bottomSheetMenu.OptionRequest;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.rx.SchedulerProvider;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static android.content.Context.CLIPBOARD_SERVICE;

public class RedeemSummaryViewModel extends BaseViewModel {

    public ObservableField<RewardMeta> meta = new ObservableField<>(new RewardMeta());
    public HashMap<String, Object> baseProperties = new HashMap<>();
    public ObservableField<Boolean> isContentShared = new ObservableField<>(false);

    public String getGameName() {
        return meta.get() != null && meta.get().getGame() != null ? meta.get().getGame() : "";
    }

    public String getGameCurrency() {
        return meta.get() != null && meta.get().getCurrency() != null ? meta.get().getCurrency() : "";
    }

    public RedeemSummaryViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void onVoucherClick(View view) {
        SegmentTracker.getInstance(view.getContext()).trackEvent(SegmentConstants.EVENT_REWARD_SUMMARY_COPY_CLIPBOARD_CLICKED, baseProperties);

        ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("voucher", Objects.requireNonNull(meta.get()).getVoucherCode());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(view.getContext(), view.getContext().getString(R.string.voucher_clipboard_message), Toast.LENGTH_SHORT).show();
    }

    public void onRedeemClick(View view) {
        if (meta.get() != null && meta.get().getRedeemUrl() != null) {
            SegmentTracker.getInstance(view.getContext()).trackEvent(SegmentConstants.EVENT_REWARD_SUMMARY_REDEEM_CLICKED, baseProperties);
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Objects.requireNonNull(meta.get()).getRedeemUrl()));
                view.getContext().startActivity(i);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(view.getContext(), "No Application is found to handle this Action.", Toast.LENGTH_LONG).show();
            }
        }
    }

}
