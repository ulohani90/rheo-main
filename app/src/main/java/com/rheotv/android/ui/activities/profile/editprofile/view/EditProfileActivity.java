package com.rheotv.android.ui.activities.profile.editprofile.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.Observable;

import com.google.android.material.chip.Chip;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.onboarding.LanguageObject;
import com.rheotv.android.databinding.EditProfileActivityLayoutBinding;
import com.rheotv.android.ui.activities.profile.editprofile.viewmodel.EditProfileViewModel;
import com.rheotv.android.ui.base.BaseActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import okhttp3.MultipartBody;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.rheotv.android.utils.AppConstants.GALLERY;
import static com.rheotv.android.utils.AppConstants.PERMISSION_REQUEST_CODE;

public class EditProfileActivity extends BaseActivity<EditProfileActivityLayoutBinding, EditProfileViewModel> {

    @Inject
    EditProfileViewModel mViewModel;

    EditProfileActivityLayoutBinding mBinding;
    ProgressDialog progressDialog;
    boolean isDestroyed;
    String username;
    String type;

    ArrayList<LanguageObject> languageObjects;
    HashMap<String, String> languageMap = new HashMap<>();
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static Intent getCallingIntent(Context context, String source) {
        Intent intent = new Intent(context, EditProfileActivity.class);
        intent.putExtra(AppConstants.SCREEN_SOURCE, source);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SegmentTracker.getInstance(this).recordScreenName(SegmentConstants.SCREEN_NAME_EDIT_PROFILE, new HashMap<>());
        mBinding = getViewDataBinding();
        mBinding.setViewModel(mViewModel);
        if (getIntent() != null && getIntent().hasExtra(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getIntent().hasExtra(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_EDIT_PROFILE);

        SegmentTracker.getInstance(this).recordScreenName(SegmentConstants.SCREEN_NAME_EDIT_PROFILE, baseProperties);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mViewModel.setFields(bundle);
        }

        mViewModel.languages.observe(this, this::setLanguageChipGroup);
        mViewModel.saving.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                finishOnSuccess(mViewModel.saving.get());
            }
        });

        mBinding.saveBtn.setOnClickListener(view -> {
            if (checkValidity())
                mViewModel.saveAuthorProfile();
        });

        mBinding.editProfilePic.setOnClickListener(v -> openGallery("edit_profile"));
        mBinding.editCoverPic.setOnClickListener(v -> openGallery("edit_cover"));
        setSupportActionBar(mBinding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        mBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setLanguageChipGroup(ArrayList<LanguageObject> languageObjects) {
        if (languageObjects == null) return;
        for (LanguageObject object : languageObjects) {
            Chip chip = new Chip(this, null, R.attr.chipChoiceStyle);
            chip.setTag(object.getId());
            chip.setText(object.getName());
            chip.setChecked(object.isSelected());
            if (object.isSelected()) {
                mViewModel.languageMap.put(object.getId(), object.getName());
            }

            chip.setOnCheckedChangeListener((compoundButton, b) -> {
                String tag = compoundButton.getTag().toString();
                if (mViewModel.languageMap.containsKey(tag)) {
                    mViewModel.languageMap.remove(tag);
                } else {
                    mViewModel.languageMap.put(tag, compoundButton.getText().toString());
                }
            });
            mBinding.languageChipGroup.addView(chip);
        }
    }

    private boolean checkValidity() {
        if (mViewModel.getFirstName() != null && mViewModel.getFirstName().length() > 0) {
            if (mViewModel.getLastName() != null && mViewModel.getLastName().length() > 0) {
                if (mBinding.usernameEt.getText() != null && mViewModel.getUserNameValue().length() > 0) {
                    Pattern ps = Pattern.compile("^[a-zA-Z0-9_-]*$");
                    Matcher ms = ps.matcher(mViewModel.getUserNameValue());
                    boolean bs = ms.matches();
                    if (bs) {
                        if (mViewModel.getDescription() != null && mViewModel.getDescription().trim().length() > 0) {
                            if (mViewModel.getBioValue() != null && mViewModel.getBioValue().trim().length() > 0) {
                                if (!mViewModel.languageMap.entrySet().isEmpty()) {
                                    return true;
                                } else {
                                    showToast("Please select at least one language", Toast.LENGTH_SHORT);
                                    return false;
                                }
                            } else {
                                mBinding.bioEt.setError("Please enter a bio");
                                return false;
                            }
                        } else {
                            mBinding.descriptionEt.setError("Please enter a description");
                            return false;
                        }
                    } else {
                        mBinding.usernameEt.setError("Only alphabets and numbers are allowed");
                        return false;
                    }
                } else {
                    mBinding.usernameEt.setError("Please enter a username");
                    return false;
                }
            } else {
                mBinding.lastNameEt.setError("Please enter last name");
                return false;
            }
        } else {
            mBinding.firstNameEt.setError("Please enter first name");
            return false;
        }
    }

    public void showToast(String message, int duration) {
        Toast.makeText(this, message, duration).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.edit_profile_activity_layout;
    }

    @Override
    public EditProfileViewModel getViewModel() {
        return mViewModel;
    }

    public void openGallery(String imageType) {
        this.type = imageType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isPermissionGranted()) {
                requestPermission();
                return;
            }
        }

        try {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setType("image/*");
            Intent chooserIntent = Intent.createChooser(galleryIntent, (imageType != null && imageType.equalsIgnoreCase("edit_profile")) ? "Select profile pic" : "Select cover pic");
            startActivityForResult(chooserIntent, GALLERY);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showToast("No Application found", Toast.LENGTH_SHORT);
        }
    }

    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(RheoTvApp.getNonUiContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery(type);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            ViewUtils.showMessageOKCancel(this, getResources().getString(R.string.photo_upload_permission),
                                    (dialog, which) -> requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE));
                        } else {
                            Toast.makeText(this, RheoTvApp.getNonUiContext().getResources().getString(R.string.photo_upload_permission), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if ((requestCode == GALLERY) && resultCode == Activity.RESULT_OK && data != null) {
                if (type != null) {
                    Uri uploadedImageUri = data.getData();
                    if (type.contentEquals(getString(R.string.edit_profile))) {
                        mViewModel.avatar.set(Objects.requireNonNull(uploadedImageUri).toString());
                    } else {
                        mViewModel.backdrop.set(Objects.requireNonNull(uploadedImageUri).toString());
                    }

                    Uri selectedImageUri = data.getData();
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (bitmap != null)
                        decodeFileAndStartImageUpload(bitmap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void decodeFileAndStartImageUpload(Bitmap thumbnail) {
        MultipartBody.Part part = CommonUtils.getMultiPartFile(this, thumbnail, "file");
        if (part != null) {
            mViewModel.uploadImage(part, this.type);
        }
    }

    public void finishOnSuccess(Status status) {
        if (status == Status.SUCCESS) {
            new Handler().postDelayed(() -> {
                Intent intent = getIntent();
                intent.putExtra("refresh_profile", true);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }, 1000);
        }
    }
}
