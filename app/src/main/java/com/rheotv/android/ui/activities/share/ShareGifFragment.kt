package com.rheotv.android.ui.activities.share

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentShareGifBinding
import com.rheotv.android.utils.AppUtilsKt
import com.rheotv.android.utils.pager.PageChangeListener

/**
 * A placeholder fragment containing a simple view.
 */
class ShareGifFragment : ShareFragment<FragmentShareGifBinding>(), PageChangeListener {

    override val mimeType: String
        get() = "image/gif"

    override fun getLayoutId() = R.layout.fragment_share_gif

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel.gif.observe(viewLifecycleOwner, Observer { giphyGif ->
            viewDataBinding.giphy = giphyGif
            val gif = giphyGif ?: return@Observer
            viewDataBinding.reloadGiphy.visibility = if (mViewModel.gifCollection.size == 1) {
                View.GONE
            } else {
                View.VISIBLE
            }
            viewDataBinding.executePendingBindings()
            downloadFile(gif.url, AppUtilsKt.getInternalMediaFile(viewDataBinding.root.context.filesDir, gifFileName))
        })
        mViewModel.shuffleGif()
        viewDataBinding.reloadGiphy.setOnClickListener {
            mViewModel.gif.value = null
            mShareableFile = null
            mFileDownloading = true
            mViewModel.currentFileMimeType = mimeType
            mViewModel.isCurrentFileDownloading = true
            mViewModel.currentFile = null
            mViewModel.shuffleGif()
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

        private const val gifFileName = "sample.gif"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): ShareGifFragment {
            return ShareGifFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}