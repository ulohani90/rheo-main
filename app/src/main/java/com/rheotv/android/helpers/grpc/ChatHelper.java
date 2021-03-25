package com.rheotv.android.helpers.grpc;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.rheotv.android.BuildConfig;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.LocalCommentMessageCallback;
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.score.ScoreboardResponse;
import com.rheotv.android.ui.activities.player.activity.ChatHelperCallbacks;
import com.rheotv.android.ui.activities.player.activity.PlayerActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.ChatLogs;
import com.rheotv.android.utils.CommonUtils;

import java.lang.ref.WeakReference;

import goChat.Services;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

public class ChatHelper {

    ManagedChannel channel;
    StreamObserver<Services.ChatMessage> streamObserverServer;
    StreamObserver<Services.ChatMessage> allGroupStreamObserverServer;

    int chatState;

    public static int CHAT_STATE_CONNECTING = 1;
    public static int CHAT_STATE_CONNECTED = 2;
    public static int CHAT_STATE_DISCONNECTED = 3;

    public static ChatHelper getInstance(Context context) {
        return new ChatHelper();
    }

    public void setChatState(int chatState) {
        this.chatState = chatState;
    }

    public int getChatState() {
        return chatState;
    }

    public static ChatHelper getInstance() {
        return new ChatHelper();
    }

    public ChatHelper() {

    }

