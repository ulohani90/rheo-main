package com.rheotv.android.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class Downloader constructor(
        var context: Context? = null,
        private var dirType: String = "/rheo_clips/",
        private var subPath: String = "${System.currentTimeMillis()}_clip_video.mp4",
        private var downloadLink: String? = null, // link of content required to be downloaded
        private val onDownloadComplete: ((String) -> Unit)? = null
) {

    init {
        startDownload()
    }

    private fun startDownload() {
        CoroutineScope(Dispatchers.IO)
                .launch {
                    try {
                        val file = File(context?.filesDir, dirType)
                        if (!file.exists()) file.mkdirs()
                        context?.let {
                            val fileToShare = File(it.filesDir?.absolutePath + dirType, subPath)
                            val outputFile = AppUtilsKt.downloadMediaFileFromRemote(downloadLink, fileToShare)
                            onDownloadComplete?.invoke(outputFile?.absolutePath ?: "")
                            Thread.sleep(100000)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
    }
}