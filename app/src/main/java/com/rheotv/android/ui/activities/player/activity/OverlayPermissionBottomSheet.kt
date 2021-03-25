package com.rheotv.android.ui.activities.player.activity

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rheotv.android.R
import com.rheotv.android.databinding.OverlayPermissionDialogLayoutBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OverlayPermissionBottomSheet.newInstance] factory method to
 * create an instance of this fragment.
 */
class OverlayPermissionBottomSheet
//    : BaseBottomSheetDialogFragment<OverlayPermissionDialogLayoutBinding, OverPermissionViewModel>()
    : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var callback: OnOptionSelected? = null
    private var isOptionSelected = false

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    private lateinit var viewDataBinding: OverlayPermissionDialogLayoutBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        DataBindingUtil.inflate<OverlayPermissionDialogLayoutBinding>(
                inflater, R.layout.overlay_permission_dialog_layout, container, false
        ).run {
            viewDataBinding = this
            return this.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            with(viewDataBinding) {
                viewPager.adapter = OverlayPermissionAdapter().also {
                    it.submitList(listOf(
                            ContextCompat.getDrawable(requireContext(), R.drawable.bg_floating_video)
//                            ContextCompat.getDrawable(requireContext(), R.drawable.bg_floating_audio)
                    ))
                }

                tabLayout.setupWithViewPager(viewPager, true)
                cancelAction.setOnClickListener {
                    isOptionSelected = true
                    callback?.onAudioSelected()
                    dismiss()
                }

                allowAction.setOnClickListener {
                    isOptionSelected = true
                    callback?.onVideoSelected()
                    dismiss()
                }

                closeButton.setOnClickListener {
                    isOptionSelected = false
                    dismiss()
                }
            }
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!isOptionSelected)
            callback?.onNothingSelected()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OverlayPermissionBottomSheet.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(callback: OnOptionSelected? = null) =
                OverlayPermissionBottomSheet().apply {
                    this.callback = callback
                }

        fun show(fragmentManager: FragmentManager, fragment: OverlayPermissionBottomSheet) =
                fragment.show(fragmentManager, "share")

    }

    interface OnOptionSelected {
        fun onAudioSelected()
        fun onVideoSelected()
        fun onNothingSelected();
    }
}