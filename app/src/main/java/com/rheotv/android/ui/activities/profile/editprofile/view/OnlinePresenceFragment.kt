package com.rheotv.android.ui.activities.profile.editprofile.view

import android.os.Bundle
import android.view.View
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentOnlinePresenceBinding
import com.rheotv.android.ui.activities.profile.model.SocialMedia
import com.rheotv.android.ui.activities.profile.viewmodel.UserProfileViewModel
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.OnlinePresenceAdapter
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.OnlinePresenceSpinnerAdapter
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.utils.addBackPressCallback
import com.rheotv.android.utils.navController
import com.rheotv.android.utils.onItemSelected
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 * Use the [OnlinePresenceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OnlinePresenceFragment : BaseFragment<FragmentOnlinePresenceBinding, UserProfileViewModel>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var onlinePresenceSpinnerAdapter: OnlinePresenceSpinnerAdapter

    @Inject
    lateinit var onlinePresenceAdapter: OnlinePresenceAdapter

    override fun getBindingVariable() = BR.viewModel

    override fun getLayoutId() = R.layout.fragment_online_presence

    override fun getViewModel() = ViewModelProvider(parentFragment?.parentFragment
            ?: this, viewModelFactory)
            .get(UserProfileViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addBackPressCallback()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewDataBinding) {
            actionBack.setOnClickListener { navController()?.popBackStack() }
            recyclerView.adapter = onlinePresenceAdapter.also {
                it.setInEdit(true)
                it.onItemClick = { s, a ->
                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_ONLINE_PRESENCE_SOCIAL_MEDIA_REMOVED, viewModel?.analyticsProperties)
                    viewModel?.updateOnlinePresence(s, a)
                }
            }
            socialMediaSpinner.apply {
                adapter = onlinePresenceSpinnerAdapter
                onItemSelected { viewModel?.currentSocialMedia = it as? SocialMedia }
            }
            editButton.setOnClickListener {
                if (onlinePresenceAdapter.itemCount > 0) {
                    this@OnlinePresenceFragment.viewModel.onEditSocialMediaClick()
                }
            }
        }
    }

    private fun checkEditButtonVisibility() {
        viewDataBinding.editButton.visibility = if (onlinePresenceAdapter.itemCount > 0)
            View.VISIBLE else View.GONE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewModel) {
            loadSocialMedia()
            profile.get()?.profileDetail?.socialMediaList?.let {
                onlinePresenceAdapter.submitList(it)
                checkEditButtonVisibility()
            }
            socialMediaList.observe(viewLifecycleOwner, Observer {
                onlinePresenceSpinnerAdapter.submitList(it)
            })
            selectedOnlinePresence.observe(viewLifecycleOwner, Observer {
                onlinePresenceAdapter.onUserAction(it)
                checkEditButtonVisibility()
            })
            inSocialMediaMode.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    onlinePresenceAdapter.toggleAllowEdit()
                }
            })
        }
    }

    override fun onDestroyView() {
        viewModel.clearSocialMediaData()
        super.onDestroyView()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment OnlinePresenceFragment.
         */
        @JvmStatic
        fun newInstance() = OnlinePresenceFragment()
    }
}