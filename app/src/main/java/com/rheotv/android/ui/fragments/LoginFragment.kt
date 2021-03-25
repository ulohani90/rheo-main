package com.rheotv.android.ui.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.databinding.LayoutLoginFragmentBinding
import com.rheotv.android.helpers.AnalyticsHelper
import com.rheotv.android.ui.activities.onboarding.v2.UserLanguage
import com.rheotv.android.ui.activities.onboarding.v2.viewmodel.OnBoardingViewModel
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.ui.customViews.WebviewActivity
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog.LoginFragmentCallback
import com.rheotv.android.utils.*
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.GrayscaleTransformation
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

class LoginFragment : BaseFragment<LayoutLoginFragmentBinding?, LoginViewModel?>(), LoginNavigator {
    var mBinding: LayoutLoginFragmentBinding? = null
    var loginViewModel: LoginViewModel? = null

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    var mAuth: FirebaseAuth? = null
    var mGoogleSignInClient: GoogleSignInClient? = null
    private var properties = HashMap<String, Any>()

    private var hasLoginClicked = false
    private var mCallback: LoginFragmentCallback? = null

    fun setCallback(callback: LoginFragmentCallback?) {
        mCallback = callback
    }

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.layout_login_fragment

    override fun getViewModel(): LoginViewModel {
        try {
            loginViewModel = ViewModelProvider(this, mViewModelFactory).get(LoginViewModel::class.java)
            loginViewModel?.baseProperties = properties
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (loginViewModel)!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity != null) activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        loginViewModel?.navigator = this
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
        setupAuthEssentials()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding
        setUp()
    }

