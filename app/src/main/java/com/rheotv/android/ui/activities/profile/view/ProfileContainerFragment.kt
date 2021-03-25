package com.rheotv.android.ui.activities.profile.view

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentProfileContainerBinding
import com.rheotv.android.utils.EventBusModel
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.ui.fragments.LoginFragment
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.AppUtilsKt
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class ProfileContainerFragment : BaseFragment<FragmentProfileContainerBinding, UserProfileViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_profile_container

    override fun getViewModel(): UserProfileViewModel =
            ViewModelProvider(this, mViewModelFactory)[UserProfileViewModel::class.java].apply {
                queryParam = arguments?.getString(AppConstants.AUTHOR_NAME)
                arguments?.getString(AppConstants.SCREEN_SOURCE)?.let { analyticsProperties[AppConstants.SCREEN_SOURCE] = it }
                arguments?.getBoolean("is_from_deep_link")?.let {
                    if (it == true) {
                        tabPosition = 2
                    } else {
                        tabPosition = 0
                    }
                }
            }

    override fun onDestroy() {
        super.onDestroy()
        AppUtilsKt.runGC()
    }

    companion object {
        fun newInstance(userName: String?, screenSource: String?, isDeeplinkActivity: Boolean) =
                ProfileContainerFragment().apply {
                    arguments = Bundle().also {
                        userName?.apply { it.putString(AppConstants.AUTHOR_NAME, this) }
                        it.putString(AppConstants.SCREEN_SOURCE, screenSource)
                        it.putBoolean("is_from_deep_link", isDeeplinkActivity)
                    }
                }
    }
}