package com.rheotv.android.ui.activities.player.activity

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat
import com.rheotv.android.databinding.MenuHeaderProfileListItemBinding
import com.rheotv.android.utils.CommonUtils
import io.reactivex.internal.operators.parallel.ParallelMap
import java.util.*

class ChatMenuOptionBottomSheet : PlayerMenuBottomSheet() {

    var chatMenuOptionData: ChatMenuOptionData? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mHeaderView = { option, root ->
            showUserBottomSheet(chatMenuOptionData?.username, chatMenuOptionData?.profilePic, option as? ListOption.Header, root)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun showUserBottomSheet(username: String?, profilePic: String?, header: ListOption.Header?, itemView: ViewGroup): View? {
        val listItemBinding: MenuHeaderProfileListItemBinding = MenuHeaderProfileListItemBinding.inflate(LayoutInflater.from(itemView.context), itemView, false)
        listItemBinding.imageUri = profilePic
        listItemBinding.username = username
        listItemBinding.loader.visibility = View.VISIBLE
        listItemBinding.followButton.setOnClickListener {
            if (CommonUtils.isUserLoggedin()) {
                chatMenuOptionData?.onFollowClick?.invoke(header?.followStatusString, header?.userId
                        ?: 0, username, object : FollowStatusCompleteListener {
                    override fun success() {
                        header?.followStatus = header?.followStatus == false
                        if (header?.followStatus == true) {
                            header?.followCount = header.followCount + 1
                        } else {
                            header?.followCount = (header?.followCount?.minus(1))
                                    ?.coerceAtLeast(0) ?: 0
                        }
                        listItemBinding.isFollowing = header?.followStatus
                        listItemBinding.followerCount.text = header?.followCountString
                        listItemBinding.executePendingBindings()
                    }

                    override fun error() {
                        Toast.makeText(listItemBinding.root.context, "Unable to " + header?.followStatusString + " " + username, Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                chatMenuOptionData?.onNotLoggedIn?.invoke()
            }
        }
        chatMenuOptionData?.onFollowStatusChange?.invoke(username, object : ApiCompleteListener {
            override fun updateProfileDataForBottomSheet(result: FollowResult?) {
                if (result is FollowResult.Success) {
                    val success = result.result
                    header?.userId = success.user.id
                    header?.followCount = success.followersCount
                    if (listItemBinding.loader.visibility == View.VISIBLE) {
                        header?.followStatus = success.isFollowed
                        listItemBinding.isFollowing = success.isFollowed
                    }
                    listItemBinding.followButton.visibility = View.VISIBLE
                    listItemBinding.followerCount.visibility = View.VISIBLE
                    listItemBinding.followerCount.text = success.followersCountString
                    listItemBinding.introTextView.text = success.intro
                    listItemBinding.introTextView.visibility = View.VISIBLE
                    listItemBinding.loader.visibility = View.GONE
                } else {
                    listItemBinding.followButton.visibility = View.INVISIBLE
                    listItemBinding.followerCount.visibility = View.GONE
                    listItemBinding.introTextView.visibility = View.GONE
                    listItemBinding.loader.visibility = View.GONE
                }
                listItemBinding.executePendingBindings()
            }
        }, object : FollowStatusListener {
            override fun followStatus(isFollowed: Boolean) {
                header?.followStatus = isFollowed
                listItemBinding.isFollowing = isFollowed
                listItemBinding.followButton.visibility = View.VISIBLE
                listItemBinding.executePendingBindings()
                listItemBinding.loader.visibility = View.GONE
            }
        })
        listItemBinding.executePendingBindings()
        return listItemBinding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        chatMenuOptionData?.onDismiss?.invoke()
    }

    companion object {
        const val TAG = "ChatMenuOptionBottomSheet"
        private const val ARG_KEY_LIST = "list"
        fun newInstance(list: ArrayList<ListOption>,
                        listener: ((option: ListOption) -> Unit)? = null): ChatMenuOptionBottomSheet = ChatMenuOptionBottomSheet().also {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ARG_KEY_LIST, list)
            it.arguments = bundle
            it.setClickListener(listener)
        }
    }
}

data class ChatMenuOptionData(
        var username: String?,
        var profilePic: String?,
        var authorName: String?,
        var onFollowStatusChange: ((String?, ApiCompleteListener?, FollowStatusListener?) -> Unit)?,
        var onFollowClick: ((String?, Int, String?, FollowStatusCompleteListener?) -> Unit)? = null,
        var onNotLoggedIn: (() -> Unit)? = null,
        var onDismiss: (() -> Unit)? = null
)

interface FollowStatusListener {
    fun followStatus(isFollowed: Boolean)
}