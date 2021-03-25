package com.rheotv.android.utils.pager

import android.util.Log
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.rheotv.android.ui.activities.share.CustomFragmentPagerAdapter
import com.rheotv.android.utils.AdapterFragmentItem
import com.rheotv.android.utils.ViewUtils
import com.rheotv.android.utils.viewpager2.transformer.TranslateEdgeToEdgeTransformer
import java.lang.ref.WeakReference

class PagerMediator(private val viewPager: ViewPager2,
                    private val tabLayout: TabLayout,
                    private val adapter: CustomFragmentPagerAdapter,
                    private val startPosition: Int = 0,
                    private val onPageChangeListener: PageChangeListener? = null
) {
    private var onTabChangeListener: TabLayout.OnTabSelectedListener? = null
    fun attach() {
        onTabChangeListener = FragmentOnTabChangeCallback(adapter)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.text = adapter.getItemAtPosition(position).title
        }.attach()
        viewPager.clipChildren = false
        viewPager.clipToPadding = false
        viewPager.offscreenPageLimit = 3
        viewPager.setPageTransformer(TranslateEdgeToEdgeTransformer(ViewUtils.dpToPx(10), ViewUtils.dpToPx(20)))
        onTabChangeListener?.let { tabLayout.addOnTabSelectedListener(it) }
        viewPager.setCurrentItem(startPosition, false)
    }

    /**
     * Unlink the ViewPager callback
     */
    fun detach() {
        onTabChangeListener?.let { tabLayout.removeOnTabSelectedListener(it) }
        onTabChangeListener = null
    }

    fun updateAdapter(list: List<AdapterFragmentItem>) {
        adapter.updateList(list)
    }

    fun setFirstPageAsCurrentPage() {
        if (adapter.itemCount < 0) return
        tabLayout.getTabAt(0)?.select()
    }

    private inner class FragmentOnTabChangeCallback(adapter: CustomFragmentPagerAdapter) : TabLayout.OnTabSelectedListener {
        private val adapterRef: WeakReference<CustomFragmentPagerAdapter> = WeakReference(adapter)

        override fun onTabReselected(tab: TabLayout.Tab?) = onTabSelected(tab)

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            onPageChangeListener?.onPageUnselected(tab?.position ?: 0)
            (adapter.getItemAtPosition(tab?.position
                    ?: 0).fragment as? PageChangeListener)?.onPageUnselected(tab?.position ?: 0)
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            val adapter = adapterRef.get()

            Log.i(javaClass.simpleName, "pager_change: position ${tab?.position} and ${tab?.position?.minus(1)}")

            (adapter?.getItemAtPosition(tab?.position
                    ?: 0)?.fragment as? PageChangeListener)?.onPageSelected(tab?.position ?: 0)
            onPageChangeListener?.onPageSelected(tab?.position ?: 0)
        }
    }
}
