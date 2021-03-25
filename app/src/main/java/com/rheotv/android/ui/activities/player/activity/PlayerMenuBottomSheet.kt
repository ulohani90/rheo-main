package com.rheotv.android.ui.activities.player.activity

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.*
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rheotv.android.R
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult
import com.rheotv.android.databinding.BottomSheetDialogFragmentBinding
import com.rheotv.android.utils.CommonUtils

open class PlayerMenuBottomSheet : BottomSheetDialogFragment() {

    private lateinit var mBinding: BottomSheetDialogFragmentBinding
    private var mListener: ((option: ListOption) -> Unit)? = null
    protected var mHeaderView: ((option: ListOption.Header, root: ViewGroup) -> View?)? = null

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = BottomSheetDialogFragmentBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adjustWindow(view)
        var listOptions: List<ListOption>? = null
        if (arguments?.containsKey(ARG_KEY_LIST) == true) {
            listOptions = arguments?.getParcelableArrayList(ARG_KEY_LIST)
        }

        mBinding.rvList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        mBinding.rvList.adapter = PlayerMenuAdapter().also {
            it.submitList(listOptions ?: listOf())
            it.setClickListener(mListener)
            it.setHeaderViewCallback(mHeaderView)
        }
    }

    open fun adjustWindow(view: View) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val dialog = dialog as? BottomSheetDialog
                val bottomSheet = dialog?.findViewById<FrameLayout?>(com.google.android.material.R.id.design_bottom_sheet)
                if (Resources.getSystem().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    val params: CoordinatorLayout.LayoutParams
                    if (bottomSheet != null) {
                        params = bottomSheet.layoutParams as CoordinatorLayout.LayoutParams
                        params.setMargins(220, 0, 220, 0)
                        bottomSheet.layoutParams = params
                        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                    }
                }
                val behavior: BottomSheetBehavior<*>
                if (bottomSheet != null) {
                    behavior = BottomSheetBehavior.from(bottomSheet)
                    behavior?.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        })
    }

    fun setClickListener(listener: ((option: ListOption) -> Unit)?) {
        mListener = {
            dismiss()
            listener?.invoke(it)
        }
    }

    fun setHeaderViewCallback(listener: ((option: ListOption.Header, root: ViewGroup?) -> View?)?) {
        mHeaderView = listener
    }

    companion object {
        const val TAG = "PlayerMenuBottomSheet"
        private const val ARG_KEY_LIST = "list"
        fun newInstance(list: ArrayList<ListOption>,
                        listener: ((option: ListOption) -> Unit)? = null,
                        headerView: ((option: ListOption.Header, root: ViewGroup?) -> View?)? = null): PlayerMenuBottomSheet = PlayerMenuBottomSheet().also {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ARG_KEY_LIST, list)
            it.arguments = bundle
            it.setClickListener(listener)
            it.setHeaderViewCallback(headerView)
        }
    }
}

sealed class ListOption : Parcelable {
    data class Header(val id: Int) : ListOption() {
        constructor(parcel: Parcel) : this(parcel.readInt()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
        }

        override fun describeContents(): Int {
            return 0
        }

        var followStatus: Boolean = false
        var userId: Int = -1
        var followCount: Int = 0

        val followCountString: String
            get() = if (followCount == 0) "0 Follower" else CommonUtils.getFormattedNumberString(followCount, "Follower")

        val followStatusString: String
            get() = if (!followStatus) "follow" else "unfollow"

        companion object CREATOR : Parcelable.Creator<Header> {
            override fun createFromParcel(parcel: Parcel): Header {
                return Header(parcel)
            }

            override fun newArray(size: Int): Array<Header?> {
                return arrayOfNulls(size)
            }
        }

    }

    data class Item(val id: Int, val text: String, val imageResourceId: Int = -1, val imageResource: Drawable? = null) : ListOption() {
        constructor(parcel: Parcel) : this(
                parcel.readInt(),
                parcel.readString() ?: "",
                parcel.readValue(Int::class.java.classLoader) as Int,
                BitmapDrawable(parcel.readParcelable(Item::class.java.classLoader) as? Bitmap) as? Drawable
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeString(text)
            parcel.writeValue(imageResourceId)
            parcel.writeParcelable(((imageResource) as? BitmapDrawable)?.bitmap, flags)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Item> {
            override fun createFromParcel(parcel: Parcel): Item {
                return Item(parcel)
            }

            override fun newArray(size: Int): Array<Item?> {
                return arrayOfNulls(size)
            }
        }
    }
}

sealed class FollowResult {
    data class Error(val throwable: Throwable?) : FollowResult()
    data class Success(val result: ProfileResult) : FollowResult()
}

interface ApiCompleteListener {
    fun updateProfileDataForBottomSheet(result: FollowResult?)
}

interface FollowStatusCompleteListener {
    fun success()
    fun error()
}