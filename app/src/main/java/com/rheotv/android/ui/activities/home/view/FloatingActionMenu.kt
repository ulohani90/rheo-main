package com.rheotv.android.ui.activities.home.view

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.R
import com.rheotv.android.databinding.LayoutFloatingActionMenuBinding
import com.rheotv.android.databinding.ListItemFloatingActionBinding
import com.rheotv.android.ui.base.BaseViewHolder
import com.rheotv.android.ui.customViews.bottomSheetMenu.OptionRequest
import com.rheotv.android.utils.recyclerdecorators.VerticalLinearItemDecoration
import com.rheotv.android.utils.ViewUtils
import com.rheotv.android.utils.getContextDrawable

class FloatingActionMenu(context: Context, priority: FloatingActionAdapter.FloatingActionMenuIntent) {

    private var mFloatingActionMenuPopupWindow: PopupWindow? = null
    private var mPopupViewDataBinding: LayoutFloatingActionMenuBinding? = null

    init {
        createPopupWindow(context, getPrioritizedList(context, priority))
    }

    private fun getPrioritizedList(context: Context, priority: FloatingActionAdapter.FloatingActionMenuIntent): List<OptionRequest> {
        return when (priority) {
            FloatingActionAdapter.FloatingActionMenuIntent.GoLive -> listOf(
                    OptionRequest(FloatingActionAdapter.FloatingActionMenuIntent.GoLive.actionId,
                            "Go Live", backGroundColor = Color.parseColor("#dd352e"))
                            .apply { drawable = context.getContextDrawable(R.drawable.ic_go_live) },
                    OptionRequest(FloatingActionAdapter.FloatingActionMenuIntent.UploadClip.actionId,
                            "Upload a Clip", backGroundColor = Color.parseColor("#00a160"))
                            .apply { drawable = context.getContextDrawable(R.drawable.avd_clips) },
                    OptionRequest(FloatingActionAdapter.FloatingActionMenuIntent.ShareStory.actionId,
                            "Share a story", backGroundColor = Color.parseColor("#438cfd"))
                            .apply { drawable = context.getContextDrawable(R.drawable.ic_share_story) })
            FloatingActionAdapter.FloatingActionMenuIntent.ShareStory -> listOf(
                    OptionRequest(FloatingActionAdapter.FloatingActionMenuIntent.ShareStory.actionId,
                            "Share a story", backGroundColor = Color.parseColor("#438cfd"))
                            .apply { drawable = context.getContextDrawable(R.drawable.ic_share_story) },
                    OptionRequest(FloatingActionAdapter.FloatingActionMenuIntent.UploadClip.actionId,
                            "Upload a Clip", backGroundColor = Color.parseColor("#00a160"))
                            .apply { drawable = context.getContextDrawable(R.drawable.avd_clips) },
                    OptionRequest(FloatingActionAdapter.FloatingActionMenuIntent.GoLive.actionId,
                            "Go Live", backGroundColor = Color.parseColor("#dd352e"))
                            .apply { drawable = context.getContextDrawable(R.drawable.ic_go_live) })
            FloatingActionAdapter.FloatingActionMenuIntent.UploadClip -> listOf(
                    OptionRequest(FloatingActionAdapter.FloatingActionMenuIntent.UploadClip.actionId,
                            "Upload a Clip", backGroundColor = Color.parseColor("#00a160"))
                            .apply { drawable = context.getContextDrawable(R.drawable.avd_clips) },
                    OptionRequest(FloatingActionAdapter.FloatingActionMenuIntent.GoLive.actionId,
                            "Go Live", backGroundColor = Color.parseColor("#dd352e"))
                            .apply { drawable = context.getContextDrawable(R.drawable.ic_go_live) },
                    OptionRequest(FloatingActionAdapter.FloatingActionMenuIntent.ShareStory.actionId,
                            "Share a story", backGroundColor = Color.parseColor("#438cfd"))
                            .apply { drawable = context.getContextDrawable(R.drawable.ic_share_story) })
        }
    }

    private fun createPopupWindow(context: Context, list: List<OptionRequest>) {
        mPopupViewDataBinding =
                DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.layout_floating_action_menu, null, false)
        mPopupViewDataBinding?.menuRecyclerList?.adapter = FloatingActionAdapter().also {
            it.submitList(list)
        }
        mPopupViewDataBinding?.menuRecyclerList?.addItemDecoration(VerticalLinearItemDecoration(ViewUtils.dpToPx(12)))
        mPopupViewDataBinding?.menuRecyclerList?.adapter?.notifyDataSetChanged()
        val lp = (mPopupViewDataBinding?.menuRecyclerList?.layoutParams as? ViewGroup.MarginLayoutParams)
                ?: ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT)
        lp.bottomMargin = ViewUtils.dpToPx(54)
        mPopupViewDataBinding?.menuRecyclerList?.layoutParams = lp
        mFloatingActionMenuPopupWindow = PopupWindow().apply {
            contentView = mPopupViewDataBinding?.root
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            isFocusable = true
            mPopupViewDataBinding?.root?.setOnClickListener { dismiss() }
        }
    }

    fun showPopup(context: Context, priority: FloatingActionAdapter.FloatingActionMenuIntent,
                  onItemSelected: ((Int) -> Unit)? = null, onDismiss: (() -> Unit)? = null) {
        (mPopupViewDataBinding?.menuRecyclerList?.adapter as? FloatingActionAdapter)?.setOnItemSelected {
            onItemSelected?.invoke(it)
            mFloatingActionMenuPopupWindow?.dismiss()
        }
        if (mFloatingActionMenuPopupWindow == null || mPopupViewDataBinding?.root == null)
            createPopupWindow(context, getPrioritizedList(context, priority))
        mFloatingActionMenuPopupWindow?.setOnDismissListener {
            onDismiss?.invoke()
        }
        mFloatingActionMenuPopupWindow?.showAtLocation(mPopupViewDataBinding?.root, Gravity.BOTTOM, 0, 0)
    }

    class FloatingActionAdapter : RecyclerView.Adapter<BaseViewHolder>() {

        private val mList: MutableList<OptionRequest> = mutableListOf()
        private var mOnItemSelected: ((Int) -> Unit)? = null
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            return FloatingActionMenuViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                    R.layout.list_item_floating_action, parent, false))
        }

        override fun getItemCount(): Int = mList.size

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) =
                holder.onBind(position)

        fun submitList(list: List<OptionRequest>) {
            mList.clear()
            mList.addAll(list)
            notifyDataSetChanged()
        }

        fun setOnItemSelected(onItemSelected: ((Int) -> Unit)? = null) {
            mOnItemSelected = onItemSelected
        }

        inner class FloatingActionMenuViewHolder(private val binding: ListItemFloatingActionBinding) : BaseViewHolder(binding.root) {
            override fun onBind(position: Int) {
                binding.menuOption = mList[position]
                itemView.setOnClickListener { mOnItemSelected?.invoke(mList[position].id) }

            }
        }

        sealed class FloatingActionMenuIntent {
            object ShareStory : FloatingActionMenuIntent() {
                override val actionId = 0x000
            }

            object UploadClip : FloatingActionMenuIntent() {
                override val actionId = 0x001
            }

            object GoLive : FloatingActionMenuIntent() {
                override val actionId = 0x002
            }

            abstract val actionId: Int
        }
    }
}


