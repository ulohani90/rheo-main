package com.rheotv.android.utils

import android.content.Context
import android.util.Log
import io.reactivex.Observable
import okhttp3.ResponseBody
import java.io.*

class ServerFileDownloader {

    fun downloadFile(body: ResponseBody, fileName: String): Observable<File?> {
        return Observable.create<File?> {
            try {
                Log.d(TAG, body.string())
                val futureStudioIconFile = File(fileName)
                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null
                try {
                    val fileReader = ByteArray(4096)
                    val fileSize: Long = body.contentLength()
                    var fileSizeDownloaded: Long = 0
                    inputStream = body.byteStream()
                    outputStream = FileOutputStream(futureStudioIconFile)
                    while (true) {
                        val read: Int = inputStream.read(fileReader)
                        if (read == -1) {
                            break
                        }
                        outputStream.write(fileReader, 0, read)
                        fileSizeDownloaded += read.toLong()
                        Log.d(TAG, "file download: $fileSizeDownloaded of $fileSize")
                    }
                    outputStream.flush()
                    it.onNext(futureStudioIconFile)
                } catch (e: IOException) {
                    it.onError(e)
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                }
            } catch (e: IOException) {
                it.onError(e)
            }
            it.onComplete()
        }
    }

    companion object {
        const val TAG = "ServerFileDownloader"
    }
}