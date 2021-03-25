package com.rheotv.android.ui.activities.profile.viewprofile.view

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentUserAnalyticsBinding
import com.rheotv.android.ui.activities.follower.FollowActivity
import com.rheotv.android.ui.activities.profile.viewprofile.viewmodel.UserAnalyticsViewModel
import com.rheotv.android.ui.activities.profile.viewprofile.adapter.UserAnalyticsAdapter
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import javax.inject.Inject

class UserAnalyticsFragment : BaseFragment<FragmentUserAnalyticsBinding, UserAnalyticsViewModel>() {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mAdapter: UserAnalyticsAdapter

    override fun getLayoutId() = R.layout.fragment_user_analytics

    override fun getViewModel() = ViewModelProvider(this, mViewModelFactory).get(UserAnalyticsViewModel::class.java)

    override fun getBindingVariable() = com.rheotv.android.BR.viewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.loadStreamerAnalytics()
        viewModel.analyticsList.observe(viewLifecycleOwner, Observer {
            mAdapter.submitList(it)
        })
        viewDataBinding.viewPager.adapter = mAdapter
        viewDataBinding.tabLayout.setupWithViewPager(viewDataBinding.viewPager)
        viewDataBinding.actionButton.setOnClickListener { showRecentFollower() }
    }

    private fun showRecentFollower() {
        val intent = Intent(activity, FollowActivity::class.java)
        intent.putExtra(AppConstants.ARG_IS_FOLLOW_SCREEN, false)
        intent.putExtra(AppConstants.ARG_USERNAME, CommonUtils.getUserName())
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_PROFILE_SELF)
        startActivity(intent)
    }

    companion object {
        fun newInstance() = UserAnalyticsFragment()
    }
}