package com.rheotv.android.ui.activities.moderators;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.rheotv.android.R;
import com.rheotv.android.databinding.ActivityAddModeratorsLayoutBinding;
import com.rheotv.android.ui.adapters.AddModeratorsRVAdapter;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;

import javax.inject.Inject;

public class AddModeratorsActivity extends BaseActivity<ActivityAddModeratorsLayoutBinding, AddModeratorsViewModel> implements AddModeratorsNavigator {

    @Inject
    AddModeratorsViewModel mViewModel;

    ActivityAddModeratorsLayoutBinding mBinding;

    AddModeratorsRVAdapter adapter;

    String moderators;

    ProgressDialog dialog;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    boolean isDestroyed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = getViewDataBinding();
        mViewModel.setNavigator(this);
        moderators = getIntent().getStringExtra("moderators");

        if (getIntent() != null && getIntent().hasExtra(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getIntent().getStringExtra(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_ADD_MODERATOR);
        SegmentTracker.getInstance(this).recordScreenName(SegmentConstants.SCREEN_ADD_MODERATOR, baseProperties);

        setUp(moderators);
    }

    private void setUp(String moderators) {
        mBinding.moderatorsLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mBinding.moderatorsLayout.addItemDecoration(new ModeratorsListItemDecorator((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics())));
        adapter = new AddModeratorsRVAdapter(moderators);
        mBinding.moderatorsLayout.setAdapter(adapter);
        setSupportActionBar(mBinding.toolbar);

        mBinding.addMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter.getItemCount() < 5) {
                    adapter.incrementItemCount();
                }
            }
        });

        mBinding.saveBtn.setOnClickListener(v -> onSaveClicked());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSaveClicked() {
        CommonUtils.hideKeyboard(this);
        StringBuilder builder = new StringBuilder();
        boolean isErrorSeen = false;
        for (int i = 0; i < adapter.getItemCount(); i++) {
            View view = mBinding.moderatorsLayout.getChildAt(i);
            if (view != null) {
                EditText moderatorEmail = view.findViewById(R.id.first_name_et);
                if (moderatorEmail.getText() != null && moderatorEmail.getText().toString().trim().length() > 0) {
                    String emailText = moderatorEmail.getText().toString().trim();
                    if (AppUtils.isValidEmail(emailText)) {
                        if (builder.length() > 0) {
                            builder.append(",");
                        }
                        builder.append(emailText);
                    } else {
                        isErrorSeen = true;
                        moderatorEmail.setError("Please enter a valid email address");
                        break;
                    }
                } else {
                    isErrorSeen = true;
                    moderatorEmail.setError("Please enter an email address");
                    break;
                }
            }
        }
        if (!isErrorSeen) {
            dialog = ProgressDialog.show(this, null, "Adding Moderators. Please wait");
            moderators = builder.toString();
            mViewModel.postModeratorsData(moderators);
        }

    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_add_moderators_layout;
    }

    @Override
    public AddModeratorsViewModel getViewModel() {
        return mViewModel;
    }

    @Override
    public void onRequestSuccess() {
        try {
            if (dialog != null)
                dialog.dismiss();
            showToast("Moderators added as per request");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.putExtra("moderators", moderators);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }, 2000);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public void onRequestFailed() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void showToast(String message) {
        if (dialog != null) {
            dialog.dismiss();
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public class ModeratorsListItemDecorator extends RecyclerView.ItemDecoration {

        int spacing;

        public ModeratorsListItemDecorator(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = spacing;
            outRect.right = spacing;
            outRect.bottom = spacing;
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = spacing;
            }
        }
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        super.onDestroy();
    }
}
