package com.rheotv.android.ui.activities.tabcontainer.profile;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rheotv.android.BR;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.postlisting.responses.User;
import com.rheotv.android.data.network.models.useProfile.responses.ButtonData;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.databinding.AuthorProfileBinding;
import com.rheotv.android.ui.activities.player.activity.PlayerActivity;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.activities.profile.editprofile.view.EditProfileActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivity;
import com.rheotv.android.ui.activities.tabcontainer.posts.OnFollowActionCompleteListener;
import com.rheotv.android.ui.activities.tabcontainer.videoUpload.VideoUploadFragment;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivity;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.customViews.WebviewActivity;
import com.rheotv.android.ui.fragments.UploadContactsDialogFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.PlayerHeadServiceHelper;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.TimeUtils;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import okhttp3.MultipartBody;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.rheotv.android.utils.AppConstants.SEE_ALL_TYPE_CHAT;
import static com.rheotv.android.utils.AppConstants.SEE_ALL_TYPE_INVOICE;

public class ProfileFragment extends BaseFragment<AuthorProfileBinding, ProfileViewModel>
        implements ProfileNavigator, PostListAdapter.BlogAdapterListener {

    private static final int GALLERY = 901;
    private static final int PERMISSION_REQUEST_CODE = 111;
    AuthorProfileBinding mFragmentBlogBinding;
    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    String journalistName = "me";
    private ProfileResult mProfileResult;

    @Inject
    PostListAdapter postListAdapter;

    String path;
    String type;

    LinearLayoutManager mLayoutManager;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private ProfileViewModel mBlogViewModel;
    private Context context;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static ProfileFragment newInstance(String creatorUserName) {
        Bundle args = new Bundle();
        args.putString(AppConstants.AUTHOR_NAME, creatorUserName);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ProfileFragment newInstance(ProfileResult profileResult, String creatorUserName, String source) {
        Bundle args = new Bundle();
        args.putParcelable(AppConstants.AUTHOR_PROFILE, profileResult);
        args.putString(AppConstants.AUTHOR_NAME, creatorUserName);
        args.putString(AppConstants.SCREEN_SOURCE, source);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void showLoader(boolean show) {
//        mFragmentBlogBinding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void editUserName() {
//        int etVisiblity = mFragmentBlogBinding.userNameEt.getVisibility();
//        if (etVisiblity == View.VISIBLE) {
//            String username = mFragmentBlogBinding.userNameEt.getText().toString();
//            Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");
//            Matcher ms = ps.matcher(username);
//            boolean bs = ms.matches();
//            if (!bs) {
//                Toast.makeText(getActivity(), "Only alphabets and numbers are allowed", Toast.LENGTH_SHORT).show();
//                mFragmentBlogBinding.userNameEt.setTextColor(Color.RED);
//            } else {
//                mFragmentBlogBinding.userNameEt.setVisibility(View.GONE);
//                mFragmentBlogBinding.authorName.setVisibility(View.VISIBLE);
//                mFragmentBlogBinding.editNameButton.setBackground(getActivity().getDrawable(R.drawable.edit_p));
//                getViewModel().checkUsernameAndSignup(username);
//            }
//        } else {
//            mFragmentBlogBinding.userNameEt.setVisibility(View.VISIBLE);
//            mFragmentBlogBinding.authorName.setVisibility(View.GONE);
//            mFragmentBlogBinding.editNameButton.setBackground(getActivity().getDrawable(R.drawable.done_p));
//        }
    }

    @Override
    public Context getContextInstance() {
        return getContext();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateUserName(String username) {
//        mFragmentBlogBinding.authorName.setText(username);
    }

    @Override
    public void openPartnerFlow(ButtonData buttonData) {
        Intent intent = new Intent(getActivity(), WebviewActivity.class);
        intent.putExtra("URL", buttonData.getDeeplink());
        startActivity(intent);
    }

    @Override
    public void updateUI(ProfileResult body) {
        if (body.getProgressData() != null) {
            //mFragmentBlogBinding.pgbProgress5.setProgress(body.getProgressData().getProgress());
        }
    }

    @Override
    public void navigateToHome() {
        if (getActivity() instanceof TabContainerActivity) {
            ((TabContainerActivity) getActivity()).handleLogout();
        }
    }

    @Override
    public void setBio(ProfileResult bio) {

    }

    @Override
    public void startEditProfileActivity() {
        if (mBlogViewModel.authorProfileData.get() != null && mBlogViewModel.authorProfileData.get().getUser() != null) {
            Bundle bundle = new Bundle();
            User user = mBlogViewModel.authorProfileData.get().getUser();
            bundle.putString("first_name", user.getFirstName());
            bundle.putString("last_name", user.getLastName());
            bundle.putString("description", mBlogViewModel.authorProfileData.get().getIntro());
            bundle.putString("profile_pic", mBlogViewModel.authorProfileData.get().getProfilePic());
            bundle.putString("cover_pic", mBlogViewModel.authorProfileData.get().getCoverPic());
            bundle.putString("bio", mBlogViewModel.authorProfileData.get().getBio());
            bundle.putString("username", user.getUsername());
            bundle.putParcelableArrayList("language_objs", mBlogViewModel.authorProfileData.get().getLanguages());
            Intent intent = EditProfileActivity.getCallingIntent(context, SegmentConstants.SCREEN_NAME_SELF_PROFILE_DETAILS);
            intent.putExtras(bundle);
            getActivity().startActivityForResult(intent, AppConstants.REQUEST_CODE_EDIT_PROFILE);
        } else {
            Log.i(ProfileFragment.class.getCanonicalName(), "Null Author object while edit action");
        }
    }

    @Override
    public void startUploadActivity() {
        if (mBlogViewModel.authorProfileData.get() != null && mBlogViewModel.authorProfileData.get().getUser() != null) {
            VideoUploadFragment uploadFragment = VideoUploadFragment.newInstance(SegmentConstants.SCREEN_NAME_SELF_PROFILE_DETAILS);
            uploadFragment.show(getChildFragmentManager(), null);
        } else {
            Log.i(ProfileFragment.class.getCanonicalName(), "Null Author object while edit action");
        }
    }

    @Override
    public void setUpRheoProgressView() {

    }

    @Override
    public void updateProfileViewModelData() {

    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public String getAuthorUsername() {
        return null;
    }

    @Override
    public void setUpTabs() {

    }

    @Override
    public void setUpFloatinActionButton() {

    }


    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.author_profile;
    }

    @Override
    public ProfileViewModel getViewModel() {
        mBlogViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ProfileViewModel.class);
        return mBlogViewModel;
    }

    @Override
    public void handleError(Throwable throwable) {
        Toast.makeText(getActivity(), "Connection Issue, Please try again later!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void editProfile() {
        Toast.makeText(getActivity(), "I am in edit profile", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setupViewsForLoggedinUser() {
        Log.i(getClass().getSimpleName(), "setupViewsForLoggedinUser 1");
        if (journalistName.equalsIgnoreCase("me") || journalistName.equalsIgnoreCase(CommonUtils.getUserName(getContext()))) {
            mFragmentBlogBinding.loginButton.setVisibility(View.GONE);
            mFragmentBlogBinding.placeholderText.setVisibility(View.GONE);
            mFragmentBlogBinding.verifiedAuthorButtons.setVisibility(View.GONE);

            mFragmentBlogBinding.editLayout.setVisibility(View.GONE);
            if (mBlogViewModel.authorProfileData != null && mBlogViewModel.authorProfileData.get() != null) {
                String profileImageUrl = mBlogViewModel.authorProfileData.get().getProfilePic();
                sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.USER_PROFILE_PIC,
                        profileImageUrl);
                String bio = mBlogViewModel.authorProfileData.get().getBio();
                if (bio != null && bio.length() > 0) {
                    mFragmentBlogBinding.bioText.setText(mBlogViewModel.authorProfileData.get().getBio());
                    mFragmentBlogBinding.addBio.setVisibility(View.GONE);
                } else {
                    mFragmentBlogBinding.bioText.setText(getString(R.string.good_bio_msg));
                    mFragmentBlogBinding.addBio.setVisibility(View.VISIBLE);
                    mFragmentBlogBinding.addBio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startEditProfileActivity();
                        }
                    });
                }
            }
        }
    }

    private void openSupport() {
        if (mBlogViewModel.getProfileData().getValue().getUser() != null && mBlogViewModel.getProfileData().getValue().getUser().getUsername() != null) {
            Intent intent = new Intent(getActivity(), UniversalActivity.class);
            intent.putExtra(AppConstants.SEE_ALL_TYPE, SEE_ALL_TYPE_CHAT);
            intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, mBlogViewModel.getProfileData().getValue().getUser().getUsername());
            intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_PROFILE_SELF);
            startActivity(intent);
        }
    }

    private void openInvoices() {
        if (mBlogViewModel.getProfileData().getValue().getUser() != null && mBlogViewModel.getProfileData().getValue().getUser().getUsername() != null) {
            Intent intent = new Intent(getActivity(), UniversalActivity.class);
            intent.putExtra(AppConstants.SEE_ALL_TYPE, SEE_ALL_TYPE_INVOICE);
            intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, mBlogViewModel.getProfileData().getValue().getUser().getUsername());
            intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_PROFILE_SELF);
            startActivity(intent);
        }
    }

    public void showEditLayout() {
        if (mFragmentBlogBinding.editLayout.getVisibility() == View.GONE) {
            mFragmentBlogBinding.editLayout.setVisibility(View.VISIBLE);
        } else {
            mFragmentBlogBinding.editLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void setupViewsForNonLoggedinUser() {
        if (journalistName.equalsIgnoreCase("me")) {
            mFragmentBlogBinding.editLayout.setVisibility(View.GONE);
            mFragmentBlogBinding.loginButton.setVisibility(View.VISIBLE);
            mFragmentBlogBinding.verifiedAuthorButtons.setVisibility(View.GONE);
            mFragmentBlogBinding.bioText.setVisibility(View.GONE);
            mFragmentBlogBinding.addBio.setVisibility(View.GONE);
            mFragmentBlogBinding.bioHeader.setVisibility(View.GONE);

        }
    }

    @Override
    public void handleLogin() {
        try {
            if (getActivity() instanceof TabContainerActivity) {
                ((TabContainerActivity) getBaseActivity()).launchLogInFragment();
            } else if (getActivity() instanceof PlayerActivity) {
                ((PlayerActivity) getActivity()).openLoginFlow();
            } else if (getActivity() instanceof ProfileActivity) {
                ((ProfileActivity) getActivity()).openLoginFlow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setUpLayoutForAuthor() {
//        mFragmentBlogBinding.blogRecyclerView.setVisibility(View.VISIBLE);

        mFragmentBlogBinding.editLayout.setVisibility(View.GONE);
        mFragmentBlogBinding.verifiedAuthorButtons.setVisibility(View.GONE);
        mFragmentBlogBinding.placeholderText.setVisibility(View.GONE);
    }

    @Override
    public void openGallery(String type) {
        this.type = type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isPermissionGranted()) {
                requestPermission();
                return;
            }
        }
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    @Override
    public User getNewUserObjectFromView() {
        return new User(mFragmentBlogBinding.firstNameET.getText().toString(), mFragmentBlogBinding.lastNameET.getText().toString(), mFragmentBlogBinding.descET.getText().toString());
    }

    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(RheoTvApp.getNonUiContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        openGallery(type);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                                ViewUtils.showMessageOKCancel(getContext(), getResources().getString(R.string.photo_upload_permission),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                                            }
                                        });
                                return;
                            } else {
                                Toast.makeText(context, RheoTvApp.getNonUiContext().getResources().getString(R.string.photo_upload_permission), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d(MojoTimesApplication.TAG, "Edit profile " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
//        if ((requestCode == GALLERY) && data != null) {
//            Uri uploadedImageUri = data.getData();
//
//            if (type.contentEquals(getString(R.string.edit_profile))) {
//                Picasso.get().load(uploadedImageUri).placeholder(context.getResources().getDrawable(R.drawable.avd_avatar)).into(mFragmentBlogBinding.authorID2);
//            } else {
//                Picasso.get().load(uploadedImageUri).placeholder(context.getResources().getDrawable(R.drawable.profile_cover)).into(mFragmentBlogBinding.coverPic);
//            }
//
//            Uri selectedImageUri = data.getData();
//            path = CommonUtils.getPathFromUrl(getBaseActivity(), selectedImageUri);
//
//            Bitmap bitmap = CommonUtils.getBitmapFromUrl(path);
//            decodeFileAndStartImageUpload(bitmap);
//        }
    }

    private void decodeFileAndStartImageUpload(Bitmap thumbnail) {
        MultipartBody.Part part = CommonUtils.getMultiPartFile(getContext(), thumbnail, "file");
        if (part != null)
            mBlogViewModel.uploadImage(part, this.type);
        else {
//            Toast.makeText(getContext(), R.string.err_msgNoresponse, Toast.LENGTH_SHORT).show();
//            hideDialog();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mProfileResult != null)
            outState.putParcelable(AppConstants.AUTHOR_PROFILE, mProfileResult);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBlogViewModel.setNavigator(this);
        postListAdapter.setListener(this);
        if (getArguments() != null) {
            if (getArguments().getString(AppConstants.AUTHOR_NAME) != null) {
                journalistName = getArguments().getString(AppConstants.AUTHOR_NAME);
            }

            if (getArguments().containsKey(AppConstants.AUTHOR_PROFILE))
                mProfileResult = getArguments().getParcelable(AppConstants.AUTHOR_PROFILE);

            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        }

        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_SELF_PROFILE_DETAILS);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.SCREEN_NAME_SELF_PROFILE_DETAILS, baseProperties);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentBlogBinding = getViewDataBinding();
        setUp();
        subscribeToNewData();
        checkAndShowUploadContacts();
    }

    private void checkAndShowUploadContacts() {
        if (!CommonUtils.getHideSyncContacts()) {
            long lastShownTS = sharedPrefsUtils.getLongPreference(getActivity(), SharedPrefsUtils.LAST_UPLOAD_CONTACTS_SHOWN_TIME, -1);
            if (!CommonUtils.getContactsUploadSuccess() && (lastShownTS == -1 || (System.currentTimeMillis() - lastShownTS >= TimeUtils.MILLIS_IN_DAY))) {
                UploadContactsDialogFragment fragment = UploadContactsDialogFragment.newInstance();
                getActivity().getSupportFragmentManager().beginTransaction().add(fragment, AppConstants.UPLOAD_CONTACTS_DIALOG_FRAGMENT).commitAllowingStateLoss();
                sharedPrefsUtils.setLongPreference(getActivity(), SharedPrefsUtils.LAST_UPLOAD_CONTACTS_SHOWN_TIME, System.currentTimeMillis());
            }
        } else {
            CommonUtils.setHideSyncContacts(false);
        }
    }

    private void setUp() {
        if (mProfileResult == null)
            mBlogViewModel.fetchProfile(journalistName);
        else
            mBlogViewModel.setAuthorProfile(mProfileResult, journalistName);
        mLayoutManager = new LinearLayoutManager(context);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    }

    private void subscribeToNewData() {
        mBlogViewModel.getProfileData().observe(getViewLifecycleOwner(), data -> mBlogViewModel.updateProfileData(data));
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
    public void onItemClick(String id, PostObject post) {
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
        if (getContext() == null) return;
        StreamPlayerActivity.Companion.startActivity(getContext(),
                new StreamPlayerContainerFragment.Builder()
                        .addPost(post)
                        .addSourceScreenName(SegmentConstants.SCREEN_NAME_SELF_PROFILE_DETAILS)
                        .buildExtras());
    }

    @Override
    public void onRetryClick() {
        //no retry
//        mFragmentBlogBinding.blogRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onLikeButtonClicked(String body, Result post) {
        mBlogViewModel.onLikeItemClicked(body, post);
    }

    @Override
    public void onShareButtonClicked(PostObject post) {
        //mBlogViewModel.onShareItemClicked(body, post, context);
    }

    @Override
    public void onAuthorClicked(String userName) {
        //do nothing
    }

    @Override

    public void onCarouselItemClicked(String id, List<PostObject> results, PostObject post, String carouselTitle) {

    }


    @Override
    public void onMultiViewItemClicked(String id, List<PostObject> results) {

    }

    @Override
    public void onSeeMoreClicked(List<PostObject> result) {

    }

    @Override
    public void onLeaderboardClicked(String id) {

    }

    @Override
    public void onSeeAllClicked(String game, String id) {
        //
    }

    @Override
    public void onAlertCardClicked() {

    }

    @Override
    public void onFollowBtnClicked(String author, int id, boolean isFollowed, OnFollowActionCompleteListener listener) {

    }

    @Override
    public void onProfileViewAction(String authorUsername, OnFollowActionCompleteListener listener) {

    }

    @Override
    public void onMoreOptionsBtnClick(String id) {

    }

    @Override
    public void onSuperPrimeReminderListener(PostObject result) {

    }

    @Override
    public void onSuperStreamerCardClick(String id) {

    }

    @Override
    public void onGameClicked(String game, String gameId) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("game", game);
        properties.put("id", gameId);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_GAME_TAG_CLICKED, properties);

        Intent intent = new Intent(getActivity(), UniversalActivity.class);
        intent.putExtra(AppConstants.SEE_ALL_TYPE, game);
        intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, gameId);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_PROFILE_SELF);
        startActivity(intent);
    }

    @Override
    public void onDeleteVideoClicked(String id, int position) {

    }

    @Override
    public void onDownloadVideoClicked(String id, int position) {

    }

    @Override
    public void onMedalViewClick() {

    }

    @Override
    public void onContentModeratorVoted() {

    }
}
