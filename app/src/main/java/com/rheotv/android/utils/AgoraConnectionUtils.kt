package com.rheotv.android.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.graphics.Rect
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import android.view.SurfaceView
import com.rheotv.android.R
import com.rheotv.android.services.AudioRoomService
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.models.UserInfo
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

class AgoraConnectionUtils {

    private var mRtcEngine: RtcEngine? = null
    var onUserJoined: ((uid: Int) -> Unit)? = null
    var onVideoCallJoined: ((uid: Int, width: Int, height: Int, elapsed: Int) -> Unit)? = null
    var onCallLeft: ((uid: Int) -> Unit)? = null
    var onCallConnected: (() -> Unit)? = null
    var onCallDisconnected: (() -> Unit)? = null
    var onSpeakerIndicate: ((speakers: Array<out IRtcEngineEventHandler.AudioVolumeInfo>?) -> Unit)? = null
    private var mLocalView: SurfaceView? = null
    private var mRemoteView: SurfaceView? = null
    var onFirstUser: (() -> Unit)? = null
    var agoraAccessToken = ""
    var agoraChannelId = ""
    var activeUserCount = 0

    var onUserMuteAudio: ((Int, Boolean) -> Unit)? = null
    var onUserCount: ((Int) -> Unit)? = null

    private val mIRtcAudioEventHandler = object : IRtcEngineEventHandler() {

        /**
         * Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
         *
         * There are two reasons for users to become offline:
         *
         * Leave the channel: When the user/host leaves the channel, the user/host sends a
         * goodbye message. When this message is received, the SDK determines that the
         * user/host leaves the channel.
         *
         * Drop offline: When no data packet of the user or host is received for a certain
         * period of time (20 seconds for the communication profile, and more for the live
         * broadcast profile), the SDK assumes that the user/host drops offline. A poor
         * network connection may lead to false detections, so we recommend using the
         * Agora RTM SDK for reliable offline detection.
         *
         * @param uid ID of the user or host who leaves the channel or goes offline.
         * @param reason Reason why the user goes offline:
         *
         * USER_OFFLINE_QUIT(0): The user left the current channel.
         * USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data packet was received within a certain period of time. If a user quits the call and the message is not passed to the SDK (due to an unreliable channel), the SDK assumes the user dropped offline.
         * USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from the host to the audience.
         */
        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i(TAG, "User offline, uid: " + (uid and 0xFFFFFFFFL.toInt()))
            onCallLeft?.invoke(uid)
        }

        override fun onUserMuteAudio(uid: Int, muted: Boolean) {
            Log.i(TAG, "onUserMuteAudio --> $uid -- $muted")
            onUserMuteAudio?.invoke(uid, muted)
        }

        override fun onConnectionStateChanged(state: Int, reason: Int) {
            super.onConnectionStateChanged(state, reason)
            Log.i(TAG, "onConnectionStateChanged --> state -- $state reason -- $reason")
            if (state == 3 && reason == 1) {
                retryCount = 0
                onCallConnected?.invoke()
            } else if (state == 1 && reason == 5) {
                onCallDisconnected?.invoke()
            }
        }

        /**
         * @param routing
         * AUDIO_ROUTE_DEFAULT(-1): Default audio route.
         * AUDIO_ROUTE_HEADSET(0): Headset.
         * AUDIO_ROUTE_EARPIECE(1): Earpiece.
         * AUDIO_ROUTE_HEADSETNOMIC(2): Headset with no microphone.
         * AUDIO_ROUTE_SPEAKERPHONE(3): Speakerphone.
         * AUDIO_ROUTE_LOUDSPEAKER(4): Loudspeaker.
         * AUDIO_ROUTE_HEADSETBLUETOOTH(5): Bluetooth headset.
         */
        override fun onAudioRouteChanged(p0: Int) {
            super.onAudioRouteChanged(p0)
            Log.i(TAG, "onAudioRouteChanged --> $p0")
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
//            Log.i(TAG, "Join channel success, uid: " + (uid and 0xFFFFFFFFL.toInt()))
            Log.i(TAG, "onJoinChannelSuccess --> $uid  --- $elapsed")
        }

