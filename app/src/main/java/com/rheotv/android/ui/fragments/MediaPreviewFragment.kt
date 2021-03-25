package com.rheotv.android.ui.fragments

import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentMediaPreviewBinding
import com.rheotv.android.ui.base.BaseDialog

private const val ARG_URL = "url"
private const val ARG_MIME_TYPE = "mime_type"
private const val ARG_SOURCE = "source"

/**
 * A simple [Fragment] subclass.
 * Use the [MediaPreviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MediaPreviewFragment : BaseDialog() {
    private var binding: FragmentMediaPreviewBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val decorView = activity?.window?.decorView
            decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
        val root = RelativeLayout(activity)
        root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        // creating the fullscreen dialog
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(true)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        DataBindingUtil.inflate<FragmentMediaPreviewBinding>(inflater, R.layout.fragment_media_preview, container, false).also {
            binding = it
        }.run {
            return this.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            binding?.url = it.getString(ARG_URL)
            binding?.mimeType = it.getString(ARG_MIME_TYPE)
            binding?.notifyChange()
        }
        binding?.closeButton?.setOnClickListener {
            dismiss()
        }

        val clickCallback = View.OnClickListener {
            if (binding?.videoView?.isPlaying == true) {
                binding?.videoView?.pause()
                binding?.playerIndicator?.visibility = View.VISIBLE
            } else {
                binding?.videoView?.start()
                binding?.playerIndicator?.visibility = View.GONE
            }
        }

        binding?.playerIndicator?.setOnClickListener(clickCallback)
        binding?.videoView?.setOnClickListener(clickCallback)
        binding?.videoView?.setOnCompletionListener { binding?.playerIndicator?.visibility = View.VISIBLE }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param url Parameter 1.
         * @param mimeType Parameter 2.
         * @param source Parameter 3.
         * @return A new instance of fragment MediaPreviewFragment.
         */
        @JvmStatic
        fun newInstance(url: String?, mimeType: String?, source: String?) =
                MediaPreviewFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_URL, url)
                        putString(ARG_MIME_TYPE, mimeType)
                        putString(ARG_SOURCE, source)
                    }
                }
    }
}