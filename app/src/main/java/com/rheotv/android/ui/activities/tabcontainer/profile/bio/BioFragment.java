package com.rheotv.android.ui.activities.tabcontainer.profile.bio;


import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.rheotv.android.R;
import com.rheotv.android.databinding.BioFragmentBinding;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;

import javax.inject.Inject;

public class BioFragment extends BaseFragment<BioFragmentBinding, BioFragmentViewModel>
        implements BioFragmentNavigator {

    BioFragmentBinding mFragmentBlogBinding;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private BioFragmentViewModel mBlogViewModel;
    private Context context;
    private String journalistName;

    public static BioFragment newInstance(String creatorUserName) {
        Bundle args = new Bundle();
        args.putString(AppConstants.AUTHOR_NAME, creatorUserName);
        BioFragment fragment = new BioFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.bio_fragment;
    }

    @Override
    public BioFragmentViewModel getViewModel() {
        mBlogViewModel = ViewModelProviders.of(this, mViewModelFactory).get(BioFragmentViewModel.class);
        return mBlogViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBlogViewModel.setNavigator(this);
        if (getArguments() != null) {
            if (getArguments().getString(AppConstants.AUTHOR_NAME) != null) {
                journalistName = getArguments().getString(AppConstants.AUTHOR_NAME);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentBlogBinding = getViewDataBinding();
        setUp();
        subscribeToNewData();
    }

    private void setUp() {
        mBlogViewModel.getBio(journalistName);
        mFragmentBlogBinding.followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mFragmentBlogBinding.editBio.getText() != null && mFragmentBlogBinding.editBio.getText().length() > 0) {
                    mFragmentBlogBinding.chatboxLL.setVisibility(View.GONE);
                    mFragmentBlogBinding.editButton.setVisibility(View.VISIBLE);
                    mFragmentBlogBinding.followButton.setVisibility(View.GONE);
                    mBlogViewModel.setBio(mFragmentBlogBinding.editBio.getText());
                }
            }
        });
        if (journalistName.equalsIgnoreCase("me")) {
            mFragmentBlogBinding.editButton.setVisibility(View.VISIBLE);
        } else {
            mFragmentBlogBinding.editButton.setVisibility(View.GONE);
        }
        mFragmentBlogBinding.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentBlogBinding.followButton.setVisibility(View.VISIBLE);
                mFragmentBlogBinding.editButton.setVisibility(View.GONE);
                mFragmentBlogBinding.chatboxLL.setVisibility(View.VISIBLE);
                mFragmentBlogBinding.editBio.setText(mFragmentBlogBinding.bioText.getText());
                mFragmentBlogBinding.bioText.setVisibility(View.GONE);
            }
        });
    }

    private void subscribeToNewData() {
        mBlogViewModel.getProfileData().observe(this, data -> mBlogViewModel.updateProfileData(data));
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        context = null;
        super.onDetach();
    }

    @Override
    public void setupViewsForLoggedinUser() {
        mFragmentBlogBinding.editButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void setupViewsForNonLoggedinUser() {
        mFragmentBlogBinding.editButton.setVisibility(View.GONE);
    }

    @Override
    public void showLoader(boolean b) {

    }

    @Override
    public void setBio(String bio) {
        mFragmentBlogBinding.bioText.setVisibility(View.VISIBLE);
        mFragmentBlogBinding.bioText.setText(bio);
    }
}