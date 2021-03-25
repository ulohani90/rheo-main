package com.rheotv.android.ui.activities.profile.editprofile.view

import android.os.Bundle
import android.view.View
import androidx.databinding.Observable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.databinding.DonationEditFragmentBinding
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.Status
import com.rheotv.android.utils.addBackPressCallback
import com.rheotv.android.utils.navController
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.HashMap
import javax.inject.Inject

class DonationEditFragment : BaseFragment<DonationEditFragmentBinding, UserProfileViewModel>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.donation_edit_fragment

    override fun getViewModel(): UserProfileViewModel? = try {
        ViewModelProvider(parentFragment?.parentFragment
                ?: this, viewModelFactory).get(UserProfileViewModel::class.java)
    } catch (e: IllegalStateException) {
        e.printStackTrace()
        null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addBackPressCallback()
        SegmentTracker.getInstance().recordScreenName(SegmentConstants.SCREEN_NAME_EDIT_PROFILE, HashMap(viewModel?.analyticsProperties ?: hashMapOf()).also {
            AppConstants.SCREEN_NAME to SegmentConstants.SCREEN_NAME_EDIT_PROFILE
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewDataBinding) {
            actionBack.setOnClickListener { navController()?.popBackStack() }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel?.donationStatus?.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (viewModel?.donationStatus?.get() == Status.SUCCESS) {
                    viewModel?.viewModelScope?.launch(Dispatchers.IO) {
                        delay(1000)
                        withContext(Dispatchers.Main) { navController()?.popBackStack() }
                    }
                }
            }
        })
    }

    companion object {
        fun newInstance() = DonationEditFragment()
    }
}