package com.rheotv.android.ui.activities.onboarding;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.onboarding.LanguageObject;
import com.rheotv.android.data.network.models.onboarding.OnBoardingResponse;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.databinding.ActivityOnboardingLayoutBinding;
import com.rheotv.android.ui.activities.alertInformation.AlertInformationActivity;
import com.rheotv.android.ui.activities.clips.ClipsActivity;
import com.rheotv.android.ui.activities.home.view.HomeActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.LinkHandler;
import com.rheotv.android.utils.ListHolder;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

public class OnBoardingActivity extends BaseActivity<ActivityOnboardingLayoutBinding, OnBoardingActivityViewModel> implements OnBoardingActivityNavigator {

    @Inject
    OnBoardingActivityViewModel onBoardingActivityViewModel;

    ActivityOnboardingLayoutBinding mBinding;

    ProgressDialog dialog;

    List<String> selectedIds = new ArrayList<>();

    String intentOpenUrl;

    boolean showUpdateMessage;

    boolean isDestroyed;

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_onboarding_layout;
    }

    @Override
    public OnBoardingActivityViewModel getViewModel() {
        return onBoardingActivityViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mBinding = getViewDataBinding();
        onBoardingActivityViewModel.setNavigator(this);
        SegmentTracker.getInstance(this).recordScreenName(SegmentConstants.SCREEN_NAME_ONBOARDING, new HashMap<>());
        setUp();
    }

    private void setUp() {
        intentOpenUrl = getIntent().getStringExtra("intent_open_url");
        showUpdateMessage = getIntent().getBooleanExtra("show_update_message", false);
        mBinding.contentLayout.setVisibility(View.GONE);
        mBinding.skipBtn.setVisibility(View.GONE);
        mBinding.topGradientBg.setVisibility(View.GONE);
        mBinding.loadingView.setVisibility(View.VISIBLE);
        onBoardingActivityViewModel.fetchOnBoardingData();
    }

    @Override
    public void setOnBoardingData(OnBoardingResponse response) {
        mBinding.contentLayout.setVisibility(View.VISIBLE);
        mBinding.skipBtn.setVisibility(View.VISIBLE);
        //mBinding.topGradientBg.setVisibility(View.VISIBLE);
        mBinding.loadingView.setVisibility(View.GONE);
        /*LanguagesSpinnerAdapter adapter = new LanguagesSpinnerAdapter(this, R.layout.spinner_language_item_layout, response.getLanguageObjects());
        mBinding.languagesSpinner.setAdapter(adapter);
        mBinding.languagesSpinner.setSelection(findSelectedPositionFromLanguages(response.getLanguageObjects()));*/
        List<LanguageObject> languageObjects = response.getLanguageObjects();
        mBinding.language1.setText(languageObjects.get(0).getDisplayName());
        if (languageObjects.get(0).isSelected()) {
            mBinding.language1.setSelected(true);
            selectedIds.add(languageObjects.get(0).getId());
        } else {
            mBinding.language1.setSelected(false);
        }
        mBinding.language2.setText(languageObjects.get(1).getDisplayName());
        if (languageObjects.get(1).isSelected()) {
            mBinding.language2.setSelected(true);
            selectedIds.add(languageObjects.get(1).getId());
        } else {
            mBinding.language2.setSelected(false);
        }
        mBinding.language3.setText(languageObjects.get(2).getDisplayName());
        if (languageObjects.get(2).isSelected()) {
            mBinding.language3.setSelected(true);
            selectedIds.add(languageObjects.get(2).getId());
        } else {
            mBinding.language3.setSelected(false);
        }
        mBinding.language4.setText(languageObjects.get(3).getDisplayName());
        if (languageObjects.get(3).isSelected()) {
            mBinding.language4.setSelected(true);
            selectedIds.add(languageObjects.get(3).getId());
        } else {
            mBinding.language4.setSelected(false);
        }
        ImagesAdapter imagesAdapter = new ImagesAdapter();
        mBinding.onboardingVp.setAdapter(imagesAdapter);
        mBinding.tabsContainer.setVisibility(View.VISIBLE);
        mBinding.tabsContainer.setupWithViewPager(mBinding.onboardingVp);
        mBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedIds.size() == 0) {
                    Toast.makeText(OnBoardingActivity.this, "Please select at least one language", Toast.LENGTH_SHORT).show();
                } else {
                    updateLanguage(response.getLanguageObjects());
                }
            }
        });

        mBinding.skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeOnBoarding();
            }
        });

        mBinding.language1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBinding.language1.isSelected()) {
                    mBinding.language1.setSelected(false);
                    selectedIds.remove(languageObjects.get(0).getId());
                } else {
                    mBinding.language1.setSelected(true);
                    selectedIds.add(languageObjects.get(0).getId());
                }
            }
        });

        mBinding.language2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBinding.language2.isSelected()) {
                    mBinding.language2.setSelected(false);
                    selectedIds.remove(languageObjects.get(1).getId());
                } else {
                    mBinding.language2.setSelected(true);
                    selectedIds.add(languageObjects.get(1).getId());
                }
            }
        });

        mBinding.language3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBinding.language3.isSelected()) {
                    mBinding.language3.setSelected(false);
                    selectedIds.remove(languageObjects.get(2).getId());
                } else {
                    mBinding.language3.setSelected(true);
                    selectedIds.add(languageObjects.get(2).getId());
                }
            }
        });

        mBinding.language4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBinding.language4.isSelected()) {
                    mBinding.language4.setSelected(false);
                    selectedIds.remove(languageObjects.get(3).getId());
                } else {
                    mBinding.language4.setSelected(true);
                    selectedIds.add(languageObjects.get(3).getId());
                }
            }
        });
    }

    private void updateLanguage(List<LanguageObject> languageObjects) {
        dialog = ProgressDialog.show(this, null, "Saving content language preference..");
        onBoardingActivityViewModel.updateLanguage(selectedIds);
    }

    private int findSelectedPositionFromLanguages(List<LanguageObject> languageObjects) {
        for (int i = 0; i < languageObjects.size(); i++) {
            if (languageObjects.get(i).isSelected()) {
                return i;
            }
        }
        return 0;
    }


    public class ImagesAdapter extends PagerAdapter {

        public int[] imageUrls = {R.drawable.ic_onboarding_1, R.drawable.ic_onboarding_2, R.drawable.ic_onboarding_3};

        public ImagesAdapter() {

        }

        @Override
        public int getCount() {
            return imageUrls.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == (View) object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View itemView = LayoutInflater.from(container.getContext()).inflate(R.layout.pager_image_item_layout, container, false);
            ImageView image = (ImageView) itemView.findViewById(R.id.image);
            image.setImageResource(imageUrls[position]);
            container.addView(itemView);
            return itemView;
        }
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        super.onDestroy();
    }

    @Override
    public void showToast(String message) {
        if (!isDestroyed) {
            if (dialog != null) {
                dialog.dismiss();
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void closeOnBoarding() {
        if (intentOpenUrl != null) {
            if (intentOpenUrl.contains("/user/")) {
                try {
                    intentOpenUrl = CommonUtils.getUrlWithoutParameters(intentOpenUrl);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                String[] params = intentOpenUrl.split("\\/");
                String username = params[params.length - 1];
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_ONBOARDING);
                intent.putExtra("author_name", username);
                intent.putExtra("is_deeplink", true);
                startActivity(intent);
                finish();
            } else if (intentOpenUrl.contains("/competition/")) {
                String[] params = intentOpenUrl.split("\\/");
                String competitionId = params[params.length - 1];
                onBoardingActivityViewModel.getCompetitionData(competitionId);
            } else if (intentOpenUrl.contains("content/clips/")) {
                Intent intent = new Intent(this, ClipsActivity.class);
                String[] params = intentOpenUrl.split("\\/");
                String clipId = params[params.length - 1];
                intent.putExtra("clip_id", clipId);
                intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_ONBOARDING);
                startActivity(intent);
            } else {
                moveToHomePage(showUpdateMessage);
            }
        } else {
            moveToHomePage(showUpdateMessage);
        }

    }

    @Override
    public void showCompetionPage(Result result) {
        Intent intent = new Intent(this, AlertInformationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("is_deep_link", true);
        ListHolder.getInstance().setAlertInfoObject(result);
        startActivity(intent);
        finish();
    }

    public void moveToHomePage(boolean showUpdateMsg) {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.putExtra("open_url", intentOpenUrl);
        intent.putExtra("show_update_msg", showUpdateMsg);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_ONBOARDING);
        if (intentOpenUrl != null && !intentOpenUrl.isEmpty()) {
            LinkHandler.setIntentOpenUrl(intentOpenUrl);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        startActivity(intent);
        finish();
    }
}
