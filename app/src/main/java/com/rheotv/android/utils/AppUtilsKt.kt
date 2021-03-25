package com.rheotv.android.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit


object AppUtilsKt {

    suspend fun downloadMediaFileFromRemote(remoteUrl: String?, outputFile: File): File? =
            withContext(Dispatchers.IO) {
                try {
                    val url = URL(remoteUrl)
                    url.openConnection().connect()
                    val input = url.openStream()
                    FileOutputStream(outputFile).runCatching {
                        val data = ByteArray(4096)
                        var total: Long = 0
                        var count: Int
                        while (input.read(data).also { count = it } != -1) {
                            // allow canceling with back button
                            if (!isActive) return@withContext null
                            total += count.toLong()
                            // publishing the progress....
                            write(data, 0, count)
                        }
                        flush()
                    }
                } catch (e: FileNotFoundException) {
                    return@withContext null
                } catch (e: IOException) {
                    return@withContext null
                } catch (e: IllegalArgumentException) {
                    return@withContext null
                }
                return@withContext outputFile
            }

    fun manageViewPagerDrag(viewpger: ViewPager2) {
        viewpger.reduceDragSensitivity()
    }

    fun getInternalMediaFile(filesDir: File?, fileName: String): File {
        val directory = File(filesDir, "media")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return File(directory, fileName)
    }

    fun convertDpToPx(context: Context?, dp: Float): Int {
        context ?: return 0
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    fun removeDirectoryHierarchy(fileDirectory: File) {
        if (fileDirectory.isDirectory) {
            for (child in fileDirectory.listFiles()) {
                removeDirectoryHierarchy(child)
            }
        }
        fileDirectory.delete()
    }

    fun isSharablePackage(it: ResolveInfo) =
            it.activityInfo.packageName.contains("whatsapp", ignoreCase = true) ||
                    it.activityInfo.packageName.contains("facebook", ignoreCase = true) ||
                    (it.activityInfo.packageName.contains("instagram", ignoreCase = true) &&
//                            !intentType.contains("gif", ignoreCase = true) &&
                            !it.activityInfo.name.equals("com.instagram.direct.share.handler.DirectShareHandlerActivity", ignoreCase = true) ||
                            it.activityInfo.packageName.contains("discord", ignoreCase = true))

    fun getInstalledAppPackages(context: Context?, activityIntent: Intent): List<ResolveInfo> = context
            ?.packageManager
            ?.queryIntentActivities(activityIntent, 0)
            ?.filter { isSharablePackage(it) }
            ?.distinctBy { it.loadLabel(context.packageManager).toString() } ?: listOf()

    fun isCustomRoomMessage(type: String?): Boolean {
        return listOf("customroom_created", "customroom_filled", "add_to_custom_room",
                "customroom_id_password", "custom_room_started", "custom_room_refunded",
                "customroom_refunded", "customroom_winner", "custom_room_start_time_updated").contains(type)
    }

    fun increaseFontSizeForPath(spannable: Spannable, path: String?, increaseTime: Float, color: Int? = null) {
        path ?: return
        val startIndexOfPath = spannable.toString().indexOf(path)
        color?.let {
            spannable.setSpan(ForegroundColorSpan(color), startIndexOfPath,
                    startIndexOfPath + path.length, 0)
        }
        spannable.setSpan(RelativeSizeSpan(increaseTime), startIndexOfPath,
                startIndexOfPath + path.length, 0)
    }

    fun boldFontSizeForPath(spannable: Spannable, path: String?, color: Int? = null) {
        path ?: return
        val startIndexOfPath = spannable.toString().indexOf(path)
        color?.let {
            spannable.setSpan(ForegroundColorSpan(color), startIndexOfPath,
                    startIndexOfPath + path.length, 0)
        }
        spannable.setSpan(StyleSpan(Typeface.BOLD), startIndexOfPath,
                startIndexOfPath + path.length, 0)
    }

    fun runGC() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(2000)
                Runtime.getRuntime()?.gc()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun runOnIO(action: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            Log.i("GlobalScope", "is global scope io")
            action.invoke()
        }
    }

    fun runOnMain(action: () -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            Log.i("GlobalScope", "is global scope main 2020-08-29 21:55:59.599 ")
            action.invoke()
        }
    }

    fun runOnIO(viewModel: ViewModel, action: () -> Unit) {
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            Log.i("GlobalScope", "is global scope io")
            action.invoke()
        }
    }

    fun runOnMain(viewModel: ViewModel, action: () -> Unit) {
        viewModel.viewModelScope.launch(Dispatchers.Main) {
            Log.i("GlobalScope", "is global scope main 2020-08-29 21:55:59.599 ")
            action.invoke()
        }
    }

    suspend fun runOnIOWithContext(dispatcher: CoroutineDispatcher, action: () -> Unit) {
        withContext(dispatcher) {
            action.invoke()
        }
    }


    fun <T> getDistinctValue(list: MutableList<T>, condition: (T) -> String): List<T> {
        return list.distinctBy { condition(it) }
    }

    fun <T, R> getTypedList(list: MutableList<T>, condition: (T) -> R): List<R> {
        return list.map { condition(it) }
    }

    fun <T> getCollectionToArrayOfString(collection: Collection<T>): String {
        return collection.joinToString(",", "[", "]") { "\"$it\"" }
    }

    fun addJavaClips(chipGroup: ChipGroup, list: List<String>) {
        chipGroup.addChips(list.toMutableList())
    }

    fun getTimeFromMillis(millis: Long) = millis.getTime(TimeUnit.MILLISECONDS)

    fun getMillisFromString(time : String) = time.getLongTime()
}

abstract class BackPressUpdateClickListener : View.OnClickListener {

    override fun onClick(v: View?) {
        EventBus.getDefault().post(EventBusModel.UpdateBackPress())
        onViewClick(v)
    }

    abstract fun onViewClick(v: View?)
}