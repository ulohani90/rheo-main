package com.rheotv.android.utils.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.rheotv.android.data.DataManager
import com.rheotv.android.db.ClipDao
import com.rheotv.android.db.ClipItem
import com.rheotv.android.factories.ChildWorkerFactory
import com.rheotv.android.utils.AppUtilsKt
import com.rheotv.android.utils.Downloader
import com.rheotv.android.utils.SharedPrefsUtils
import com.rheotv.android.utils.segmentTracker.SegmentConstants
import com.rheotv.android.utils.segmentTracker.SegmentTracker
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.*
import org.joda.time.DateTime
import org.joda.time.Duration
import java.io.File
import java.lang.Exception
import java.util.concurrent.TimeUnit
import kotlin.coroutines.suspendCoroutine

/**
 * IMPORTANT NOTE!
 *
 * The [Context] need to be named with [appContext] and [WorkerParameters] with [params]
 * as long as these name are identical with [ChildWorkerFactory.create]'s method parameters
 *
 */
class ClipSyncWorker @AssistedInject constructor(
        @Assisted private val appContext: Context,
        @Assisted params: WorkerParameters,
        val dataManager: DataManager,
        private val dao: ClipDao
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            Log.i(TAG, "Work started ${System.currentTimeMillis()}")
            val response: MutableList<Deferred<*>> = ArrayList()
            val clips = getClips()

            withContext(Dispatchers.IO) { dao.deleteAllClips() }

            AppUtilsKt.removeDirectoryHierarchy(File(appContext.filesDir, "rheo_clips"))

            clips?.forEach { response.add(async { saveClip(downloadClip(it)) }) }

            response.awaitAll()
            SegmentTracker.getInstance(appContext).trackEvent(SegmentConstants.EVENT_CLIP_SYNC, HashMap<String, Any>())
            Log.i(TAG, "Work completed ${System.currentTimeMillis()}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Result.success()
    }

    private fun getClips(): List<ClipItem>? {
        val call = dataManager.topClips
        val response = call.execute()
        return if (response.isSuccessful) response.body()?.result else null
    }

    private suspend fun downloadClip(clip: ClipItem): ClipItem =
            suspendCoroutine {
                Downloader(context = appContext, downloadLink = clip.videoUrl, subPath = "${clip.id}.mp4")
                { uri ->
                    clip.videoUrl = uri
                    it.resumeWith(result = kotlin.Result.success(clip))
                }
            }

    private suspend fun saveClip(clip: ClipItem) {
        withContext(Dispatchers.IO) {
            Log.i(TAG, "Clips Saved")
            dao.insertClip(clip)
        }
    }

    @AssistedInject.Factory
    interface Factory : ChildWorkerFactory

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {

        const val TAG = "ClipSyncWorker"
        const val TAG_SYNC_DATA = "sync_clips"
        const val SYNC_DATA_WORK_NAME = "work_clip"
        private const val SELF_REMINDER_HOUR = 24

        fun schedulePeriodicSync(context: Context) {
            val delay = if (DateTime.now().hourOfDay < SELF_REMINDER_HOUR) {
                Duration(DateTime.now(), DateTime.now().withTimeAtStartOfDay().plusHours(SELF_REMINDER_HOUR)).standardMinutes
            } else {
                Duration(DateTime.now(), DateTime.now().withTimeAtStartOfDay().plusDays(1).plusHours(SELF_REMINDER_HOUR)).standardMinutes
            }

            // Create Network constraint
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()

            val periodicSyncDataWork = PeriodicWorkRequest
                    .Builder(ClipSyncWorker::class.java, 1, TimeUnit.DAYS)
                    .addTag(TAG_SYNC_DATA)
                    .setConstraints(constraints)
                    // setting a backoff on case the work needs to retry
                    .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MINUTES)
                    .setInitialDelay(delay, TimeUnit.MINUTES)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    SYNC_DATA_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP, //Existing Periodic Work policy
                    periodicSyncDataWork //work request
            )
        }
    }
}