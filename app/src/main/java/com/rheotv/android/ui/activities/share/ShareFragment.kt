package com.rheotv.android.ui.activities.share

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.utils.AppUtilsKt
import kotlinx.coroutines.*
import java.io.File
import java.lang.Runnable
import java.util.*
import javax.inject.Inject

abstract class ShareFragment<T : ViewDataBinding> : BaseFragment<T, ShareViewModel>() {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    protected lateinit var mViewModel: ShareViewModel
    protected var mShareableFile: File? = null
    protected var mFileDownloading = false
    protected var mActionQueue: Queue<Runnable> = LinkedList()

    abstract val mimeType: String

    override fun getBindingVariable() = com.rheotv.android.BR.viewModel

    override fun getViewModel() =
            ViewModelProvider(parentFragment ?: this, mViewModelFactory)
                    .get(ShareViewModel::class.java).also {
                        mViewModel = it
                    }

    private var mDownloadJob: Job? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        while (mActionQueue.isNotEmpty()) {
            mActionQueue.poll()?.run()
        }
    }

    fun downloadFile(remoteUrl: String?, outputFile: File) {
        stopOnGoingTasks()
        mDownloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                mFileDownloading = true
                if (mViewModel.currentFileMimeType == mimeType) mViewModel.isCurrentFileDownloading = mFileDownloading
                mShareableFile = AppUtilsKt.downloadMediaFileFromRemote(remoteUrl, outputFile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mFileDownloading = false
            if (mViewModel.currentFileMimeType == mimeType) {
                mViewModel.isCurrentFileDownloading = mFileDownloading
                mViewModel.currentFile = mShareableFile
            }
            withContext(Dispatchers.Main) {
                if (mimeType == mViewModel.currentFileMimeType) {
                    mViewModel.onDownloadFileListener?.invoke()
                    mViewModel.onDownloadFileListener = null
                }
            }
        }
    }

    private fun stopOnGoingTasks() {
        try {
            mDownloadJob?.children?.forEach {
                it.cancel()
            }
            mDownloadJob?.cancelChildren()
            mDownloadJob?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        AppUtilsKt.removeDirectoryHierarchy(File(viewDataBinding.root.context.filesDir, "media"))
        stopOnGoingTasks()
        super.onDestroy()
    }
}