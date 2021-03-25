package com.rheotv.android.ui.activities.profile.viewprofile.view

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import com.rheotv.android.R
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult
import com.rheotv.android.databinding.FragmentProfileShareBinding
import com.rheotv.android.ui.base.BaseDialog
import com.rheotv.android.ui.customViews.bottomSheetMenu.OptionRequest
import com.rheotv.android.ui.customViews.shareMenu.OnShareSelection
import com.rheotv.android.utils.*
import java.io.File

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileShareFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileShareFragment : BaseDialog() {
    var mProfile: ProfileResult? = null
    var source: String? = null
    var shareTitle = AppConstants.SHARE_TITLE_PROFILE
    var shareDescription = AppConstants.SHARE_DESCRIPTION_PROFILE
    private var shareLink: String? = null
    lateinit var binding: FragmentProfileShareBinding
    var callback: ((String) -> Unit)? = null

    private val onShareSelection = object : OnShareSelection {
        override fun onCopy(packageName: String?) {
            if (!isAdded) return
            FirebaseDynamicLinkUtils.shareToExternal(context,
                    FirebaseDynamicLinkUtils.FirebaseDynamicLinkData().also {
                        it.campaignInfo = mProfile?.campaignInfo
                        it.title = shareTitle
                        it.identifier = AppConstants.IDENTIFIER_PROFILE_SHARE
                        it.description = shareDescription
                        it.imageUrl = mProfile?.profilePic
                        it.map = hashMapOf(
                                AppConstants.BRANCH_PROFILE_URL_SHARE to mProfile?.shareUrl,
                                AppConstants.BRANCH_SHARE_TYPE to AppConstants.BRANCH_SHARE_TYPE_PROFILE
                        )
                        it.authorName = mProfile?.user?.username
                        it.isLive = mProfile?.liveStatus?.isLive.toString()
                    },
                    object : FirebaseDynamicLinkUtils.ShareLinkGenerateListener {
                        override fun onLinkGenerationSuccess(shareUrl: String) {
                            shareLink = shareUrl
                            context?.copyToClipBoard(shareUrl)
//                            trackShareEvent(packageName ?: "Copy Link")
                            callback?.invoke(packageName ?: "Copy Link")
                            context?.showToast("Copied")
                        }

                        override fun onLinkGenerationFailure(errorMessage: String?) {
                            context?.showToast(errorMessage)
                        }
                    }
            )
        }

        override fun onMoreSelected(tag: String?) {
            if (!isAdded) return
            if (shareLink == null) {
                getShareLink {
                    onMoreSelected(tag)
                }
            } else {
                val packageIntent = Intent(Intent.ACTION_SEND)
                try {
                    val f = binding.shareCardView.getPreview()
                    FileProvider.getUriForFile(requireContext(),
                            "com.rheotv.android.app.provider",
                            f ?: return)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Unable to get file!", Toast.LENGTH_LONG).show()
                    return
                }?.also {
                    packageIntent.setDataAndType(it, File(it.path).mimeType())
                    packageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    packageIntent.putExtra(Intent.EXTRA_STREAM, it)
                    packageIntent.putExtra(Intent.EXTRA_TEXT, "$shareDescription $shareLink #rheoapp")
                    startActivity(Intent.createChooser(packageIntent, "Share with"))
                    //trackShareEvent(tag ?: "More")
                    callback?.invoke(tag ?: "Share")
                }
            }
        }

        override fun onShareSelected(packageIntent: Intent, optionRequest: OptionRequest) {
            if (!isAdded) return
            if (optionRequest.tag?.contains("facebook", ignoreCase = true) == true) {
                if (shareLink == null) {
                    getShareLink {
                        onShareSelected(packageIntent, optionRequest)
                    }
                } else {
                    try {
                        val f = binding.shareCardView.getPreview()
                        FileProvider.getUriForFile(requireContext(),
                                "com.rheotv.android.app.provider",
                                f?.absoluteFile ?: return)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Unable to get file!", Toast.LENGTH_LONG).show()
                        return
                    }?.also {
                        ShareDialog(this@ProfileShareFragment)
                                .apply {
                                    show(SharePhotoContent.Builder()
                                            .addPhoto(SharePhoto.Builder()
                                                    .setImageUrl(it)
                                                    .build())
                                            .build())
                                    //trackShareEvent((optionRequest.tag + optionRequest.label) ?: "Share")
                                    callback?.invoke((optionRequest.tag + optionRequest.label) ?: "Share")
                                }
                    }
                }
                return
            }
            if (shareLink == null) {
                getShareLink {
                    onShareSelected(packageIntent, optionRequest)
                }
            } else {
                packageIntent.putExtra(Intent.EXTRA_TEXT, "$shareDescription $shareLink #rheoapp")
                packageIntent.putExtra(Intent.EXTRA_REPLACEMENT_EXTRAS, "$shareDescription $shareLink #rheoapp")
                try {
                    val f = binding.shareCardView.getPreview()
                    FileProvider.getUriForFile(requireContext(),
                            "com.rheotv.android.app.provider",
                            f?.absoluteFile ?: return)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Unable to get file!", Toast.LENGTH_LONG).show()
                    return
                }?.also {
                    packageIntent.setDataAndType(it, File(it.path).mimeType())
                    packageIntent.putExtra(Intent.EXTRA_STREAM, it)
                    packageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    try {
                        startActivity(packageIntent)
//                        trackShareEvent((optionRequest.tag + optionRequest.label) ?: "Share")
                        callback?.invoke(optionRequest.tag + optionRequest.label)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        private fun getShareLink(cb: (() -> Unit)? = null) {
            if (!isAdded) return
            FirebaseDynamicLinkUtils.generateShareLink(context, FirebaseDynamicLinkUtils.FirebaseDynamicLinkData().also {
                it.campaignInfo = mProfile?.campaignInfo
                it.title = shareTitle
                it.identifier = AppConstants.IDENTIFIER_PROFILE_SHARE
                it.description = shareDescription
                it.imageUrl = mProfile?.profilePic
                it.map = hashMapOf(
                        AppConstants.BRANCH_PROFILE_URL_SHARE to mProfile?.shareUrl,
                        AppConstants.BRANCH_SHARE_TYPE to AppConstants.BRANCH_SHARE_TYPE_PROFILE
                )
                it.authorName = mProfile?.user?.username
                it.isLive = mProfile?.liveStatus?.isLive.toString()
            }, object : FirebaseDynamicLinkUtils.ShareLinkGenerateListener {
                override fun onLinkGenerationSuccess(shareUrl: String) {
                    shareLink = shareUrl
                    cb?.invoke()
                }

                override fun onLinkGenerationFailure(errorMessage: String?) {
                    context?.showToast(errorMessage)
                }
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_share, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            profile = mProfile
            closeButton.setOnClickListener { dismiss() }
            shareMenu.apply {
                callback = onShareSelection
                initShareAppList(listOf("image/*"))
                setShareAppList("image/*")
            }
            executePendingBindings()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param profile Parameter 1.
         * @return A new instance of fragment ProfileShareFragment.
         */
        @JvmStatic
        fun newInstance(profile: ProfileResult, source: String, shareTitle: String, shareDescription: String, shareListener: (String) -> Unit): ProfileShareFragment =
                ProfileShareFragment().apply {
                    this.mProfile = profile
                    this.source = source
                    this.shareTitle = shareTitle
                    this.shareDescription = shareDescription
                    this.callback = shareListener
                }

        fun show(fragmentManager: FragmentManager, fragment: ProfileShareFragment) =
                fragment.show(fragmentManager, "share_profile")

    }
}