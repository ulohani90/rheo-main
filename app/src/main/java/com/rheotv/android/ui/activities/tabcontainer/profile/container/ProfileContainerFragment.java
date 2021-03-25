package com.rheotv.android.ui.activities.tabcontainer.profile.container;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.Observable;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.freshchat.consumer.sdk.Freshchat;
import com.freshchat.consumer.sdk.FreshchatUser;
import com.freshchat.consumer.sdk.exception.MethodNotAllowedException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.postlisting.responses.User;
import com.rheotv.android.data.network.models.useProfile.responses.ButtonData;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.databinding.AuthorProfileContainerBinding;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.ui.activities.profile.editprofile.view.EditProfileActivity;
import com.rheotv.android.ui.activities.follower.FollowActivity;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.activities.moderators.AddModeratorsActivity;
import com.rheotv.android.ui.activities.player.activity.PlayerActivity;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment;
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.activities.rank.RankActivity;
import com.rheotv.android.ui.activities.story.CreateStoryActivity;
import com.rheotv.android.ui.activities.story.StoryActivity;
import com.rheotv.android.ui.activities.tabcontainer.TabContainerActivity;
import com.rheotv.android.ui.activities.tabcontainer.posts.OnFollowActionCompleteListener;
import com.rheotv.android.ui.activities.tabcontainer.profile.ProfileFragment;
import com.rheotv.android.ui.activities.tabcontainer.profile.ProfileNavigator;
import com.rheotv.android.ui.activities.tabcontainer.profile.ProfileTabAdapter;
import com.rheotv.android.ui.activities.tabcontainer.profile.analytics.AnalyticsFragment;
import com.rheotv.android.ui.activities.tabcontainer.profile.videos.VideosFragment;
import com.rheotv.android.ui.activities.tabcontainer.profile.wallet.WalletFragment;
import com.rheotv.android.ui.activities.tabcontainer.profile.wallet.WalletFragmentV2;
import com.rheotv.android.ui.activities.tabcontainer.videoUpload.VideoUploadFragment;
import com.rheotv.android.ui.activities.universalActivity.UniversalActivity;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.customViews.WebviewActivity;
import com.rheotv.android.ui.customViews.bottomSheetMenu.BottomSheetMenuDialog;
import com.rheotv.android.ui.customViews.bottomSheetMenu.Option;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.FirebaseDynamicLinkUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.MySpannable;
import com.rheotv.android.utils.NetworkUtils;
import com.rheotv.android.utils.PlayerHeadServiceHelper;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import io.branch.referral.Branch;
import okhttp3.MultipartBody;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.rheotv.android.utils.AppConstants.REQUEST_CODE_ADD_MODERATORS;
import static com.rheotv.android.utils.AppConstants.SEE_ALL_TYPE_CHAT;
import static com.rheotv.android.utils.AppConstants.SEE_ALL_TYPE_INVOICE;
import static com.rheotv.android.utils.segmentTracker.SegmentConstants.EVENT_RECENT_VIEWERS_TAB_CLICKED;

