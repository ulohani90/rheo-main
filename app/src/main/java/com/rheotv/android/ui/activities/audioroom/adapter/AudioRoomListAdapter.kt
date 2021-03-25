package com.rheotv.android.ui.activities.audioroom.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.rheotv.android.R
import com.rheotv.android.databinding.FooterLoadingLayoutBinding
import com.rheotv.android.databinding.ListItemAudioRoomBinding
import com.rheotv.android.services.AudioRoomService
import com.rheotv.android.ui.activities.audioroom.model.AudioRoom
import com.rheotv.android.ui.base.BaseViewHolder
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.BindingUtils
import com.rheotv.android.utils.EventBusModel
import com.rheotv.android.utils.ViewUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.random.Random

const val VIEW_TYPE_ROOM = 0

class AudioRoomListAdapter(val itemClick: (AudioRoom, Int) -> Unit) : RecyclerView.Adapter<BaseViewHolder>() {
    private val list: MutableList<AudioRoom> = ArrayList()
    private val dataSet: MutableSet<String> = hashSetOf()
    var loading: Boolean = false

    fun submitList(list: List<AudioRoom>) {
        setShowLoading(false)
        val index = this.list.size
        var size = 0
        for (item in list) {
            if (!item.groupDetails?.id.isNullOrEmpty() &&
                    dataSet.add(item.groupDetails?.id ?: "")) {
                size++
                this.list.add(item)
            }
        }
        notifyItemRangeInserted(index, size)
    }

    fun clearList() {
        list.clear()
        dataSet.clear()
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRoomConnected(param: EventBusModel.AudioRoomConnected) {
        list.map {
            if (it.activeChatRooms?.chatRoomList?.get(0).equals(param.id ?: "")) {
                it.isConnected = true
                notifyItemChanged(list.indexOf(it))
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRoomDisconnected(param: EventBusModel.AudioRoomDisconnected) {
        list.map {
            if (it.activeChatRooms?.chatRoomList?.get(0).equals(param.id ?: "")) {
                it.isConnected = false
                notifyItemChanged(list.indexOf(it))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        if (viewType == AppConstants.VIEW_TYPE_LOADING_FOOTER)
            return FooterLoadingViewHolder(FooterLoadingLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        return AudioRoomHolder(ListItemAudioRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    fun setShowLoading(showLoading: Boolean) {
        if (loading != showLoading) {
            loading = showLoading
            if (loading)
                notifyItemInserted(list.size)
            else
                notifyItemRemoved(list.size)
        }
    }

    override fun getItemViewType(position: Int) =
            if (position == list.size && loading) AppConstants.VIEW_TYPE_LOADING_FOOTER else VIEW_TYPE_ROOM

    override fun getItemCount() =
            list.size + if (loading) 1 else 0

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class AudioRoomHolder(val binding: ListItemAudioRoomBinding) : BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {
            val item = list[position]
            if (item.activeChatRooms?.chatRoomList?.isEmpty() == false)
                item.isConnected = item.activeChatRooms?.chatRoomList?.get(0).equals(AudioRoomService.connectedRoomId ?: "")
            if (item.groupDetails?.id?.equals(AppConstants.FEMALE_ONLY_GROUP) == true)
                item.activeChatRooms?.totalActiveUsers = Random.nextInt(18, 21)
            binding.room = item
            setRoundImageUri(binding.thumbnailImageView,
                    item.groupDetails?.ownerDetails?.profileImageUrl,
                    item.groupDetails?.ownerDetails?.username)
            binding.root.setOnClickListener {
                itemClick.invoke(item, position)
            }

            binding.tagProtector.setOnClickListener {
                itemClick.invoke(item, position)
            }
        }

        fun setRoundImageUri(imageView: ImageView, uri: String?, text: String?) {
            var uri = uri
            try {
                Log.i("BindingUtils", "roundImageUri$uri")
                if (imageView.visibility == View.VISIBLE) {
                    if (text != null) {
                        if (uri == null || AppConstants.DEFAULT_AVATAR.equals(uri, ignoreCase = true)
                                || AppConstants.DEFAULT_PROFILE_PIC.equals(uri, ignoreCase = true)
                                || AppConstants.DEFAULT_AVATAR_V2.equals(uri, ignoreCase = true)) uri = ""
                        Glide.with(imageView.context)
                                .load(uri)
                                .override(ViewUtils.dpToPx(48))
                                .placeholder(BindingUtils.getDefaultTextDrawable(text))
                                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                                .error(BindingUtils.getDefaultTextDrawable(text))
                                .transition(DrawableTransitionOptions().crossFade())
                                .into(imageView)
                    } else {
                        Glide.with(imageView.context)
                                .load(uri)
                                .override(ViewUtils.dpToPx(48))
                                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                                .error(R.drawable.ic_login_white_outline_102dp)
                                .transition(DrawableTransitionOptions().crossFade())
                                .into(imageView)
                    }
                }
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }

    class FooterLoadingViewHolder internal constructor(binding: FooterLoadingLayoutBinding) : BaseViewHolder(binding.root) {
        override fun onBind(position: Int) {}
    }
}