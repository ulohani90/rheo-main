package com.rheotv.android.utils.worker

import android.app.NotificationManager
import android.content.Context
import androidx.work.*
import com.rheotv.android.db.AppPushNotification
import com.rheotv.android.db.PushNotificationDao
import com.rheotv.android.factories.ChildWorkerFactory
import com.rheotv.android.utils.TimeUtils
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class ClearNotificationWorker @AssistedInject constructor(
        @Assisted private val appContext: Context,
        @Assisted params: WorkerParameters,
        private val dao: PushNotificationDao
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = coroutineScope {
        val notifications = withContext(Dispatchers.IO) { dao.getNotification() }

        notifications.forEach {
            clearNotification(it)
            deleteNotificationRecord(it)
        }

        Result.success()
    }

    private suspend fun clearNotification(notification: AppPushNotification) {
        withContext(Dispatchers.Main) {
            if (System.currentTimeMillis() - notification.createAt >= 2 * TimeUtils.MILLIS_AN_HOUR) {
                val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(notification.id)
            }
        }
    }

    private suspend fun deleteNotificationRecord(notification: AppPushNotification) {
        withContext(Dispatchers.IO) {
            dao.deleteNotification(notification)
        }
    }

    @AssistedInject.Factory
    interface Factory : ChildWorkerFactory

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        const val TAG_CLEAR_NOTIFICATION = "tag_clear_notification"
        const val NAME_CLEAR_NOTIFICATION = "name_clear_notification"

        fun schedulePeriodicClearTask(context: Context) {
            // Create Network constraint
            val constraints = Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()

            val periodicSyncDataWork = PeriodicWorkRequest
                    .Builder(ClearNotificationWorker::class.java, 2, TimeUnit.HOURS)
                    .addTag(TAG_CLEAR_NOTIFICATION)
                    .setConstraints(constraints)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    NAME_CLEAR_NOTIFICATION,
                    ExistingPeriodicWorkPolicy.REPLACE, //Existing Periodic Work policy
                    periodicSyncDataWork //work request
            )
        }
    }
}