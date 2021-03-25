package com.rheotv.android.ui.activities.onboarding.v2.view.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.databinding.FragmentOnBoardingLoginBinding
import com.rheotv.android.helpers.AnalyticsHelper
import com.rheotv.android.ui.activities.onboarding.v2.viewmodel.OnBoardingViewModel
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.ui.customViews.WebviewActivity
import com.rheotv.android.ui.fragments.LoginFragmentBottomDialog
import com.rheotv.android.ui.fragments.LoginNavigator
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.SharedPrefsUtils
import com.rheotv.android.utils.Status
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.GrayscaleTransformation
import java.util.*
import javax.inject.Inject

class OnBoardingLoginFragment : BaseFragment<FragmentOnBoardingLoginBinding, OnBoardingViewModel>(), LoginNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_on_boarding_login
    private var isReLogin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isReLogin = arguments?.getBoolean(AppConstants.ARG_IS_RELOGIN, false) == true
    }

    override fun getViewModel(): OnBoardingViewModel? =
            try {
                ViewModelProvider(parentFragment ?: this,
                        mViewModelFactory)[OnBoardingViewModel::class.java].also {
                    it.navigator = this
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                null
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Picasso.get().load(R.drawable.login_bg)
                .config(Bitmap.Config.RGB_565)
                .transform(GrayscaleTransformation())
                .into(viewDataBinding.backgroundImageView)
        viewDataBinding?.loginWithGoogleBtn?.setOnClickListener {
            signIntoGoogle()
        }
        viewDataBinding?.termsAndConditionTextView?.setOnClickListener {
            launchWebView(AppConstants.POLICY_LINK)
        }
        setupAuthEssentials()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel?.let {
            with(it) {
                /*userActionLiveData.observe(viewLifecycleOwner, {
                when (it) {
                    is OnBoardingViewModel.UserAction.Login -> Unit
                }
            })*/

                preferredLanguage.observe(viewLifecycleOwner, {
                    viewDataBinding.headerTextView.text = it?.people_watching_live
                    viewDataBinding.messageTextView.text = it?.login_to_chat
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel?.let {
            it.userActionLiveData.value = OnBoardingViewModel.UserAction.HideNextButton
            if (!isReLogin) {
                it.userActionLiveData.value = OnBoardingViewModel.UserAction.ShowBackButton
            } else {
                it.userActionLiveData.value = OnBoardingViewModel.UserAction.HideBackButton
            }
            it.navigator = this
        }

        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_NEW_LOGIN_PAGE_SHOWED, HashMap())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (resultCode == Activity.RESULT_OK && requestCode == LoginFragmentBottomDialog.RC_SIGN_IN && data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                viewModel?.viewState?.set(Status.SUCCESS)
                e.printStackTrace()
                handleFailure("Google account not setup.")
                Log.i(javaClass.name, "onActivityResult_catch: " + e.message)
            }
        } else {
            viewModel?.viewState?.set(Status.SUCCESS)
        }
    }

    override fun handleFailure(failureMessage: String?) {
        viewModel?.viewState?.set(Status.SUCCESS)
        var message: String? = failureMessage
        if (!isAdded) return
//        mBinding.loginProgress.setVisibility(View.INVISIBLE)
        Log.i(javaClass.name, "handleFailure_failureMessage:")
        AnalyticsHelper.getInstance(baseActivity).sendSignInEvent(false, failureMessage)
        if (message.isNullOrEmpty()) {
            message = "Something went wrong with google signin. Please try again"
        }
        Toast.makeText(activity, failureMessage, Toast.LENGTH_SHORT).show()
    }

    override fun handleLoginSuccess() {
        SharedPrefsUtils().setBooleanPreference(context, SharedPrefsUtils.IS_ONBOARDING_DONE, true)
        viewModel?.viewState?.set(Status.SUCCESS)
        viewModel?.userActionLiveData?.value = OnBoardingViewModel.UserAction.LoginSuccess
    }

    override fun askUsername(message: String?, name: String?, photoUrl: String?) {
        SharedPrefsUtils().setBooleanPreference(context, SharedPrefsUtils.IS_ONBOARDING_DONE, true)
        viewModel?.viewState?.set(Status.SUCCESS)
        viewModel?.userActionLiveData?.value = OnBoardingViewModel.UserAction.AskUsername(message, name, photoUrl)
    }

    override fun handleBackendLoginResponse(isSuccessful: Boolean) {
        viewModel?.viewState?.set(Status.SUCCESS)
        if (isSuccessful) {
//            mBinding.loginProgress.setVisibility(View.INVISIBLE)
            viewModel?.loanReward()
            if (activity != null) {
                CommonUtils.resetTrainingTooltip(activity)
            }
            val map: MutableMap<String, Any> = HashMap<String, Any>(/*properties*/)
            map["is_re_login"] = CommonUtils.isReLogin()
            map["is_new_user"] = CommonUtils.isNewAppUser()
            map["device_id"] = CommonUtils.getDevId(context)
            SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_LOGIN_COMPLETED, map)
            CommonUtils.setReLogin()
        } else {
            handleFailure("")
        }
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
        viewModel?.viewState?.set(Status.LOADING)
        val signInIntent: Intent? = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, LoginFragmentBottomDialog.RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.i(javaClass.name, "firebaseAuthWithGoogle: ")
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth?.let {
            it.signInWithCredential(credential)
                    .addOnCompleteListener(requireActivity(), object : OnCompleteListener<AuthResult?> {
                        override fun onComplete(task: Task<AuthResult?>) {
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                val user: FirebaseUser? = mAuth?.currentUser
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
                                viewModel?.sendAuthenticatedUserToServer(name, phone, email, photoUrl, uid)
                                //RheoTvApp.postFCMToken();
                            } else {
                                viewModel?.viewState?.set(Status.SUCCESS)
                                handleFailure("Authentication Failed from google, please try again.")
                                Log.i(javaClass.name, "firebaseAuthWithGoogle_failed")
                            }
                        }

                    }
                    )
            return
        }
        viewModel?.viewState?.set(Status.SUCCESS)
    }

    private fun launchWebView(url: String) {
        val intent = Intent(activity, WebviewActivity::class.java)
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_LOGIN)
        intent.putExtra("URL", url)
        startActivity(intent)
    }

    companion object {
        fun newInstance(isReLogin: Boolean?) = OnBoardingLoginFragment().also {
            it.arguments = Bundle().apply {
                putBoolean(AppConstants.ARG_IS_RELOGIN, isReLogin == true)
            }
        }
    }
}