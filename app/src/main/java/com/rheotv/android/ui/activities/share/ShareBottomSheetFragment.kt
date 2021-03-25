package com.rheotv.android.ui.activities.share

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import com.rheotv.android.R
import com.rheotv.android.data.network.models.share.ShareData
import com.rheotv.android.databinding.ActivityShareBinding
import com.rheotv.android.ui.base.BaseBottomSheetDialogFragment
import com.rheotv.android.ui.customViews.bottomSheetMenu.OptionRequest
import com.rheotv.android.ui.customViews.shareMenu.OnShareSelection
import com.rheotv.android.utils.*
import com.rheotv.android.utils.pager.PageChangeListener
import com.rheotv.android.utils.pager.PagerMediator
import javax.inject.Inject


open class ShareBottomSheetFragment : BaseBottomSheetDialogFragment<ActivityShareBinding, ShareViewModel>() {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    protected lateinit var mViewModel: ShareViewModel
    protected lateinit var mPagerMediator: PagerMediator
    protected var baseProperties: MutableMap<String, Any>? = null

    protected var exitListener: PostShareCallback? = null

    override fun getTheme() = R.style.BottomSheetDialogTheme

    override fun getBindingVariable() = com.rheotv.android.BR.viewModel

    override fun getLayoutId() = R.layout.activity_share

    override fun getViewModel() =
            ViewModelProvider(this, mViewModelFactory)
                    .get(ShareViewModel::class.java)
                    .apply {
                        mViewModel = this
                        if (arguments?.containsKey(KEY_BUILDER) == true) {
                            arguments?.getParcelable<Builder>(KEY_BUILDER)?.let {
                                builder = it
                            }
                        }
                    }

    open fun setViewModelObserver() = Unit
    open fun getInitialPageList(): List<AdapterFragmentItem> = listOf()
    open fun loadDataFromServer() = Unit
    open fun onCopyClicked() = Unit
    open fun onMoreSelected() = Unit
    open fun onShareSelected() = Unit
    open fun trackShareEvent(event: String) = Unit

