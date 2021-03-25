package com.rheotv.android.ui.activities.moments.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.rheotv.android.ui.activities.moments.model.MomentsListItem
import com.rheotv.android.ui.activities.moments.view.fragments.MomentsFragment
import com.rheotv.android.ui.activities.player.activity.CustomFragmentStateAdapter

class MomentsPagerAdapter(val sourceScreen: String?, fragmentManager: FragmentManager, lifecycle: Lifecycle)
    : CustomFragmentStateAdapter<MomentsListItem>(fragmentManager, lifecycle) {

    private val mList: MutableList<MomentsListItem> = mutableListOf()
    private val mFragmentMap: MutableSet<Long> = hashSetOf()
    private var currentFragment: Fragment? = null

    override fun getItemCount(): Int = mList.size

    override fun createFragment(position: Int): Fragment {
        currentFragment = MomentsFragment.getInstance(mList[position], sourceScreen)
        return currentFragment!!
    }

    override fun getItem(position: Int): MomentsFragment? {
        return currentFragment as? MomentsFragment
    }


    override fun getIdAt(position: Int): Long? {
        return if (mList.isEmpty()) -1L else mList[position].id.hashCode().toLong()
    }

    override fun getPositionForId(Id: String?): Int {
        return if (mList.isEmpty()) 0 else mList.indexOfFirst { it.id == Id }
    }

    override fun addListItem(list: List<MomentsListItem>?) {
        mList.addAll(list ?: return)
        notifyDataSetChanged()
    }

    fun clearAllData() {
        mList.clear()
        mFragmentMap.clear()
        notifyDataSetChanged()
    }

    override fun addListItem(item: MomentsListItem, currentPosition: Int) {
        if (item.id == null) return
        try {
            addItem(item, currentPosition + 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addItem(item: MomentsListItem, position: Int) {
        val itemHashCode = item.id.hashCode().toLong()
        if (!mFragmentMap.contains(itemHashCode)) {
            mList.add(position, item)
            mFragmentMap.add(itemHashCode)
            notifyItemInserted(position)
        }
    }
}