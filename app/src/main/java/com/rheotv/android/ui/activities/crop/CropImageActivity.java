package com.rheotv.android.ui.activities.crop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.rheotv.android.R;
import com.rheotv.android.databinding.ActivityCropImageBinding;
import com.rheotv.android.ui.base.BaseActivity;

import javax.inject.Inject;

public class CropImageActivity extends BaseActivity<ActivityCropImageBinding, CropImageViewModel> {

    @Inject
    CropImageViewModel mViewModel;

    private ActivityCropImageBinding mBinding;
    public static final String ARG_IMAGE_URI = "arg_image";
    public static final int CODE_CROP_IMAGE = 1000;

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_crop_image;
    }

    @Override
    public CropImageViewModel getViewModel() {
        mViewModel.cropPath.observe(this, this::publishResult);
        return mViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            if (getIntent().hasExtra(ARG_IMAGE_URI))
                mViewModel.imageUri.set(getIntent().getStringExtra(ARG_IMAGE_URI));
        }

        mBinding = getViewDataBinding();
        mBinding.setViewModel(mViewModel);
        setupViews();
    }

    private void publishResult(String path) {
        Intent intent = new Intent();
        intent.putExtra(ARG_IMAGE_URI, path);
        setResult(CODE_CROP_IMAGE, intent);
        finish();
    }

    private void setupViews() {
        if (mViewModel.imageUri.get() != null) {
            mBinding.imageCropView.setImageFilePath(mViewModel.imageUri.get());
            mBinding.imageCropView.setAspectRatio(9, 16);
        } else {
            Toast.makeText(this, "Picture not found. Please select another picture.", Toast.LENGTH_LONG).show();
        }
    }
}