    public void setPostChatJoinTask(PlayerActivity playerActivity, Result currentPost) {
//        Log.i(getClass().getName(), "setPostChatJoinTask called  " + playerActivity.isConnectionRequestMade + " and " + new Gson().toJson(currentPost));
        Log.i("Chat_Helper", "Join task");
        final boolean[] isStreamInfoSent = {false};
        StreamObserver streamObserver = new StreamObserver<Services.ChatMessage>() {

            @Override
            public void onNext(Services.ChatMessage value) {
                Log.i("Chat_Helper", "On Next");
                Log.i(RheoTvApp.TAG, "On Next called " + value.getMessage());
                if (!isStreamInfoSent[0]) {
                    String userName = CommonUtils.getUserName();
                    Services.ChatMessage chatMessage = Services.ChatMessage.newBuilder()
                            .setSender(userName)
                            .setProfilePic(CommonUtils.getUserProfilePic())
                            .setMessage("")
                            .setReceiver("postId")
                            .setDeviceId(CommonUtils.getDevId())
                            .setVersionCode(Integer.toString(BuildConfig.VERSION_CODE))
                            .build();
                    Log.i(RheoTvApp.TAG, "streamObserverServer 1" + chatMessage);
                    streamObserverServer.onNext(chatMessage);
                    Log.i(RheoTvApp.TAG, "streamObserverServer 2" + chatMessage);
                    isStreamInfoSent[0] = true;
                    if (currentPost.getIsLive()) {
//                        getTotal(postId, playerActivity);
                    }
                } else {
                    Log.i(RheoTvApp.TAG, "Message Received is " + value.getMessage());
                    playerActivity.updateNewChat(value);
                    boardCastMessage(value);
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                Log.i("Chat_Helper", "On Error");
                playerActivity.waitAndRequestReconnection();
                Log.d(getClass().getSimpleName(), "setPostChatJoin_mirage" + "onError throwable t" + t.toString());
            }

            @Override
            public void onCompleted() {
                Log.i("Chat_Helper", "On Completed");
                Log.d("setPostChatJoin_mirage", "onCompleted");
            }
        };

        if (currentPost.getId() == null) {
            return;
        }
        streamObserverServer = AsyncStubHelper.INSTANCE.getGlobalStub().routeChat(streamObserver);
    }

    private Gson gson = new Gson();

    private void boardCastMessage(Services.ChatMessage chatMessage) {
        if (chatMessage.getMsgType().equalsIgnoreCase(AppConstants.MSG_SCORE)) {
            ScoreboardResponse scoreboardResponse = gson.fromJson(chatMessage.getMessage(), ScoreboardResponse.class);
            Intent intent = new Intent(AppConstants.MSG_SCORE);
            intent.putExtra(AppConstants.ARG_SCORECARD_TEAMS, scoreboardResponse);
            LocalBroadcastManager.getInstance(RheoTvApp.getNonUiContext()).sendBroadcast(intent);
        }
    }

    public void setPostChatJoinTask(WeakReference<ChatHelperCallbacks> callbacks, String postId, boolean isLive, String username) {
//        Log.i(getClass().getName(), "setPostChatJoinTask called  " + playerActivity.isConnectionRequestMade + " and " + new Gson().toJson(currentPost));
        Log.i("Chat_Helper", "Join task");
        setChatState(CHAT_STATE_CONNECTING);
        final boolean[] isStreamInfoSent = {false};
        StreamObserver streamObserver = new StreamObserver<Services.ChatMessage>() {

            @Override
            public void onNext(Services.ChatMessage value) {
                Log.i("Chat_Helper", "On Next called");
                Log.i(RheoTvApp.TAG, "On Next called " + value.getMessage());
                if (!isStreamInfoSent[0]) {
                    String userName = CommonUtils.getUserName();
                    Services.ChatMessage chatMessage = Services.ChatMessage.newBuilder()
                            .setSender(userName)
                            .setProfilePic(CommonUtils.getUserProfilePic())
                            .setMessage("")
                            .setReceiver(postId)
                            .setDeviceId(CommonUtils.getDevId())
                            .setVersionCode(Integer.toString(BuildConfig.VERSION_CODE))
                            .build();
                    Log.i(RheoTvApp.TAG, "streamObserverServer 1" + chatMessage);
                    streamObserverServer.onNext(chatMessage);
                    Log.i(RheoTvApp.TAG, "streamObserverServer 2" + chatMessage);
                    isStreamInfoSent[0] = true;
                    if (isLive) {
                        getTotal(postId, callbacks.get());
                    }
                } else {
                    setChatState(CHAT_STATE_CONNECTED);
                    Log.i(RheoTvApp.TAG, "Message Received is " + value.toString());

                    if (callbacks != null) {
                        callbacks.get().onMessageSend(value);
                        if (!value.getMsgType().equalsIgnoreCase(AppConstants.MSG_DYNAMIC_PLAYER_ACTIONS)) {
                            ChatLogs.getInstance().addEventToFile("Message Received::" + value.getMessage(), System.currentTimeMillis(), username);
                            callbacks.get().showToast("Message received::" + value.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                Log.i("Chat_Helper", "On Error");
                setChatState(CHAT_STATE_DISCONNECTED);
                if (callbacks != null && callbacks.get() != null) {
                    ChatLogs.getInstance().addEventToFile("Chat Disconnected --> " + (t != null ? t.getMessage() : ""), System.currentTimeMillis(), username);
                    callbacks.get().showToast("Chat Disconnected --> " + t != null ? t.getMessage() : "");
                    closeConnection(postId, callbacks.get(), username, false);
                    callbacks.get().waitAndReconnect();
                }
                Log.d(getClass().getSimpleName(), "setPostChatJoin_mirage" + "onError throwable t" + t.toString());
            }

            @Override
            public void onCompleted() {
                Log.i("Chat_Helper", "On Completed");
                setChatState(CHAT_STATE_DISCONNECTED);
                if (callbacks != null && callbacks.get() != null) {
                    ChatLogs.getInstance().addEventToFile("Chat Disconnected on Complete", System.currentTimeMillis(), username);
                    callbacks.get().showToast("Chat Disconnected");
                    callbacks.get().onConnectionComplete();
                }
                Log.d("setPostChatJoin_mirage", "onCompleted");
            }
        };

        if (postId == null)
            return;

        streamObserverServer = AsyncStubHelper.INSTANCE.getGlobalStub().routeChat(streamObserver);
        ChatLogs.getInstance().addEventToFile("Chat connection initiated", System.currentTimeMillis(), username);
        if (callbacks != null)
            callbacks.get().showToast("Chat Connection Initiated");
    }


    private String groupId;

    public void connectToGroup(String groupId, ChatHelperCallbacks callbacks) {
//        Log.i(getClass().getName(), "setPostChatJoinTask called  " + playerActivity.isConnectionRequestMade + " and " + new Gson().toJson(currentPost));
        Log.i("Chat_Helper_group", "Join task");
        this.groupId = groupId;
        final boolean[] isStreamInfoSent = {false};
        StreamObserver streamObserver = new StreamObserver<Services.ChatMessage>() {

            @Override
            public void onNext(Services.ChatMessage value) {
                Log.i(RheoTvApp.TAG, "On Next called " + value.getMessage());
                if (!isStreamInfoSent[0]) {
                    String userName = CommonUtils.getUserName();
                    Services.ChatMessage chatMessage = Services.ChatMessage.newBuilder()
                            .setSender(userName)
                            .setProfilePic(CommonUtils.getUserProfilePic())
                            .setMessage("")
                            .setReceiver(groupId)
                            .setDeviceId(CommonUtils.getDevId())
                            .setVersionCode(Integer.toString(BuildConfig.VERSION_CODE))
                            .build();
                    Log.i(RheoTvApp.TAG, "streamObserverServer 1" + chatMessage);
                    allGroupStreamObserverServer.onNext(chatMessage);
                    Log.i(RheoTvApp.TAG, "streamObserverServer 2" + chatMessage);
                    isStreamInfoSent[0] = true;
                } else {
                    Log.i(RheoTvApp.TAG, "Message Received is " + value.toString());
                    if (callbacks != null)
                        callbacks.onMessageSend(value);
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                if (callbacks != null)
                    callbacks.waitAndReconnect();
                Log.d(getClass().getSimpleName(), "setPostChatJoin_mirage" + "onError throwable t" + t.toString());
            }

            @Override
            public void onCompleted() {
                Log.d("setPostChatJoin_mirage", "onCompleted");
            }
        };

        if (groupId == null)
            return;
        allGroupStreamObserverServer = AsyncStubHelper.INSTANCE.getGlobalStub().routeChat(streamObserver);
    }

    public void getTotal(String postId, ChatHelperCallbacks callbacks) {

        StreamObserver streamObserver = new StreamObserver<Services.ClientCount>() {
            @Override
            public void onNext(Services.ClientCount value) {
                if (callbacks != null) callbacks.updateLiveCount(value.getCount());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                Log.d(getClass().getSimpleName(), "mirage" + "onError throwable t " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                Log.d("mirage", "onCompleted");
                if (callbacks != null) callbacks.setUpViewersRequest();
            }
        };
        if (postId == null || postId.isEmpty()) {
            return;
        }
        Log.i(getClass().getName(), "player_connection: " + postId);
        AsyncStubHelper.INSTANCE.getGlobalStub().getGroupClientCount(Services.GroupInfo.newBuilder()
                .setClient(CommonUtils.getDevId())
                .setGroupName(postId)
                .build(), streamObserver);
    }

    public boolean sendMessage(PlayerActivity playerActivity, String message, String postId) {
        try {
            Log.d(RheoTvApp.TAG, "sending message");
            String userName = CommonUtils.getUserName();
            long time = System.currentTimeMillis();

            Services.ChatMessage chatMessage = Services.ChatMessage.newBuilder()
                    .setSender(userName)
                    .setProfilePic(CommonUtils.getUserProfilePic())
                    .setMessage(message)
                    .setReceiver(postId)
                    .setDeviceId(CommonUtils.getDevId())
                    .build();
            streamObserverServer.onNext(chatMessage);
            playerActivity.updateNewChat(chatMessage);
        } catch (Exception e) {
            Log.d(RheoTvApp.TAG, "something went wrong while sending message");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendMessage(String message, String postId, ChatHelperCallbacks callbacks) {
        return sendMessage(message, postId, callbacks, null);
    }

    public boolean sendMessage(String message, String postId, ChatHelperCallbacks callbacks, LocalCommentMessageCallback localCommentMessageCallback) {
        try {

            Log.d(RheoTvApp.TAG, "sending message");
            String userName = CommonUtils.getUserName();
            long time = System.currentTimeMillis();

            Services.ChatMessage chatMessage = Services.ChatMessage.newBuilder()
                    .setSender(userName)
                    .setProfilePic(CommonUtils.getUserProfilePic())
                    .setMessage(message)
                    .setReceiver(postId)
                    .setIsContentModerator(CommonUtils.isContentModerator())
                    .setDeviceId(CommonUtils.getDevId())
                    .build();
            if (localCommentMessageCallback != null) {
                localCommentMessageCallback.ownMessageSent(chatMessage);
            } else {
                FirebaseCrashlytics.getInstance().log("Local callback is null!!");
            }
            streamObserverServer.onNext(chatMessage);

            if (callbacks != null)
                callbacks.onMessageSend(chatMessage);
        } catch (Exception e) {
            Log.d(RheoTvApp.TAG, "something went wrong while sending message");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendMessage(String message, String messageType, String postId, ChatHelperCallbacks callbacks) {
        return sendMessage(message, messageType, postId, callbacks, null);
    }

    public boolean sendMessage(String message, String messageType, String postId, ChatHelperCallbacks callbacks, LocalCommentMessageCallback localCommentMessageCallback) {
        try {
            Log.d(RheoTvApp.TAG, "sending message " + messageType);
            String userName = CommonUtils.getUserName();
            long time = System.currentTimeMillis();

            Services.ChatMessage chatMessage = Services.ChatMessage.newBuilder()
                    .setSender(userName)
                    .setProfilePic(CommonUtils.getUserProfilePic())
                    .setMessage(message)
                    .setReceiver(postId)
                    .setMsgType(messageType)
                    .setIsContentModerator(CommonUtils.isContentModerator())
                    .setDeviceId(CommonUtils.getDevId())
                    .build();
            if (localCommentMessageCallback != null) {
                localCommentMessageCallback.ownMessageSent(chatMessage);
            } else {
                FirebaseCrashlytics.getInstance().log("Local callback is null!!");
            }
            streamObserverServer.onNext(chatMessage);
            if (callbacks != null)
                callbacks.onMessageSend(chatMessage);
        } catch (Exception e) {
            Log.d(RheoTvApp.TAG, "something went wrong while sending message");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean pinMessage(CommentChat comment, String type, String postId, ChatHelperCallbacks callbacks) {
        try {
            Log.d(RheoTvApp.TAG, "sending message");
            String userName = CommonUtils.getUserName();
            long time = System.currentTimeMillis();

            Services.ChatMessage chatMessage = Services.ChatMessage.newBuilder()
                    .setSender(userName)
                    .setMessage(comment != null ? comment.getMessage() : "")
                    .setReceiver(postId)
                    .setMsgType(type)
                    .setSender(comment != null ? comment.getUsername() : "")
                    .setProfilePic(comment != null ? comment.getProfile_pic() : "")
                    .setDeviceId(CommonUtils.getDevId())
                    .setIsContentModerator(CommonUtils.isContentModerator())
                    .build();
            streamObserverServer.onNext(chatMessage);
            if (callbacks != null)
                callbacks.onMessageSend(chatMessage);
        } catch (Exception e) {
            Log.d(RheoTvApp.TAG, "something went wrong while sending message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendDeletedMessage(PlayerActivity playerActivity, String message, String senderName, String postId, String messageType) {
        try {
            Log.d(RheoTvApp.TAG, "sending message");

            long time = System.currentTimeMillis();

            Services.ChatMessage chatMessage = Services.ChatMessage.newBuilder()
                    .setSender(senderName)
                    .setProfilePic(CommonUtils.getUserProfilePic())
                    .setMessage(message)
                    .setReceiver(postId)
                    .setDeviceId(CommonUtils.getDevId())
                    .setMsgType(messageType)
                    .build();
            streamObserverServer.onNext(chatMessage);
        } catch (Exception e) {
            Log.d(RheoTvApp.TAG, "something went wrong while sending message");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendDeletedMessage(String message, String senderName, String postId, String messageType, ChatHelperCallbacks callbacks) {
        try {
            Log.d(RheoTvApp.TAG, "sending message");

            long time = System.currentTimeMillis();

            Services.ChatMessage chatMessage = Services.ChatMessage.newBuilder()
                    .setSender(senderName)
                    .setProfilePic(CommonUtils.getUserProfilePic())
                    .setMessage(message)
                    .setReceiver(postId)
                    .setDeviceId(CommonUtils.getDevId())
                    .setMsgType(messageType)
                    .setIsContentModerator(CommonUtils.isContentModerator())
                    .build();
            streamObserverServer.onNext(chatMessage);
            if (callbacks != null)
                callbacks.onMessageDelete(chatMessage);

        } catch (Exception e) {
            Log.d(RheoTvApp.TAG, "something went wrong while sending message");
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public boolean closeGroupConnection() {
        try {
            Log.i(getClass().getSimpleName(), "closingConnection");
            String userName = CommonUtils.getUserName();

            StreamObserver streamObserver = new StreamObserver<Services.Empty>() {
                @Override
                public void onNext(Services.Empty value) {
                    Log.d(getClass().getSimpleName(), "closeConnection on next disconnect");
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                    Log.d(getClass().getSimpleName(), "closeConnection onError throwable t" + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    Log.d(getClass().getSimpleName(), "closeConnection onCompleted and disconnected");
                }
            };

            AsyncStubHelper.INSTANCE.getGlobalStub().leaveRoom(Services.GroupInfo.newBuilder()
                    .setClient(userName)
                    .setGroupName(groupId)
                    .setDeviceId(CommonUtils.getDevId())
                    .build(), streamObserver);
            allGroupStreamObserverServer.onCompleted();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(RheoTvApp.TAG, "something went wrong while closing connection.");
            return false;
        }
    }

    public boolean closeConnection(String postId, ChatHelperCallbacks callbacks, String postUsername, boolean shouldReconnectOnCompletion) {
        try {
            Log.i(getClass().getSimpleName(), "closingConnection");
            ChatLogs.getInstance().addEventToFile("Closing connection", System.currentTimeMillis(), postUsername);
            String userName = CommonUtils.getUserName();

            StreamObserver streamObserver = new StreamObserver<Services.Empty>() {
                @Override
                public void onNext(Services.Empty value) {
                    Log.d(getClass().getSimpleName(), "closeConnection on next disconnect");
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                    setChatState(CHAT_STATE_DISCONNECTED);
                    ChatLogs.getInstance().addEventToFile("Error on closing connection -->" + t.getMessage(), System.currentTimeMillis(), postUsername);
                    String userName = CommonUtils.getUserName();
                    if (callbacks != null)
                        callbacks.showToast("Connection Closed --> " + t.getMessage());
                    Log.d(getClass().getSimpleName(), "closeConnection onError throwable t" + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    setChatState(CHAT_STATE_DISCONNECTED);
                    if (callbacks != null) {
                        ChatLogs.getInstance().addEventToFile("Connection Closed", System.currentTimeMillis(), postUsername);
                        callbacks.showToast("Connection Closed");
                        if (shouldReconnectOnCompletion)
                            callbacks.onConnectionComplete();
                    }
                    Log.d(getClass().getSimpleName(), "closeConnection onCompleted and disconnected");
                }
            };

            if (postId == null)
                return false;
            AsyncStubHelper.INSTANCE.getGlobalStub().leaveRoom(Services.GroupInfo.newBuilder()
                    .setClient(userName)
                    .setGroupName("postId")
                    .setDeviceId(CommonUtils.getDevId())
                    .build(), streamObserver);
            if (streamObserverServer != null) {
                streamObserverServer.onCompleted();
                streamObserverServer = null;
            }

            return true;
        } catch (Exception e) {
            setChatState(CHAT_STATE_DISCONNECTED);
            e.printStackTrace();
            ChatLogs.getInstance().addEventToFile("Exception in closing connection --> " + e.getLocalizedMessage(), System.currentTimeMillis(), postUsername);
            Log.d(RheoTvApp.TAG, "something went wrong while closing connection.");
            return false;
        }
    }
}
