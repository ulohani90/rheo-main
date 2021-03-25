package com.rheotv.android.ui.activities.story;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.rheotv.android.R;
import com.rheotv.android.databinding.ActivityCreateStoryTemplateBinding;
import com.rheotv.android.ui.adapters.TemplateColorAdapter;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.story.Constants;
import com.rheotv.story.model.Story;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

public class CreateStoryTemplateActivity extends BaseActivity<ActivityCreateStoryTemplateBinding, CreateTemplateViewModel> {
    public static final int ARG_TEMPLATE_CODE = 1111;
    public static final String ARG_TEMPLATE_URI = "template_uri";
    public static final String ARG_TEMPLATE_DATA = "template_data";

    private String dirType = "/rheo_stories/";
    private String subPath = System.currentTimeMillis() + "_story.jpg";

    @Inject
    CreateTemplateViewModel mViewModel;

    private ActivityCreateStoryTemplateBinding mBinding;
    private TemplateColorAdapter colorAdapter;
    private Story.TextStory mTextStory;

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_story_template;
    }

    @Override
    public CreateTemplateViewModel getViewModel() {
        return mViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = getViewDataBinding();
        mTextStory = new Story.TextStory();
        setUpViews();
    }

    private void setUpViews() {
        colorAdapter = new TemplateColorAdapter();
        ArrayList<String> colors = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.template_color)));
        colorAdapter.addColors(colors);
        colorAdapter.addListener(this::onColorChange);

        mBinding.rootLayout.setBackgroundColor(Color.parseColor("#09192c"));
        mBinding.addedColorRv.setAdapter(colorAdapter);
        mBinding.navigateBackView.setOnClickListener(v -> finish());
        mBinding.doneButton.setOnClickListener(v -> createAndPublishTemplate());
    }

    private void onColorChange(String color) {
        mTextStory.setBackgroundColor(color);
        mBinding.rootLayout.setBackgroundColor(Color.parseColor(color));
        mBinding.templateContainer.setBackgroundColor(Color.parseColor(color));
        switch (color) {
            case "#219dd1":
            case "#d77ebb":
            case "#8e9bb2":
            case "#f55e5e":
            case "#fd7554":
                mBinding.statusEditText.setHintTextColor(ContextCompat.getColor(this, R.color.selected_region));
                break;

            case "#f5d02a":
                mBinding.statusEditText.setHintTextColor(ContextCompat.getColor(this, R.color.game_tag_bg_color));
                break;

            default:
                mBinding.statusEditText.setHintTextColor(ContextCompat.getColor(this, R.color.color_edit_text_hint));
                break;
        }
    }

    private void createAndPublishTemplate() {
        if (mBinding.statusEditText.getText().length() <= 0) {
            Toast.makeText(this, "Please enter some message", Toast.LENGTH_SHORT).show();
            return;
        }
        CommonUtils.hideKeyboard(this);
        mBinding.statusEditText.clearFocus();
        mBinding.statusEditText.setCursorVisible(false);

        new Handler().postDelayed(() -> {
            String path = getSaveImageFilePath();
            String textData = getTemplateData();
            if (path != null && textData != null) {
                Intent intent = new Intent();
                intent.putExtra(ARG_TEMPLATE_URI, path);
                intent.putExtra(ARG_TEMPLATE_DATA, textData);
                setResult(ARG_TEMPLATE_CODE, intent);
                finish();
            }
        }, 100);
    }

    private String getTemplateData() {
        if (mTextStory == null) {
            mTextStory = new Story.TextStory();
            mTextStory.setBackgroundColor("#09192c");
        }
        mTextStory.setText(mBinding.statusEditText.getText().toString());
        if (mTextStory.getBackgroundColor() == null || mTextStory.getBackgroundColor().isEmpty()) {
            mTextStory.setBackgroundColor("#09192c");
        }
        return new Gson().toJson(mTextStory);
    }

    private String getSaveImageFilePath() {
        mBinding.toolbar.setVisibility(View.INVISIBLE);
        mBinding.addedColorRv.setVisibility(View.INVISIBLE);
        File file = new File(Environment.getExternalStorageDirectory() + dirType);
        if (!file.exists()) file.mkdir();
        File bitmapFile = new File(Environment.getExternalStorageDirectory() + dirType, subPath);
        Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".app.provider", bitmapFile);
        if (fileUri != null) {
            mBinding.rootLayout.setDrawingCacheEnabled(true);
            mBinding.rootLayout.buildDrawingCache();
            Bitmap bitmap = Bitmap.createBitmap(mBinding.rootLayout.getDrawingCache());

            int maxSize = 1080;

            int bWidth = bitmap.getWidth();
            int bHeight = bitmap.getHeight();

            if (bWidth > bHeight) {
                int imageHeight = (int) Math.abs(maxSize * ((float) bitmap.getWidth() / (float) bitmap.getHeight()));
                bitmap = Bitmap.createScaledBitmap(bitmap, maxSize, imageHeight, true);
            } else {
                int imageWidth = (int) Math.abs(maxSize * ((float) bitmap.getWidth() / (float) bitmap.getHeight()));
                bitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, maxSize, true);
            }
            mBinding.rootLayout.setDrawingCacheEnabled(false);
            mBinding.rootLayout.destroyDrawingCache();
            mBinding.toolbar.setVisibility(View.VISIBLE);
            mBinding.addedColorRv.setVisibility(View.VISIBLE);

            OutputStream fOut;
            try {
                File file1 = new File(bitmapFile.getPath());
                fOut = new FileOutputStream(file1);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mTextStory.setAttachFileType(Constants.IMAGE);
            return bitmapFile.getPath();
        }

        return null;
    }
}
