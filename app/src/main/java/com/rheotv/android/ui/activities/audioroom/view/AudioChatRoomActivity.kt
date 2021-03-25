package com.rheotv.android.ui.activities.audioroom.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.rheotv.android.R
import com.rheotv.android.services.AudioRoomService
import com.rheotv.android.ui.activities.audioroom.model.AudioGroup
import com.rheotv.android.ui.activities.home.view.HomeActivity
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.CommonUtils
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class AudioChatRoomActivity : AppCompatActivity(), HasAndroidInjector {

    @Inject
    lateinit var mFragmentDispatcher: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = mFragmentDispatcher

    private var mStartTime = System.currentTimeMillis()

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_chat_room)
        supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, AudioChatRoomFragment.newInstance(intent?.extras), AudioChatRoomFragment.TAG)
                .commit()
        mStartTime = System.currentTimeMillis()
    }

    override fun onBackPressed() {
        (supportFragmentManager.findFragmentByTag(AudioChatRoomFragment.TAG) as? AudioChatRoomFragment)?.also {
            if (it.mViewModel?.connectAudioLiveData?.value == true) {
                showBottomSheetDialog(it)
                return
            }
        }
        super.onBackPressed()
    }

    override fun onStop() {
        super.onStop()

    }

    private fun showBottomSheetDialog(audioChatRoomFragment: AudioChatRoomFragment) {
        val view: View = View.inflate(this, R.layout.exit_chat_room_alert_dialog_layout, null)
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        view.findViewById<MaterialButton>(R.id.stay_action).setOnClickListener {
            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_STAY_BUTTON_CLICKED,
                    HashMap(audioChatRoomFragment.mViewModel?.analyticsProperties ?: hashMapOf()))
            audioChatRoomFragment.registerBackPress()
            dialog.dismiss()
            audioChatRoomFragment.mViewModel?.isStayingConnected = true
            finish()
        }
        view.findViewById<MaterialButton>(R.id.exit_action).setOnClickListener {
            SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_CHAT_ROOM_EXIT_BUTTON_CLICKED,
                    HashMap(audioChatRoomFragment.mViewModel?.analyticsProperties
                            ?: hashMapOf()).apply {
                        put("connection_duration", (System.currentTimeMillis() - mStartTime) / 1000)
                        put("is_streamer_present", audioChatRoomFragment.mUserRecyclerAdapter.dataSet.contains(audioChatRoomFragment.mUserRecyclerAdapter.ownerDetail?.id))
                        put("on_click", "back_button")
                    })
            audioChatRoomFragment.mViewModel?.agoraConnectionUtils?.endCall()
            dialog.dismiss()
            if (audioChatRoomFragment.isFromDeeplink) {
                HomeActivity.startActivity(this,
                        bundleOf(AppConstants.SCREEN_SOURCE to SegmentConstants.SCREEN_NAME_AUDIO_CHAT_ROOM),
                        listOf(Intent.FLAG_ACTIVITY_CLEAR_TOP, Intent.FLAG_ACTIVITY_NEW_TASK, Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
            stopService(Intent(this@AudioChatRoomActivity, AudioRoomService::class.java))
            audioChatRoomFragment.mViewModel?.isStayingConnected = false
            finish()
        }
        dialog.setContentView(view)
        dialog.show()
    }


    companion object {
        const val ARG_GROUP_DETAILS = "group_details"
        const val ARG_ONLINE_COUNT = "online_count"
        const val ARG_CHAT_ROOM_ID = "chat_room_id"
        const val ARG_GROUP_ID = "group_id"
        fun startMe(context: Context, groupDetail: AudioGroup?, onlineMemberCount: Int?, chatRoomId: String?, screenSource: String?) = context.startActivity(Intent(context, AudioChatRoomActivity::class.java).apply {
            putExtras(Bundle().also {
                if (groupDetail != null) it.putParcelable(ARG_GROUP_DETAILS, groupDetail)
                if (onlineMemberCount != null) it.putInt(ARG_ONLINE_COUNT, onlineMemberCount)
                if (chatRoomId != null) it.putString(ARG_CHAT_ROOM_ID, chatRoomId)
                if (!screenSource.isNullOrEmpty()) it.putString(AppConstants.SCREEN_SOURCE, screenSource)
            })
        })

        fun startMe(context: Context, groupId: String?, onlineMemberCount: Int?, chatRoomId: String?, screenSource: String?, isFromDeeplink: Boolean = false) = context.startActivity(Intent(context, AudioChatRoomActivity::class.java).apply {
            putExtras(Bundle().also {
                if (groupId != null) it.putString(ARG_GROUP_ID, groupId)
                if (onlineMemberCount != null) it.putInt(ARG_ONLINE_COUNT, onlineMemberCount)
                if (chatRoomId != null) it.putString(ARG_CHAT_ROOM_ID, chatRoomId)
                it.putBoolean(AppConstants.ARG_FROM_DEEPLINK, isFromDeeplink)
                if (!screenSource.isNullOrEmpty()) it.putString(AppConstants.SCREEN_SOURCE, screenSource)
            })
        })
    }
}