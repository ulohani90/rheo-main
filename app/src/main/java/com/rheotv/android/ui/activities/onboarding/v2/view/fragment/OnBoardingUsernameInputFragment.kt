package com.rheotv.android.ui.activities.onboarding.v2.view.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.databinding.FragmentOnBoardingUsernameBinding
import com.rheotv.android.helpers.AnalyticsHelper
import com.rheotv.android.ui.activities.onboarding.v2.viewmodel.OnBoardingViewModel
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.ui.fragments.LoginNavigator
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.SharedPrefsUtils
import com.rheotv.android.utils.Status
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.GrayscaleTransformation
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

class OnBoardingUsernameInputFragment : BaseFragment<FragmentOnBoardingUsernameBinding, OnBoardingViewModel>(), LoginNavigator {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_on_boarding_username

    override fun getViewModel(): OnBoardingViewModel? = try {
        ViewModelProvider(parentFragment ?: this,
                mViewModelFactory)[OnBoardingViewModel::class.java]
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
        viewDataBinding.usernameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                checkUsernameAndVerify()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        viewDataBinding.usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun afterTextChanged(s: Editable?) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel?.updateNextButtonState(s)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel?.let {
            it.userActionLiveData.value = OnBoardingViewModel.UserAction.DisableNextButton
            it.userActionLiveData.value = OnBoardingViewModel.UserAction.ShowBackButton
            viewDataBinding?.usernameEditText?.setText(viewModel?.originalUserName)
            it.navigator = this
        }
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_NEW_USERNAME_SELECTION_PAGE_SHOWED, HashMap())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel?.let { vm ->
            with(vm) {
                userActionLiveData.observe(viewLifecycleOwner, {
                    if (it is OnBoardingViewModel.UserAction.UsernameAdded) {
                        checkUsernameAndVerify()
                    }
                })

                preferredLanguage.observe(viewLifecycleOwner, {
                    viewDataBinding.headerTextView.text = it?.pick_username_title
                    viewDataBinding.messageTextView.text = it?.pick_username_subtitle
                })
            }
        }
    }

    override fun handleLoginSuccess() {
        SharedPrefsUtils().setBooleanPreference(context, SharedPrefsUtils.IS_ONBOARDING_DONE, true)
        viewModel?.viewState?.set(Status.SUCCESS)
        viewModel?.userActionLiveData?.value = OnBoardingViewModel.UserAction.LoginSuccess
    }

    override fun askUsername(message: String?, name: String?, photoUrl: String?) {
        viewModel?.viewState?.set(Status.SUCCESS)
        viewDataBinding?.usernameEditText?.setText(viewModel?.originalUserName ?: "")
        viewDataBinding?.usernameErrorTextView?.setText(R.string.username_is_taken_text)
        viewDataBinding?.usernameErrorTextView?.visibility = if (!message?.trim { it <= ' ' }.isNullOrEmpty()) {
            viewDataBinding?.usernameStateImageView?.setImageResource(R.drawable.ic_red_close)
            viewDataBinding?.usernameStateImageView?.alpha = 1f
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun handleBackendLoginResponse(isSuccessful: Boolean) {
        if (!isAdded) return
        viewModel?.viewState?.set(Status.SUCCESS)
        if (isSuccessful) {
            viewDataBinding?.usernameStateImageView?.setImageResource(R.drawable.avd_correct)
            viewDataBinding?.usernameStateImageView?.alpha = 1f
            viewModel?.loanReward()
            if (activity != null) {
                CommonUtils.resetTrainingTooltip(activity)
            }
            val map: MutableMap<String, Any?> = HashMap<String, Any?>(/*properties*/)
            map["is_re_login"] = CommonUtils.isReLogin()
            map["is_new_user"] = CommonUtils.isNewAppUser()
            map["device_id"] = CommonUtils.getDevId(context)
            SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).trackEvent(SegmentConstants.EVENT_LOGIN_COMPLETED, map)
            CommonUtils.setReLogin()
        } else {
            handleFailure("")
        }
    }

    override fun handleFailure(failureMessage: String?) {
        var message: String? = failureMessage
        if (!isAdded) return
        viewModel?.viewState?.set(Status.ERROR)
        Log.i(javaClass.name, "handleFailure_failureMessage:")
        AnalyticsHelper.getInstance(baseActivity).sendSignInEvent(false, failureMessage)
        if (message.isNullOrEmpty()) {
            message = "Something went wrong with google signin. Please try again"
        }
        viewDataBinding.usernameErrorTextView.visibility = View.VISIBLE
//        Toast.makeText(activity, failureMessage, Toast.LENGTH_SHORT).show()
    }

    private fun checkUsernameAndVerify() {
        if (!viewDataBinding?.usernameEditText?.text.isNullOrEmpty()) {
            verifyUsername(viewDataBinding?.usernameEditText?.text?.toString()?.trim() ?: "")
        } else {
            viewDataBinding.usernameErrorTextView.setText(R.string.empty_username_error)
            viewDataBinding.usernameErrorTextView.visibility = View.VISIBLE
        }
    }

    private fun verifyUsername(username: String) {
        if (username == viewModel?.originalUserName) {
            SegmentTracker.getInstance(RheoTvApp.getNonUiContext()).setIdentityUsername(username)
            viewModel?.sharedPrefsUtils?.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.USER_NAME, username)
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
            viewModel?.viewState?.set(Status.LOADING)
            viewModel?.checkUsernameAndSignup(username)
        }
    }

}