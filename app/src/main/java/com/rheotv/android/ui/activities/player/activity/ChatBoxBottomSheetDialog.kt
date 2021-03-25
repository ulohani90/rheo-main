package com.rheotv.android.ui.activities.player.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentChatBoxBottomSheetDialogBinding
import com.rheotv.android.utils.isNullOrEmptyOrBlank
import com.rheotv.android.utils.showToast

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_STREAM_NAME = "stream_name"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatBoxBottomSheetDialog.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatBoxBottomSheetDialog : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentChatBoxBottomSheetDialogBinding
    private var callbackListener: ChatBoxCallbackListener? = null
    private var streamName: String? = null

    override fun getTheme() = R.style.FloatingBottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            streamName = it.getString(ARG_STREAM_NAME)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        DataBindingUtil.inflate<FragmentChatBoxBottomSheetDialogBinding>(
                inflater, R.layout.fragment_chat_box_bottom_sheet_dialog, container, false
        ).run {
            binding = this
            return this.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.streamName = streamName
        showWithMaxHeight(view)
        openKeyboard()
        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()
            if (message.isNullOrEmptyOrBlank())
                context?.showToast("Please enter a message")
            else {
                callbackListener?.onChatSend(message)
                dismiss()
            }
        }
        binding.executePendingBindings()
    }

    private fun showWithMaxHeight(view: View?) {
        view?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (isStateSaved || view == null) return
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val dialog = dialog as BottomSheetDialog?
                val bottomSheet = dialog?.findViewById<View?>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
                        ?: return
                val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<FrameLayout?>(bottomSheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                // behavior.setPeekHeight(0); // Remove this line to hide a dark background if you manually hide the dialog.
            }
        })
    }

    private fun openKeyboard() {
        with(binding.messageEditText) {
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                binding.messageEditText.post {
                    val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.showSoftInput(this@with, InputMethodManager.SHOW_IMPLICIT)
                }
            }
            requestFocus()
        }
    }

    companion object {
        public val TAG = ChatBoxBottomSheetDialog::class.java.simpleName

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param callbackListener Parameter 1.
         * @param streamName Parameter 2.
         * @return A new instance of fragment ChatBoxBottomSheetDialog.
         */
        @JvmStatic
        fun newInstance(callbackListener: ChatBoxCallbackListener?, streamName: String?) =
                ChatBoxBottomSheetDialog().apply {
                    this.callbackListener = callbackListener
                    arguments = Bundle().apply {
                        putString(ARG_STREAM_NAME, streamName ?: "")
                    }
                }
    }
}

interface ChatBoxCallbackListener {
    fun onChatSend(message: String)
}