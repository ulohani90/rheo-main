package com.rheotv.android.ui.activities.moments.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.rheotv.android.ui.activities.moments.adapter.MomentsSlidingPagerAdapter
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.EventBusModel
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MomentsSlidingPagerFragment : Fragment() {

    private var mRootView: ViewPager? = null
    private val backPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPress()
        }
    }

    private fun onBackPress() {
        if (mRootView?.currentItem == 1) {
            mRootView?.setCurrentItem(0, true)
        } else {
            backPressCallback.remove()
            activity?.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, backPressCallback)
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = ViewPager(inflater.context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.MATCH_PARENT)
            id = View.generateViewId()
            addOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {

                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                }

                override fun onPageSelected(position: Int) {
                    if (position == 1) {
                        (mRootView?.adapter as? MomentsSlidingPagerAdapter)?.let { adapter ->
                            (adapter.getItem(1) as? StreamPlayerFragmentV3)?.let { player ->
                                val map = HashMap<String, Any?>()
                                        .apply {
                                            put("post_id", player.postObject.id)
                                            put("title", player.postObject.title)
                                            put("authorUsername", player.postObject.author.user.username)
                                        }
                                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_MOMENTS_LEFT_SWIPED, map);
                            }
                        }

                    }
                }

            })

        }

        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRootView?.adapter = MomentsSlidingPagerAdapter(childFragmentManager).apply {
            this.bundle = this@MomentsSlidingPagerFragment.arguments
        }
        mRootView?.offscreenPageLimit = 1
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventModel: EventBusModel.UpdateMomentData) {
        (mRootView?.adapter as? MomentsSlidingPagerAdapter)?.let { adapter ->
            (adapter.getItem(1) as? StreamPlayerFragmentV3)?.let { player ->
                if (player.postObject?.id != eventModel.moment?.postDetails?.id) {
                    player.moments = eventModel.moment
                    player.postObject = eventModel.moment?.postDetails
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventModel: EventBusModel.RemoveMomentsView) {
        (mRootView?.adapter as? MomentsSlidingPagerAdapter)?.let { adapter ->
            (adapter.getItem(1) as? StreamPlayerFragmentV3)?.destroyView()
        }
    }

    companion object {
        const val TAG = "MomentsPagerFragment"
        private const val ARG_KEY_MOMENTS = "arg_key_moments"
        fun getInstance(screenSource: String?) = MomentsSlidingPagerFragment().apply {
            arguments = bundleOf(AppConstants.SCREEN_SOURCE to screenSource)
        }
    }
}