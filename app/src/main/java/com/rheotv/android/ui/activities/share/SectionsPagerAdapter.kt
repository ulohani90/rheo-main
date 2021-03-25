package com.rheotv.android.ui.activities.share

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.rheotv.android.utils.AdapterFragmentItem
import java.lang.ref.WeakReference

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val fragment: Fragment)
    : CustomFragmentPagerAdapter(fragment.childFragmentManager, fragment.lifecycle) {

    private val mList: MutableList<AdapterFragmentItem> = mutableListOf()

    override fun updateList(list: List<AdapterFragmentItem>) {
        mList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mList.size

    override fun createFragment(position: Int): Fragment = mList[position].fragment

    override fun getItemAtPosition(position: Int): AdapterFragmentItem = mList[position]
}

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */

class SectionsStatePagerAdapter(private val fragment: Fragment)
    : FragmentStatePagerAdapter(fragment.childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val mList: MutableList<AdapterFragmentItem> = mutableListOf()

    override fun getItem(position: Int) = mList[position].fragment

    override fun getCount() = mList.size

    override fun getPageTitle(position: Int): CharSequence? {
        return mList[position].title
    }

    fun updateList(list: List<AdapterFragmentItem>) {
        mList.addAll(list)
        notifyDataSetChanged()
    }
}