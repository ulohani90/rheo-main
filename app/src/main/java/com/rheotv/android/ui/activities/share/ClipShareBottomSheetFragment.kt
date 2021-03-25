package com.rheotv.android.ui.activities.share

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import com.rheotv.android.R
import com.rheotv.android.utils.AdapterFragmentItem
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker

class ClipShareBottomSheetFragment : ShareBottomSheetFragment() {

    override fun getViewModel(): ShareViewModel =
            super.getViewModel().also {
                it.id = it.builder?.clipId ?: ""
                it.videoUrl = it.builder?.videoUrl
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPagerMediator.setFirstPageAsCurrentPage()
    }

    override fun getInitialPageList(): List<AdapterFragmentItem> =
            listOf(AdapterFragmentItem(ShareVideoFragment()
                    .also { fragment ->
                        viewDataBinding.shareMenu.setShareAppList(fragment.mimeType)
                        mViewModel.currentFileMimeType = fragment.mimeType
                    },
                    context?.getString(R.string.share_video) ?: ""))

    override fun loadDataFromServer() = mViewModel.loadSharableContent()

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        exitListener?.onShareSheetDismiss()
    }

    override fun trackShareEvent(event: String) {
        HashMap<String, Any?>(baseProperties ?: mapOf())
                .apply {
                    put("share_action", event)
                    put("is_first", CommonUtils.isFirstShare())
                    put("type", when {
                        mViewModel.currentFileMimeType.contains("gif") -> "GIF"
                        mViewModel.currentFileMimeType.contains("video") -> "Video"
                        else -> "Image"
                    })
                    put("source", mViewModel.source)
                    SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_CLIP_SHARED_PLATFORM, this)
                    SegmentTracker.getInstance(requireContext()).trackEvent(SegmentConstants.EVENT_CLIP_SHARED, this)
                    CommonUtils.setFirstShare()
                }
    }

    companion object {

        fun builder(baseProperties: MutableMap<String, Any>): Builder = Builder(baseProperties)

        fun build(builder: Builder, listener: PostShareCallback): ClipShareBottomSheetFragment =
                ClipShareBottomSheetFragment().also {
                    it.exitListener = listener
                    it.baseProperties = builder.baseProperties
                    it.arguments = Bundle().apply {
                        putParcelable(KEY_BUILDER, builder)
                    }
                }
    }
}