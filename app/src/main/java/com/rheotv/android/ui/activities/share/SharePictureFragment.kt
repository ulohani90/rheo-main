package com.rheotv.android.ui.activities.share

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentSharePictureBinding
import com.rheotv.android.utils.AppUtilsKt
import com.rheotv.android.utils.pager.PageChangeListener

class SharePictureFragment : ShareFragment<FragmentSharePictureBinding>(), PageChangeListener {

    override val mimeType: String
        get() = "image/jpg"

    private var mLastUrl: String? = null
    override fun getLayoutId() = R.layout.fragment_share_picture

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel.picture.observe(viewLifecycleOwner, Observer {
            val image = it ?: return@Observer
            viewDataBinding.reloadImage.visibility = if (mViewModel.pictureCollection.size == 1) {
                View.GONE
            } else {
                View.VISIBLE
            }
            if (mLastUrl != image.url) {
                viewDataBinding.pictureUrl = image.url
                mLastUrl = image.url
            }
            viewDataBinding.executePendingBindings()
            downloadFile(image.url, AppUtilsKt.getInternalMediaFile(viewDataBinding.root.context.filesDir, pictureFileName))
        })
        mViewModel.builder?.thumbnailUrl?.let {
            if (it.isNotBlank()) {
                mViewModel.pictureCollection.add(it)
                mViewModel.shufflePicture()
            }
        }
        viewDataBinding.reloadImage.setOnClickListener {
            mShareableFile = null
            mFileDownloading = true
            mViewModel.currentFileMimeType = mimeType
            mViewModel.isCurrentFileDownloading = true
            mViewModel.currentFile = null
            mViewModel.shufflePicture()
        }
    }

    override fun onPageSelected(position: Int) {
        if (!isAdded) {
            mActionQueue.add(Runnable { onPageSelected(position) })
            return
        }
        mViewModel.apply {
            isCurrentFileDownloading = mFileDownloading
            currentFileMimeType = mimeType
            currentFile = mShareableFile
        }
    }

    override fun onPageUnselected(position: Int) {
        if (!isAdded) {
            mActionQueue.add(Runnable { onPageUnselected(position) })
            return
        }
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"
        private const val pictureFileName = "sample.jpg"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): SharePictureFragment {
            return SharePictureFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}
