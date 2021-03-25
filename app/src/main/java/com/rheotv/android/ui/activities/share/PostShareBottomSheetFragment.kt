package com.rheotv.android.ui.activities.share

import android.content.DialogInterface
import android.os.Bundle
import androidx.lifecycle.Observer
import com.rheotv.android.R
import com.rheotv.android.utils.AdapterFragmentItem
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import java.util.*
import kotlin.collections.HashMap

class PostShareBottomSheetFragment : ShareBottomSheetFragment() {

    private val queue: Queue<FragmentType> = LinkedList()

    override fun getViewModel(): ShareViewModel =
            super.getViewModel().also {
                it.id = it.builder?.postId ?: ""
            }

    override fun setViewModelObserver() =
            mViewModel.shareableData.observe(viewLifecycleOwner, Observer {
                val data = it ?: return@Observer
                val viewPagerItem: MutableList<AdapterFragmentItem> = mutableListOf()
                while (queue.isNotEmpty()) {
                    when (queue.poll()) {
                        is FragmentType.IMAGE ->
                            if (!data.thumbnails.isNullOrEmpty() || mViewModel.pictureCollection.isNotEmpty()) {
                                viewPagerItem.add(AdapterFragmentItem(SharePictureFragment(),
                                        context?.getString(R.string.share_image) ?: ""))
                            }
                        is FragmentType.VIDEO ->
                            if (!data.clip.isNullOrEmpty() || mViewModel.videoCollection.isNotEmpty()) {
                                viewPagerItem.add(AdapterFragmentItem(ShareVideoFragment(),
                                        context?.getString(R.string.share_video) ?: ""))
                            }
                        is FragmentType.GIF ->
                            if (!data.giphy.isNullOrEmpty() || mViewModel.gifCollection.isNotEmpty()) {
                                viewPagerItem.add(AdapterFragmentItem(ShareGifFragment(),
                                        context?.getString(R.string.share_gif) ?: ""))
                            }
                    }
                }

                mPagerMediator.updateAdapter(viewPagerItem)
            })

    override fun getInitialPageList(): List<AdapterFragmentItem> =
            listOf(
                    if (viewModel.builder?.shareData != null) {
                        queue.add(FragmentType.GIF)
                        queue.add(FragmentType.IMAGE)
                        AdapterFragmentItem(ShareVideoFragment()
                                .also { fragment ->
                                    viewDataBinding.shareMenu.setShareAppList(fragment.mimeType)
                                    mViewModel.currentFileMimeType = fragment.mimeType
                                },
                                context?.getString(R.string.share_video) ?: "")
                    } else {
                        queue.add(FragmentType.VIDEO)
                        queue.add(FragmentType.GIF)
                        AdapterFragmentItem(SharePictureFragment()
                                .also { fragment ->
                                    viewDataBinding.shareMenu.setShareAppList(fragment.mimeType)
                                    mViewModel.currentFileMimeType = fragment.mimeType
                                },
                                context?.getString(R.string.share_image) ?: "")
                    })

    override fun trackShareEvent(event: String) {
        if (!isAdded) return
        HashMap<String, Any?>(baseProperties ?: mapOf())
                .apply {
                    put("share_action", event)
                    put("is_first", CommonUtils.isFirstShare())
                    "author" to mViewModel.builder?.author
                    put("type", when {
                        mViewModel.currentFileMimeType.contains("gif") -> "GIF"
                        mViewModel.currentFileMimeType.contains("video") -> "Video"
                        else -> "Image"
                    })
                    put("source", mViewModel.source)
                    SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_PLAYER_POST_SHARED, this)
                    CommonUtils.setFirstShare()
                }
    }

    override fun loadDataFromServer() = mViewModel.loadSharableContent()

    override fun onShareSelected() {
        CommonUtils.markFirstShareAvailable()
        mViewModel.onResourceShare()
    }

    override fun onMoreSelected() = onShareSelected()

    override fun onCopyClicked() = onShareSelected()

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        exitListener?.onShareSheetDismiss()
    }

    companion object {

        fun builder(baseProperties: MutableMap<String, Any>): Builder = Builder(baseProperties)

        fun build(builder: Builder, listener: PostShareCallback): PostShareBottomSheetFragment =
                PostShareBottomSheetFragment().also {
                    it.exitListener = listener
                    it.baseProperties = builder.baseProperties
                    it.arguments = Bundle().apply {
                        putParcelable(KEY_BUILDER, builder)
                    }
                }
    }
}

sealed class FragmentType {
    object VIDEO : FragmentType()
    object IMAGE : FragmentType()
    object GIF : FragmentType()
}