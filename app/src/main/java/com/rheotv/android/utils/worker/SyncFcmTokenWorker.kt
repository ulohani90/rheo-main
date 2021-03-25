package com.rheotv.android.utils.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.freshchat.consumer.sdk.Freshchat
import com.google.firebase.messaging.FirebaseMessaging
import com.moengage.push.PushManager
import com.rheotv.android.data.DataManager
import com.rheotv.android.factories.ChildWorkerFactory
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.SharedPrefsUtils
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.suspendCoroutine

class SyncFcmTokenWorker @AssistedInject constructor(
        @Assisted private val appContext: Context,
        @Assisted params: WorkerParameters,
        val dataManager: DataManager,
        private val sharedPrefsUtils: SharedPrefsUtils
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork() = coroutineScope {
        try {
            val token = sharedPrefsUtils.getStringPreference(appContext, AppConstants.FCM_TOKEN)
            subscribeToTopic()
            syncToken(token)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun subscribeToTopic(): Unit =
            suspendCoroutine {
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_KEY)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.i(TAG, "fcm token sync success")
                                sharedPrefsUtils.setBooleanPreference(appContext, ARG_TOPIC, true)
                                it.resumeWith(result = kotlin.Result.success(Unit))
                            } else {
                                Log.i(TAG, "fcm token sync fail ${task.exception}")
                                it.resumeWith(result = kotlin.Result.failure(RuntimeException("Unable to subscribe to topic")))
                            }
                        }
            }

    private suspend fun syncToken(token: String?) = withContext(Dispatchers.Default) {
        if (token.isNullOrEmpty())
            return@withContext Result.failure()
        val response = dataManager.postFcmToken(token).execute()
        if (response.isSuccessful) {
            Freshchat.getInstance(appContext).setPushRegistrationToken(token)
            PushManager.getInstance().refreshToken(appContext, token)
            sharedPrefsUtils.setBooleanPreference(appContext, AppConstants.FCM_TOKEN_SENT, true)
            Result.success()
        } else
            Result.retry()
    }

    @AssistedInject.Factory
    interface Factory : ChildWorkerFactory

    companion object {
        val TAG: String = SyncFcmTokenWorker::class.java.simpleName
        private const val TAG_SYNC_TOKEN = "tag_sync_token"
        private const val ARG_TOPIC = "subscribed_all"
        private const val TOPIC_KEY = "all"

        fun syncToken(context: Context) {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()

            val worker = OneTimeWorkRequest
                    .Builder(SyncFcmTokenWorker::class.java)
                    .addTag(TAG_SYNC_TOKEN)
                    .setConstraints(constraints)
                    .build()

            WorkManager.getInstance(context)
                    .enqueue(worker)
        }
    }
}