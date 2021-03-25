package com.rheotv.android.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.databinding.LoginBottomSheetLayoutBinding;
import com.rheotv.android.helpers.AnalyticsHelper;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.base.BaseBottomSheetDialogFragment;
import com.rheotv.android.ui.customViews.WebviewActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class LoginFragmentBottomDialog extends BaseBottomSheetDialogFragment<LoginBottomSheetLayoutBinding, LoginViewModel> implements LoginNavigator {

    public static final String TAG = "LoginFragmentBottomDialog";
    EditText nameEditText;

    int xCutSize = 20;
    int yCutSize = 5;

    FirebaseAuth mAuth;

    public static int RC_SIGN_IN = 7;
    GoogleSignInClient mGoogleSignInClient;

    View loadingSection;
    View userNameContainer;
    View googleSignInRL;
    TextView loginText;
    private TextView newRewardText;
    private View cancelButton;
    public String rewardText = null;
    private TextView chatLiveTextView;

    LoginViewModel loginViewModel;

    LoginBottomSheetLayoutBinding mBinding;
    private boolean hasLoginClicked = false;

    int windowWidth;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    LoginFragmentCallback mCallback;
    private HashMap<String, Object> properties = new HashMap<>();

    @Override
    public void onStart() {

        super.onStart();
        /*if (behavior != null) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }*/
        if (getDialog() == null) {
            return;
        }
        //adjustSize();
        setupAuthEssentials();
    }

    private void adjustSize() {
       /* Window window = getDialog().getWindow();
        int currentWidth = ScreenUtils.getScreenWidth(activity);
        int currentHeight = ScreenUtils.getScreenHeight(activity);

        int dimension = 0;
        if (currentHeight > currentWidth) {
            dimension = currentWidth - ((currentWidth * xCutSize) / 100);
        } else {
            dimension = currentHeight - ((currentHeight * yCutSize) / 100);
        }
        assert window != null;
        window.setLayout(dimension, dimension);
        window.setGravity(Gravity.CENTER);*/
    }

    public static LoginFragmentBottomDialog getInstance(String source) {
        LoginFragmentBottomDialog fragmentBottomDialog = new LoginFragmentBottomDialog();
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        fragmentBottomDialog.setArguments(bundle);
        return fragmentBottomDialog;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.login_bottom_sheet_layout;
    }

    @Override
    public LoginViewModel getViewModel() {
        try {
            loginViewModel = ViewModelProviders.of(this, mViewModelFactory).get(LoginViewModel.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return loginViewModel;
    }

    public void setmCallback(LoginFragmentCallback mCallback) {
        this.mCallback = mCallback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.MyBottomSheetDialogTheme);
        loginViewModel.setNavigator(this);
        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE)) {
            properties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        }
        properties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_LOGIN);
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_LOGIN, properties);
    }


    private void initViews() {
        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        windowWidth = metrics.widthPixels;

        mBinding.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mBinding.loginWithGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hasLoginClicked = true;
                mBinding.loginProgress.setVisibility(View.VISIBLE);
                SegmentTracker.getInstance(getActivity()).trackEvent(SegmentConstants.EVENT_LOGIN_CLICK, properties);
                signIntoGoogle();
            }
        });
        mBinding.username.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    CommonUtils.hideKeyboard(getActivity());
                    checkUsernameAndVerify();
                }
                return false;
            }
        });

        mBinding.verifyUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUsernameAndVerify();
            }
        });

        mBinding.termsAndConditions.setText(getSpannableText(getString(R.string.read_terms_text)));
        mBinding.termsAndConditions.setOnClickListener(v -> launchWebView(AppConstants.POLICY_LINK));
    }

    public void checkUsernameAndVerify() {
        if (mBinding.username != null && mBinding.username.getText() != null && mBinding.username.getText().toString().trim().length() > 0) {
            verifyUsername();
        } else {
            mBinding.username.setError("Please enter a username");
        }
    }

    private void verifyUsername() {
        String username = mBinding.username.getText().toString().trim();
        Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");
        Matcher ms = ps.matcher(username);
        boolean bs = ms.matches();
        if (!bs) {
            mBinding.username.setError("Only alphabets and numbers are allowed");
        } else {
            mBinding.loginProgress.setVisibility(View.VISIBLE);
            loginViewModel.checkUsernameAndSignup(username);
        }
    }

    public void slideViewLeftOutRightIn(View slidingLeftOutView, View slidingRightOutView, int width) {
        slidingRightOutView.setVisibility(View.VISIBLE);
        ObjectAnimator animatorLeftOut = ObjectAnimator.ofFloat(slidingLeftOutView, View.TRANSLATION_X, 0, (width * -1));
        ObjectAnimator animatorRightIn = ObjectAnimator.ofFloat(slidingRightOutView, View.TRANSLATION_X, width, 0);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(300);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                slidingLeftOutView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        set.playTogether(animatorLeftOut, animatorRightIn);
        set.start();

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //((View) rootView.getParent()).setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
                FrameLayout bottomSheet = (FrameLayout)
                        dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                // behavior.setPeekHeight(0); // Remove this line to hide a dark background if you manually hide the dialog.
            }
        });

        initViews();
    }

    public void dismissDialog() {
        try {
            dismiss();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

    public void setupAuthEssentials() {
        try {
            mAuth = FirebaseAuth.getInstance();
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void signIntoGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (resultCode == Activity.RESULT_OK && requestCode == RC_SIGN_IN && data != null) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase

                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
                handleFailure("Google account not setup.");
                Log.i(getClass().getName(), "onActivityResult_catch: " + e.getMessage());
            }
        }
    }


    @Override
    public void askUsername(String message, String name, String photoUrl) {
        if (isAdded()) {
            mBinding.loginProgress.setVisibility(View.INVISIBLE);
            mBinding.loginWithGoogleBtn.setVisibility(View.GONE);
            mBinding.verifyUsername.setVisibility(View.VISIBLE);
            mBinding.loginText.setText("Hello " + name + "\n\n" + getString(R.string.enter_username));
            mBinding.username.setVisibility(View.VISIBLE);
            BindingUtils.setProfileImageUrlFromCache(mBinding.logo, photoUrl, true);
            if (message != null && message.trim().length() > 0) {
                mBinding.username.setError(message);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.i(getClass().getName(), "firebaseAuthWithGoogle: ");
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            String email = user.getEmail();
                            String name = user.getDisplayName();
                            String phone = user.getPhoneNumber();
                            Uri photoUrl = user.getPhotoUrl();
                            String uid = user.getUid();


                            Log.i(getClass().getName(), "firebaseAuthWithGoogle_completed: " + email + " uid " + uid);

                            if (email == null || email.isEmpty()) {
                                handleFailure("Email id is not available.");
                                return;
                            }
                            //TODO: write data in sharedpreferences

                            loginViewModel.sendAuthenticatedUserToServer(name, phone, email, photoUrl, uid);
                            //RheoTvApp.postFCMToken();
                        } else {
                            handleFailure("Authentication Failed from google, please try again.");
                            Log.i(getClass().getName(), "firebaseAuthWithGoogle_failed");
                        }

                        // ...
                    }
                });
    }

    @Override
    public void handleFailure(String failureMessage) {
        if (isAdded()) {
            mBinding.loginProgress.setVisibility(View.INVISIBLE);
            Log.i(getClass().getName(), "handleFailure_failureMessage:");
            AnalyticsHelper.getInstance(getActivity()).sendSignInEvent(false, failureMessage);
            if (failureMessage.isEmpty()) {
                failureMessage = "Something went wrong with google signin. Please try again";
            }
            Toast.makeText(getActivity(), failureMessage, Toast.LENGTH_SHORT).show();
            dismissDialog();
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    private void setRewardTextVisible(boolean isVisible) {
        if (newRewardText != null) {
            newRewardText.setText(rewardText);
            newRewardText.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            chatLiveTextView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        }
    }

    public String getRewardText() {
        return rewardText;
    }

    public void setRewardText(String rewardText) {
        this.rewardText = rewardText;
        if (this.rewardText == null) {
            setRewardTextVisible(false);
        } else {
            setRewardTextVisible(true);
        }
    }

    @Override
    public void handleLoginSuccess() {
        EventBus.getDefault().post(EventBusModel.LoginSuccess.INSTANCE);
        EventBus.getDefault().post(EventBusModel.UpdateCoin.INSTANCE);
        if (mCallback != null)
            mCallback.onLoginSuccess();
        dismissDialog();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (mCallback != null && !hasLoginClicked)
            mCallback.onLoginDialogClose();
        super.onDismiss(dialog);
    }

    @Override
    public void handleBackendLoginResponse(boolean isSuccessful) {
        if (isSuccessful) {
            mBinding.loginProgress.setVisibility(View.INVISIBLE);
            CommonUtils.resetTrainingTooltip(getActivity());
            getViewModel().loanReward();
            Map<String, Object> map = new HashMap<>(properties);
            map.put("is_re_login", CommonUtils.isReLogin());
            map.put("is_new_user", CommonUtils.isNewAppUser());
            map.put("device_id", CommonUtils.getDevId(RheoTvApp.getNonUiContext()));
            SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_LOGIN_COMPLETED, map);
            CommonUtils.setReLogin();
        } else {
            handleFailure("");
        }
    }


    public interface LoginFragmentCallback {
        void onLoginSuccess();

        void onLoginDialogClose();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private SpannableString getSpannableText(String res) {
        SpannableString builder = new SpannableString(res);
        builder.setSpan(new UnderlineSpan(), 16, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.color_accent)), 16, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    private void launchWebView(String url) {
        Intent intent = new Intent(getActivity(), WebviewActivity.class);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_LOGIN);
        intent.putExtra("URL", url);
        startActivity(intent);
    }
}