    private val onShareSelection = object : OnShareSelection {
        override fun onCopy(packageName: String?) {
            if (!isAdded) return
            FirebaseDynamicLinkUtils.shareToExternal(context,
                    FirebaseDynamicLinkUtils.FirebaseDynamicLinkData().also {
                        it.campaignInfo = mViewModel.builder?.campaignInfo
                        it.title = mViewModel.builder?.shareTitle
                        it.identifier = mViewModel.builder?.shareIdentifier
                        it.description = mViewModel.builder?.shareDescription
                        it.imageUrl = mViewModel.builder?.postUrl
                        it.map = mViewModel.builder?.shareMap
                        it.authorName = mViewModel.builder?.authorName
                        it.isLive = mViewModel.builder?.isLive
                    },
                    object : FirebaseDynamicLinkUtils.ShareLinkGenerateListener {
                        override fun onLinkGenerationSuccess(shareUrl: String) {
                            mViewModel.shareLink = shareUrl
                            context?.copyToClipBoard(shareUrl)
                            context?.showToast("Copied")
                            trackShareEvent(packageName ?: "Copy Link")
                        }

                        override fun onLinkGenerationFailure(errorMessage: String?) {
                            context?.showToast(errorMessage)
                        }
                    }
            )
            onCopyClicked()
        }

        override fun onMoreSelected(tag: String?) {
            if (!isAdded) return
            if (mViewModel.isCurrentFileDownloading) {
                mViewModel.registerDownloadListener {
                    if (mViewModel.shareLink == null) {
                        getShareLink {
                            onMoreSelected(tag)
                        }
                    } else {
                        onMoreSelected(tag)
                    }
                }
                viewDataBinding.loaderMessage = when {
                    mViewModel.currentFileMimeType.contains("gif") -> "GIF"
                    mViewModel.currentFileMimeType.contains("video") -> "Video"
                    else -> "Image"
                }
                viewDataBinding.loader.visibility = View.VISIBLE
                viewDataBinding.executePendingBindings()
                return
            }
            if (mViewModel.shareLink == null) {
                getShareLink {
                    onMoreSelected(tag)
                }
            } else {
                viewDataBinding.loader.visibility = View.GONE
                val packageIntent = Intent(Intent.ACTION_SEND)
                packageIntent.type = mViewModel.currentFileMimeType
                try {
                    FileProvider.getUriForFile(context!!,
                            "com.rheotv.android.app.provider",
                            mViewModel.currentFile ?: return)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Unable to get file!", Toast.LENGTH_LONG).show()
                    return
                }?.also {
                    packageIntent.setDataAndType(it, context?.contentResolver?.getType(it))
                    packageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    packageIntent.putExtra(Intent.EXTRA_STREAM, it)
                    packageIntent.putExtra(Intent.EXTRA_TEXT, mViewModel.builder?.shareDescription + " ${mViewModel.shareLink} #rheoapp")
                    trackShareEvent(tag ?: "More")
                    this@ShareBottomSheetFragment.onMoreSelected()
                    startActivity(Intent.createChooser(packageIntent, "Share with"))
                }
            }
        }

        private fun getShareLink(cb: (() -> Unit)? = null) {
            FirebaseDynamicLinkUtils.generateShareLink(context, FirebaseDynamicLinkUtils.FirebaseDynamicLinkData().also {
                it.campaignInfo = mViewModel.builder?.campaignInfo
                it.title = mViewModel.builder?.shareTitle
                it.identifier = mViewModel.builder?.shareIdentifier
                it.description = mViewModel.builder?.shareDescription
                it.imageUrl = mViewModel.builder?.postUrl
                it.map = mViewModel.builder?.shareMap
                it.authorName = mViewModel.builder?.authorName
                it.isLive = mViewModel.builder?.isLive
            }, object : FirebaseDynamicLinkUtils.ShareLinkGenerateListener {
                override fun onLinkGenerationSuccess(shareUrl: String) {
                    mViewModel.shareLink = shareUrl
                    cb?.invoke()
                }

                override fun onLinkGenerationFailure(errorMessage: String?) {
                    context?.showToast(errorMessage)
                }
            })
        }

        override fun onShareSelected(packageIntent: Intent, optionRequest: OptionRequest) {
            if (!isAdded) return
            if (optionRequest.tag?.contains("facebook", ignoreCase = true) == true && optionRequest.title?.contains("feed", ignoreCase = true) == true) {
                if (mViewModel.shareLink == null) {
                    getShareLink {
                        onShareSelected(packageIntent, optionRequest)
                    }
                } else {
                    viewDataBinding.loader.visibility = View.GONE
                    ShareDialog(this@ShareBottomSheetFragment)
                            .apply {
                                trackShareEvent((optionRequest.tag + optionRequest.label)
                                        ?: "Share")
                                show(ShareLinkContent.Builder()
                                        .setContentUrl(Uri.parse(mViewModel.shareLink))
                                        .build())
                            }
                }
                return
            }
            if (mViewModel.isCurrentFileDownloading) {
                mViewModel.registerDownloadListener {
                    if (mViewModel.shareLink == null) {
                        getShareLink {
                            onShareSelected(packageIntent, optionRequest)
                        }
                    } else {
                        onShareSelected(packageIntent, optionRequest)
                    }
                }
                viewDataBinding.loaderMessage = when {
                    mViewModel.currentFileMimeType.contains("gif") -> "GIF"
                    mViewModel.currentFileMimeType.contains("video") -> "Video"
                    else -> "Image"
                }
                viewDataBinding.loader.visibility = View.VISIBLE
                viewDataBinding.executePendingBindings()
                getShareLink()
                return
            }
            if (mViewModel.shareLink == null) {
                getShareLink {
                    onShareSelected(packageIntent, optionRequest)
                }
            } else {
                viewDataBinding.loader.visibility = View.GONE
                packageIntent.type = mViewModel.currentFileMimeType
                packageIntent.putExtra(Intent.EXTRA_TEXT, mViewModel.builder?.shareDescription + " ${mViewModel.shareLink} #rheoapp")
                packageIntent.putExtra(Intent.EXTRA_REPLACEMENT_EXTRAS, mViewModel.builder?.shareDescription + " ${mViewModel.shareLink} #rheoapp")
                try {

                    FileProvider.getUriForFile(context!!,
                            "com.rheotv.android.app.provider",
                            mViewModel.currentFile ?: return)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Unable to get file!", Toast.LENGTH_LONG).show()
                    return
                }?.also {
                    Log.i(TAG,   "::uri::" + it.toString() + "::type::" + context?.contentResolver?.getType(it))
                    packageIntent.setDataAndType(it, context?.contentResolver?.getType(it))
                    packageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    packageIntent.putExtra(Intent.EXTRA_STREAM, it)
                    try {
                        trackShareEvent((optionRequest.tag + optionRequest.label) ?: "Share")
                        onShareSelected()
                        startActivity(packageIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.adjustWindow(view)
        fitViewPager()
        setViewModelObserver()
        viewDataBinding.shareMenu.apply {
            callback = onShareSelection
            initShareAppList(listOf("video/*", "image/gif", "image/jpg"))
        }

        mPagerMediator = PagerMediator(viewDataBinding.sharePager,
                viewDataBinding.tabLayout,
                SectionsPagerAdapter(this)
                        .also {
                            it.updateList(getInitialPageList())
                        },
                0,
                object : PageChangeListener {
                    override fun onPageSelected(position: Int) {
                        viewDataBinding.shareMenu.setShareAppList(mViewModel.currentFileMimeType)
                    }

                    override fun onPageUnselected(position: Int) = Unit
                }
        )
        mPagerMediator.attach()

        if (viewModel.builder?.shareData != null)
            viewModel.setShareData(viewModel.builder!!.shareData)
        else
            loadDataFromServer()
    }

    private fun fitViewPager() {
        (viewDataBinding.sharePager.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            val m = (ViewUtils.dpToPx(40 * 2))
            val w = ViewUtils.getScreenWidthInPx(context) - m
            val h = w.times(9) / 16 + m
            height = h.toInt()
            viewDataBinding.sharePager.layoutParams = this
        }
    }

    class Builder internal constructor(val baseProperties: MutableMap<String, Any>?) : Parcelable {
        internal var postId: String? = null
        internal var clipId: String? = null
        internal var gameName: String? = null
        internal var postUrl: String? = null
        internal var shareUrl: String? = null
        internal var thumbnailUrl: String? = null
        internal var campaignInfo: String? = null
        internal var shareTitle: String? = null
        internal var shareDescription: String? = null
        internal var shareIdentifier: String? = null
        internal var containsiOSParams = false
        internal var shareMap: HashMap<String, String>? = null
        internal var videoUrl: String? = null
        internal var shareData: ShareData? = null
        internal var source: String? = null
        internal var author: String? = null
        internal var authorName: String? = null
        internal var isLive: String? = null

        constructor(parcel: Parcel) : this(null) {
            postId = parcel.readString()
            clipId = parcel.readString()
            gameName = parcel.readString()
            postUrl = parcel.readString()
            shareUrl = parcel.readString()
            thumbnailUrl = parcel.readString()
            campaignInfo = parcel.readString()
            shareTitle = parcel.readString()
            shareDescription = parcel.readString()
            shareIdentifier = parcel.readString()
            containsiOSParams = parcel.readInt() != 0
            shareMap = parcel.readSerializable() as? HashMap<String, String>
            videoUrl = parcel.readString()
            shareData = parcel.readParcelable(ShareData::class.java.classLoader)
            source = parcel.readString()
            author = parcel.readString()
            authorName = parcel.readString()
            isLive = parcel.readString()

        }

        fun setPostId(postId: String?): Builder {
            this.postId = postId
            return this
        }

        fun setClipId(clipId: String?): Builder {
            this.clipId = clipId
            return this
        }

        fun setGameName(gameName: String?): Builder {
            this.gameName = gameName
            return this
        }

        fun setPostUrl(postUrl: String?): Builder {
            this.postUrl = postUrl
            return this
        }

        fun setAuthor(author: String?): Builder {
            this.author = author
            return this
        }

        fun setShareUrl(shareUrl: String?): Builder {
            this.shareUrl = shareUrl
            return this
        }

        fun setThumbnailUrl(thumbnailUrl: String?): Builder {
            this.thumbnailUrl = thumbnailUrl
            return this
        }

        fun setCampaignInfo(campaignInfo: String?): Builder {
            this.campaignInfo = campaignInfo
            return this
        }

        fun setAuthorName(authorName: String?): Builder {
            this.authorName = authorName
            return this
        }

        fun setIsLive(isLive: String?): Builder {
            this.isLive = isLive
            return this
        }

        fun setShareTitle(shareTitle: String?): Builder {
            this.shareTitle = shareTitle
            return this
        }

        fun setShareDescription(shareDescription: String?): Builder {
            this.shareDescription = shareDescription
            return this
        }

        fun setShareIdentifier(shareIdentifier: String?): Builder {
            this.shareIdentifier = shareIdentifier
            return this
        }

        fun setVideoUrl(videoUrl: String?): Builder {
            this.videoUrl = videoUrl
            return this
        }

        fun setShareMap(shareMap: HashMap<String, String>?): Builder {
            this.shareMap = shareMap
            return this
        }

        fun setContainsiOSParams(containsiOSParams: Boolean): Builder {
            this.containsiOSParams = containsiOSParams
            return this
        }

        fun setShareData(shareData: ShareData?): Builder {
            this.shareData = shareData
            return this
        }

        fun setSource(source: String): Builder {
            this.source = source
            return this
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(postId)
            parcel.writeString(clipId)
            parcel.writeString(gameName)
            parcel.writeString(postUrl)
            parcel.writeString(shareUrl)
            parcel.writeString(thumbnailUrl)
            parcel.writeString(campaignInfo)
            parcel.writeString(shareTitle)
            parcel.writeString(shareDescription)
            parcel.writeString(shareIdentifier)
            parcel.writeInt(if (containsiOSParams) 1 else 0)
            parcel.writeSerializable(shareMap)
            parcel.writeString(videoUrl)
            parcel.writeParcelable(shareData, flags)
            parcel.writeString(source)
            parcel.writeString(author)
            parcel.writeString(authorName)
            parcel.writeString(isLive)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Builder> {
            override fun createFromParcel(parcel: Parcel): Builder {
                return Builder(parcel)
            }

            override fun newArray(size: Int): Array<Builder?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {

        @JvmStatic
        protected val KEY_BUILDER = "key_builder"

        val TAG = ShareBottomSheetFragment::class.java.simpleName

        fun builder(baseProperties: MutableMap<String, Any>): Builder = Builder(baseProperties)

        fun build(builder: Builder, listener: PostShareCallback): ShareBottomSheetFragment =
                PostShareBottomSheetFragment().also {
                    it.exitListener = listener
                    it.baseProperties = builder.baseProperties
                    it.arguments = Bundle().apply {
                        putParcelable(KEY_BUILDER, builder)
                    }
                }

        @JvmStatic
        fun show(fragmentManager: FragmentManager, fragment: ShareBottomSheetFragment) =
                fragment.show(fragmentManager, "share")
    }
}