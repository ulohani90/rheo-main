package com.rheotv.android.ui.activities.onboarding.v2.view.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.facebook.FacebookSdk.getApplicationContext
import com.rheotv.android.BR
import com.rheotv.android.R
import com.rheotv.android.data.network.models.postlisting.responses.Author
import com.rheotv.android.data.network.models.postlisting.responses.User
import com.rheotv.android.databinding.FragmentOnBoardingStreamerSelectionBinding
import com.rheotv.android.helpers.WakefulAlarmReceiver
import com.rheotv.android.ui.activities.onboarding.v2.adapter.TopStreamerSelectionAdapter
import com.rheotv.android.ui.activities.onboarding.v2.model.ShowData
import com.rheotv.android.ui.activities.onboarding.v2.viewmodel.OnBoardingViewModel
import com.rheotv.android.ui.base.BaseFragment
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import com.rheotv.android.utils.showToast
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

class OnBoardingStreamerSelectionFragment : BaseFragment<FragmentOnBoardingStreamerSelectionBinding, OnBoardingViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    private var adapter: TopStreamerSelectionAdapter? = null

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_on_boarding_streamer_selection

    override fun getViewModel(): OnBoardingViewModel? {
        if (parentFragment?.isAdded == false || !isAdded)
            return null
        return ViewModelProvider(parentFragment
                ?: this, mViewModelFactory)[OnBoardingViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewDataBinding) {
            recyclerView.adapter = TopStreamerSelectionAdapter {
                viewModel?.userActionLiveData?.value =
                        if ((adapter?.selection?.size ?: 0) >= 1)
                            OnBoardingViewModel.UserAction.EnableNextButton
//                        else if ((adapter?.itemCount ?: 0) < 3 && (adapter?.selection?.size ?: 0) == (adapter?.itemCount ?: 0))
//                            OnBoardingViewModel.UserAction.EnableNextButton
                        else
                            OnBoardingViewModel.UserAction.DisableNextButton
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_TOP_SHOWS_CLICKED,
                        hashMapOf<String, Any?>(
                                "user_id" to it.author?.user?.id,
                                "author_name" to it.author?.user?.username,
                                "start_time" to it.startTime
                        ))
            }.also { adapter = it }

            skipButton.setOnClickListener {
                SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_TOP_SHOWS_SKIP_CLICKED, hashMapOf())
                viewModel?.userActionLiveData?.value = OnBoardingViewModel.UserAction.TopShowSelection
            }
        }

