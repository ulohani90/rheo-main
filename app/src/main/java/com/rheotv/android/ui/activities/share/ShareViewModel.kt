package com.rheotv.android.ui.activities.share

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.facebook.share.model.ShareContent
import com.freshchat.consumer.sdk.b.b.da
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.DataManager
import com.rheotv.android.data.network.models.share.ShareData
import com.rheotv.android.data.network.models.share.ShareResponse
import com.rheotv.android.ui.base.BaseViewModel
import com.rheotv.android.utils.rx.SchedulerProvider
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ShareViewModel constructor(
        dataManager: DataManager?,
        schedulerProvider: SchedulerProvider?
) : BaseViewModel<Any>(dataManager, schedulerProvider) {
    var source: String = SegmentConstants.SCREEN_NAME_VIDEO_PLAYER
    var id = ""
    var builder: ShareBottomSheetFragment.Builder? = null
    var videoUrl: String? = null

    var picIndex = 0
    var gifIndex = 0
    var videoIndex = 0
    val pictureCollection = ArrayList<String>()
    val gifCollection = ArrayList<String>()
    val videoCollection = ArrayList<String>()
    val video = MutableLiveData<ShareContent>()
    val picture = MutableLiveData<ShareContent>()
    val gif = MutableLiveData<ShareContent>()
    val backdrop = ObservableField<String>()
    val shareableData = MutableLiveData<ShareData>()

    var currentFileMimeType = "image/*"
    var currentFile: File? = File(RheoTvApp.getNonUiContext().filesDir.absolutePath)
    var isCurrentFileDownloading = true
    var shareLink: String? = null

    fun setPicture(url: String?) {
        picture.value = ShareContent(url)
    }

    fun setVideo(url: String?) {
        video.value = ShareContent(url)
    }

    fun setGif(url: String?) {
        gif.value = ShareContent(url)
    }

    fun loadSharableContent() {
        dataManager.loadShareContent(id).enqueue(object : Callback<ShareResponse> {
            override fun onFailure(call: Call<ShareResponse>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ShareResponse>, response: Response<ShareResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    setShareData(response.body()?.shareData)
                }
            }
        })
    }

    fun setShareData(shareData: ShareData?) {
        shareableData.value = shareData
        shareData?.let {
            it.clip?.let { videos ->
                videoCollection.addAll(videos)
                if (videos.isNotEmpty()) {
                    setVideo(videos[0])
                    videoIndex++
                } else {
                    videoUrl?.let { url ->
                        if (url.isNotBlank()) {
                            videoCollection.add(url)
                            setVideo(videoCollection[0])
                            videoIndex = 0
                        }
                    }
                }
            }
            it.thumbnails?.let { pictures ->
                if (pictures.isNotEmpty()) {
                    for (picture in pictures) {
                        if (!pictureCollection.contains(picture)) {
                            pictureCollection.add(picture)
                        }
                    }

                    backdrop.set(pictures[0])
                    if (pictureCollection.size > 2) {
                        setPicture(pictureCollection[0])
                    }
                    picIndex = pictureCollection.size
                }
            }

            it.giphy?.let { gifs ->
                gifCollection.addAll(gifs)
                if (gifs.isNotEmpty()) {
                    setGif(gifs[0])
                    gifIndex++
                }
            }
        }
    }

    fun onResourceShare() {
        dataManager.onPostShare(id).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {

                }
            }
        })
    }

    fun shufflePicture() {
        if (pictureCollection.isNullOrEmpty()) return
        if (picIndex > pictureCollection.size - 1) picIndex = 0
        pictureCollection[picIndex]?.let {
            setPicture(it)
            picIndex++
        }
    }

    fun shuffleGif() {
        if (gifCollection.isNullOrEmpty()) return
        if (gifIndex > gifCollection.size - 1) gifIndex = 0
        gifCollection[gifIndex]?.let {
            setGif(it)
            gifIndex++
        }
    }

    var onDownloadFileListener: (() -> Unit)? = null
    fun registerDownloadListener(onDownloadFileListener: () -> Unit) {
        this.onDownloadFileListener = onDownloadFileListener
    }

    fun shuffleVideo() {
        if (videoCollection.isNullOrEmpty()) return
        if (videoIndex > videoCollection.size - 1) videoIndex = 0
        videoCollection[videoIndex]?.let {
            setVideo(it)
            videoIndex++
        }
        videoCollection
    }

    data class ShareContent(
            val url: String? = null,
            val backdrop: String? = null
    )
}