package com.rheotv.android.ui.activities.moments.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.rheotv.android.ui.activities.moments.view.fragments.MomentsContainerFragment
import com.rheotv.android.ui.activities.moments.view.fragments.StreamPlayerFragmentV3
import com.rheotv.android.ui.activities.player.activity.newPlayer.StreamPlayerFragmentV2
import com.rheotv.android.utils.AppConstants

class MomentsSlidingPagerAdapter(fragmentManager: FragmentManager) :
        FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var bundle: Bundle? = null
    private var mContainerFragment: StreamPlayerFragmentV3? = null

    override fun getCount(): Int = 2

    private var playerFragment: StreamPlayerFragmentV3? = null

    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            MomentsContainerFragment.Companion.Builder().build().apply {
                arguments = bundle
            }
        } else {
            if (playerFragment == null)
                playerFragment = StreamPlayerFragmentV3.getInstance(mContainerFragment?.moments, bundle?.getString(AppConstants.SCREEN_SOURCE))
            playerFragment!!
        }
    }
}