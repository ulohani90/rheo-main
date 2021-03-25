package com.rheotv.android.ui.activities.tabcontainer.profile.wallet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.postlisting.responses.User;
import com.rheotv.android.data.network.models.useProfile.responses.ButtonData;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.databinding.WalletFragmentLayoutBinding;
import com.rheotv.android.ui.activities.tabcontainer.profile.ProfileNavigator;
import com.rheotv.android.ui.activities.tabcontainer.profile.container.ProfileContainerViewModel;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.customViews.WebviewActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.MySpannable;

import javax.inject.Inject;

public class WalletFragment extends BaseFragment<WalletFragmentLayoutBinding, ProfileContainerViewModel> implements ProfileNavigator {

    ProfileContainerViewModel mWalletViewModel;

    WalletFragmentLayoutBinding mBinding;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    String authorUsername;
    private ProfileResult mProfileResult;

    ProgressDialog progressDialog;

    public static WalletFragment newInstance(String authorUsername) {
        WalletFragment fragment = new WalletFragment();
        Bundle bundle = new Bundle();
        bundle.putString("author", authorUsername);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static WalletFragment newInstance(ProfileResult profileResult, String authorUsername) {
        WalletFragment fragment = new WalletFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(AppConstants.AUTHOR_PROFILE, profileResult);
        bundle.putString("author", authorUsername);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWalletViewModel.setNavigator(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        setUp();
        subscribeToLiveData();

    }

    private void setUp() {
        authorUsername = getArguments().getString("author");
        mProfileResult = getArguments().getParcelable(AppConstants.AUTHOR_PROFILE);
        if (mProfileResult == null)
            mWalletViewModel.fetchProfile(authorUsername);
        else
            mWalletViewModel.updateWalletWithResult(mProfileResult, authorUsername);
        mBinding.redeemNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = ProgressDialog.show(getContext(), null, "Reuquesting Payout");
                mWalletViewModel.requestPayout();
            }
        });
    }

    public void updateLiveData(ProfileResult profileResult) {
        if (profileResult == null) return;
        if (profileResult.getShouldShowPorgress()) {
            mBinding.rheoWalletLayout.setVisibility(View.GONE);
            mBinding.rheoProgress.setVisibility(View.VISIBLE);
            if (profileResult.getProgressData() != null) {
                mBinding.pgbProgress5.setVisibility(View.VISIBLE);
                mBinding.pgbProgress5.setShowText(false);
                mBinding.percentage.setText((int) profileResult.getProgressData().getProgress() + "%");
                mBinding.pgbProgress5.setProgress(profileResult.getProgressData().getProgress());
            }
            if (profileResult.getProgressData() != null && profileResult.getProgressData().getLabel2() != null) {
                mBinding.label2.setMovementMethod(LinkMovementMethod.getInstance());
                mBinding.label2.setText(getSpannableTextWithViewMore(profileResult, profileResult.getProgressData().getLabel2(), " View Details"), TextView.BufferType.SPANNABLE);
            }
        } else {
            mBinding.rheoProgress.setVisibility(View.GONE);
            mBinding.rheoWalletLayout.setVisibility(View.VISIBLE);
            mBinding.amount.setText("\u20B9 " + profileResult.getWallet().intValue());
            mBinding.redeemNowBtn.setVisibility(profileResult.getCanAllowPayout() ? View.VISIBLE : View.GONE);
            if (profileResult.getCanAllowPayout()) {
                mBinding.redeemNowBtn.setVisibility(View.VISIBLE);
                mBinding.disablePayoutMessage.setVisibility(View.GONE);
            } else {
                mBinding.redeemNowBtn.setVisibility(View.GONE);
                mBinding.disablePayoutMessage.setVisibility(View.VISIBLE);
                mBinding.disablePayoutMessage.setText(profileResult.getDiablePayoutReason());
            }
        }

    }

    private void subscribeToLiveData() {
        mWalletViewModel.getProfileData().observe(this, profileResult -> {

            if (mBinding == null) return;
            if (profileResult.getShouldShowPorgress()) {
                mBinding.rheoWalletLayout.setVisibility(View.GONE);
                mBinding.rheoProgress.setVisibility(View.VISIBLE);
                if (profileResult.getProgressData() != null) {
                    mBinding.pgbProgress5.setVisibility(View.VISIBLE);
                    mBinding.pgbProgress5.setShowText(false);
                    mBinding.percentage.setText((int) profileResult.getProgressData().getProgress() + "%");
                    mBinding.pgbProgress5.setProgress(profileResult.getProgressData().getProgress());
                }
                if (profileResult.getProgressData() != null && profileResult.getProgressData().getLabel2() != null) {
                    mBinding.label2.setMovementMethod(LinkMovementMethod.getInstance());
                    mBinding.label2.setText(getSpannableTextWithViewMore(profileResult, profileResult.getProgressData().getLabel2(), " View Details"), TextView.BufferType.SPANNABLE);
                }
            } else {
                mBinding.rheoProgress.setVisibility(View.GONE);
                mBinding.rheoWalletLayout.setVisibility(View.VISIBLE);
                if (profileResult != null) {
                    mBinding.amount.setText("\u20B9 " + (profileResult.getWallet() != null ? profileResult.getWallet().intValue() : 0));
                    mBinding.redeemNowBtn.setVisibility(profileResult.getCanAllowPayout() ? View.VISIBLE : View.GONE);
                    if (profileResult.getCanAllowPayout()) {
                        mBinding.redeemNowBtn.setVisibility(View.VISIBLE);
                        mBinding.disablePayoutMessage.setVisibility(View.GONE);
                    } else {
                        mBinding.redeemNowBtn.setVisibility(View.GONE);
                        mBinding.disablePayoutMessage.setVisibility(View.VISIBLE);
                        mBinding.disablePayoutMessage.setText(profileResult.getDiablePayoutReason());
                    }
                }
            }


        });


    }

    private SpannableStringBuilder getSpannableTextWithViewMore(ProfileResult profileResult, String label2, String moreText) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(label2);
        builder.append(moreText);
        builder.setSpan(new MySpannable(true) {
            @Override
            public void onClick(View widget) {
                if (profileResult.getButtonData() != null) {
                    openPartnerFlow(profileResult.getButtonData());
                }
            }
        }, label2.length() + 1, builder.length(), 0);
        return builder;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.wallet_fragment_layout;
    }

    @Override
    public ProfileContainerViewModel getViewModel() {
        if (getActivity() != null) {
            mWalletViewModel = new ViewModelProvider(getActivity(), mViewModelFactory).get(ProfileContainerViewModel.class);
        } else {
            mWalletViewModel = new ViewModelProvider(this, mViewModelFactory).get(ProfileContainerViewModel.class);
        }
        return mWalletViewModel;
    }

    @Override
    public void handleError(Throwable throwable) {

    }

    @Override
    public void editProfile() {

    }

    @Override
    public void setupViewsForLoggedinUser() {

    }

    @Override
    public void setupViewsForNonLoggedinUser() {

    }

    @Override
    public void handleLogin() {

    }

    @Override
    public void setUpLayoutForAuthor() {

    }

    @Override
    public void openGallery(String type) {

    }

    @Override
    public User getNewUserObjectFromView() {
        return null;
    }

    @Override
    public void showLoader(boolean show) {

    }

    @Override
    public void editUserName() {

    }

    @Override
    public Context getContextInstance() {
        return getContext();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateUserName(String username) {

    }

    @Override
    public void openPartnerFlow(ButtonData buttonData) {
        Intent intent = new Intent(getActivity(), WebviewActivity.class);
        intent.putExtra("URL", buttonData.getDeeplink());
        startActivity(intent);
    }

    @Override
    public void updateUI(ProfileResult body) {

    }

    @Override
    public void navigateToHome() {

    }

    @Override
    public void setBio(ProfileResult bio) {

    }

    @Override
    public void startEditProfileActivity() {

    }

    @Override
    public void startUploadActivity() {

    }

    @Override
    public void setUpRheoProgressView() {

    }

    @Override
    public void updateProfileViewModelData() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void hideProgressBar() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public String getAuthorUsername() {
        return authorUsername;
    }

    @Override
    public void setUpTabs() {

    }

    @Override
    public void setUpFloatinActionButton() {

    }

    @Override
    public void onMedalViewClick() {

    }

    @Override
    public void onContentModeratorVoted() {

    }
}
