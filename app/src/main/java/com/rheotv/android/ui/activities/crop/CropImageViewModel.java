package com.rheotv.android.ui.activities.crop;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.naver.android.helloyako.imagecrop.view.ImageCropView;
import com.rheotv.android.data.DataManager;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.io.File;

public class CropImageViewModel extends BaseViewModel {
    String dirType = "/rheo_stories/";
    String subPath = System.currentTimeMillis() + "_story.jpg";

    public ObservableField<String> imageUri = new ObservableField<>();
    public MutableLiveData<String> cropPath = new MutableLiveData<>();

    public CropImageViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void onCrop(View view, ImageCropView imageView) {
        File file = CommonUtils.bitmapConvertToFile(view.getContext(), imageView.getCroppedImage(), dirType, subPath);
        Toast.makeText(view.getContext(), "file saved", Toast.LENGTH_LONG).show();
        cropPath.setValue(file.getAbsolutePath());
    }

    public void dismiss(View view) {
        ((Activity) view.getContext()).finish();
    }

}
