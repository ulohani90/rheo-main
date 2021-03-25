package com.rheotv.android.helpers.grpc

import android.util.Log
import com.rheotv.android.BuildConfig
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.CommonUtils
import goChat.Services
import goChat.Services.ClientCount
import io.grpc.stub.StreamObserver
import java.util.concurrent.atomic.AtomicBoolean

class GrpcConnectionManager {
    private val TAG: String = javaClass.simpleName
    private var streamObserverServer: StreamObserver<Services.ChatMessage>? = null
    private var groupId: String? = null
    val isConnectChatRequestMade = AtomicBoolean()

    fun connectToGroup(
            groupId: String?,
            listener: IncomingChatListener?,
            deviceId: String? = CommonUtils.getDevId(),
    ) {
        Log.i(TAG, "Join task - $groupId deviceId - $deviceId")
        if (isConnectChatRequestMade.get()) return
        isConnectChatRequestMade.set(true)
        this.groupId = groupId
        val isStreamInfoSent = booleanArrayOf(false)
        val streamObserver = object : StreamObserver<Services.ChatMessage> {

            override fun onNext(value: Services.ChatMessage) {
                Log.i(TAG, "On Next called " + value.message)
                if (!isStreamInfoSent[0]) {
                    val userName = CommonUtils.getUserName()
                    val chatMessage = Services.ChatMessage.newBuilder()
                            .setSender(userName)
                            .setProfilePic(CommonUtils.getUserProfilePic())
                            .setMessage("")
                            .setReceiver(groupId)
                            .setDeviceId(deviceId)
                            .setVersionCode(BuildConfig.VERSION_CODE.toString())
                            .build()
                    Log.i(TAG, "streamObserverServer 1$chatMessage")
                    streamObserverServer?.onNext(chatMessage)
                    Log.i(TAG, "streamObserverServer 2$chatMessage")
                    isStreamInfoSent[0] = true
                } else {
                    Log.i(TAG, "Message Received is $value")
                    listener?.parseMessage(value, true)
                }
//                CoroutineScope(Dispatchers.Main).launch {
//                    RheoTvApp.getNonUiContext()?.showToast("chat-server group-id --> $groupId  ---> onNext")
//                }
            }

            override fun onError(t: Throwable) {
                isConnectChatRequestMade.set(false)
//                CoroutineScope(Dispatchers.Main).launch {
//                    RheoTvApp.getNonUiContext()?.showToast("chat-server group-id --> $groupId  ---> onError")
//                }
                t.printStackTrace()
                closeGroupConnection(deviceId)
                listener?.waitAndReconnect()
                Log.d(TAG, "setPostChatJoin_mirageonError throwable t$t")
            }

            override fun onCompleted() {
                Log.d(TAG, "onCompleted")
//                CoroutineScope(Dispatchers.Main).launch {
//                    RheoTvApp.getNonUiContext()?.showToast("chat-server group-id --> $groupId  ---> onComplete")
//                }
                isConnectChatRequestMade.set(false)
                listener?.onConnectionComplete()
            }
        }

        if (groupId == null)
            return
        streamObserverServer = AsyncStubHelper.getGlobalStub().routeChat(streamObserver)
    }

    fun sendMessage(postId: String?, comment: CommentChat?, listener: IncomingChatListener?, type: String? = null): Boolean {
        try {
            Log.d(TAG, "sending message: $postId")
            val messageBuilder = Services.ChatMessage.newBuilder()
                    .setSender(comment?.username ?: "")
                    .setProfilePic(comment?.profile_pic ?: "")
                    .setMessage(comment?.message ?: "")
                    .setDeviceId(CommonUtils.getDevId())
                    .setMsgType(comment?.messageType ?: type ?: "")
            if (postId != null) messageBuilder.receiver = postId
            val message = messageBuilder.build()
            streamObserverServer?.onNext(message)
            listener?.parseMessage(message, false)
        } catch (e: Exception) {
            Log.d(TAG, "something went wrong while sending message ${e.message}")
            e.printStackTrace()
            return false
        }

        return true
    }

    fun closeGroupConnection(deviceId: String? = CommonUtils.getDevId()): Boolean {
        try {
            Log.i(javaClass.simpleName, "closingConnection - $groupId deviceId - $deviceId")
            val userName = CommonUtils.getUserName()

            val streamObserver = object : StreamObserver<Services.Empty> {
                override fun onNext(value: Services.Empty) {
                    Log.d(TAG, "closeConnection on next disconnect")
                }

                override fun onError(t: Throwable) {
                    t.printStackTrace()
                    Log.d(TAG, "closeConnection onError throwable t" + t.message)
                }

                override fun onCompleted() {
                    Log.d(TAG, "closeConnection onCompleted and disconnected")
                }
            }

            AsyncStubHelper.getGlobalStub().leaveRoom(Services.GroupInfo.newBuilder()
                    .setClient(userName)
                    .setGroupName(groupId)
                    .setDeviceId(deviceId)
                    .build(), streamObserver)
            streamObserverServer?.onCompleted()
            isConnectChatRequestMade.set(false)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "something went wrong while closing connection.")
            return false
        }
    }

    fun getGroupTotalCount(postId: String?, listener: IncomingChatListener?) {
        val streamObserver = object : StreamObserver<ClientCount> {
            override fun onNext(value: ClientCount) {
                listener?.updateGroupTotalCount(value.count?.toDouble() ?: 0.toDouble())
//                CoroutineScope(Dispatchers.Main).launch {
//                    RheoTvApp.getNonUiContext()?.showToast("total-count group-id --> $postId  ---> onNext")
//                }
            }

            override fun onError(t: Throwable) {
                t.printStackTrace()
                listener?.retryGroupConnect()
//                CoroutineScope(Dispatchers.Main).launch {
//                    RheoTvApp.getNonUiContext()?.showToast("total-count group-id --> $postId  ---> onError")
//                }
                Log.d(javaClass.simpleName, "mirage" + "onError throwable t " + t.message)
            }

            override fun onCompleted() {
                Log.d("mirage", "onCompleted")
//                CoroutineScope(Dispatchers.Main).launch {
//                    RheoTvApp.getNonUiContext()?.showToast("total-count group-id --> $postId  ---> onComplete")
//                }
                listener?.retryGroupConnect()
            }
        }
        if (postId == null || postId.isEmpty()) {
            return
        }
        Log.i(javaClass.name, "player_connection: $postId")
        AsyncStubHelper.getGlobalStub()
                .getGroupClientCount(Services.GroupInfo.newBuilder()
                        .setClient(CommonUtils.getDevId())
                        .setGroupName(postId)
                        .build(), streamObserver)
    }
}