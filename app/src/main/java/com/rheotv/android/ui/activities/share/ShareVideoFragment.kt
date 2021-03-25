package com.rheotv.android.ui.activities.share

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentShareVideoBinding
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.AppUtilsKt
import com.rheotv.android.utils.pager.PageChangeListener
import java.util.*

class ShareVideoFragment : ShareFragment<FragmentShareVideoBinding>(), PageChangeListener {

    private val mVideoActionQueue: Queue<Runnable> = LinkedList()
    override val mimeType: String
        get() = "video/*"

    override fun getLayoutId() = R.layout.fragment_share_video

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel.video.observe(viewLifecycleOwner, Observer {
            val video = it ?: return@Observer
            viewDataBinding.videoUrl = video.url

            viewDataBinding.executePendingBindings()
            while (mVideoActionQueue.isNotEmpty()) {
                mVideoActionQueue.poll()?.run()
            }
            downloadFile(video.url, AppUtilsKt.getInternalMediaFile(viewDataBinding.root.context.filesDir, videoFileName))
        })

        mViewModel.picture.observe(viewLifecycleOwner, Observer {
            viewDataBinding.thumbnail = it.url;
            viewDataBinding.executePendingBindings()
        })
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
        if (viewDataBinding == null || viewDataBinding.videoView.player == null) {
            mVideoActionQueue.add(Runnable { setPlayer(true) })
            return
        }
        setPlayer(true)
    }

    private fun setPlayer(playVideo: Boolean) {
        viewDataBinding?.videoView?.player?.playWhenReady = playVideo
        viewDataBinding?.imageViewPlay?.setImageDrawable(ContextCompat.getDrawable(requireContext(), if (playVideo) R.drawable.avd_pause else R.drawable.avd_play))
        viewDataBinding?.executePendingBindings()
    }

    override fun onPageUnselected(position: Int) {
        if (!isAdded) {
            mActionQueue.add(Runnable { onPageSelected(position) })
            return
        }
        if (viewDataBinding == null || viewDataBinding.videoView.player == null) {
            mVideoActionQueue.add(Runnable { setPlayer(false) })
            return
        }
        setPlayer(false)
    }

    override fun onDestroyView() {
        viewDataBinding?.videoView?.player?.release()
        super.onDestroyView()
        val intent = Intent(AppConstants.FILTER_VIDEO_STATE)
        intent.putExtra(AppConstants.VIDEO_STATE, false)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    override fun onPause() {
        super.onPause()
        viewDataBinding?.videoView?.player?.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        viewDataBinding?.videoView?.player?.playWhenReady = true
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        private const val videoFileName = "sample.mp4"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): ShareVideoFragment {
            return ShareVideoFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}