public class ProfileContainerFragment extends BaseFragment<AuthorProfileContainerBinding, ProfileContainerViewModel>
        implements ProfileNavigator, PostListAdapter.BlogAdapterListener {

    private static final int GALLERY = 901;
    private static final int PERMISSION_REQUEST_CODE = 111;
    AuthorProfileContainerBinding mFragmentBlogBinding;
    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    String userId;

    @Inject
    PostListAdapter mBlogAdapter;

    String path;
    String type;

    LinearLayoutManager mLayoutManager;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private ProfileContainerViewModel mBlogViewModel;
    private Context context;
    private VideoUploadFragment uploadFragment;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static ProfileContainerFragment newInstance(String creatorUserName, String source) {
        Bundle args = new Bundle();
        args.putString(AppConstants.AUTHOR_NAME, creatorUserName);
        args.putString(AppConstants.SCREEN_SOURCE, source);
        ProfileContainerFragment fragment = new ProfileContainerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ProfileContainerFragment newInstance() {
        Bundle args = new Bundle();
        ProfileContainerFragment fragment = new ProfileContainerFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void showLoader(boolean show) {
        if (mFragmentBlogBinding != null && mFragmentBlogBinding.progressBar != null)
            mFragmentBlogBinding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void editUserName() {
       /* int etVisiblity = mFragmentBlogBinding.userNameEt.getVisibility();
        if (etVisiblity == View.VISIBLE) {
            String username = mFragmentBlogBinding.userNameEt.getText().toString();
            Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");
            Matcher ms = ps.matcher(username);
            boolean bs = ms.matches();
            if (!bs) {
                Toast.makeText(getActivity(), "Only alphabets and numbers are allowed", Toast.LENGTH_SHORT).show();
                mFragmentBlogBinding.userNameEt.setTextColor(Color.RED);
            } else {
                mFragmentBlogBinding.userNameEt.setVisibility(View.GONE);
                mFragmentBlogBinding.authorName.setVisibility(View.VISIBLE);
                mFragmentBlogBinding.editNameButton.setBackground(getActivity().getDrawable(R.drawable.edit_p));
                getViewModel().checkUsernameAndSignup(username);
            }
        } else {
            mFragmentBlogBinding.userNameEt.setVisibility(View.VISIBLE);
            mFragmentBlogBinding.authorName.setVisibility(View.GONE);
            mFragmentBlogBinding.editNameButton.setBackground(getActivity().getDrawable(R.drawable.done_p));
        }*/
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
        mFragmentBlogBinding.authorName.setText(username);
    }

    @Override
    public void openPartnerFlow(ButtonData buttonData) {
        Intent intent = new Intent(getActivity(), WebviewActivity.class);
        intent.putExtra("URL", buttonData.getDeeplink());
        startActivity(intent);
    }

    @Override
    public void updateUI(ProfileResult body) {

       /* if (body.getProgressData() != null) {
            mFragmentBlogBinding.pgbProgress5.setProgress(body.getProgressData().getProgress());
        }
        if (body.getProgressData() != null && body.getProgressData().getLabel2() != null) {
            mFragmentBlogBinding.label2.setMovementMethod(LinkMovementMethod.getInstance());
            mFragmentBlogBinding.label2.setText(getSpannableTextWithViewMore(body.getProgressData().getLabel2(), " View Details"), TextView.BufferType.SPANNABLE);
        }*/

    }

    private SpannableStringBuilder getSpannableTextWithViewMore(String label2, String moreText) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(label2);
        builder.append(moreText);
        builder.setSpan(new MySpannable(true) {
            @Override
            public void onClick(View widget) {
                mBlogViewModel.onMoreInfoPartnerClicked();
            }
        }, label2.length() + 1, builder.length(), 0);
        return builder;
    }

    @Override
    public void navigateToHome() {
        if (getActivity() instanceof TabContainerActivity) {
            ((TabContainerActivity) getActivity()).handleLogout();
            //((TabContainerActivity) getActivity()).handleBackendLoginResponse(true);
        }
    }


    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.author_profile_container;
    }

    @Override
    public ProfileContainerViewModel getViewModel() {
        mBlogViewModel = new ViewModelProvider(this, mViewModelFactory).get(ProfileContainerViewModel.class);
        return mBlogViewModel;
    }

    @Override
    public void handleError(Throwable throwable) {
        try {
            Toast.makeText(getActivity(), "Connection Issue, Please try again later!", Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void editProfile() {
        Toast.makeText(getActivity(), "I am in edit profile", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setupViewsForLoggedinUser() {
//        if (journalistName.equalsIgnoreCase("me")) {
//            mFragmentBlogBinding.loginButton.setVisibility(View.GONE);
//            mFragmentBlogBinding.placeholderText.setVisibility(View.GONE);
//            mFragmentBlogBinding.verifiedAuthorButtons.setVisibility(View.GONE);
//            mFragmentBlogBinding.editProfileLayout.setVisibility(View.VISIBLE);
//            mFragmentBlogBinding.editLayout.setVisibility(View.GONE);
        mFragmentBlogBinding.follow.setVisibility(View.GONE);
        mFragmentBlogBinding.settingImageView.setVisibility(View.VISIBLE);

        //mFragmentBlogBinding.share.setVisibility(View.GONE);

//        mFragmentBlogBinding.invoices.setOnClickListener(view -> openInvoices());
//        mFragmentBlogBinding.chatSupport.setOnClickListener(view -> openSupport());
        mFragmentBlogBinding.goLiveBtn.setOnClickListener(view -> {
            if (getActivity() != null && getActivity() instanceof TabContainerActivity)
                ((TabContainerActivity) getActivity()).goLiveClicked(SegmentConstants.SCREEN_NAME_TABS_HOME_PAGE);
        });
//            mFragmentBlogBinding.blogRecyclerView.setVisibility(View.GONE);
//            mFragmentBlogBinding.authorName.setVisibility(View.VISIBLE);
//            mFragmentBlogBinding.authorIntro.setVisibility(View.VISIBLE);
//            if (mBlogViewModel.authorProfileData != null && mBlogViewModel.authorProfileData.get() != null) {
//                String profileImageUrl = mBlogViewModel.authorProfileData.get().getProfilePic();
//                sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.USER_PROFILE_PIC,
//                        profileImageUrl);
//                BindingUtils.setImageUrlCircular(mFragmentBlogBinding.authorID2, profileImageUrl);
//            }
//        }

    }

    private void openSupport() {
        if (mBlogViewModel.getProfileData().getValue() != null && mBlogViewModel.getProfileData().getValue().getUser() != null && mBlogViewModel.getProfileData().getValue().getUser().getUsername() != null) {
            Intent intent = new Intent(getActivity(), UniversalActivity.class);
            intent.putExtra(AppConstants.SEE_ALL_TYPE, SEE_ALL_TYPE_CHAT);
            intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, mBlogViewModel.getProfileData().getValue().getUser().getUsername());
            intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_PROFILE_SELF);
            startActivity(intent);
        }
    }

    private void openInvoices() {
        if (mBlogViewModel.getProfileData().getValue() != null && mBlogViewModel.getProfileData().getValue().getUser() != null && mBlogViewModel.getProfileData().getValue().getUser().getUsername() != null) {
            Intent intent = new Intent(getActivity(), UniversalActivity.class);
            intent.putExtra(AppConstants.SEE_ALL_TYPE, SEE_ALL_TYPE_INVOICE);
            intent.putExtra(AppConstants.SEE_ALL_TYPE_ID, mBlogViewModel.getProfileData().getValue().getUser().getUsername());
            intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_PROFILE_SELF);
            startActivity(intent);
        }
    }

    private void showEditLayout() {
        ((ProfileFragment) mFragmentBlogBinding.viewPager.getAdapter().instantiateItem(mFragmentBlogBinding.viewPager, mFragmentBlogBinding.viewPager.getCurrentItem())).showEditLayout();
    }

    @Override
    public void setupViewsForNonLoggedinUser() {
        if (mBlogViewModel.getAuthorName().equalsIgnoreCase("me")) {
            mFragmentBlogBinding.follow.setVisibility(View.GONE);
            mFragmentBlogBinding.contentModeratorView.setVisibility(View.VISIBLE);
            mFragmentBlogBinding.moderatorTitle.setVisibility(View.GONE);
            mFragmentBlogBinding.moderatorVoteButton.setVisibility(View.GONE);
            mFragmentBlogBinding.moderatorSuccessMessage.setVisibility(View.GONE);
            mFragmentBlogBinding.moderatorShareText.setVisibility(View.VISIBLE);
            mFragmentBlogBinding.moderatorShareButton.setVisibility(View.VISIBLE);

            // mFragmentBlogBinding.share.setVisibility(View.GONE);
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
        mFragmentBlogBinding.follow.setVisibility(View.VISIBLE);
        //mFragmentBlogBinding.share.setVisibility(View.VISIBLE);
//        mFragmentBlogBinding.blogRecyclerView.setVisibility(View.VISIBLE);
//        mFragmentBlogBinding.editProfileLayout.setVisibility(View.GONE);
//        mFragmentBlogBinding.editLayout.setVisibility(View.GONE);
//        mFragmentBlogBinding.loginButton.setVisibility(View.GONE);
//        mFragmentBlogBinding.verifiedAuthorButtons.setVisibility(View.VISIBLE);
//        mFragmentBlogBinding.placeholderText.setVisibility(View.GONE);
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

        return new User("", "", "");
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
        if ((requestCode == GALLERY) && data != null) {
            Uri uploadedImageUri = data.getData();

            if (type.contentEquals(getString(R.string.edit_profile))) {
                Picasso.get().load(uploadedImageUri).placeholder(context.getResources().getDrawable(R.drawable.avd_avatar)).into(mFragmentBlogBinding.authorID2);
            } else {
                Picasso.get().load(uploadedImageUri).placeholder(context.getResources().getDrawable(R.drawable.profile_cover)).into(mFragmentBlogBinding.coverPic);
            }

            Uri selectedImageUri = data.getData();
            path = CommonUtils.getPathFromUrl(getBaseActivity(), selectedImageUri);

            Bitmap bitmap = CommonUtils.getBitmapFromUrl(path);
            decodeFileAndStartImageUpload(bitmap);
        }
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBlogViewModel.setNavigator(this);
        mBlogAdapter.setListener(this);
        if (getArguments() != null) {
            if (getArguments().getString(AppConstants.AUTHOR_NAME) != null) {
                mBlogViewModel.setAuthorName(getArguments().getString(AppConstants.AUTHOR_NAME));
            }
        }

        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_PROFILE_SELF);

        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_PROFILE_SELF, baseProperties);

        trackProfile();
        if (getContext() != null)
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkConnected() && mBlogViewModel != null && isAdded() && !isRemoving()) {
                checkInternetAvailability();
                mBlogViewModel.fetchProfile(mBlogViewModel.getAuthorName());
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null && mBlogViewModel != null && mFragmentBlogBinding.offlineLayout.getRoot().getVisibility() == View.VISIBLE && NetworkUtils.isNetworkConnected(getContext())) {
            checkInternetAvailability();
            mBlogViewModel.fetchProfile(mBlogViewModel.getAuthorName());
        }
        if (mBlogViewModel != null && !mBlogViewModel.isFirstApiCalled())
            mBlogViewModel.fetchProfile(mBlogViewModel.getAuthorName());
    }

    private void checkInternetAvailability() {
        if (NetworkUtils.isNetworkConnected(RheoTvApp.getNonUiContext())) {
            mFragmentBlogBinding.offlineLayout.setLayoutVisible(false);
        } else {
            mFragmentBlogBinding.offlineLayout.setLayoutVisible(true);
        }
    }

    private void trackProfile() {
        Map<String, Object> map = new HashMap<>(baseProperties);
        map.put("is_self", mBlogViewModel.getAuthorName() != null &&
                (mBlogViewModel.getAuthorName().equalsIgnoreCase(CommonUtils.getUserName(getContext()))
                        || mBlogViewModel.getAuthorName().equalsIgnoreCase("me")));
        map.put("is_self_first", CommonUtils.isFirsTimeSelfProfileVisited((boolean) map.get("is_self")));
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_PROFILE_PAGE_VISITED, map);
        CommonUtils.setFirsTimeSelfProfileVisited();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mBlogViewModel != null)
            mBlogViewModel.fetchProfile(mBlogViewModel.getAuthorName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusModel.RefreshProfile eventBusModel) {
        if (mBlogViewModel == null || !isAdded() || isDetached() || isRemoving()) return;
        mBlogViewModel.fetchProfile(mBlogViewModel.getAuthorName());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentBlogBinding = getViewDataBinding();
        setUp();
        subscribeToNewData();

        // if (getActivity() instanceof TabContainerActivity) {
        //     ((TabContainerActivity) getActivity()).hideSearchAndAddMargin();
            /*int statusBarHeight = (int) Math.ceil(25 * context.getResources().getDisplayMetrics().density);
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 144, getResources().getDisplayMetrics());
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height + statusBarHeight);
            mFragmentBlogBinding.coverPic.setLayoutParams(lp);*/
        //}
    }

    private void setUp() {
        mLayoutManager = new LinearLayoutManager(context);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mFragmentBlogBinding.settingImageView.setOnClickListener(v -> showSettings());
        mFragmentBlogBinding.settingImageView.setVisibility((mBlogViewModel.getAuthorName().contentEquals(CommonUtils.getUserName(getContext())) || mBlogViewModel.getAuthorName().contentEquals("me")) && CommonUtils.isUserLoggedin() ? View.VISIBLE : View.GONE);
        mFragmentBlogBinding.errorText.setOnClickListener(v -> mBlogViewModel.fetchProfile(mBlogViewModel.getAuthorName()));
        mFragmentBlogBinding.avatarContainerLayout.setOnClickListener(v -> {
            if (CommonUtils.isUserLoggedin()) {
                if (mBlogViewModel.getProfileData() != null &&
                        mBlogViewModel.getProfileData().getValue() != null &&
                        mBlogViewModel.getProfileData().getValue().getStoryAvailable() != null &&
                        mBlogViewModel.getProfileData().getValue().getStoryAvailable()
                ) {
                    if ("me".equalsIgnoreCase(mBlogViewModel.getAuthorName())) {
                        Intent intent = new Intent(getContext(), CreateStoryActivity.class);
                        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_PROFILE_SELF);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getContext(), StoryActivity.class);
                        intent.putExtra(StoryActivity.ARG_AUTHOR_ID, mBlogViewModel.authorProfileData.get().getId());
                        intent.putExtra(StoryActivity.ARG_AUTHOR_INDEX, 0);
                        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_PROFILE_SELF);
                        startActivity(intent);
                    }
                } else if ("me".equalsIgnoreCase(mBlogViewModel.getAuthorName())) {
                    Intent intent = new Intent(getContext(), CreateStoryActivity.class);
                    intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_PROFILE_SELF);
                    startActivity(intent);
                }
            } else {
                handleLogin();
            }
        });

        mFragmentBlogBinding.followerCountLayout.setOnClickListener(v -> {
            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_COUNT_CLICKED, baseProperties);

            if (mBlogViewModel.authorProfileData.get() != null) {
                Intent intent = new Intent(getActivity(), FollowActivity.class);
                intent.putExtra(AppConstants.ARG_IS_FOLLOW_SCREEN, true);
                intent.putExtra(AppConstants.ARG_USERNAME, mBlogViewModel.authorProfileData.get().getUser().getUsername());
                intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_PROFILE_SELF);
                startActivity(intent);
            }
        });

        mFragmentBlogBinding.offlineLayout.retryButton.setOnClickListener(v -> mBlogViewModel.fetchProfile(mBlogViewModel.getAuthorName()));

        //        mFragmentBlogBinding.blogRecyclerView.setLayoutManager(mLayoutManager);
