package com.rheotv.android.ui.activities.trim;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.lb.video_trimmer_library.interfaces.VideoTrimmingListener;
import com.rheotv.android.R;
import com.rheotv.android.databinding.ActivityTrimVideoBinding;
import com.rheotv.android.ui.base.BaseActivity;

import java.io.File;

import javax.inject.Inject;

public class TrimVideoActivity extends BaseActivity<ActivityTrimVideoBinding, TrimVideoViewModel> implements VideoTrimmingListener {

    public static final String ARG_VIDEO_URI = "arg_video";
    public static final int CODE_TRIM_VIDEO = 1100;

    @Inject
    TrimVideoViewModel mViewModel;
    private ActivityTrimVideoBinding mBinding;

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_trim_video;
    }

    @Override
    public TrimVideoViewModel getViewModel() {
        return mViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = getViewDataBinding();

        if (getIntent() != null) {
            if (getIntent().hasExtra(ARG_VIDEO_URI))
                mViewModel.videoUri.set(getIntent().getStringExtra(ARG_VIDEO_URI));
        }

        mBinding = getViewDataBinding();
        mBinding.setViewModel(mViewModel);
        setupViews();
    }

    private void setupViews() {
        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mBinding.toolbar.setTitle("");
        mBinding.toolbar.setNavigationOnClickListener(v -> finish());
        mBinding.videoTrimmerView.setMaxDurationInMs(15 * 1000);
        mBinding.videoTrimmerView.setOnK4LVideoListener(TrimVideoActivity.this);

        File file = new File(Environment.getExternalStorageDirectory() + mViewModel.dirType);
        if (!file.exists()) file.mkdir();
        File videoFile = new File(Environment.getExternalStorageDirectory() + mViewModel.dirType, mViewModel.subPath);
        Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".app.provider", videoFile);
        if (fileUri != null) {
            mBinding.videoTrimmerView.setDestinationFile(videoFile);

            mBinding.videoTrimmerView.setVideoURI(Uri.parse("file:///" + mViewModel.videoUri.get()));
            mBinding.videoTrimmerView.setVideoInformationVisibility(true);
        }
    }


    @Override
    public void onErrorWhileViewingVideo(int i, int i1) {
        mBinding.trimmingProgressView.setVisibility(View.GONE);
        Toast.makeText(this, "error while previewing video", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFinishedTrimming(Uri uri) {
        mBinding.trimmingProgressView.setVisibility(View.GONE);
        if (uri == null) {
            Toast.makeText(this, "failed trimming", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Video Saved", Toast.LENGTH_SHORT).show();
            publishResult(uri.getPath());
        }
    }

    @Override
    public void onTrimStarted() {
        mBinding.trimmingProgressView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onVideoPrepared() {

    }

    private void publishResult(String path) {
        Intent intent = new Intent();
        intent.putExtra(ARG_VIDEO_URI, path);
        setResult(CODE_TRIM_VIDEO, intent);
        finish();
    }
}