        /**
         * Occurs when the first remote video frame is received and decoded.
         * This callback is triggered in either of the following scenarios:
         *
         * The remote user joins the channel and sends the video stream.
         * The remote user stops sending the video stream and re-sends it after 15 seconds. Possible reasons include:
         * The remote user leaves channel.
         * The remote user drops offline.
         * The remote user calls the muteLocalVideoStream method.
         * The remote user calls the disableVideo method.
         *
         * @param uid User ID of the remote user sending the video streams.
         * @param width Width (pixels) of the video stream.
         * @param height Height (pixels) of the video stream.
         * @param elapsed Time elapsed (ms) from the local user calling the joinChannel method until this callback is triggered.
         */
        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
//            Log.i(TAG, "First remote video decoded, uid: " + (uid and 0xFFFFFFFFL.toInt()))
            Log.i(TAG, "onFirstRemoteVideoDecoded --> $uid --- $width --- $height --- $elapsed")
        }

        override fun onWarning(warnCode: Int) {
            super.onWarning(warnCode)
            Log.i(TAG, "onWarning --> $warnCode")
        }

        override fun onError(errorCode: Int) {
            super.onError(errorCode)
            Log.i(TAG, "onError --> $errorCode")
        }

        override fun onRejoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onRejoinChannelSuccess(channel, uid, elapsed)
            Log.i(TAG, "onRejoinChannelSuccess --> $channel -- $uid -- $elapsed")
        }

        override fun onLeaveChannel(stats: RtcStats?) {
            super.onLeaveChannel(stats)

            Log.i(TAG, "onLeaveChannel --> $stats")
        }

        override fun onClientRoleChanged(oldRole: Int, newRole: Int) {
            super.onClientRoleChanged(oldRole, newRole)
            Log.i(TAG, "onClientRoleChanged --> $oldRole -- $newRole")
        }

        override fun onLocalUserRegistered(uid: Int, userAccount: String?) {
            super.onLocalUserRegistered(uid, userAccount)
//            Log.i(TAG, "onLocalUserRegistered --> $uid -- $userAccount")
        }

        override fun onUserInfoUpdated(uid: Int, userInfo: UserInfo?) {
            super.onUserInfoUpdated(uid, userInfo)
            Log.i(TAG, "onUserInfoUpdated --> $uid -- $userInfo")
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            Log.i(TAG, "onUserJoined --> $uid -- $elapsed")
            onUserJoined?.invoke(uid)
        }

        override fun onConnectionInterrupted() {
            super.onConnectionInterrupted()
            Log.i(TAG, "onConnectionInterrupted --> empty")
        }

        override fun onConnectionLost() {
            super.onConnectionLost()
            Log.i(TAG, "onConnectionLost --> empty")
        }

        override fun onConnectionBanned() {
            super.onConnectionBanned()
            Log.i(TAG, "onConnectionBanned --> empty")
        }

        override fun onApiCallExecuted(error: Int, api: String?, result: String?) {
            super.onApiCallExecuted(error, api, result)
            Log.i(TAG, "onApiCallExecuted --> $error -- $api -- $result")
        }

        override fun onTokenPrivilegeWillExpire(token: String?) {
            super.onTokenPrivilegeWillExpire(token)
            Log.i(TAG, "onTokenPrivilegeWillExpire --> $token")
        }

        override fun onRequestToken() {
            super.onRequestToken()
            Log.i(TAG, "onRequestToken --> empty")
        }

        override fun onMicrophoneEnabled(enabled: Boolean) {
            super.onMicrophoneEnabled(enabled)
            Log.i(TAG, "onMicrophoneEnabled --> $enabled")
        }

        override fun onAudioVolumeIndication(
                speakers: Array<out AudioVolumeInfo>?,
                totalVolume: Int
        ) {
            super.onAudioVolumeIndication(speakers, totalVolume)
            onSpeakerIndicate?.invoke(speakers)
            Log.i(TAG, "onAudioVolumeIndication --> $totalVolume")
//            Log.i(TAG, "onAudioVolumeIndication --> ${speakers}-- $totalVolume")
        }

        override fun onActiveSpeaker(uid: Int) {
            super.onActiveSpeaker(uid)
//            Log.i(TAG, "onMicrophoneEnabled --> $uid")
        }

        override fun onFirstLocalAudioFrame(elapsed: Int) {
            super.onFirstLocalAudioFrame(elapsed)
            Log.i(TAG, "onFirstLocalAudioFrame --> $elapsed")
        }

        override fun onFirstLocalAudioFramePublished(elapsed: Int) {
            super.onFirstLocalAudioFramePublished(elapsed)
            Log.i(TAG, "onFirstLocalAudioFramePublished --> $elapsed")
        }

        override fun onFirstRemoteAudioFrame(uid: Int, elapsed: Int) {
            super.onFirstRemoteAudioFrame(uid, elapsed)
            Log.i(TAG, "onFirstRemoteAudioFrame --> $uid -- $elapsed")
        }

        override fun onVideoStopped() {
            super.onVideoStopped()
//            Log.i(TAG, "onVideoStopped --> empty")
        }

        override fun onFirstLocalVideoFrame(width: Int, height: Int, elapsed: Int) {
            super.onFirstLocalVideoFrame(width, height, elapsed)
//            Log.i(TAG, "onFirstLocalVideoFrame --> $width -- $height -- $elapsed")
        }

        override fun onFirstLocalVideoFramePublished(elapsed: Int) {
            super.onFirstLocalVideoFramePublished(elapsed)
//            Log.i(TAG, "onFirstLocalVideoFramePublished --> $elapsed")
        }

        override fun onFirstRemoteVideoFrame(uid: Int, width: Int, height: Int, elapsed: Int) {
            super.onFirstRemoteVideoFrame(uid, width, height, elapsed)
//            Log.i(TAG, "onFirstRemoteVideoFrame --> $uid -- $width -- $height -- $elapsed")
        }

        override fun onUserMuteVideo(uid: Int, muted: Boolean) {
            super.onUserMuteVideo(uid, muted)
//            Log.i(TAG, "onUserMuteVideo --> $uid -- $muted")
        }

        override fun onUserEnableVideo(uid: Int, enabled: Boolean) {
            super.onUserEnableVideo(uid, enabled)
//            Log.i(TAG, "onUserEnableVideo --> $uid -- $enabled")
        }

        override fun onUserEnableLocalVideo(uid: Int, enabled: Boolean) {
            super.onUserEnableLocalVideo(uid, enabled)
//            Log.i(TAG, "onUserEnableLocalVideo --> $uid -- $enabled")
        }

        override fun onVideoSizeChanged(uid: Int, width: Int, height: Int, rotation: Int) {
            super.onVideoSizeChanged(uid, width, height, rotation)
//            Log.i(TAG, "onVideoSizeChanged --> $uid -- $width -- $height -- $rotation")

        }

        override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
            Log.i(TAG, "onRemoteAudioStateChanged --> $uid -- $state -- $reason -- $elapsed")
        }


        /**
         *  @param oldState
         *  @param newState
         *  PUB_STATE_IDLE(0)
         *  PUB_STATE_NO_PUBLISHED(1)
         *  PUB_STATE_PUBLISHING(2)
         *  PUB_STATE_PUBLISHED(3)
         */
        override fun onAudioPublishStateChanged(
                channel: String?,
                oldState: Int,
                newState: Int,
                elapseSinceLastState: Int
        ) {
            super.onAudioPublishStateChanged(channel, oldState, newState, elapseSinceLastState)
            Log.i(
                    TAG,
                    "onAudioPublishStateChanged --> $channel -- $oldState -- $newState -- $elapseSinceLastState"
            )
        }

        /**
         *  @param oldState
         *  @param newState
         *  PUB_STATE_IDLE(0)
         *  PUB_STATE_NO_PUBLISHED(1)
         *  PUB_STATE_PUBLISHING(2)
         *  PUB_STATE_PUBLISHED(3)
         */
        override fun onVideoPublishStateChanged(
                channel: String?,
                oldState: Int,
                newState: Int,
                elapseSinceLastState: Int
        ) {
            super.onVideoPublishStateChanged(channel, oldState, newState, elapseSinceLastState)
            Log.i(
                    TAG,
                    "onVideoPublishStateChanged --> $channel -- $oldState -- $newState -- $elapseSinceLastState"
            )
        }

        /**
         *  @param oldState
         *  @param newState
         *  PUB_STATE_IDLE(0)
         *  PUB_STATE_NO_PUBLISHED(1)
         *  PUB_STATE_PUBLISHING(2)
         *  PUB_STATE_PUBLISHED(3)
         */
        override fun onAudioSubscribeStateChanged(
                channel: String?,
                uid: Int,
                oldState: Int,
                newState: Int,
                elapseSinceLastState: Int
        ) {
            super.onAudioSubscribeStateChanged(
                    channel,
                    uid,
                    oldState,
                    newState,
                    elapseSinceLastState
            )
            Log.i(
                    TAG,
                    "onAudioSubscribeStateChanged --> $channel -- $uid -- $oldState -- $newState-- $elapseSinceLastState"
            )
        }

        /**
         *  @param oldState
         *  @param newState
         *  PUB_STATE_IDLE(0)
         *  PUB_STATE_NO_PUBLISHED(1)
         *  PUB_STATE_PUBLISHING(2)
         *  PUB_STATE_PUBLISHED(3)
         */
        override fun onVideoSubscribeStateChanged(
                channel: String?,
                uid: Int,
                oldState: Int,
                newState: Int,
                elapseSinceLastState: Int
        ) {
            super.onVideoSubscribeStateChanged(
                    channel,
                    uid,
                    oldState,
                    newState,
                    elapseSinceLastState
            )
            Log.i(
                    TAG,
                    "onVideoSubscribeStateChanged --> $channel -- $uid -- $oldState -- $newState-- $elapseSinceLastState"
            )
        }

        /**
         * @param state; - State of the remote video:
         * REMOTE_VIDEO_STATE_STOPPED(0): The remote video is in the default state, probably due to
         *                                REMOTE_VIDEO_STATE_REASON_LOCAL_MUTED(3),
         *                                REMOTE_VIDEO_STATE_REASON_REMOTE_MUTED(5), or
         *                                REMOTE_VIDEO_STATE_REASON_REMOTE_OFFLINE(7).
         * REMOTE_VIDEO_STATE_STARTING(1): The first remote video packet is received.
         * REMOTE_VIDEO_STATE_DECODING(2): The remote video stream is decoded and plays normally, probably due to
         *                                 REMOTE_VIDEO_STATE_REASON_NETWORK_RECOVERY (2),
         *                                 REMOTE_VIDEO_STATE_REASON_LOCAL_UNMUTED(4),
         *                                 REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED(6), or
         *                                 REMOTE_VIDEO_STATE_REASON_AUDIO_FALLBACK_RECOVERY(9).
         * REMOTE_VIDEO_STATE_FROZEN(3): The remote video is frozen, probably due to
         *                               REMOTE_VIDEO_STATE_REASON_NETWORK_CONGESTION(1) or
         *                               REMOTE_VIDEO_STATE_REASON_AUDIO_FALLBACK(8).
         * REMOTE_VIDEO_STATE_FAILED(4): The remote video fails to start, probably due to
         *                               REMOTE_VIDEO_STATE_REASON_INTERNAL(0).
         *
         * @param reason
         * REMOTE_VIDEO_STATE_REASON_INTERNAL(0): Internal reasons.
         * REMOTE_VIDEO_STATE_REASON_NETWORK_CONGESTION(1): Network congestion.
         * REMOTE_VIDEO_STATE_REASON_NETWORK_RECOVERY(2): Network recovery.
         * REMOTE_VIDEO_STATE_REASON_LOCAL_MUTED(3): The local user stops receiving the remote video stream or disables the video module.
         * REMOTE_VIDEO_STATE_REASON_LOCAL_UNMUTED(4): The local user resumes receiving the remote video stream or enables the video module.
         * REMOTE_VIDEO_STATE_REASON_REMOTE_MUTED(5): The remote user stops sending the video stream or disables the video module.
         * REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED(6): The remote user resumes sending the video stream or enables the video module.
         * REMOTE_VIDEO_STATE_REASON_REMOTE_OFFLINE(7): The remote user leaves the channel.
         * REMOTE_VIDEO_STATE_REASON_AUDIO_FALLBACK(8): The remote media stream falls back to the audio-only stream due to poor network conditions.
         * REMOTE_VIDEO_STATE_REASON_AUDIO_FALLBACK_RECOVERY(9): The remote media stream switches back to the video stream after the network conditions improve.
         */
        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
            Log.i(TAG, "onRemoteVideoStateChanged --> $uid -- $state -- $reason -- $elapsed")
        }

        /**
         * @param state
         * RELAY_STATE_IDLE(0): The SDK is initializing.
         * RELAY_STATE_CONNECTING(1): The SDK tries to relay the media stream to the destination channel.
         * RELAY_STATE_RUNNING(2): The SDK successfully relays the media stream to the destination channel.
         * RELAY_STATE_FAILURE(3): A failure occurs. See the details in code.
         *
         * @param code
         * RELAY_OK(0): The state is normal.
         * RELAY_ERROR_SERVER_ERROR_RESPONSE(1): An error occurs in the server response.
         * RELAY_ERROR_SERVER_NO_RESPONSE(2): No server response. You can call the leaveChannel method to leave the channel.
         * RELAY_ERROR_NO_RESOURCE_AVAILABLE(3): The SDK fails to access the service, probably due to limited resources of the server.
         * RELAY_ERROR_FAILED_JOIN_SRC(4): Fails to send the relay request.
         * RELAY_ERROR_FAILED_JOIN_DEST(5): Fails to accept the relay request.
         * RELAY_ERROR_FAILED_PACKET_RECEIVED_FROM_SRC(6): The server fails to receive the media stream.
         * RELAY_ERROR_FAILED_PACKET_SENT_TO_DEST(7): The server fails to send the media stream.
         * RELAY_ERROR_SERVER_CONNECTION_LOST(8): The SDK disconnects from the server due to poor network connections. You can call the leaveChannel method to leave the channel.
         * RELAY_ERROR_INTERNAL_ERROR(9): An internal error occurs in the server.
         * RELAY_ERROR_SRC_TOKEN_EXPIRED(10): The token of the source channel has expired.
         * RELAY_ERROR_DEST_TOKEN_EXPIRED(11): The token of the destination channel has expired.
         */
        override fun onChannelMediaRelayStateChanged(state: Int, code: Int) {
            super.onChannelMediaRelayStateChanged(state, code)
            Log.i(TAG, "onChannelMediaRelayStateChanged --> $state -- $code")
        }

        /**
         * @param code
         * RELAY_EVENT_NETWORK_DISCONNECTED(0): The user disconnects from the server due to poor network connections.
         * RELAY_EVENT_NETWORK_CONNECTED(1): The network reconnects.
         * RELAY_EVENT_PACKET_JOINED_SRC_CHANNEL(2): The user joins the source channel.
         * RELAY_EVENT_PACKET_JOINED_DEST_CHANNEL(3): The user joins the destination channel.
         * RELAY_EVENT_PACKET_SENT_TO_DEST_CHANNEL(4): The SDK starts relaying the media stream to the destination channel.
         * RELAY_EVENT_PACKET_RECEIVED_VIDEO_FROM_SRC(5): The server receives the video stream from the source channel.
         * RELAY_EVENT_PACKET_RECEIVED_AUDIO_FROM_SRC(6): The server receives the audio stream from the source channel.
         * RELAY_EVENT_PACKET_UPDATE_DEST_CHANNEL(7): The destination channel is updated.
         * RELAY_EVENT_PACKET_UPDATE_DEST_CHANNEL_REFUSED(8): The destination channel update fails due to internal reasons.
         * RELAY_EVENT_PACKET_UPDATE_DEST_CHANNEL_NOT_CHANGE(9): The destination channel does not change, which means that the destination channel fails to be updated.
         * RELAY_EVENT_PACKET_UPDATE_DEST_CHANNEL_IS_NULL(10): The destination channel name is NULL.
         * RELAY_EVENT_VIDEO_PROFILE_UPDATE(11): The video profile is sent to the server.
         */
        override fun onChannelMediaRelayEvent(code: Int) {
            super.onChannelMediaRelayEvent(code)
            Log.i(TAG, "onChannelMediaRelayEvent --> $code")
        }


        /**
         * @param isFallbackOrRecover
         * true: The published stream fell back to audio-only due to poor network conditions.
         * false: The published stream switched back to the video after the network conditions improved.
         */
        override fun onLocalPublishFallbackToAudioOnly(isFallbackOrRecover: Boolean) {
            super.onLocalPublishFallbackToAudioOnly(isFallbackOrRecover)
            Log.i(TAG, "onLocalPublishFallbackToAudioOnly --> $isFallbackOrRecover")
        }

        /**
         * @param isFallbackOrRecover
         * true: The remote media stream fell back to audio-only due to poor network conditions.
         * false: The remote media stream switched back to the video stream after the network conditions improved.
         */
        override fun onRemoteSubscribeFallbackToAudioOnly(uid: Int, isFallbackOrRecover: Boolean) {
            super.onRemoteSubscribeFallbackToAudioOnly(uid, isFallbackOrRecover)
            Log.i(TAG, "onRemoteSubscribeFallbackToAudioOnly --> $uid -- $isFallbackOrRecover")
        }

        override fun onCameraReady() {
            super.onCameraReady()
            Log.i(TAG, "onCameraReady --> empty")
        }

        override fun onCameraFocusAreaChanged(rect: Rect?) {
            super.onCameraFocusAreaChanged(rect)
            Log.i(TAG, "onCameraFocusAreaChanged --> $rect")
        }

        override fun onCameraExposureAreaChanged(rect: Rect?) {
            super.onCameraExposureAreaChanged(rect)
            Log.i(TAG, "onCameraExposureAreaChanged --> $rect")
        }

        override fun onFacePositionChanged(
                imageWidth: Int,
                imageHeight: Int,
                faces: Array<out AgoraFacePositionInfo>?
        ) {
            super.onFacePositionChanged(imageWidth, imageHeight, faces)
            Log.i(TAG, "onFacePositionChanged --> $imageWidth -- $imageHeight -- $faces")
        }

        /**
         * @param quality
         * QUALITY_UNKNOWN(0): The quality is unknown.
         * QUALITY_EXCELLENT(1): The quality is excellent.
         * QUALITY_GOOD(2): The quality is quite good, but the bitrate may be slightly lower than excellent.
         * QUALITY_POOR(3): Users can feel the communication slightly impaired.
         * QUALITY_BAD(4): Users can communicate not very smoothly.
         * QUALITY_VBAD(5): The quality is so bad that users can barely communicate.
         * QUALITY_DOWN(6): The network is disconnected and users cannot communicate at all.
         * QUALITY_DETECTING(8): The SDK is detecting the network quality.
         */
        override fun onAudioQuality(uid: Int, quality: Int, delay: Short, lost: Short) {
            super.onAudioQuality(uid, quality, delay, lost)
            Log.i(TAG, "onAudioQuality --> $uid -- $quality -- $delay")
        }

        override fun onRtcStats(stats: RtcStats?) {
            super.onRtcStats(stats)
            if ((stats?.users ?: 0) == 1) {
                onFirstUser?.invoke()
            }

            stats?.users?.let {
                if (activeUserCount != it) {
                    activeUserCount = it
                    onUserCount?.invoke(activeUserCount)
                }
            }

            Log.i(TAG, "onRtcStats --> usercount ---> ${stats?.users}")
        }

        /**
         * @param quality
         * QUALITY_UNKNOWN(0): The quality is unknown.
         * QUALITY_EXCELLENT(1): The quality is excellent.
         * QUALITY_GOOD(2): The quality is quite good, but the bitrate may be slightly lower than excellent.
         * QUALITY_POOR(3): Users can feel the communication slightly impaired.
         * QUALITY_BAD(4): Users can communicate not very smoothly.
         * QUALITY_VBAD(5): The quality is so bad that users can barely communicate.
         * QUALITY_DOWN(6): The network is disconnected and users cannot communicate at all.
         * QUALITY_DETECTING(8): The SDK is detecting the network quality.
         */
        override fun onLastmileQuality(quality: Int) {
            super.onLastmileQuality(quality)
            Log.i(TAG, "onLastmileQuality --> $quality")
        }

        override fun onLastmileProbeResult(result: LastmileProbeResult?) {
            super.onLastmileProbeResult(result)
            Log.i(TAG, "onLastmileProbeResult --> $result")
        }

        /**
         * @param txQuality
         * Uplink transmission quality of the user in terms of the transmission bitrate, packet loss rate,
         * average RTT (Round-Trip Time) and jitter of the uplink network. txQuality is a quality rating
         * helping you understand how well the current uplink network conditions can support the selected
         * VideoEncoderConfiguration. For example, a 1000 Kbps uplink network may be adequate for video
         * frames with a resolution of 680 × 480 and a frame rate of 30 fps, but may be inadequate for
         * resolutions higher than 1280 × 720.
         *
         * QUALITY_UNKNOWN(0): The quality is unknown.
         * QUALITY_EXCELLENT(1): The quality is excellent.
         * QUALITY_GOOD(2): The quality is quite good, but the bitrate may be slightly lower than excellent.
         * QUALITY_POOR(3): Users can feel the communication slightly impaired.
         * QUALITY_BAD(4): Users can communicate not very smoothly.
         * QUALITY_VBAD(5): The quality is so bad that users can barely communicate.
         * QUALITY_DOWN(6): The network is disconnected and users cannot communicate at all.
         * QUALITY_DETECTING(8): The SDK is detecting the network quality.
         *
         * @param rxQuality
         * Downlink network quality rating of the user in terms of packet loss rate,
         * average RTT, and jitter of the downlink network.
         *
         * QUALITY_UNKNOWN(0): The quality is unknown.
         * QUALITY_EXCELLENT(1): The quality is excellent.
         * QUALITY_GOOD(2): The quality is quite good, but the bitrate may be slightly lower than excellent.
         * QUALITY_POOR(3): Users can feel the communication slightly impaired.
         * QUALITY_BAD(4): Users can communicate not very smoothly.
         * QUALITY_VBAD(5): The quality is so bad that users can barely communicate.
         * QUALITY_DOWN(6): The network is disconnected and users cannot communicate at all.
         * QUALITY_DETECTING(8): The SDK is detecting the network quality.
         */
        override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
            super.onNetworkQuality(uid, txQuality, rxQuality)