//        mFragmentBlogBinding.blogRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mFragmentBlogBinding.blogRecyclerView.setAdapter(mBlogAdapter);

        // setUpTabs();
        //setUpFloatinActionButton();
        mBlogViewModel.isSetUpForSelf.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (mBlogViewModel.authorProfileData.get() != null && mBlogViewModel.authorProfileData.get().isEnableContentModerator()) {
                    if (mBlogViewModel.isSetUpForSelf.get()) {
                        mFragmentBlogBinding.divider.setVisibility(View.VISIBLE);
                        mFragmentBlogBinding.moderatorDivider.setVisibility(View.GONE);
                        setModeratorForSelf();
                    } else {
                        mFragmentBlogBinding.divider.setVisibility(View.GONE);
                        mFragmentBlogBinding.moderatorDivider.setVisibility(View.VISIBLE);
                    }
                } else {
                    mFragmentBlogBinding.contentModeratorImage.setVisibility(View.GONE);
                    mFragmentBlogBinding.contentModeratorView.setVisibility(View.GONE);
                }
            }
        });

        mFragmentBlogBinding.moderatorShareButton.setOnClickListener(v -> {
            if (mBlogViewModel.authorProfileData.get() != null) {
                HashMap<String, Object> properties = new HashMap<>();
                properties.put("username", mBlogViewModel.authorProfileData.get().getUser().getUsername());
                SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_SELF_PROFILE_SHARE_CLICK, properties);

                HashMap<String, String> map = new HashMap<>();
                map.put(AppConstants.BRANCH_PROFILE_URL_SHARE, mBlogViewModel.authorProfileData.get().getShareUrl());
                map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_PROFILE);

                FirebaseDynamicLinkUtils.share(context, mBlogViewModel.authorProfileData.get().getCampaignInfo(),
                        "self_profile_share", "Vote for me to become moderator on Rheo",
                        "Hi I want to be a moderator on Rheo. Please click on the link to vote for me on Rheo.",
                        mBlogViewModel.authorProfileData.get().getProfilePic(), map,
                        mBlogViewModel.authorProfileData.get().getShareUrl(), true);

                AnalyticsHelper.getInstance(RheoTvApp.getNonUiContext()).sendClick("journalist_share");
                //String text = "Hey Mate!!!! I have started streaming live on Rheo TV. Check this out and do not forget to follow me.\n" + authorProfileData.get().getShareUrl();
                //new ShareTaskHelper().share(view.getContext(), text, ShareTaskHelper.ShareTarget.Others);
            }
        });
        mFragmentBlogBinding.moderatorVoteButton.setOnClickListener(v -> mBlogViewModel.requestForContentModerator());

        mFragmentBlogBinding.recentViewersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SegmentTracker.getInstance(getContext()).trackEvent(EVENT_RECENT_VIEWERS_TAB_CLICKED, baseProperties);
                Intent intent = new Intent(getActivity(), FollowActivity.class);
                intent.putExtra(AppConstants.ARG_IS_FOLLOW_SCREEN, false);
                intent.putExtra(AppConstants.ARG_USERNAME, mBlogViewModel.authorProfileData.get().getUser().getUsername());
                intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_PROFILE_SELF);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onContentModeratorVoted() {
        mFragmentBlogBinding.moderatorSuccessMessage.setVisibility(View.VISIBLE);
        mFragmentBlogBinding.moderatorVoteButton.setVisibility(View.GONE);
        mFragmentBlogBinding.moderatorTitle.setVisibility(View.GONE);
        mFragmentBlogBinding.moderatorShareText.setVisibility(View.GONE);
        mFragmentBlogBinding.contentModeratorView.setVisibility(View.VISIBLE);
        mFragmentBlogBinding.moderatorShareButton.setVisibility(View.GONE);
    }

    private void setModeratorForSelf() {
        mFragmentBlogBinding.moderatorTitle.setVisibility(View.GONE);
        mFragmentBlogBinding.moderatorDivider.setVisibility(View.GONE);
        mFragmentBlogBinding.moderatorVoteButton.setVisibility(View.GONE);
        mFragmentBlogBinding.moderatorSuccessMessage.setVisibility(View.GONE);
        mFragmentBlogBinding.moderatorShareText.setVisibility(View.VISIBLE);
        mFragmentBlogBinding.contentModeratorView.setVisibility(View.VISIBLE);
        mFragmentBlogBinding.moderatorShareButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void setUpRheoProgressView() {
    /*    if (mBlogViewModel.authorProfileData != null && mBlogViewModel.authorProfileData.get() != null && mBlogViewModel.authorProfileData.get().getShouldShowPorgress() != null && mBlogViewModel.authorProfileData.get().getShouldShowPorgress()) {
            mFragmentBlogBinding.rheoProgress.setVisibility(View.VISIBLE);
        } else {
            mFragmentBlogBinding.rheoProgress.setVisibility(View.GONE);
        }*/

    }

    @Override
    public void setUpFloatinActionButton() {

    }

    @Override
    public void setUpTabs() {
        boolean isSelfProfile = true;
        String source = getArguments().getString(AppConstants.SCREEN_SOURCE);
        int selectedTab = mBlogViewModel.selectedTab;

        ProfileResult result = mBlogViewModel.authorProfileData.get();
        if (isAdded()) {
            ProfileTabAdapter adapter = new ProfileTabAdapter(getChildFragmentManager());

            String tabTitle = "Partner Profile";
            if (!CommonUtils.isUserLoggedin() && mBlogViewModel.getAuthorName().contentEquals("me")) {
                tabTitle = "Profile";
                adapter.addFragment(ProfileFragment.newInstance(result, mBlogViewModel.getAuthorName(), source), tabTitle);
            } else if ((mBlogViewModel.getAuthorName().contentEquals(CommonUtils.getUserName(getContext())) || mBlogViewModel.getAuthorName().contentEquals("me")) && CommonUtils.isUserLoggedin()) {
                tabTitle = "Profile";
                adapter.addFragment(ProfileFragment.newInstance(result, mBlogViewModel.getAuthorName(), source), tabTitle);
                if (mBlogViewModel.authorProfileData.get() != null && mBlogViewModel.authorProfileData.get().getPaymentModel() == 1 &&
                        ((mBlogViewModel.authorProfileData.get().getShouldShowPorgress() != null && mBlogViewModel.authorProfileData.get().getShouldShowPorgress())
                                || (mBlogViewModel.authorProfileData.get().getShouldShowWallet() != null && mBlogViewModel.authorProfileData.get().getShouldShowWallet())))
                    adapter.addFragment(WalletFragment.newInstance(result, mBlogViewModel.getAuthorName()), "Wallet");
                else if (mBlogViewModel.authorProfileData.get() != null && mBlogViewModel.authorProfileData.get().getPaymentModel() == 2)
                    adapter.addFragment(WalletFragmentV2.Companion.newInstance(result, mBlogViewModel.getAuthorName()), "Wallet");
                if (mBlogViewModel.authorProfileData.get() != null && mBlogViewModel.authorProfileData.get().getUser() != null) {
                    adapter.addFragment(VideosFragment.newInstance(mBlogViewModel.authorProfileData.get().getUser().getId(), SegmentConstants.SCREEN_NAME_PROFILE_SELF), "Videos");
                }
                adapter.addFragment(AnalyticsFragment.newInstance(mBlogViewModel.getAuthorName()), "Analytics");
            } else {
                isSelfProfile = false;
                if (mBlogViewModel.authorProfileData.get() != null && mBlogViewModel.authorProfileData.get().getUser() != null) {
                    adapter.addFragment(VideosFragment.newInstance(mBlogViewModel.authorProfileData.get().getUser().getId(), SegmentConstants.SCREEN_NAME_PROFILE_SELF), "Videos");
                }
            }

            mFragmentBlogBinding.viewPager.setAdapter(adapter);
            if (isSelfProfile) {
                if (!CommonUtils.isUserLoggedin() && mBlogViewModel.getAuthorName().contentEquals("me")) {
                    mFragmentBlogBinding.tabLayout.setVisibility(View.GONE);
                } else {
                    mFragmentBlogBinding.tabLayout.setVisibility(View.VISIBLE);
                    mFragmentBlogBinding.tabLayout.setupWithViewPager(mFragmentBlogBinding.viewPager);
                    mFragmentBlogBinding.tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
                        @Override
                        public void onTabSelected(TabLayout.Tab tab) {
                            mBlogViewModel.selectedTab = tab != null ? tab.getPosition() : 0;
                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) {

                        }

                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {

                        }
                    });
                }
            } else {
                mFragmentBlogBinding.tabLayout.setVisibility(View.GONE);
            }
        }

        if (mFragmentBlogBinding != null && selectedTab < mFragmentBlogBinding.tabLayout.getTabCount()) {
            mFragmentBlogBinding.tabLayout.selectTab(mFragmentBlogBinding.tabLayout.getTabAt(selectedTab));
        }
    }

    private void subscribeToNewData() {

        mBlogViewModel.getProfileData().observe(getViewLifecycleOwner(), data ->
                {
                    mBlogViewModel.updateProfileData(data);
                    BindingUtils.setImageUrlUsingCache(mFragmentBlogBinding.coverPic, data.getCoverPic(), true);
                    BindingUtils.setProfileImageUrlFromCache(mFragmentBlogBinding.authorID2, data.getProfilePic(), true);
                    if (data.getStoryAvailable() != null && data.getStoryAvailable()) {
                        mFragmentBlogBinding.storyIndicatorView.setVisibility(View.VISIBLE);
                        if (data.getStoryViewed())
                            mFragmentBlogBinding.storyIndicatorView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.grey_circle_border_bg));
                        else
                            mFragmentBlogBinding.storyIndicatorView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.accent_circle_border_bg));
                    } else {
                        mFragmentBlogBinding.storyIndicatorView.setVisibility(View.INVISIBLE);
                    }
                    if (data.isEnableContentModerator()) {
                        if (!mBlogViewModel.isSetUpForSelf.get()) {
                            if (data.getContentModerator()) {
                                mFragmentBlogBinding.contentModeratorView.setVisibility(View.GONE);
                                mFragmentBlogBinding.contentModeratorImage.setVisibility(View.VISIBLE);
                            } else {
                                mFragmentBlogBinding.contentModeratorImage.setVisibility(View.GONE);
                                mFragmentBlogBinding.divider.setVisibility(View.GONE);
                                mFragmentBlogBinding.moderatorDivider.setVisibility(View.VISIBLE);
                                mFragmentBlogBinding.contentModeratorView.setVisibility(View.VISIBLE);
                                mFragmentBlogBinding.moderatorShareText.setVisibility(View.GONE);
                                mFragmentBlogBinding.moderatorShareButton.setVisibility(View.GONE);
                                if (data.isVotedAsModerator()) {
                                    mFragmentBlogBinding.moderatorTitle.setVisibility(View.GONE);
                                    mFragmentBlogBinding.moderatorVoteButton.setVisibility(View.GONE);
                                    mFragmentBlogBinding.moderatorSuccessMessage.setVisibility(View.VISIBLE);
                                } else {
                                    mFragmentBlogBinding.moderatorTitle.setVisibility(View.VISIBLE);
                                    mFragmentBlogBinding.moderatorVoteButton.setVisibility(View.VISIBLE);
                                    mFragmentBlogBinding.moderatorSuccessMessage.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            if (data.getContentModerator()) {
                                mFragmentBlogBinding.contentModeratorImage.setVisibility(View.VISIBLE);
                            } else {
                                if (data.isModeratorVotingEnabled()) {
                                    setModeratorForSelf();
                                } else {
                                    mFragmentBlogBinding.contentModeratorView.setVisibility(View.GONE);
                                }
                                mFragmentBlogBinding.contentModeratorImage.setVisibility(View.GONE);
                            }

                        }
                    } else {
                        mFragmentBlogBinding.contentModeratorImage.setVisibility(View.GONE);
                        mFragmentBlogBinding.contentModeratorView.setVisibility(View.GONE);
                    }
                    if ((mBlogViewModel.getAuthorName().contentEquals(CommonUtils.getUserName(getContext())) || mBlogViewModel.getAuthorName().contentEquals("me")) && CommonUtils.isUserLoggedin()) {
                        if (data.getFollowersCount() < 100) {
                            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_RECENT_VIEWERS_UNLOCK_VIEW_SHOWN, baseProperties);
                            mFragmentBlogBinding.recentViewersLayout.setVisibility(View.GONE);
                            mFragmentBlogBinding.whoViewedHeader.setVisibility(View.VISIBLE);
                            mFragmentBlogBinding.whoViewedLayout.setVisibility(View.VISIBLE);
                            BindingUtils.setSpannableText(mFragmentBlogBinding.shareProfileForFollowers, getString(R.string.reach_100_followers), 52, 65);
                            mFragmentBlogBinding.whoViewedLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_RECENT_VIEWERS_SHARE_CLICKED, baseProperties);
                                    mBlogViewModel.onShareSelfClicked(view);
                                }
                            });
                        } else {
                            SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_RECENT_VIEWERS_VIEW_TAB_SHOWN, baseProperties);
                            mFragmentBlogBinding.recentViewersLayout.setVisibility(View.VISIBLE);
                            mFragmentBlogBinding.whoViewedHeader.setVisibility(View.GONE);
                            mFragmentBlogBinding.whoViewedLayout.setVisibility(View.GONE);
                        }
                    } else {

                        mFragmentBlogBinding.recentViewersLayout.setVisibility(View.GONE);
                        mFragmentBlogBinding.whoViewedHeader.setVisibility(View.GONE);
                        mFragmentBlogBinding.whoViewedLayout.setVisibility(View.GONE);
                    }
                }
        );
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        context = null;
//        if (getActivity() instanceof TabContainerActivity) {
//            ((TabContainerActivity) getActivity()).showSearchAndAddMargin();
//        }
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        if (getContext() != null)
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(networkStateReceiver);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(String id, PostObject post) {
        PlayerHeadServiceHelper.getInstance().stopPlayerHeaderService();
        if (getContext() == null) return;
        StreamPlayerActivity.Companion.startActivity(getContext(),
                new StreamPlayerContainerFragment.Builder()
                        .addPost(post)
                        .addSourceScreenName(SegmentConstants.SCREEN_NAME_PROFILE_SELF)
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
        sharePost(post);
    }

    private void sharePost(PostObject post) {
        if (post == null || post.getAuthor() == null || post.getAuthor().getUser() == null || post.getAuthor().getUser().getUsername() == null)
            return;
        HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.BRANCH_PROFILE_URL_SHARE, post.getShareUrl());
        map.put(AppConstants.BRANCH_SHARE_TYPE, AppConstants.BRANCH_SHARE_TYPE_LIVE_STREAM);
        FirebaseDynamicLinkUtils.share(context, post.getAuthor().getCampaignInfo(), "player_live_share", post.getAuthor().getUser().getUsername() + " is Live on Rheo TV",
                "Watch " + post.getAuthor().getUser().getUsername() + "playing " + post.getGame() + " live on Rheo TV",
                post.getThumbnail(), map, post.getShareUrl(), true, post.isLive(), post.getAuthor().getUser().getUsername());
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
    public void setBio(ProfileResult result) {
        if (!isAdded() || mFragmentBlogBinding == null || result == null) return;
        String bio = result.getBio();
        if (!mBlogViewModel.isSetUpForSelf.get() && bio != null && bio.length() > 0) {

            mFragmentBlogBinding.bioView.setVisibility(View.VISIBLE);
            mFragmentBlogBinding.bioView.setText(bio);
            if (bio != null && bio.trim().length() > 50) {
                makeTextViewResizable(mFragmentBlogBinding.bioView, 2, ".. Read More", true);
            }

        } else {
            mFragmentBlogBinding.bioView.setVisibility(View.GONE);
        }

        mFragmentBlogBinding.levelBadgeImageView.setImageResource(result.getBadge());
        mFragmentBlogBinding.levelBadgeTextView.setText(result.getLevel());
        mFragmentBlogBinding.levelBadgeTextView.setTextColor(ContextCompat.getColor(requireContext(), result.getBadgeColor()));

        if (result.getLevel() == null)
            mBlogViewModel.setIsBadgeVisible(false);

    }

    public static void makeTextViewResizable(final TextView tv, final int maxLine, final String expandText, final boolean viewMore) {

        if (tv.getTag() == null) {
            tv.setTag(tv.getText());
        }
        ViewTreeObserver vto = tv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {

                ViewTreeObserver obs = tv.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);
                if (maxLine == 0) {
                    int lineEndIndex = tv.getLayout().getLineEnd(0);
                    int endIndex = lineEndIndex - expandText.length() + 1;

                    if (endIndex > 0 && tv.getText().toString().length() > endIndex) {
                        String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                        tv.setText(text);
                        tv.setMovementMethod(LinkMovementMethod.getInstance());
                        tv.setText(
                                addClickablePartTextViewResizable(tv.getText().toString(), tv, maxLine, expandText,
                                        viewMore), TextView.BufferType.SPANNABLE);
                    } else {
                        return;
                    }

                } else if (maxLine > 0 && tv.getLineCount() >= maxLine) {
                    int lineEndIndex = tv.getLayout().getLineEnd(maxLine - 1);
                    int endIndex = lineEndIndex - expandText.length() + 1;
                    if (endIndex > 0 && tv.getText().toString().length() > endIndex) {
                        String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                        tv.setText(text);
                        tv.setMovementMethod(LinkMovementMethod.getInstance());
                        tv.setText(
                                addClickablePartTextViewResizable(tv.getText().toString(), tv, maxLine, expandText,
                                        viewMore), TextView.BufferType.SPANNABLE);
                    } else {
                        return;
                    }
                } else {
                    int lineEndIndex = tv.getLayout().getLineEnd(tv.getLayout().getLineCount() - 1);
                    String text = tv.getText().subSequence(0, lineEndIndex) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(tv.getText().toString(), tv, lineEndIndex, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                }
            }
        });

    }

    private static SpannableStringBuilder addClickablePartTextViewResizable(final String strSpanned, final TextView tv,
                                                                            final int maxLine, final String spanableText, final boolean viewMore) {
        String str = strSpanned;
        SpannableStringBuilder ssb = new SpannableStringBuilder(strSpanned);

        if (str.contains(spanableText)) {


            ssb.setSpan(new MySpannable(false) {
                @Override
                public void onClick(View widget) {
                    if (viewMore) {
                        tv.setLayoutParams(tv.getLayoutParams());
                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                        tv.invalidate();
                        makeTextViewResizable(tv, -1, "Read Less", false);
                    } else {
                        tv.setLayoutParams(tv.getLayoutParams());
                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                        tv.invalidate();
                        makeTextViewResizable(tv, 2, ".. Read More", true);
                    }
                }
            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length(), 0);

        }
        return ssb;

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
            Intent intent = EditProfileActivity.getCallingIntent(context, SegmentConstants.SCREEN_NAME_PROFILE_SELF);
            intent.putExtras(bundle);
            getActivity().startActivityForResult(intent, AppConstants.REQUEST_CODE_EDIT_PROFILE);
        } else {
            Log.i(ProfileContainerFragment.class.getCanonicalName(), "Null Author object while edit action");
        }
    }

    @Override
    public void startUploadActivity() {
        if (mBlogViewModel.authorProfileData.get() != null && mBlogViewModel.authorProfileData.get().getUser() != null) {
            uploadFragment = VideoUploadFragment.newInstance(SegmentConstants.SCREEN_NAME_PROFILE_SELF);
            uploadFragment.show(getChildFragmentManager(), null);
        } else {
            Log.i(ProfileContainerFragment.class.getCanonicalName(), "Null Author object while edit action");
        }
    }

    @Override
    public void updateProfileViewModelData() {
        if (getActivity() instanceof ProfileActivity) {
            ((ProfileActivity) getActivity()).getProfileViewModel().setAuthorProfileData(mBlogViewModel.authorProfileData.get());
            if ((mBlogViewModel.authorProfileData != null && mBlogViewModel.authorProfileData.get() != null
                    && mBlogViewModel.authorProfileData.get().getPrimeStreamer() != null && mBlogViewModel.authorProfileData.get().getPrimeStreamer().booleanValue()))
                ((ProfileActivity) getActivity()).setPrimeShowHostTagVisibility();
        }
    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public String getAuthorUsername() {
        return null;
    }

    @Override
    public void onGameClicked(String game, String gameId) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("game", game);
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
        if (!mBlogViewModel.isSetUpForSelf.get()) return;
        if (mBlogViewModel.authorProfileData.get() != null && mBlogViewModel.authorProfileData.get().getUser() != null) {
            RankActivity.startMe(this, getActivity(),
                    mBlogViewModel.authorProfileData.get().getPaymentModel(),
                    mBlogViewModel.authorProfileData.get().getUser().getId().intValue(),
                    mBlogViewModel.getLevel(), SegmentConstants.SCREEN_NAME_PROFILE_SELF);
        }
    }

    private void showSettings() {
        new BottomSheetMenuDialog.Builder()
                .add(R.menu.menu_profile_setting)
                .header("Profile Settings")
                .setListener(this::onSettingItemClicked)
                .show(getChildFragmentManager(), "BottomSheetMenuDialog");
    }

    private void onSettingItemClicked(String tag, Option option) {
        switch (option.getId()) {
            case R.id.action_contact_us:
//                openSupport();
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_CONTACT_US_CLICKED, baseProperties);
                FreshchatUser freshchatUser = Freshchat.getInstance(context).getUser();
                freshchatUser.setFirstName(CommonUtils.getUserName(context));
                freshchatUser.setLastName("Mobile");
                try {
                    Freshchat.getInstance(context).setUser(freshchatUser);
                } catch (MethodNotAllowedException e) {
                    e.printStackTrace();
                }

                Freshchat.showConversations(context);
                break;

            case R.id.action_logout:
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_LOGOUT_CLICKED, baseProperties);
                Branch.getInstance().logout();
                FirebaseAuth.getInstance().signOut();
                logoutGoogleClient();
                sharedPrefsUtils.setBooleanPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.IS_LOGGED_IN, false);
                sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.USER_NAME, null);
                sharedPrefsUtils.setIntegerPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.USER_ID, 0);
                sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.AUTH_TOKEN, null);
                //CommonUtils.setBranchExtraInfo(getNonUiContext(), null);
                if (getActivity() instanceof TabContainerActivity)
                    ((TabContainerActivity) getActivity()).handleLogout();
                break;

            case R.id.action_add_moderator:
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_ADD_MODERATE_CLICKED, baseProperties);
                Intent intent = new Intent(getActivity(), AddModeratorsActivity.class);
                intent.putExtra("moderators", mBlogViewModel.authorProfileData.get().getModerators());
                intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_PROFILE_SELF);
                getActivity().startActivityForResult(intent, REQUEST_CODE_ADD_MODERATORS);
                break;
        }
    }

    public void updateModerators(String moderators) {
        mBlogViewModel.authorProfileData.get().setModerators(moderators);
    }

    private void logoutGoogleClient() {
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
            mGoogleSignInClient.signOut();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