//        adapter?.submitList(
//                listOf(
//                        ShowData(
//                                "2020-10-06T20:42:00",
//                                "2020-10-13T23:59:00.000",
//                                "https://cdn.fastly.picmonkey.com/contentful/h6goo9gw1hh6/2sNZtFAWOdP1lmQ33VwRN3/24e953b920a9cd0ff2e1d587742a2472/1-intro-photo-final.jpg?w=800&q=70",
//                                "",
//                                Author(User(1364004,"rose"), "https://cdn.fastly.picmonkey.com/contentful/h6goo9gw1hh6/2sNZtFAWOdP1lmQ33VwRN3/24e953b920a9cd0ff2e1d587742a2472/1-intro-photo-final.jpg?w=800&q=70", 99),
//                                "Now in 10 min",
//                                "Welcome mates",
//                                "",
//                                0,
//                                ""
//                        ),
//
//                        ShowData(
//                                "2020-10-06T20:42:00",
//                                "2020-10-14T23:59:00.000",
//                                "https://cdn.fastly.picmonkey.com/contentful/h6goo9gw1hh6/2sNZtFAWOdP1lmQ33VwRN3/24e953b920a9cd0ff2e1d587742a2472/1-intro-photo-final.jpg?w=800&q=70",
//                                "COD 2",
//                                Author(User(833090,"don"), "https://cdn.fastly.picmonkey.com/contentful/h6goo9gw1hh6/2sNZtFAWOdP1lmQ33VwRN3/24e953b920a9cd0ff2e1d587742a2472/1-intro-photo-final.jpg?w=800&q=70", 99),
//                                "Now in 10 min",
//                                "Hello ",
//                                "",
//                                0,
//                                ""
//                        ),
//
//                        ShowData(
//                                "2020-10-06T19:42:00",
//                                "2020-10-17T23:59:00.000",
//                                "https://cdn.fastly.picmonkey.com/contentful/h6goo9gw1hh6/2sNZtFAWOdP1lmQ33VwRN3/24e953b920a9cd0ff2e1d587742a2472/1-intro-photo-final.jpg?w=800&q=70",
//                                "COD 3",
//                                Author(User(833090,"john"), "https://cdn.fastly.picmonkey.com/contentful/h6goo9gw1hh6/2sNZtFAWOdP1lmQ33VwRN3/24e953b920a9cd0ff2e1d587742a2472/1-intro-photo-final.jpg?w=800&q=70", 99),
//                                "Now in 10 min",
//                                "Hi Mates",
//                                "",
//                                0,
//                                ""
//                        )
//                )
//        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel ?: return
        with(viewModel) {
            this?.preferredLanguage?.observe(viewLifecycleOwner, {
                viewDataBinding.headerTextView.text = it?.most_awaited_shows
                viewDataBinding.messageTextView.text = it?.select_at_least_3
            })

            this?.userActionLiveData?.observe(viewLifecycleOwner, {
                if (it is OnBoardingViewModel.UserAction.SubmitLanguage)
                    viewModel?.fetchTopShows()
            })

            this?.topShows?.observe(viewLifecycleOwner, {
                it?.let { it1 -> adapter?.submitList(it1) }
            })

            this?.userActionLiveData?.observe(viewLifecycleOwner, {
                if (it == OnBoardingViewModel.UserAction.SubmitTopShow) {
                    SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_TOP_SHOWS_SUBMITTED, hashMapOf())
                    setShowReminder(adapter?.selection?.values?.mapNotNull { s -> s?.id })
//                    setShowReminder(listOf("aff9b4bf-6737-476d-a766-4d1543076cb7"))
                }
            })

            this?.fetchTopShows()
        }
    }

    private fun setAlarm() {
        GlobalScope.launch(Dispatchers.Main) {
            val response: MutableList<Deferred<*>> = ArrayList()
            try {
                adapter?.selection?.values?.forEach { show ->
                    response.add(async { setReminder(show) })
                }
                response.awaitAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) { activity?.showToast("You will be notified 5 min before the stream!") }
            delay(1000)
            withContext(Dispatchers.Main) {
                viewModel?.userActionLiveData?.value = OnBoardingViewModel.UserAction.TopShowSelection
            }
        }
    }

    private fun setReminder(show: ShowData?) {
        try {
            show ?: return
            Log.i(javaClass.simpleName, "set_reminder: " + show.startTime)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = (show.streamStartAt().time - 5 * 60 * 1000)

            val alarmManager =
                    context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val alarmIntent = Intent(context, WakefulAlarmReceiver::class.java).let { intent ->
                intent.putExtra(AppConstants.ARG_TITLE, show.title)
                intent.putExtra(AppConstants.EVENT_IMAGE_URL, show.slotImageUrl)
//            intent.putExtra(AppConstants.EVENT_POST_ID, show.postId)
                intent.putExtra(AppConstants.USER_ID, show.author?.user?.id)
                intent.putExtra(AppConstants.START_TIME, show.startTime)
                intent.putExtra(AppConstants.SOURCE, SegmentConstants.ONBOARDING_CARD)
                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                PendingIntent.getBroadcast(context,
                        Random.nextInt(0, Int.MAX_VALUE),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT)
            }
//        if (alarmIntent != null && alarmManager != null) {
//            alarmManager.cancel(alarmIntent)
//        }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager?.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        alarmIntent
                )
            } else {
                alarmManager?.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        alarmIntent
                )
            }
//        SharedPrefsUtils().setStringPreference(context, AppConstants.EVENT_IMAGE_URL, show.slotImageUrl)
//        SharedPrefsUtils().setStringPreference(context, AppConstants.EVENT_POST_ID, show.postId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setMultipleAlarms() {
        /*
      now we have 3 minutes for that
      30 40 50,we are going to set these minutes
      to multiple alarms
    */
        val minutes: MutableList<Int> = ArrayList()
        minutes.add(1)
        minutes.add(2)
        minutes.add(3)
        minutes.add(4)
        /*
      our alarmManager array size will be that minutes list size
    */
        val alarmManagers = arrayOfNulls<AlarmManager>(minutes.size)
        val intents = arrayOfNulls<Intent>(alarmManagers.size)
        for (i in alarmManagers.indices) {
            intents[i] = Intent(context, WakefulAlarmReceiver::class.java)
            /*
        Here is very important,when we set one alarm, pending intent id becomes zero
        but if we want set multiple alarms pending intent id has to be unique so i counter
        is enough to be unique for PendingIntent
      */
            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(context, i, intents[i], PendingIntent.FLAG_ONE_SHOT)
            val calendar = Calendar.getInstance()
            calendar[Calendar.MINUTE] = calendar[Calendar.MINUTE] + minutes[i]
            alarmManagers[i] = getApplicationContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManagers[i]!![AlarmManager.RTC_WAKEUP, calendar.timeInMillis] = pendingIntent
        }
    }

    override fun onResume() {
        super.onResume()
        SegmentTracker.getInstance().trackEvent(SegmentConstants.EVENT_TOP_SHOWS_CARD_SHOWN, HashMap())
    }
}