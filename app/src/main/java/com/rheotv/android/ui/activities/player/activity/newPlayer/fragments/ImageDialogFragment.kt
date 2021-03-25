package com.rheotv.android.ui.activities.player.activity.newPlayer.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.rheotv.android.R
import com.rheotv.android.data.network.models.objects.PostObject
import com.rheotv.android.data.network.requestLayer.ApiService
import com.rheotv.android.di.module.AppModule
import com.rheotv.android.helpers.WakefulAlarmReceiver
import com.rheotv.android.ui.activities.home.view.HomeActivity
import com.rheotv.android.ui.activities.player.activity.streamplayer.view.StreamPlayerActivity
import com.rheotv.android.utils.*
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ImageDialogFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FrameLayout(inflater.context)
    }

    private val properties: MutableMap<String, Any?> = hashMapOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rootLayoutParams = ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT,
                ViewGroup.MarginLayoutParams.MATCH_PARENT)
        view.layoutParams = rootLayoutParams
        val slotEventData = arguments?.getParcelable(SLOT_EVENT_DATA) as? PostObject
        properties["user_id"] = slotEventData?.author?.user?.id
        properties["title"] = slotEventData?.title
        properties["start_time"] = slotEventData?.reminderTime

        val startTime = TimeUtils.getDateFromString(slotEventData?.reminderTime ?: "",
                TimeUtils.YYYY_MM_DD_T_HH_MM_SS_SSSXXX) ?: Date()
        with(view as ViewGroup) {
            addView(ImageView(view.context).apply {
                minimumHeight = ViewUtils.dpToPx(250)
                scaleType = ImageView.ScaleType.CENTER_CROP
                BindingUtils.setImageUri(this, slotEventData?.thumbnail)
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT)
            })
            addView(MaterialButton(view.context, null, R.style.BaseButtonStyle).also {
                it.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.color_accent))
                it.setText(R.string.remind_me)
                it.setTextColor(ContextCompat.getColor(view.context, R.color.white_text_color))
                it.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD)
                it.elevation = ViewUtils.dpToPx(2).toFloat()
                it.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply {
                    bottomMargin = ViewUtils.dpToPx(36) + when (activity) {
                        is HomeActivity -> ViewUtils.dpToPx(56)
                        is StreamPlayerActivity -> ViewUtils.getNavBarHeight(context)
                        else -> 0
                    }
                }
                val padding = ViewUtils.dpToPx(12)
                it.setPadding(2 * padding, padding, 2 * padding, padding)
                it.cornerRadius = ViewUtils.dpToPx(24)
                it.setOnClickListener {
                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_REMIND_ME_BUTTON_CLICKED, properties)
//                    setShowAlarm(startTime, slotEventData)
                    setReminder(context, slotEventData)
                }
            })
        }
    }

    private fun setReminder(context: Context, slotEventData: PostObject?) {
        slotEventData?.id?.let { id ->
            val map = HashMap<Any?, Any?>()
            map["slot_banner_ids"] = listOf(id)
            map["source_type"] =  SegmentConstants.FEED_CARD
            buildEventService(context).setShowReminder(map).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful)
                        context.showToast("You will be notified 5 min before the stream!")
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                }
            })
        }
    }

    private fun setShowAlarm(startTime: Date, slotEventData: PostObject?) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = (startTime.time - 5 * 60 * 1000)
//                    calendar.timeInMillis = (System.currentTimeMillis() + 5 * 1000)

        val alarmManager =
                context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = Intent(context, WakefulAlarmReceiver::class.java).let { intent ->
            intent.putExtra(AppConstants.ARG_TITLE, slotEventData?.title)
            intent.putExtra(AppConstants.EVENT_IMAGE_URL, slotEventData?.thumbnail)
//                        intent.putExtra(AppConstants.EVENT_POST_ID, slotEventData?.id)
            intent.putExtra(AppConstants.USER_ID, slotEventData?.author?.user?.id)
            intent.putExtra(AppConstants.START_TIME, slotEventData?.reminderTime)
            intent.putExtra(AppConstants.SOURCE, SegmentConstants.FEED_CARD)
            PendingIntent.getBroadcast(context, 0, intent, 0)
        }
        if (alarmIntent != null && alarmManager != null) {
            alarmManager.cancel(alarmIntent)
        }
        alarmManager?.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                alarmIntent
        )
//                    SharedPrefsUtils().setStringPreference(context, AppConstants.EVENT_IMAGE_URL, slotEventData?.thumbnail)
//                    SharedPrefsUtils().setStringPreference(context, AppConstants.EVENT_POST_ID, slotEventData?.reminderTime)
        activity?.showToast("You will be notified 5 min before the stream!")
    }

    private fun buildEventService(context: Context): ApiService {
        val interceptor = AppModule.getServiceInterceptor(context)
        val httpLoggingInterceptor = AppModule.httpLoggingInterceptor()
        val cache = AppModule.provideCache(context)
        val client = AppModule.provideOkhttp(interceptor, httpLoggingInterceptor, cache)
        return AppModule.provideApiService(client, Gson())
    }

    override fun onResume() {
        super.onResume()
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_REMIND_ME_PAGE_SHOWED, properties)
    }

    companion object {

        const val TAG = "ImageDialogFragment"
        private const val SLOT_EVENT_DATA = "slot_event_data"
        fun newInstance(postObject: PostObject?): ImageDialogFragment =
                ImageDialogFragment().also {
                    it.arguments = Bundle().apply {
                        postObject?.let { obj -> putParcelable(SLOT_EVENT_DATA, obj) }
                    }
                }
    }
}