    override fun onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventBusModel: EventBusModel.LoginSuccess?) {
        if (!isStateSaved) {
            NavHostFragment.findNavController(this).navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            if (CommonUtils.isUserLoggedin()) {
                NavHostFragment.findNavController(this).navigateUp()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUp() {
        properties = HashMap()
        if (arguments != null && arguments?.containsKey(AppConstants.SCREEN_SOURCE) == true)
            properties[AppConstants.SCREEN_SOURCE] = arguments?.getString(AppConstants.SCREEN_SOURCE) ?: ""
        properties[AppConstants.SCREEN_NAME] = SegmentConstants.SCREEN_NAME_LOGIN

        setGreyDrawable(viewDataBinding?.backgroundImageView, R.drawable.login_bg)
        setGreyDrawable(viewDataBinding?.topImageView, R.drawable.ic_header_bg)

        viewDataBinding?.loginWithGoogleBtn?.setOnClickListener {
            hasLoginClicked = true
            signIntoGoogle()
            SegmentTracker.getInstance(activity).trackEvent(SegmentConstants.EVENT_LOGIN_CLICK, properties)
        }

        viewDataBinding?.termsAndConditionTextView?.setOnClickListener {
            launchWebView(AppConstants.POLICY_LINK)
        }

        viewDataBinding?.usernameEditText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                checkUsernameAndVerify()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        viewDataBinding?.termsAndConditionTextView?.text = getSpannableText(getString(R.string.read_terms_text))
        viewDataBinding?.termsAndConditionTextView?.setOnClickListener { launchWebView(AppConstants.POLICY_LINK) }
    }

    private fun setGreyDrawable(imageView: ImageView?, drawable: Int) {
        imageView ?: return
        Picasso.get().load(drawable)
                .config(Bitmap.Config.RGB_565)
                .transform(GrayscaleTransformation())
                .into(imageView)
    }

    private fun setupAuthEssentials() {
        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(baseActivity, gso)
    }

    private fun signIntoGoogle() {
        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, LoginFragmentBottomDialog.RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if ((resultCode == Activity.RESULT_OK) && (requestCode == LoginFragmentBottomDialog.RC_SIGN_IN) && (data != null)) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                e.printStackTrace()
                handleFailure("Google account not setup.")
                Log.i(javaClass.name, "onActivityResult_catch: " + e.message)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        Log.i(javaClass.name, "firebaseAuthWithGoogle: ")
        val credential = GoogleAuthProvider.getCredential(acct!!.idToken, null)
        mAuth?.signInWithCredential(credential)
                ?.addOnCompleteListener(baseActivity, object : OnCompleteListener<AuthResult?> {
                    override fun onComplete(task: Task<AuthResult?>) {
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = mAuth?.currentUser
                            val email = user?.email
                            val name = user?.displayName
                            val phone = user?.phoneNumber
                            val photoUrl = user?.photoUrl
                            val uid = user?.uid
                            Log.i(javaClass.name, "firebaseAuthWithGoogle_completed: $email uid $uid")
                            if (email == null || email.isEmpty()) {
                                handleFailure("Email id is not available.")
                                return
                            }

                            //TODO: write data in sharedpreferences
                            loginViewModel?.sendAuthenticatedUserToServer(name, phone, email, photoUrl, uid)
                            //RheoTvApp.postFCMToken();
                        } else {
                            handleFailure("Authentication Failed from google, please try again.")
                            Log.i(javaClass.name, "firebaseAuthWithGoogle_failed")
                        }

                        // ...
                    }
                })
    }

    override fun handleLoginSuccess() {
        EventBus.getDefault().post(EventBusModel.LoginSuccess)
        EventBus.getDefault().post(EventBusModel.UpdateCoin)
        if (mCallback != null) {
            mCallback?.onLoginSuccess()
            if (!hasLoginClicked) mCallback!!.onLoginDialogClose()
        } else {
            try {
                val navController: NavController = NavHostFragment.findNavController(this)
                navController.navigateUp()
            } catch (e: Exception) {
                if (parentFragmentManager != null) {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun getSpannableText(res: String): SpannableString {
        val builder = SpannableString(res)
        builder.setSpan(UnderlineSpan(), 16, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                launchWebView(AppConstants.POLICY_LINK)
            }
        }, 16, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return builder
    }

    private fun launchWebView(url: String) {
        val intent = Intent(activity, WebviewActivity::class.java)
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_LOGIN)
        intent.putExtra("URL", url)
        startActivity(intent)
    }

    override fun askUsername(message: String?, name: String?, photoUrl: String?) {
        if (message == "new_user") {
            viewDataBinding?.usernameGroup?.visibility = View.VISIBLE
            viewDataBinding?.loginWithGoogleBtn?.visibility = View.GONE
            viewDataBinding?.messageTextView?.text = UserLanguage.English.pick_username_subtitle
        } else {
            viewDataBinding?.usernameEditText?.setText(viewModel.originalUserName)
            viewDataBinding?.usernameErrorTextView?.setText(R.string.username_is_taken_text)
            viewDataBinding?.usernameErrorTextView?.visibility = if (!message?.trim { it <= ' ' }.isNullOrEmpty()) {
                viewDataBinding?.usernameStateImageView?.setImageResource(R.drawable.ic_red_close)
                viewDataBinding?.usernameStateImageView?.alpha = 1f
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    override fun handleBackendLoginResponse(isSuccessful: Boolean) {
        if (isSuccessful) {
            viewDataBinding?.usernameStateImageView?.setImageResource(R.drawable.avd_correct)
            viewDataBinding?.usernameStateImageView?.alpha = 1f
            viewModel.loanReward()
            if (activity != null) {
                CommonUtils.resetTrainingTooltip(activity)
            }
            val map: MutableMap<String, Any?> = HashMap<String, Any?>(properties)
            map["is_re_login"] = CommonUtils.isReLogin()
            map["is_new_user"] = CommonUtils.isNewAppUser()
            map["device_id"] = CommonUtils.getDevId(RheoTvApp.getNonUiContext())
            SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_LOGIN_COMPLETED, map)
            CommonUtils.setReLogin()
        } else {
            handleFailure("")
        }
    }

    override fun handleFailure(failureMessage: String?) {
        var message: String? = failureMessage
        if (!isAdded) return
        Log.i(javaClass.name, "handleFailure_failureMessage:")
        AnalyticsHelper.getInstance(baseActivity).sendSignInEvent(false, failureMessage)
        if (message.isNullOrEmpty()) {
            message = "Something went wrong with google signin. Please try again"
        }
        viewDataBinding?.usernameErrorTextView?.visibility = View.VISIBLE
//        Toast.makeText(activity, failureMessage, Toast.LENGTH_SHORT).show()
    }

    private fun checkUsernameAndVerify() {
        if (!viewDataBinding?.usernameEditText?.text.isNullOrEmpty()) {
            verifyUsername(viewDataBinding?.usernameEditText?.text?.toString()?.trim() ?: "")
        } else {
            viewDataBinding?.usernameErrorTextView?.setText(R.string.empty_username_error)
            viewDataBinding?.usernameErrorTextView?.visibility = View.VISIBLE
        }
    }

    private fun verifyUsername(username: String) {
        if (username == viewModel.originalUserName) {
            SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).setIdentityUsername(username)
            viewModel.sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.USER_NAME, username)
            handleBackendLoginResponse(true)
            return
        }
        val ps = Pattern.compile("^[a-zA-Z0-9_-]*$")
        val ms = ps.matcher(username)
        val bs = ms.matches()
        if (!bs) {
            viewDataBinding?.usernameErrorTextView?.setText(R.string.invalid_username_error)
            viewDataBinding?.usernameErrorTextView?.visibility = View.VISIBLE
        } else {
            viewModel.checkUsernameAndSignup(username)
        }
    }

    companion object {
        val TAG = "LoginFragment"
        @JvmStatic
        fun newInstance(bundle: Bundle?): LoginFragment {
            val fragment = LoginFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}