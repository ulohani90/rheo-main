package com.rheotv.android.ui.activities.share

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rheotv.android.utils.AdapterFragmentItem

abstract class CustomFragmentPagerAdapter(fragmentManager: FragmentManager, lifeCycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifeCycle) {
    abstract fun updateList(list: List<AdapterFragmentItem>)
    abstract fun getItemAtPosition(position: Int): AdapterFragmentItem
}