//            Log.i(TAG, "onNetworkQuality --> $uid -- $txQuality -- $rxQuality")
        }

        override fun onLocalVideoStats(stats: LocalVideoStats?) {
            super.onLocalVideoStats(stats)
            Log.i(TAG, "onLocalVideoStats --> $stats")
        }

        override fun onRemoteVideoStats(stats: RemoteVideoStats?) {
            super.onRemoteVideoStats(stats)
            Log.i(TAG, "onRemoteVideoStats --> $stats")
        }

        override fun onLocalAudioStats(stats: LocalAudioStats?) {
            super.onLocalAudioStats(stats)
            Log.i(TAG, "onLocalAudioStats --> $stats")
        }

        override fun onRemoteAudioStats(stats: RemoteAudioStats?) {
            super.onRemoteAudioStats(stats)
            Log.i(TAG, "onRemoteAudioStats --> ${stats?.uid}")
        }

        override fun onLocalVideoStat(sentBitrate: Int, sentFrameRate: Int) {
            super.onLocalVideoStat(sentBitrate, sentFrameRate)
            Log.i(TAG, "onLocalVideoStat --> $sentBitrate -- $sentFrameRate")
        }

        override fun onRemoteVideoStat(
                uid: Int,
                delay: Int,
                receivedBitrate: Int,
                receivedFrameRate: Int
        ) {
            super.onRemoteVideoStat(uid, delay, receivedBitrate, receivedFrameRate)
            Log.i(
                    TAG,
                    "onRemoteVideoStat --> $uid -- $delay -- $receivedBitrate -- $receivedFrameRate"
            )
        }

        override fun onRemoteAudioTransportStats(uid: Int, delay: Int, lost: Int, rxKBitRate: Int) {
            super.onRemoteAudioTransportStats(uid, delay, lost, rxKBitRate)
            Log.i(TAG, "onRemoteAudioTransportStats --> $uid -- $delay -- $lost -- $rxKBitRate")
        }

        override fun onRemoteVideoTransportStats(uid: Int, delay: Int, lost: Int, rxKBitRate: Int) {
            super.onRemoteVideoTransportStats(uid, delay, lost, rxKBitRate)
            Log.i(TAG, "onRemoteVideoTransportStats --> $uid -- $delay -- $lost -- $rxKBitRate")
        }

        /**
         * @param state
         * MEDIA_ENGINE_AUDIO_EVENT_MIXING_PLAY(710): The audio mixing file is playing after the method call of startAudioMixing or resumeAudioMixing succeeds.
         * MEDIA_ENGINE_AUDIO_EVENT_MIXING_PAUSED(711): The audio mixing file pauses playing after the method call of pauseAudioMixing succeeds.
         * MEDIA_ENGINE_AUDIO_EVENT_MIXING_STOPPED(713): The audio mixing file stops playing after the method call of stopAudioMixing succeeds.
         * MEDIA_ENGINE_AUDIO_EVENT_MIXING_ERROR(714): An exception occurs during the playback of the audio mixing file. See the errorCode for details.
         *
         * @param errorCode
         * MEDIA_ENGINE_AUDIO_ERROR_MIXING_OPEN(701): the SDK cannot open the audio mixing file.
         * MEDIA_ENGINE_AUDIO_ERROR_MIXING_TOO_FREQUENT(702): the SDK opens the audio mixing file too frequently.
         * MEDIA_ENGINE_AUDIO_EVENT_MIXING_INTERRUPTED_EOF(703): the audio mixing file playback is interrupted.
         */
        override fun onAudioMixingStateChanged(state: Int, errorCode: Int) {
            super.onAudioMixingStateChanged(state, errorCode)
            Log.i(TAG, "onAudioMixingStateChanged --> $state -- $errorCode")
        }

        override fun onAudioMixingFinished() {
            super.onAudioMixingFinished()
            Log.i(TAG, "onAudioMixingFinished --> empty")
        }

        override fun onAudioEffectFinished(soundId: Int) {
            super.onAudioEffectFinished(soundId)
            Log.i(TAG, "onAudioEffectFinished -->  $soundId ")
        }

        override fun onFirstRemoteAudioDecoded(uid: Int, elapsed: Int) {
            super.onFirstRemoteAudioDecoded(uid, elapsed)
            Log.i(TAG, "onFirstRemoteAudioDecoded --> $uid -- $elapsed")
        }

        /**
         * @param state
         * LOCAL_AUDIO_STREAM_STATE_STOPPED(0): The local audio is in the initial state.
         * LOCAL_AUDIO_STREAM_STATE_CAPTURING(1): The recording device starts successfully.
         * LOCAL_AUDIO_STREAM_STATE_ENCODING(2): The first audio frame encodes successfully.
         * LOCAL_AUDIO_STREAM_STATE_FAILED(3): The local audio fails to start.
         *
         * @param error
         * LOCAL_AUDIO_STREAM_ERROR_OK(0): The local audio is normal.
         * LOCAL_AUDIO_STREAM_ERROR_FAILURE(1): No specified reason for the local audio failure.
         * LOCAL_AUDIO_STREAM_ERROR_DEVICE_NO_PERMISSION(2): No permission to use the local audio device.
         * LOCAL_AUDIO_STREAM_ERROR_DEVICE_BUSY(3): The microphone is in use.
         * LOCAL_AUDIO_STREAM_ERROR_CAPTURE_FAILURE(4): The local audio recording fails. Check whether the recording device is working properly.
         * LOCAL_AUDIO_STREAM_ERROR_ENCODE_FAILURE(5): The local audio encoding fails.
         */
        override fun onLocalAudioStateChanged(state: Int, error: Int) {
            super.onLocalAudioStateChanged(state, error)
            Log.i(TAG, "onLocalAudioStateChanged --> $state -- $error")
        }

        /**
         * @param localVideoState
         * LOCAL_VIDEO_STREAM_STATE_STOPPED(0): The local video is in the initial state.
         * LOCAL_VIDEO_STREAM_STATE_CAPTURING(1): The local video capturer starts successfully.
         * LOCAL_VIDEO_STREAM_STATE_ENCODING(2): The first local video frame encodes successfully.
         * LOCAL_VIDEO_STREAM_STATE_FAILED(3): The local video fails to start.
         *
         * @param error
         * LOCAL_VIDEO_STREAM_ERROR_OK(0): The local video is normal.
         * LOCAL_VIDEO_STREAM_ERROR_FAILURE(1): No specified reason for the local video failure.
         * LOCAL_VIDEO_STREAM_ERROR_DEVICE_NO_PERMISSION(2): No permission to use the local video device.
         * LOCAL_VIDEO_STREAM_ERROR_DEVICE_BUSY(3): The local video capturer is in use.
         * LOCAL_VIDEO_STREAM_ERROR_CAPTURE_FAILURE(4): The local video capture fails. C
         * heck whether the capturer is working properly.
         * - If your app runs in the background on a device running Android 9 or later, you cannot access the camera.
         * - If your app runs on a device running Android 6 or later, this error is reported if the camera
         *   is occupied by a third-part app and not property released. Once the camera is released, the SDK
         *   triggers this callback again, reporting state CAPTURING(1), and error ERROR_OK(0).
         * LOCAL_VIDEO_STREAM_ERROR_ENCODE_FAILURE(5): The local video encoding fails.
         */
        override fun onLocalVideoStateChanged(localVideoState: Int, error: Int) {
            super.onLocalVideoStateChanged(localVideoState, error)
            Log.i(TAG, "onLocalVideoStateChanged --> $localVideoState -- $error")
        }

        /**
         * @param state
         * RTMP_STREAM_PUBLISH_STATE_IDLE(0): The RTMP streaming has not started or has ended. This state is also
         *                                    triggered after you remove an RTMP address from the CDN by calling
         *                                    removePublishStreamUrl.
         * RTMP_STREAM_PUBLISH_STATE_CONNECTING(1): The SDK is connecting to Agora streaming server and the RTMP server.
         *                                          This state is triggered after you call the addPublishStreamUrl method.
         * RTMP_STREAM_PUBLISH_STATE_RUNNING(2): The RTMP streaming publishes. The SDK successfully publishes
         *                                       the RTMP streaming and returns this state.
         * RTMP_STREAM_PUBLISH_STATE_RECOVERING(3): The RTMP streaming is recovering. When exceptions occur to the CDN,
         *                                          or the streaming is interrupted, the SDK attempts to resume RTMP
         *                                          streaming and returns this state.
         * - If the SDK successfully resumes the streaming, RTMP_STREAM_PUBLISH_STATE_RUNNING(2) returns.
         * - If the streaming does not resume within 60 seconds or server errors occur,
         *    RTMP_STREAM_PUBLISH_STATE_FAILURE(4) returns. You can also reconnect to the server by calling the
         *    removePublishStreamUrl and addPublishStreamUrl methods.
         * RTMP_STREAM_PUBLISH_STATE_FAILURE(4): The RTMP streaming fails. See the errCode parameter for the
         *                                       detailed error information. You can also call the addPublishStreamUrl
         *                                       method to publish the RTMP streaming again.
         *
         * @param errCode
         * RTMP_STREAM_PUBLISH_ERROR_OK(0): The RTMP streaming publishes successfully.
         * RTMP_STREAM_PUBLISH_ERROR_INVALID_ARGUMEN(1): Invalid argument used. If, for example, you do not call the
         *                                               setLiveTranscoding method to configure the LiveTranscoding
         *                                               parameters before calling the addPublishStreamUrl method, the
         *                                               SDK returns this error. Check whether you set the parameters
         *                                               in the setLiveTranscoding method properly.
         * RTMP_STREAM_PUBLISH_ERROR_ENCRYPTED_STREAM_NOT_ALLOWED(2): The RTMP streaming is encrypted and cannot be published.
         * RTMP_STREAM_PUBLISH_ERROR_CONNECTION_TIMEOUT(3): Timeout for the RTMP streaming. Call the addPublishStreamUrl
         *                                                  method to publish the streaming again.
         * RTMP_STREAM_PUBLISH_ERROR_INTERNAL_SERVER_ERROR(4): An error occurs in Agora streaming server. Call the
         *                                                     addPublishStreamUrl method to publish the streaming again.
         * RTMP_STREAM_PUBLISH_ERROR_RTMP_SERVER_ERROR(5): An error occurs in the RTMP server.
         * RTMP_STREAM_PUBLISH_ERROR_TOO_OFTEN(6): The RTMP streaming publishes too frequently.
         * RTMP_STREAM_PUBLISH_ERROR_REACH_LIMIT(7): The host publishes more than 10 URLs. Delete the
         *                                           unnecessary URLs before adding new ones.
         * RTMP_STREAM_PUBLISH_ERROR_NOT_AUTHORIZED(8): The host manipulates other hosts' URLs. Check your app logic.
         * RTMP_STREAM_PUBLISH_ERROR_STREAM_NOT_FOUND(9): Agora server fails to find the RTMP streaming.
         * RTMP_STREAM_PUBLISH_ERROR_FORMAT_NOT_SUPPORTED(10): The format of the RTMP streaming URL is not supported.
         *                                                     Check whether the URL format is correct.
         */
        override fun onRtmpStreamingStateChanged(url: String?, state: Int, errCode: Int) {
            super.onRtmpStreamingStateChanged(url, state, errCode)
            Log.i(TAG, "onRtmpStreamingStateChanged --> $url -- $state -- $errCode ")
        }

        override fun onStreamPublished(p0: String?, p1: Int) {
            super.onStreamPublished(p0, p1)
            Log.i(TAG, "onStreamPublished --> $p0 -- $p1")
        }

        override fun onStreamUnpublished(p0: String?) {
            super.onStreamUnpublished(p0)
            Log.i(TAG, "onStreamUnpublished -->  $p0 ")
        }

        override fun onTranscodingUpdated() {
            super.onTranscodingUpdated()
            Log.i(TAG, "onTranscodingUpdated --> empty")
        }

        override fun onRtmpStreamingEvent(p0: String?, p1: Int) {
            super.onRtmpStreamingEvent(p0, p1)
            Log.i(TAG, "onRtmpStreamingEvent --> $p0 -- $p1")
        }

        override fun onStreamInjectedStatus(p0: String?, p1: Int, p2: Int) {
            super.onStreamInjectedStatus(p0, p1, p2)
            Log.i(TAG, "onStreamInjectedStatus --> $p0 -- $p1 -- $p2 ")
        }

        override fun onStreamMessage(p0: Int, p1: Int, p2: ByteArray?) {
            super.onStreamMessage(p0, p1, p2)
            Log.i(TAG, "onStreamMessage --> $p0 -- $p1 -- $p2 ")
        }

        override fun onStreamMessageError(p0: Int, p1: Int, p2: Int, p3: Int, p4: Int) {
            super.onStreamMessageError(p0, p1, p2, p3, p4)
            Log.i(TAG, "onStreamMessageError --> $p0 -- $p1 -- $p2 -- $p3 -- $p4")
        }

        override fun onMediaEngineLoadSuccess() {
            super.onMediaEngineLoadSuccess()
            Log.i(TAG, "onMediaEngineLoadSuccess --> empty")
        }

        override fun onMediaEngineStartCallSuccess() {
            super.onMediaEngineStartCallSuccess()
            Log.i(TAG, "onMediaEngineStartCallSuccess --> empty")
        }

        override fun onNetworkTypeChanged(p0: Int) {
            super.onNetworkTypeChanged(p0)
            Log.i(TAG, "onNetworkTypeChanged --> $p0")
        }
    }

    private val mRtcVideoEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        /**
         * Occurs when the local user joins a specified channel.
         * The channel name assignment is based on channelName specified in the joinChannel method.
         * If the uid is not specified when joinChannel is called, the server automatically assigns a uid.
         *
         * @param channel Channel name.
         * @param uid User ID.
         * @param elapsed Time elapsed (ms) from the user calling joinChannel until this callback is triggered.
         */
        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            Log.i(TAG, "Join channel success, uid: " + (uid and 0xFFFFFFFFL.toInt()))
        }

        /**
         * Occurs when the first remote video frame is received and decoded.
         * This callback is triggered in either of the following scenarios:
         *
         * The remote user joins the channel and sends the video stream.
         * The remote user stops sending the video stream and re-sends it after 15 seconds. Possible reasons include:
         * The remote user leaves channel.
         * The remote user drops offline.
         * The remote user calls the muteLocalVideoStream method.
         * The remote user calls the disableVideo method.
         *
         * @param uid User ID of the remote user sending the video streams.
         * @param width Width (pixels) of the video stream.
         * @param height Height (pixels) of the video stream.
         * @param elapsed Time elapsed (ms) from the local user calling the joinChannel method until this callback is triggered.
         */
        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            CoroutineScope(Dispatchers.Main).launch {
                Log.i(TAG, "First remote video decoded, uid: " + (uid and 0xFFFFFFFFL.toInt()))
                onVideoCallJoined?.invoke(uid, width, height, elapsed)
            }
        }

        /**
         * Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
         *
         * There are two reasons for users to become offline:
         *
         * Leave the channel: When the user/host leaves the channel, the user/host sends a
         * goodbye message. When this message is received, the SDK determines that the
         * user/host leaves the channel.
         *
         * Drop offline: When no data packet of the user or host is received for a certain
         * period of time (20 seconds for the communication profile, and more for the live
         * broadcast profile), the SDK assumes that the user/host drops offline. A poor
         * network connection may lead to false detections, so we recommend using the
         * Agora RTM SDK for reliable offline detection.
         *
         * @param uid ID of the user or host who leaves the channel or goes offline.
         * @param reason Reason why the user goes offline:
         *
         * USER_OFFLINE_QUIT(0): The user left the current channel.
         * USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data packet was received within a certain period of time. If a user quits the call and the message is not passed to the SDK (due to an unreliable channel), the SDK assumes the user dropped offline.
         * USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from the host to the audience.
         */
        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i(TAG, "User offline, uid: " + (uid and 0xFFFFFFFFL.toInt()))
            onCallLeft?.invoke(uid)
        }

        override fun onWarning(warnCode: Int) {
            super.onWarning(warnCode)
            Log.i(TAG, "onWarning --> $warnCode")
        }

        override fun onError(errorCode: Int) {
            super.onError(errorCode)
            Log.i(TAG, "onError --> $errorCode")
        }

        override fun onRejoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onRejoinChannelSuccess(channel, uid, elapsed)
            Log.i(TAG, "onRejoinChannelSuccess --> $channel -- $uid -- $elapsed")
        }

        override fun onLeaveChannel(stats: RtcStats?) {
            super.onLeaveChannel(stats)
            Log.i(TAG, "onLeaveChannel --> $stats")
        }

        override fun onClientRoleChanged(oldRole: Int, newRole: Int) {
            super.onClientRoleChanged(oldRole, newRole)
            Log.i(TAG, "onClientRoleChanged --> $oldRole -- $newRole")
        }

        override fun onLocalUserRegistered(uid: Int, userAccount: String?) {
            super.onLocalUserRegistered(uid, userAccount)
            Log.i(TAG, "onLocalUserRegistered --> $uid -- $userAccount")
        }

        override fun onUserInfoUpdated(uid: Int, userInfo: UserInfo?) {
            super.onUserInfoUpdated(uid, userInfo)
            Log.i(TAG, "onUserInfoUpdated --> $uid -- $userInfo")
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            Log.i(TAG, "onUserJoined --> $uid -- $elapsed")
        }

        override fun onConnectionStateChanged(state: Int, reason: Int) {
            super.onConnectionStateChanged(state, reason)
            Log.i(TAG, "onConnectionStateChanged --> $state -- $reason")
        }

        override fun onConnectionInterrupted() {
            super.onConnectionInterrupted()
            Log.i(TAG, "onConnectionInterrupted --> empty")
        }

        override fun onConnectionLost() {
            super.onConnectionLost()
            Log.i(TAG, "onConnectionLost --> empty")
        }

        override fun onConnectionBanned() {
            super.onConnectionBanned()
            Log.i(TAG, "onConnectionBanned --> empty")
        }

        override fun onApiCallExecuted(error: Int, api: String?, result: String?) {
            super.onApiCallExecuted(error, api, result)
            Log.i(TAG, "onLocalUserRegistered --> $error -- $api -- $result")
        }

        override fun onTokenPrivilegeWillExpire(token: String?) {
            super.onTokenPrivilegeWillExpire(token)
            Log.i(TAG, "onTokenPrivilegeWillExpire --> $token")
        }

        override fun onRequestToken() {
            super.onRequestToken()
            Log.i(TAG, "onRequestToken --> empty")
        }

        override fun onMicrophoneEnabled(enabled: Boolean) {
            super.onMicrophoneEnabled(enabled)
            Log.i(TAG, "onMicrophoneEnabled --> $enabled")
        }

        override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
            super.onAudioVolumeIndication(speakers, totalVolume)
            Log.i(TAG, "onAudioVolumeIndication --> $speakers -- $totalVolume")
        }

        override fun onActiveSpeaker(uid: Int) {
            super.onActiveSpeaker(uid)
            Log.i(TAG, "onMicrophoneEnabled --> $uid")
        }

        override fun onFirstLocalAudioFrame(elapsed: Int) {
            super.onFirstLocalAudioFrame(elapsed)
            Log.i(TAG, "onFirstLocalAudioFrame --> $elapsed")
        }

        override fun onFirstLocalAudioFramePublished(elapsed: Int) {
            super.onFirstLocalAudioFramePublished(elapsed)
            Log.i(TAG, "onFirstLocalAudioFramePublished --> $elapsed")
        }

        override fun onFirstRemoteAudioFrame(uid: Int, elapsed: Int) {
            super.onFirstRemoteAudioFrame(uid, elapsed)
            Log.i(TAG, "onFirstRemoteAudioFrame --> $uid -- $elapsed")
        }

        override fun onVideoStopped() {
            super.onVideoStopped()
            Log.i(TAG, "onVideoStopped --> empty")
        }

        override fun onFirstLocalVideoFrame(width: Int, height: Int, elapsed: Int) {
            super.onFirstLocalVideoFrame(width, height, elapsed)
            Log.i(TAG, "onFirstLocalVideoFrame --> $width -- $height -- $elapsed")
        }

        override fun onFirstLocalVideoFramePublished(elapsed: Int) {
            super.onFirstLocalVideoFramePublished(elapsed)
            Log.i(TAG, "onFirstLocalVideoFramePublished --> $elapsed")
        }

        override fun onFirstRemoteVideoFrame(uid: Int, width: Int, height: Int, elapsed: Int) {
            super.onFirstRemoteVideoFrame(uid, width, height, elapsed)
            Log.i(TAG, "onFirstRemoteVideoFrame --> $uid -- $width -- $height -- $elapsed")
        }

        override fun onUserMuteAudio(uid: Int, muted: Boolean) {
            super.onUserMuteAudio(uid, muted)
            Log.i(TAG, "onUserMuteAudio --> $uid -- $muted")
        }

        override fun onUserMuteVideo(uid: Int, muted: Boolean) {
            super.onUserMuteVideo(uid, muted)
            Log.i(TAG, "onUserMuteVideo --> $uid -- $muted")
        }

        override fun onUserEnableVideo(uid: Int, enabled: Boolean) {
            super.onUserEnableVideo(uid, enabled)
            Log.i(TAG, "onUserEnableVideo --> $uid -- $enabled")
        }

        override fun onUserEnableLocalVideo(uid: Int, enabled: Boolean) {
            super.onUserEnableLocalVideo(uid, enabled)
            Log.i(TAG, "onUserEnableLocalVideo --> $uid -- $enabled")
        }

        override fun onVideoSizeChanged(uid: Int, width: Int, height: Int, rotation: Int) {
            super.onVideoSizeChanged(uid, width, height, rotation)
            Log.i(TAG, "onVideoSizeChanged --> $uid -- $width -- $height -- $rotation")

        }

        override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
            Log.i(TAG, "onRemoteAudioStateChanged --> $uid -- $state -- $reason -- $elapsed")
        }


        /**
         *  @param oldState
         *  @param newState
         *  PUB_STATE_IDLE(0)
         *  PUB_STATE_NO_PUBLISHED(1)
         *  PUB_STATE_PUBLISHING(2)
         *  PUB_STATE_PUBLISHED(3)
         */
        override fun onAudioPublishStateChanged(channel: String?, oldState: Int, newState: Int, elapseSinceLastState: Int) {
            super.onAudioPublishStateChanged(channel, oldState, newState, elapseSinceLastState)
            Log.i(TAG, "onAudioPublishStateChanged --> $channel -- $oldState -- $newState -- $elapseSinceLastState")
        }

        /**
         *  @param oldState
         *  @param newState
         *  PUB_STATE_IDLE(0)
         *  PUB_STATE_NO_PUBLISHED(1)
         *  PUB_STATE_PUBLISHING(2)
         *  PUB_STATE_PUBLISHED(3)
         */
        override fun onVideoPublishStateChanged(channel: String?, oldState: Int, newState: Int, elapseSinceLastState: Int) {
            super.onVideoPublishStateChanged(channel, oldState, newState, elapseSinceLastState)
            Log.i(TAG, "onVideoPublishStateChanged --> $channel -- $oldState -- $newState -- $elapseSinceLastState")
        }

        /**
         *  @param oldState
         *  @param newState
         *  PUB_STATE_IDLE(0)
         *  PUB_STATE_NO_PUBLISHED(1)
         *  PUB_STATE_PUBLISHING(2)
         *  PUB_STATE_PUBLISHED(3)
         */
        override fun onAudioSubscribeStateChanged(channel: String?, uid: Int, oldState: Int, newState: Int, elapseSinceLastState: Int) {
            super.onAudioSubscribeStateChanged(channel, uid, oldState, newState, elapseSinceLastState)
            Log.i(TAG, "onAudioSubscribeStateChanged --> $channel -- $uid -- $oldState -- $newState-- $elapseSinceLastState")
        }

        /**
         *  @param oldState
         *  @param newState
         *  PUB_STATE_IDLE(0)
         *  PUB_STATE_NO_PUBLISHED(1)
         *  PUB_STATE_PUBLISHING(2)
         *  PUB_STATE_PUBLISHED(3)
         */
        override fun onVideoSubscribeStateChanged(channel: String?, uid: Int, oldState: Int, newState: Int, elapseSinceLastState: Int) {
            super.onVideoSubscribeStateChanged(channel, uid, oldState, newState, elapseSinceLastState)
            Log.i(TAG, "onVideoSubscribeStateChanged --> $channel -- $uid -- $oldState -- $newState-- $elapseSinceLastState")
        }

        /**
         * @param state; - State of the remote video:
         * REMOTE_VIDEO_STATE_STOPPED(0): The remote video is in the default state, probably due to
         *                                REMOTE_VIDEO_STATE_REASON_LOCAL_MUTED(3),
         *                                REMOTE_VIDEO_STATE_REASON_REMOTE_MUTED(5), or
         *                                REMOTE_VIDEO_STATE_REASON_REMOTE_OFFLINE(7).
         * REMOTE_VIDEO_STATE_STARTING(1): The first remote video packet is received.
         * REMOTE_VIDEO_STATE_DECODING(2): The remote video stream is decoded and plays normally, probably due to
         *                                 REMOTE_VIDEO_STATE_REASON_NETWORK_RECOVERY (2),
         *                                 REMOTE_VIDEO_STATE_REASON_LOCAL_UNMUTED(4),
         *                                 REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED(6), or
         *                                 REMOTE_VIDEO_STATE_REASON_AUDIO_FALLBACK_RECOVERY(9).
         * REMOTE_VIDEO_STATE_FROZEN(3): The remote video is frozen, probably due to
         *                               REMOTE_VIDEO_STATE_REASON_NETWORK_CONGESTION(1) or
         *                               REMOTE_VIDEO_STATE_REASON_AUDIO_FALLBACK(8).
         * REMOTE_VIDEO_STATE_FAILED(4): The remote video fails to start, probably due to
         *                               REMOTE_VIDEO_STATE_REASON_INTERNAL(0).
         *
         * @param reason
         * REMOTE_VIDEO_STATE_REASON_INTERNAL(0): Internal reasons.
         * REMOTE_VIDEO_STATE_REASON_NETWORK_CONGESTION(1): Network congestion.
         * REMOTE_VIDEO_STATE_REASON_NETWORK_RECOVERY(2): Network recovery.
         * REMOTE_VIDEO_STATE_REASON_LOCAL_MUTED(3): The local user stops receiving the remote video stream or disables the video module.
         * REMOTE_VIDEO_STATE_REASON_LOCAL_UNMUTED(4): The local user resumes receiving the remote video stream or enables the video module.
         * REMOTE_VIDEO_STATE_REASON_REMOTE_MUTED(5): The remote user stops sending the video stream or disables the video module.
         * REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED(6): The remote user resumes sending the video stream or enables the video module.
         * REMOTE_VIDEO_STATE_REASON_REMOTE_OFFLINE(7): The remote user leaves the channel.
         * REMOTE_VIDEO_STATE_REASON_AUDIO_FALLBACK(8): The remote media stream falls back to the audio-only stream due to poor network conditions.
         * REMOTE_VIDEO_STATE_REASON_AUDIO_FALLBACK_RECOVERY(9): The remote media stream switches back to the video stream after the network conditions improve.
         */
        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
            Log.i(TAG, "onRemoteVideoStateChanged --> $uid -- $state -- $reason -- $elapsed")
        }

        /**
         * @param state
         * RELAY_STATE_IDLE(0): The SDK is initializing.
         * RELAY_STATE_CONNECTING(1): The SDK tries to relay the media stream to the destination channel.
         * RELAY_STATE_RUNNING(2): The SDK successfully relays the media stream to the destination channel.
         * RELAY_STATE_FAILURE(3): A failure occurs. See the details in code.
         *
         * @param code
         * RELAY_OK(0): The state is normal.
         * RELAY_ERROR_SERVER_ERROR_RESPONSE(1): An error occurs in the server response.
         * RELAY_ERROR_SERVER_NO_RESPONSE(2): No server response. You can call the leaveChannel method to leave the channel.
         * RELAY_ERROR_NO_RESOURCE_AVAILABLE(3): The SDK fails to access the service, probably due to limited resources of the server.
         * RELAY_ERROR_FAILED_JOIN_SRC(4): Fails to send the relay request.
         * RELAY_ERROR_FAILED_JOIN_DEST(5): Fails to accept the relay request.
         * RELAY_ERROR_FAILED_PACKET_RECEIVED_FROM_SRC(6): The server fails to receive the media stream.
         * RELAY_ERROR_FAILED_PACKET_SENT_TO_DEST(7): The server fails to send the media stream.
         * RELAY_ERROR_SERVER_CONNECTION_LOST(8): The SDK disconnects from the server due to poor network connections. You can call the leaveChannel method to leave the channel.
         * RELAY_ERROR_INTERNAL_ERROR(9): An internal error occurs in the server.
         * RELAY_ERROR_SRC_TOKEN_EXPIRED(10): The token of the source channel has expired.
         * RELAY_ERROR_DEST_TOKEN_EXPIRED(11): The token of the destination channel has expired.
         */
        override fun onChannelMediaRelayStateChanged(state: Int, code: Int) {
            super.onChannelMediaRelayStateChanged(state, code)
            Log.i(TAG, "onChannelMediaRelayStateChanged --> $state -- $code")
        }

        /**
         * @param code
         * RELAY_EVENT_NETWORK_DISCONNECTED(0): The user disconnects from the server due to poor network connections.
         * RELAY_EVENT_NETWORK_CONNECTED(1): The network reconnects.
         * RELAY_EVENT_PACKET_JOINED_SRC_CHANNEL(2): The user joins the source channel.
         * RELAY_EVENT_PACKET_JOINED_DEST_CHANNEL(3): The user joins the destination channel.
         * RELAY_EVENT_PACKET_SENT_TO_DEST_CHANNEL(4): The SDK starts relaying the media stream to the destination channel.
         * RELAY_EVENT_PACKET_RECEIVED_VIDEO_FROM_SRC(5): The server receives the video stream from the source channel.
         * RELAY_EVENT_PACKET_RECEIVED_AUDIO_FROM_SRC(6): The server receives the audio stream from the source channel.
         * RELAY_EVENT_PACKET_UPDATE_DEST_CHANNEL(7): The destination channel is updated.
         * RELAY_EVENT_PACKET_UPDATE_DEST_CHANNEL_REFUSED(8): The destination channel update fails due to internal reasons.
         * RELAY_EVENT_PACKET_UPDATE_DEST_CHANNEL_NOT_CHANGE(9): The destination channel does not change, which means that the destination channel fails to be updated.
         * RELAY_EVENT_PACKET_UPDATE_DEST_CHANNEL_IS_NULL(10): The destination channel name is NULL.
         * RELAY_EVENT_VIDEO_PROFILE_UPDATE(11): The video profile is sent to the server.
         */
        override fun onChannelMediaRelayEvent(code: Int) {
            super.onChannelMediaRelayEvent(code)
            Log.i(TAG, "onChannelMediaRelayEvent --> $code")
        }


        /**
         * @param isFallbackOrRecover
         * true: The published stream fell back to audio-only due to poor network conditions.
         * false: The published stream switched back to the video after the network conditions improved.
         */
        override fun onLocalPublishFallbackToAudioOnly(isFallbackOrRecover: Boolean) {
            super.onLocalPublishFallbackToAudioOnly(isFallbackOrRecover)
            Log.i(TAG, "onLocalPublishFallbackToAudioOnly --> $isFallbackOrRecover")
        }

        /**
         * @param isFallbackOrRecover
         * true: The remote media stream fell back to audio-only due to poor network conditions.
         * false: The remote media stream switched back to the video stream after the network conditions improved.
         */
        override fun onRemoteSubscribeFallbackToAudioOnly(uid: Int, isFallbackOrRecover: Boolean) {
            super.onRemoteSubscribeFallbackToAudioOnly(uid, isFallbackOrRecover)
            Log.i(TAG, "onRemoteSubscribeFallbackToAudioOnly --> $uid -- $isFallbackOrRecover")
        }

        /**
         * @param routing
         * AUDIO_ROUTE_DEFAULT(-1): Default audio route.
         * AUDIO_ROUTE_HEADSET(0): Headset.
         * AUDIO_ROUTE_EARPIECE(1): Earpiece.
         * AUDIO_ROUTE_HEADSETNOMIC(2): Headset with no microphone.
         * AUDIO_ROUTE_SPEAKERPHONE(3): Speakerphone.
         * AUDIO_ROUTE_LOUDSPEAKER(4): Loudspeaker.
         * AUDIO_ROUTE_HEADSETBLUETOOTH(5): Bluetooth headset.
         */
        override fun onAudioRouteChanged(routing: Int) {
            super.onAudioRouteChanged(routing)
            Log.i(TAG, "onAudioRouteChanged --> $routing")
        }

        override fun onCameraReady() {
            super.onCameraReady()
            Log.i(TAG, "onCameraReady --> empty")
        }

        override fun onCameraFocusAreaChanged(rect: Rect?) {
            super.onCameraFocusAreaChanged(rect)
            Log.i(TAG, "onCameraFocusAreaChanged --> $rect")
        }

        override fun onCameraExposureAreaChanged(rect: Rect?) {
            super.onCameraExposureAreaChanged(rect)
            Log.i(TAG, "onCameraExposureAreaChanged --> $rect")
        }

        override fun onFacePositionChanged(imageWidth: Int, imageHeight: Int, faces: Array<out AgoraFacePositionInfo>?) {
            super.onFacePositionChanged(imageWidth, imageHeight, faces)
            Log.i(TAG, "onFacePositionChanged --> $imageWidth -- $imageHeight -- $faces")
        }

        /**
         * @param quality
         * QUALITY_UNKNOWN(0): The quality is unknown.
         * QUALITY_EXCELLENT(1): The quality is excellent.
         * QUALITY_GOOD(2): The quality is quite good, but the bitrate may be slightly lower than excellent.
         * QUALITY_POOR(3): Users can feel the communication slightly impaired.
         * QUALITY_BAD(4): Users can communicate not very smoothly.
         * QUALITY_VBAD(5): The quality is so bad that users can barely communicate.
         * QUALITY_DOWN(6): The network is disconnected and users cannot communicate at all.
         * QUALITY_DETECTING(8): The SDK is detecting the network quality.
         */
        override fun onAudioQuality(uid: Int, quality: Int, delay: Short, lost: Short) {
            super.onAudioQuality(uid, quality, delay, lost)
            Log.i(TAG, "onAudioQuality --> $uid -- $quality -- $delay")
        }

        override fun onRtcStats(stats: RtcStats?) {
            super.onRtcStats(stats)
            Log.i(TAG, "onRtcStats --> $stats")
        }

        /**
         * @param quality
         * QUALITY_UNKNOWN(0): The quality is unknown.
         * QUALITY_EXCELLENT(1): The quality is excellent.
         * QUALITY_GOOD(2): The quality is quite good, but the bitrate may be slightly lower than excellent.
         * QUALITY_POOR(3): Users can feel the communication slightly impaired.
         * QUALITY_BAD(4): Users can communicate not very smoothly.
         * QUALITY_VBAD(5): The quality is so bad that users can barely communicate.
         * QUALITY_DOWN(6): The network is disconnected and users cannot communicate at all.
         * QUALITY_DETECTING(8): The SDK is detecting the network quality.
         */
        override fun onLastmileQuality(quality: Int) {
            super.onLastmileQuality(quality)
            Log.i(TAG, "onLastmileQuality --> $quality")
        }

        override fun onLastmileProbeResult(result: LastmileProbeResult?) {
            super.onLastmileProbeResult(result)
            Log.i(TAG, "onLastmileProbeResult --> $result")
        }

        /**
         * @param txQuality
         * Uplink transmission quality of the user in terms of the transmission bitrate, packet loss rate,
         * average RTT (Round-Trip Time) and jitter of the uplink network. txQuality is a quality rating
         * helping you understand how well the current uplink network conditions can support the selected
         * VideoEncoderConfiguration. For example, a 1000 Kbps uplink network may be adequate for video
         * frames with a resolution of 680 × 480 and a frame rate of 30 fps, but may be inadequate for
         * resolutions higher than 1280 × 720.
         *
         * QUALITY_UNKNOWN(0): The quality is unknown.
         * QUALITY_EXCELLENT(1): The quality is excellent.
         * QUALITY_GOOD(2): The quality is quite good, but the bitrate may be slightly lower than excellent.
         * QUALITY_POOR(3): Users can feel the communication slightly impaired.
         * QUALITY_BAD(4): Users can communicate not very smoothly.
         * QUALITY_VBAD(5): The quality is so bad that users can barely communicate.
         * QUALITY_DOWN(6): The network is disconnected and users cannot communicate at all.
         * QUALITY_DETECTING(8): The SDK is detecting the network quality.
         *
         * @param rxQuality
         * Downlink network quality rating of the user in terms of packet loss rate,
         * average RTT, and jitter of the downlink network.
         *
         * QUALITY_UNKNOWN(0): The quality is unknown.
         * QUALITY_EXCELLENT(1): The quality is excellent.
         * QUALITY_GOOD(2): The quality is quite good, but the bitrate may be slightly lower than excellent.
         * QUALITY_POOR(3): Users can feel the communication slightly impaired.
         * QUALITY_BAD(4): Users can communicate not very smoothly.
         * QUALITY_VBAD(5): The quality is so bad that users can barely communicate.
         * QUALITY_DOWN(6): The network is disconnected and users cannot communicate at all.
         * QUALITY_DETECTING(8): The SDK is detecting the network quality.
         */
        override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
            super.onNetworkQuality(uid, txQuality, rxQuality)
            Log.i(TAG, "onNetworkQuality --> $uid -- $txQuality -- $rxQuality")
        }

        override fun onLocalVideoStats(stats: LocalVideoStats?) {
            super.onLocalVideoStats(stats)
            Log.i(TAG, "onLocalVideoStats --> $stats")
        }

        override fun onRemoteVideoStats(stats: RemoteVideoStats?) {
            super.onRemoteVideoStats(stats)
            Log.i(TAG, "onRemoteVideoStats --> $stats")
        }

        override fun onLocalAudioStats(stats: LocalAudioStats?) {
            super.onLocalAudioStats(stats)
            Log.i(TAG, "onLocalAudioStats --> $stats")
        }

        override fun onRemoteAudioStats(stats: RemoteAudioStats?) {
            super.onRemoteAudioStats(stats)
            Log.i(TAG, "onRemoteAudioStats --> $stats")
        }

        override fun onLocalVideoStat(sentBitrate: Int, sentFrameRate: Int) {
            super.onLocalVideoStat(sentBitrate, sentFrameRate)
            Log.i(TAG, "onLocalVideoStat --> $sentBitrate -- $sentFrameRate")
        }

        override fun onRemoteVideoStat(uid: Int, delay: Int, receivedBitrate: Int, receivedFrameRate: Int) {
            super.onRemoteVideoStat(uid, delay, receivedBitrate, receivedFrameRate)
            Log.i(TAG, "onRemoteVideoStat --> $uid -- $delay -- $receivedBitrate -- $receivedFrameRate")
        }

        override fun onRemoteAudioTransportStats(uid: Int, delay: Int, lost: Int, rxKBitRate: Int) {
            super.onRemoteAudioTransportStats(uid, delay, lost, rxKBitRate)
            Log.i(TAG, "onRemoteAudioTransportStats --> $uid -- $delay -- $lost -- $rxKBitRate")
        }

        override fun onRemoteVideoTransportStats(uid: Int, delay: Int, lost: Int, rxKBitRate: Int) {
            super.onRemoteVideoTransportStats(uid, delay, lost, rxKBitRate)
            Log.i(TAG, "onRemoteVideoTransportStats --> $uid -- $delay -- $lost -- $rxKBitRate")
        }

        /**
         * @param state
         * MEDIA_ENGINE_AUDIO_EVENT_MIXING_PLAY(710): The audio mixing file is playing after the method call of startAudioMixing or resumeAudioMixing succeeds.
         * MEDIA_ENGINE_AUDIO_EVENT_MIXING_PAUSED(711): The audio mixing file pauses playing after the method call of pauseAudioMixing succeeds.
         * MEDIA_ENGINE_AUDIO_EVENT_MIXING_STOPPED(713): The audio mixing file stops playing after the method call of stopAudioMixing succeeds.
         * MEDIA_ENGINE_AUDIO_EVENT_MIXING_ERROR(714): An exception occurs during the playback of the audio mixing file. See the errorCode for details.
         *
         * @param errorCode
         * MEDIA_ENGINE_AUDIO_ERROR_MIXING_OPEN(701): the SDK cannot open the audio mixing file.
         * MEDIA_ENGINE_AUDIO_ERROR_MIXING_TOO_FREQUENT(702): the SDK opens the audio mixing file too frequently.
         * MEDIA_ENGINE_AUDIO_EVENT_MIXING_INTERRUPTED_EOF(703): the audio mixing file playback is interrupted.
         */
        override fun onAudioMixingStateChanged(state: Int, errorCode: Int) {
            super.onAudioMixingStateChanged(state, errorCode)
            Log.i(TAG, "onAudioMixingStateChanged --> $state -- $errorCode")
        }

        override fun onAudioMixingFinished() {
            super.onAudioMixingFinished()
            Log.i(TAG, "onAudioMixingFinished --> empty")
        }

        override fun onAudioEffectFinished(soundId: Int) {
            super.onAudioEffectFinished(soundId)
            Log.i(TAG, "onAudioEffectFinished -->  $soundId ")
        }

        override fun onFirstRemoteAudioDecoded(uid: Int, elapsed: Int) {
            super.onFirstRemoteAudioDecoded(uid, elapsed)
            Log.i(TAG, "onFirstRemoteAudioDecoded --> $uid -- $elapsed")
        }

        /**
         * @param state
         * LOCAL_AUDIO_STREAM_STATE_STOPPED(0): The local audio is in the initial state.
         * LOCAL_AUDIO_STREAM_STATE_CAPTURING(1): The recording device starts successfully.
         * LOCAL_AUDIO_STREAM_STATE_ENCODING(2): The first audio frame encodes successfully.
         * LOCAL_AUDIO_STREAM_STATE_FAILED(3): The local audio fails to start.
         *
         * @param error
         * LOCAL_AUDIO_STREAM_ERROR_OK(0): The local audio is normal.
         * LOCAL_AUDIO_STREAM_ERROR_FAILURE(1): No specified reason for the local audio failure.
         * LOCAL_AUDIO_STREAM_ERROR_DEVICE_NO_PERMISSION(2): No permission to use the local audio device.
         * LOCAL_AUDIO_STREAM_ERROR_DEVICE_BUSY(3): The microphone is in use.
         * LOCAL_AUDIO_STREAM_ERROR_CAPTURE_FAILURE(4): The local audio recording fails. Check whether the recording device is working properly.
         * LOCAL_AUDIO_STREAM_ERROR_ENCODE_FAILURE(5): The local audio encoding fails.
         */
        override fun onLocalAudioStateChanged(state: Int, error: Int) {
            super.onLocalAudioStateChanged(state, error)
            Log.i(TAG, "onLocalAudioStateChanged --> $state -- $error")
        }

        /**
         * @param localVideoState
         * LOCAL_VIDEO_STREAM_STATE_STOPPED(0): The local video is in the initial state.
         * LOCAL_VIDEO_STREAM_STATE_CAPTURING(1): The local video capturer starts successfully.
         * LOCAL_VIDEO_STREAM_STATE_ENCODING(2): The first local video frame encodes successfully.
         * LOCAL_VIDEO_STREAM_STATE_FAILED(3): The local video fails to start.
         *
         * @param error
         * LOCAL_VIDEO_STREAM_ERROR_OK(0): The local video is normal.
         * LOCAL_VIDEO_STREAM_ERROR_FAILURE(1): No specified reason for the local video failure.
         * LOCAL_VIDEO_STREAM_ERROR_DEVICE_NO_PERMISSION(2): No permission to use the local video device.
         * LOCAL_VIDEO_STREAM_ERROR_DEVICE_BUSY(3): The local video capturer is in use.
         * LOCAL_VIDEO_STREAM_ERROR_CAPTURE_FAILURE(4): The local video capture fails. C
         * heck whether the capturer is working properly.
         * - If your app runs in the background on a device running Android 9 or later, you cannot access the camera.
         * - If your app runs on a device running Android 6 or later, this error is reported if the camera
         *   is occupied by a third-part app and not property released. Once the camera is released, the SDK
         *   triggers this callback again, reporting state CAPTURING(1), and error ERROR_OK(0).
         * LOCAL_VIDEO_STREAM_ERROR_ENCODE_FAILURE(5): The local video encoding fails.
         */
        override fun onLocalVideoStateChanged(localVideoState: Int, error: Int) {
            super.onLocalVideoStateChanged(localVideoState, error)
            Log.i(TAG, "onLocalVideoStateChanged --> $localVideoState -- $error")
        }

        /**
         * @param state
         * RTMP_STREAM_PUBLISH_STATE_IDLE(0): The RTMP streaming has not started or has ended. This state is also
         *                                    triggered after you remove an RTMP address from the CDN by calling
         *                                    removePublishStreamUrl.
         * RTMP_STREAM_PUBLISH_STATE_CONNECTING(1): The SDK is connecting to Agora streaming server and the RTMP server.
         *                                          This state is triggered after you call the addPublishStreamUrl method.
         * RTMP_STREAM_PUBLISH_STATE_RUNNING(2): The RTMP streaming publishes. The SDK successfully publishes
         *                                       the RTMP streaming and returns this state.
         * RTMP_STREAM_PUBLISH_STATE_RECOVERING(3): The RTMP streaming is recovering. When exceptions occur to the CDN,
         *                                          or the streaming is interrupted, the SDK attempts to resume RTMP
         *                                          streaming and returns this state.
         * - If the SDK successfully resumes the streaming, RTMP_STREAM_PUBLISH_STATE_RUNNING(2) returns.
         * - If the streaming does not resume within 60 seconds or server errors occur,
         *    RTMP_STREAM_PUBLISH_STATE_FAILURE(4) returns. You can also reconnect to the server by calling the
         *    removePublishStreamUrl and addPublishStreamUrl methods.
         * RTMP_STREAM_PUBLISH_STATE_FAILURE(4): The RTMP streaming fails. See the errCode parameter for the
         *                                       detailed error information. You can also call the addPublishStreamUrl
         *                                       method to publish the RTMP streaming again.
         *
         * @param errCode
         * RTMP_STREAM_PUBLISH_ERROR_OK(0): The RTMP streaming publishes successfully.
         * RTMP_STREAM_PUBLISH_ERROR_INVALID_ARGUMEN(1): Invalid argument used. If, for example, you do not call the
         *                                               setLiveTranscoding method to configure the LiveTranscoding
         *                                               parameters before calling the addPublishStreamUrl method, the
         *                                               SDK returns this error. Check whether you set the parameters
         *                                               in the setLiveTranscoding method properly.
         * RTMP_STREAM_PUBLISH_ERROR_ENCRYPTED_STREAM_NOT_ALLOWED(2): The RTMP streaming is encrypted and cannot be published.
         * RTMP_STREAM_PUBLISH_ERROR_CONNECTION_TIMEOUT(3): Timeout for the RTMP streaming. Call the addPublishStreamUrl
         *                                                  method to publish the streaming again.
         * RTMP_STREAM_PUBLISH_ERROR_INTERNAL_SERVER_ERROR(4): An error occurs in Agora streaming server. Call the
         *                                                     addPublishStreamUrl method to publish the streaming again.
         * RTMP_STREAM_PUBLISH_ERROR_RTMP_SERVER_ERROR(5): An error occurs in the RTMP server.
         * RTMP_STREAM_PUBLISH_ERROR_TOO_OFTEN(6): The RTMP streaming publishes too frequently.
         * RTMP_STREAM_PUBLISH_ERROR_REACH_LIMIT(7): The host publishes more than 10 URLs. Delete the
         *                                           unnecessary URLs before adding new ones.
         * RTMP_STREAM_PUBLISH_ERROR_NOT_AUTHORIZED(8): The host manipulates other hosts' URLs. Check your app logic.
         * RTMP_STREAM_PUBLISH_ERROR_STREAM_NOT_FOUND(9): Agora server fails to find the RTMP streaming.
         * RTMP_STREAM_PUBLISH_ERROR_FORMAT_NOT_SUPPORTED(10): The format of the RTMP streaming URL is not supported.
         *                                                     Check whether the URL format is correct.
         */
        override fun onRtmpStreamingStateChanged(url: String?, state: Int, errCode: Int) {
            super.onRtmpStreamingStateChanged(url, state, errCode)
            Log.i(TAG, "onRtmpStreamingStateChanged --> $url -- $state -- $errCode ")
        }

        override fun onStreamPublished(p0: String?, p1: Int) {
            super.onStreamPublished(p0, p1)
            Log.i(TAG, "onStreamPublished --> $p0 -- $p1")
        }

        override fun onStreamUnpublished(p0: String?) {
            super.onStreamUnpublished(p0)
            Log.i(TAG, "onStreamUnpublished -->  $p0 ")
        }

        override fun onTranscodingUpdated() {
            super.onTranscodingUpdated()
            Log.i(TAG, "onTranscodingUpdated --> empty")
        }

        override fun onRtmpStreamingEvent(p0: String?, p1: Int) {
            super.onRtmpStreamingEvent(p0, p1)
            Log.i(TAG, "onRtmpStreamingEvent --> $p0 -- $p1")
        }

        override fun onStreamInjectedStatus(p0: String?, p1: Int, p2: Int) {
            super.onStreamInjectedStatus(p0, p1, p2)
            Log.i(TAG, "onStreamInjectedStatus --> $p0 -- $p1 -- $p2 ")
        }

        override fun onStreamMessage(p0: Int, p1: Int, p2: ByteArray?) {
            super.onStreamMessage(p0, p1, p2)
            Log.i(TAG, "onStreamMessage --> $p0 -- $p1 -- $p2 ")
        }

        override fun onStreamMessageError(p0: Int, p1: Int, p2: Int, p3: Int, p4: Int) {
            super.onStreamMessageError(p0, p1, p2, p3, p4)
            Log.i(TAG, "onStreamMessageError --> $p0 -- $p1 -- $p2 -- $p3 -- $p4")
        }

        override fun onMediaEngineLoadSuccess() {
            super.onMediaEngineLoadSuccess()
            Log.i(TAG, "onMediaEngineLoadSuccess --> empty")
        }

        override fun onMediaEngineStartCallSuccess() {
            super.onMediaEngineStartCallSuccess()
            Log.i(TAG, "onMediaEngineStartCallSuccess --> empty")
        }

        override fun onNetworkTypeChanged(p0: Int) {
            super.onNetworkTypeChanged(p0)
            Log.i(TAG, "onNetworkTypeChanged --> $p0")
        }
    }

    fun initEngineForVideoCall(context: Context) {
        // This is our usual steps for joining
        // a channel and starting a call.
        initializeEngine(context, mRtcVideoEventHandler)
        setupVideoConfig()
    }

    fun initEngineForAudioCall(context: Context) {
        initializeEngine(context, mIRtcAudioEventHandler)
        mRtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
        mRtcEngine?.setAudioProfile(
                Constants.AudioProfile.MUSIC_HIGH_QUALITY_STEREO.ordinal,
                Constants.AudioScenario.CHATROOM_ENTERTAINMENT.ordinal
        )
        setupAudioConfig(context)


    }

    private fun setupAudioConfig(context: Context) {

        mRtcEngine?.enableAudioVolumeIndication(500, 5, true)
        if (!(isBluetoothHeadsetConnected() || isHeadphonesPlugged(context))) {
            mRtcEngine?.setEnableSpeakerphone(true)
        }
        /* mRtcEngine?.adjustPlaybackSignalVolume(400)
         mRtcEngine?.enableInEarMonitoring(false)

         mRtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)*/
        //mRtcEngine?.setEnableSpeakerphone(true)
    }

    private fun isHeadphonesPlugged(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioDevices: Array<AudioDeviceInfo> = audioManager.getDevices(AudioManager.GET_DEVICES_ALL)
        for (deviceInfo in audioDevices) {
            if (deviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                    || deviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                return true
            }
        }
        return false
    }

    private fun isBluetoothHeadsetConnected(): Boolean {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled
                && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED;
    }

    private fun initializeEngine(context: Context, rtcEngineEventHandler: IRtcEngineEventHandler) {
        try {
            if (mRtcEngine == null)
                mRtcEngine = RtcEngine.create(context, context.getString(R.string.agora_app_id), rtcEngineEventHandler)
        } catch (e: Exception) {
            throw RuntimeException("NEED TO check rtc sdk init fatal error${Log.getStackTraceString(e)}".trimIndent())
        }
    }

    private fun setupVideoConfig() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.
        mRtcEngine?.enableVideo()

        // Please go to this page for detailed explanation
        // https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_rtc_engine.html#af5f4de754e2c1f493096641c5c5c1d8f
        mRtcEngine?.setVideoEncoderConfiguration(VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT))
        mRtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)
        mRtcEngine?.setEnableSpeakerphone(true)
    }

    private fun setupLocalVideo() {
    }

    fun startAudioCall(context: Context, userId: Int) {
        Log.i(TAG, "connectAudio: check_active_call: $mCallActive")
        if (!mCallActive) {
            initEngineForAudioCall(context)
            setupAudioConfig(context)
            startCall(context, userId, CallType.Audio)
        }
    }

    private var retryCount = 0
    fun startCall(context: Context, userId: Int, callTYpe: CallType = CallType.Video) {
        // 1. Users can only see each other after they join the
        // same channel successfully using the same app id.
        // 2. One token is only valid for the channel name that
        // you use to generate this token.
        if (!mCallActive) {
            val result = mRtcEngine?.joinChannel(agoraAccessToken, agoraChannelId, "Extra Optional Data", userId)
            if (result == -7 && callTYpe is CallType.Audio && retryCount < 3) {
                retryCount++
                releaseAgoraEngine()
                initEngineForAudioCall(context)
                startAudioCall(context, userId)
            }
            Log.i(TAG, "join channel $result")
            mCallActive = true
        }
    }

    private var mCallActive = false

    fun isCallActive() = mCallActive

    fun endCall() {
        val result = mRtcEngine?.leaveChannel()
        Log.i(TAG, "$result")
        mCallActive = false
//        RtcEngine.destroy()
        mRtcEngine = null
    }

    fun releaseAgoraEngine() {
        RtcEngine.destroy()
        mRtcEngine = null
    }

    fun getLocalView(context: Context): SurfaceView {
        if (mLocalView == null) {
            // This is used to set a local preview.
            // The steps setting local and remote view are very similar.
            // But note that if the local user do not have a uid or do
            // not care what the uid is, he can set his uid as ZERO.
            // Our server will assign one and return the uid via the event
            // handler callback function (onJoinChannelSuccess) after
            // joining the channel successfully.
            mLocalView = RtcEngine.CreateRendererView(context)
            mLocalView?.setZOrderMediaOverlay(true)
            // Initializes the local video view.
            // RENDER_MODE_HIDDEN: Uniformly scale the video until it fills the visible boundaries. One dimension of the video may have clipped contents.
            mRtcEngine?.setupLocalVideo(VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
        }
        return mLocalView!!
    }

    fun getRemoteView(context: Context, uid: Int): SurfaceView {
        if (mRemoteView == null) {
            /*
                Creates the video renderer view.
                CreateRendererView returns the SurfaceView type. The operation and layout of the view
                are managed by the app, and the Agora SDK renders the view provided by the app.
                The video display view must be created using this method instead of directly
                calling SurfaceView.
            */
            mRemoteView = RtcEngine.CreateRendererView(context)
            // Initializes the video view of a remote user.
            mRtcEngine?.setupRemoteVideo(VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
            mRemoteView?.tag = uid
        }
        return mRemoteView!!
    }

    fun switchCamera() {
        // Switches between front and rear cameras.
        mRtcEngine?.switchCamera()
    }

    fun muteLocalAudio() {
        mRtcEngine?.muteLocalAudioStream(true)
    }

    fun unMuteLocalAudio() {
        mRtcEngine?.muteLocalAudioStream(false)
    }

    fun muteRemoteAudio(uid: Int) {
        val result = mRtcEngine?.muteRemoteAudioStream(uid, true)
        Log.i(TAG, "muteRemoteAudio -- $result")
    }

    fun muteAllRemoteAudio() {
        mRtcEngine?.muteAllRemoteAudioStreams(true)
    }

    fun unMuteAllRemoteAudio() {
        mRtcEngine?.muteAllRemoteAudioStreams(false)
    }

    fun unmuteRemoteAudio(uid: Int) {
        val result = mRtcEngine?.muteRemoteAudioStream(uid, false)
        Log.i(TAG, "unmuteRemoteAudio -- $result")
    }

    fun toggleCall(context: Context, userId: Int) {
        if (mCallActive)
            endCall()
        else
            startAudioCall(context, userId)
    }

    fun switchToSpeaker(isEnabledSpeaker: Boolean) {
        mRtcEngine?.setEnableSpeakerphone(isEnabledSpeaker)
    }

    fun muteAudio(muted: Boolean) {
        mRtcEngine?.muteLocalAudioStream(muted)
    }

    fun endVideoCall() {
        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
        mCallActive = false
        mRtcEngine = null
    }

    fun isUserConnedcted(id: Int): Boolean {
        val userInfo = UserInfo()
        userInfo.uid = id
        val result = mRtcEngine?.getUserInfoByUid(id, userInfo)
        return result == 0 && userInfo.uid == id
    }

    companion object {
        const val TAG = "AgoraUtils"
    }
}

sealed class CallType {
    object Video : CallType()
    object Audio : CallType()
}
