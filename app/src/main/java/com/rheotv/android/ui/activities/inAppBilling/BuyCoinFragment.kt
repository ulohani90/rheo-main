package com.rheotv.android.ui.activities.inAppBilling

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rheotv.android.R
import com.rheotv.android.databinding.FragmentBuyCoinBinding
import com.rheotv.android.utils.segmentTracker.SegmentTracker

/**
 * A simple [Fragment] subclass.
 * Use the [BuyCoinFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BuyCoinFragment : BottomSheetDialogFragment() {
    private var callbackListener: BuyCoinCallbackListener? = null
    private lateinit var binding: FragmentBuyCoinBinding

    override fun getTheme() = R.style.BottomSheetDialogTheme

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        DataBindingUtil.inflate<FragmentBuyCoinBinding>(
                inflater,
                R.layout.fragment_buy_coin, container, false
        ).also {
            binding = it
        }.run {
            return this.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adjustWindow(view)
        binding.continueButton.setOnClickListener {
            callbackListener?.onBuyClicked()
            dismiss()
        }
    }

    private fun adjustWindow(view: View) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val dialog = dialog as BottomSheetDialog?
                val bottomSheet = dialog!!.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    val params: CoordinatorLayout.LayoutParams
                    if (bottomSheet != null) {
                        params = bottomSheet.layoutParams as CoordinatorLayout.LayoutParams
                        params.setMargins(220, 0, 220, 0)
                        bottomSheet.layoutParams = params
                        if (dialog.window != null) {
                            dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                        }
                    }
                }
                val behavior: BottomSheetBehavior<*>
                if (bottomSheet != null) {
                    behavior = BottomSheetBehavior.from(bottomSheet)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        })
    }

    companion object {
        val TAG = BuyCoinFragment::class.java.simpleName

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param listener Parameter 1.
         * @return A new instance of fragment BuyCoinFragment.
         */
        @JvmStatic
        fun newInstance(listener: BuyCoinCallbackListener) =
                BuyCoinFragment().apply {
                    callbackListener = listener
                }

    }
}

interface BuyCoinCallbackListener {
    fun onBuyClicked()
}