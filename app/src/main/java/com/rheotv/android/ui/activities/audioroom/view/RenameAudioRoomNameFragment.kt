package com.rheotv.android.ui.activities.audioroom.view

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentChatBoxBottomSheetDialogBinding
import com.rheotv.android.databinding.FragmentRenameAudioRoomNameBinding
import com.rheotv.android.ui.activities.player.activity.ChatBoxCallbackListener
import com.rheotv.android.utils.isNullOrEmptyOrBlank
import com.rheotv.android.utils.showToast

/**
 * A simple [Fragment] subclass.
 * Use the [RenameAudioRoomNameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RenameAudioRoomNameFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentRenameAudioRoomNameBinding
    private var callbackListener: RenameAudioRoomNameListener? = null
    private var roomName: String? = null

    override fun getTheme() = R.style.FloatingBottomSheetDialogTheme

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        DataBindingUtil.inflate<FragmentRenameAudioRoomNameBinding>(
                inflater, R.layout.fragment_rename_audio_room_name, container, false
        ).run {
            binding = this
            return this.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.name = roomName
        showWithMaxHeight(view)
        openKeyboard()
        binding.sendButton.setOnClickListener {
            val message = binding.nameEditText.text.toString()
            if (message.isNullOrEmptyOrBlank())
                context?.showToast("Please enter a name")
            else {
                callbackListener?.onNameChange(message)
                dismiss()
            }
        }
        binding.executePendingBindings()
    }

    private fun showWithMaxHeight(view: View) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val dialog = dialog as BottomSheetDialog?
                val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
                val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                // behavior.setPeekHeight(0); // Remove this line to hide a dark background if you manually hide the dialog.
            }
        })
    }

    private fun openKeyboard() {
        with(binding.nameEditText) {
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                binding.nameEditText.post {
                    val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.showSoftInput(this@with, InputMethodManager.SHOW_IMPLICIT)
                }
            }
            requestFocus()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RenameAudioRoomNameFragment.
         */
        @JvmStatic
        fun newInstance(listener: RenameAudioRoomNameListener, roomName: String) =
                RenameAudioRoomNameFragment().apply {
                    this.roomName = roomName
                    this.callbackListener = listener
                }
    }
}

interface RenameAudioRoomNameListener {
    fun onNameChange(name: String)
}