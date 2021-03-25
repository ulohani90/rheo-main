package com.rheotv.android.helpers.grpc

import android.util.Log
import android.widget.Toast
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.isNullOrEmptyOrBlank
import goChat.Services

abstract class IncomingChatListener {

    /**
     * local == false
     * server == true
     * server && username == me -> return
     *
     */
    fun parseMessage(message: Services.ChatMessage, fromServer: Boolean) {
        if (message.sender.isNullOrEmptyOrBlank() && message.message.isNullOrEmptyOrBlank()) return
        val isMe = message.sender == CommonUtils.getUserName()
        if (message.sender == null || (fromServer && isMe) || (isMe && (message.msgType.equals(AppConstants.IMAGE, true) || message.msgType.equals(AppConstants.VIDEO, true)))) return
        when {
            message.msgType == AppConstants.MSG_SCORE -> onScoreUpdate(message)
            message.msgType == AppConstants.MSG_PIN -> if (message.message.isEmpty()) onPinned(null) else onPinned(message)
            message.msgType == AppConstants.MSG_DYNAMIC_PLAYER_ACTIONS -> onDynamicAction(message)
            message.msgType == AppConstants.MSG_TYPE_DELETED && !isMe -> onDelete(message)
            message.msgType == AppConstants.MSG_TYPE_BLOCKED && !isMe -> onUserBlocked(message)
            message.message == AppConstants.MSG_HEART -> onLiked(message)
            else -> onReceived(message, isMe)
        }
    }

    open fun onReceived(chatMessage: Services.ChatMessage, isMine: Boolean) = Unit

    open fun onDelete(chatMessage: Services.ChatMessage) = Unit

    open fun onUserBlocked(chatMessage: Services.ChatMessage) = Unit

    open fun onPinned(chatMessage: Services.ChatMessage?) = Unit

    open fun onScoreUpdate(chatMessage: Services.ChatMessage) = Unit

    open fun onDynamicAction(chatMessage: Services.ChatMessage) = Unit

    open fun onLiked(chatMessage: Services.ChatMessage) = Unit

    open fun waitAndReconnect() = Unit

    open fun showToast(message: String) = Unit

    open fun onConnectionComplete() = Unit

    open fun updateGroupTotalCount(count: Double) = Unit

    open fun retryGroupConnect() = Unit
}