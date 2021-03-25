package com.rheotv.android.ui.activities.player.activity.newPlayer

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.rheotv.android.data.network.models.objects.PostObject
import com.rheotv.android.ui.activities.player.activity.CustomFragmentStateAdapter
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerFragment
import com.rheotv.android.ui.activities.player.activity.newPlayer.fragments.ImageDialogFragment
import com.rheotv.android.ui.activities.player.activity.streamplayer.utils.RestrictedMap

internal class StreamPlayerAdapterV2 internal constructor(fm: FragmentManager, lifecycle: Lifecycle,
                                                          profiles: MutableList<PostObject>, val sourceScreen: String?) :
        CustomFragmentStateAdapter<PostObject>(fm, lifecycle) {
    private val mList: MutableList<PostObject> = mutableListOf()
    private val mFragmentMap: MutableSet<Long> = hashSetOf()

    private var currentFragment: Fragment? = null

    override fun getItem(position: Int): StreamPlayerFragmentV2? {
        return currentFragment as? StreamPlayerFragmentV2
    }

    override fun getItemId(position: Int): Long {
        return if (position < 0 || position >= mList.size) -1 else mList[position].id.hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        for (item in mList) {
            if (item.id.hashCode() == itemId.toInt()) return true
        }
        return false
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun getIdAt(position: Int): Long? {
        return if (mList.isEmpty()) -1L else mList[position].id.hashCode().toLong()
    }

    override fun containsId(Id: String?): Boolean {
        return mList.isNotEmpty() && Id != null && mList.indexOfFirst { it.id.hashCode() == Id.toInt() } != -1
    }

    override fun getPositionForId(Id: String?): Int {
        return if (mList.isEmpty()) 0 else mList.indexOfFirst { it.id == Id }
    }

    override fun removeItem(positionToBeDeleted: Int) {
        val id = getIdAt(positionToBeDeleted)
        mFragmentMap.remove(id)
        mList.removeAt(positionToBeDeleted)
        notifyItemRemoved(positionToBeDeleted)
    }

    override fun clearList() {
        val size = mList.size
        mList.clear()
        mFragmentMap.clear()
        notifyItemRangeRemoved(0, size)
    }

    private val mMap: RestrictedMap = RestrictedMap(3)

    override fun createFragment(position: Int): Fragment {
        val item = mList[position]
        currentFragment = if (item.isCardType)
            ImageDialogFragment.newInstance(item)
        else
            StreamPlayerFragmentV2.getInstance(item, sourceScreen)
        Log.i(StreamPlayerContainerFragment.TAG, "StreamPlayer_called: createFragmentCalled")
        return currentFragment!!
    }

    override fun addListItem(item: PostObject, currentPosition: Int) {
        if (item.id == null) return
        try {
            addItem(item, currentPosition + 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun addItemAt(item: PostObject, position: Int) {
        val itemPositionToBeAdded = Math.min(position, mList.size)
        addItem(item, itemPositionToBeAdded)
    }

    private fun addItem(item: PostObject, position: Int) {
        val itemHashCode = item.id.hashCode().toLong()
        if (!mFragmentMap.contains(itemHashCode)) {
            mList.add(position, item)
            mFragmentMap.add(itemHashCode)
            notifyItemInserted(position)
        }
    }

    override fun removeListItem(item: PostObject) {
        if (item.id == null) return
        val itemHashCode = item.id.hashCode().toLong()
        if (mFragmentMap.contains(itemHashCode) && mList.indexOfFirst { it.id.hashCode() == itemHashCode.toInt() } != -1) {
            val index = mList.indexOfFirst { it.id.hashCode() == itemHashCode.toInt() }
            mFragmentMap.remove(itemHashCode)
            mList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    init {
        addListItem(profiles)
    }

    override fun addListItem(list: MutableList<PostObject>?) {
        for (postObject in (list ?: mutableListOf())) {
            if (!postObject.id.isNullOrEmpty() && !mFragmentMap.contains(postObject.id.hashCode().toLong())) {
                mList.add(mFragmentMap.size, postObject)
                mFragmentMap.add(postObject.id.hashCode().toLong())
            }
        }
        if (!list.isNullOrEmpty()) {
            notifyDataSetChanged()
        }
    }
}