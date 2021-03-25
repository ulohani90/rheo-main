package com.rheotv.android.utils

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.KeyguardManager
import android.app.TimePickerDialog
import android.content.*
import android.content.ClipData
import android.content.Context.CLIPBOARD_SERVICE
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.provider.CalendarContract
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.rheotv.android.R
import com.rheotv.android.app.RheoTvApp
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat
import com.rheotv.android.db.AppDatabase
import com.rheotv.android.helpers.FileUploadServiceHelper
import com.rheotv.android.ui.activities.profile.model.Selectable
import com.rheotv.android.ui.fragments.UploadContactsDialogFragment
import com.rheotv.android.utils.AppConstants.*
import com.rheotv.android.utils.DownloadShareManager.isAppInstalled
import goChat.Services
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.size
import io.branch.referral.Branch
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.io.*
import java.net.URLEncoder
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

fun Context.copyToClipBoard(text: String?) {
    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("rheo_text", text)
    clipboard.setPrimaryClip(clip)
}

fun Context.showToast(text: String?) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

fun Context.showToast(resId: Int) = Toast.makeText(this, getString(resId), Toast.LENGTH_LONG).show()

fun Context.randomString(resId: Int) = resources?.getStringArray(resId)?.let { it[Random.nextInt(it.size)] }

fun Context.randomFromArrays(key: Int, value: Int): Pair<String, String> {
    val keyRes = resources?.getStringArray(key)
    val valueRes = resources?.getStringArray(value)
    val index = Random.nextInt(keyRes?.size ?: 0)
    return Pair(keyRes?.get(index) ?: "", valueRes?.get(index) ?: "")
}

internal fun View?.findSuitableParent(): ViewGroup? {
    var view = this
    var fallback: ViewGroup? = null
    do {
        if (view is CoordinatorLayout) {
            // We've found a CoordinatorLayout, use it
            return view
        } else if (view is FrameLayout) {
            if (view.id == android.R.id.content) {
                // If we've hit the decor content view, then we didn't find a CoL in the
                // hierarchy, so use it.
                return view
            } else {
                // It's not the content view but we'll use it as our fallback
                fallback = view
            }
        }

        if (view != null) {
            // Else, we will loop and crawl up the view hierarchy and try to find a parent
            val parent = view.parent
            view = if (parent is View) parent else null
        }
    } while (view != null)

    // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
    return fallback
}

suspend fun <T> retryIO(
        times: Int = Int.MAX_VALUE, // number of attempts to be made
        initialDelay: Long = 100, // 0.1 second
        maxDelay: Long = 1000,    // 1 second
        factor: Double = 2.0,
        block: suspend () -> T): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: IOException) {
            // you can log an error here and/or make a more finer-grained
            // analysis of the cause to see if retry is needed
            e.printStackTrace()
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}

fun <V> Map<String, V>.toBundle(bundle: Bundle = Bundle()): Bundle = bundle.apply {
    forEach {
        val k = it.key
        when (val v = it.value) {
            is IBinder -> putBinder(k, v)
            is Bundle -> putBundle(k, v)
            is Byte -> putByte(k, v)
            is ByteArray -> putByteArray(k, v)
            is Char -> putChar(k, v)
            is CharArray -> putCharArray(k, v)
            is CharSequence -> putCharSequence(k, v)
            is Float -> putFloat(k, v)
            is FloatArray -> putFloatArray(k, v)
            is Parcelable -> putParcelable(k, v)
            is Short -> putShort(k, v)
            is ShortArray -> putShortArray(k, v)
            else -> throw IllegalArgumentException("$v is of a type that is not currently supported")
        }
    }
}

fun String.ellipse(limit: Int): String? {
    if (isNullOrEmpty()) return null
    return if (length < limit) this else "${substring(0, limit)}..."
}

fun String.toUnixTime(): Long {
    val dateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
    return dateParser.parse(this).time
}

fun Context.getContextDrawable(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)

fun Activity.openPlayStore() {
    val viewIntent = Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=com.rheotv.android"))
    startActivity(viewIntent)
    finish()
}

fun ImageView.getColorAnimator(startColor: Int, endColor: Int, animationDuration: Long): ValueAnimator =
        ObjectAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
                .apply {
                    duration = animationDuration
                    addUpdateListener {
                        imageTintList = ColorStateList.valueOf(it.animatedValue as Int)
                    }
                }

fun View.getRotateAnimator(fromDegree: Float, toDegree: Float, animationDuration: Long): ValueAnimator =
        ObjectAnimator.ofFloat(this, View.ROTATION, fromDegree, toDegree).apply {
            duration = animationDuration
            addUpdateListener {
                rotation = it.animatedValue as Float
            }
        }

fun View.getExpandCollapseAnimator(startHeight: Int, endHeight: Int, animationDuration: Long): ValueAnimator =
        ObjectAnimator.ofInt(startHeight, endHeight).apply {
            val expanding = endHeight > startHeight
            duration = animationDuration
            addUpdateListener {
//                layoutParams.height = (if (expanding) it.animatedValue as Float else startHeight - it.animatedValue as Float).toInt()
                layoutParams.height = (it.animatedValue as Int)
                requestLayout()
            }
        }

fun ImageView.startColorAnimation(startColor: Int, endColor: Int, animationDuration: Long) {
    getColorAnimator(startColor, endColor, animationDuration).start()
}

fun Context.registerNetworkReceiver(receiver: BroadcastReceiver) {
    registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
}


fun RecyclerView.onEndPageReachedListener(onEndReached: () -> Unit, onFirstReach: (() -> Unit)? = null) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManager?.childCount ?: 0
            val totalItemCount = layoutManager?.itemCount ?: 0
            val firstVisibleItemPosition = (layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                    ?: 0
            if (totalItemCount >= 10 && visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                onEndReached.invoke()
            }

            if (firstVisibleItemPosition == 0)
                onFirstReach?.invoke()
        }
    })
}

fun Context.stickerDimension(): Int {
    val timePickerDialog = TimePickerDialog(RheoTvApp.getNonUiContext(), 0, null, 0, 0, false)
    return if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
        (resources.displayMetrics.widthPixels - (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics)).toInt()) / 3
    else
        (resources.displayMetrics.widthPixels / 2 - (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics)).toInt()) / 3
}

fun Services.ChatMessage.toCommentChat() = CommentChat("", message, sender, profilePic, msgType, CommentChat.Type.Normal)

fun Context.signOut() {
    Branch.getInstance().logout()
    FirebaseAuth.getInstance().signOut()
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    try {
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(this@signOut).clearAllTables()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    mGoogleSignInClient.signOut()
    SharedPrefsUtils().signOut(this)
    EventBus.getDefault().post(EventBusModel.LogoutSuccess)
}

fun Context.color(resId: Int) = ContextCompat.getColor(this, resId)

fun Spinner.onItemSelected(onSelection: (Any?) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(p0: AdapterView<*>?) {

        }

        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            onSelection.invoke(p0?.selectedItem)
        }
    }
}

fun CharSequence?.isNullOrEmptyOrBlank() = this == null || this.isBlank() || this.isEmpty()

fun ChipGroup.addChips(collection: MutableList<Selectable>?, resId: Int = R.attr.chipChoiceStyle, onChipClick: ((Selectable) -> Unit)? = null) {
    collection ?: return
    for (c in collection) {
        val chip = Chip(context, null, resId)
        chip.tag = c.tag
        chip.text = c.text
        chip.isChecked = c.isSelected
//        if (c.text?.length ?: 0 < 2)
//            chip.chipStartPadding = CommonUtils.toPix(10).toFloat()
        chip.setOnCheckedChangeListener { _, _ ->
            c.isSelected = !c.isSelected
            onChipClick?.invoke(c)
        }
        addView(chip)
    }
}

fun ChipGroup.addChips(collections: MutableList<String>?, onChipClick: ((String) -> Unit)? = null, resId: Int = R.attr.chipSuggestionStyle) {
    collections ?: return
    for (c in collections) {
        val chip = Chip(context, null, resId)
        chip.tag = c
        chip.text = c
        chip.isCheckable = false
        if (c.length < 2)
            chip.chipStartPadding = CommonUtils.toPix(10).toFloat()
        chip.setOnClickListener { onChipClick?.invoke(c) }
        addView(chip)
    }
}

fun ChipGroup.addChips(collections: MutableList<String>?, resId: Int = R.attr.captionChipStyle) {
    collections ?: return
    for (c in collections) {
        val chip = Chip(context, null, resId)
        chip.tag = c
        chip.text = c
        chip.isCheckable = false
        if (c.length < 2)
            chip.chipStartPadding = CommonUtils.toPix(10).toFloat()
        addView(chip)
    }
}

fun Fragment.isAllPermissionsGranted(vararg permission: String): Boolean {
    permission.forEach {
        context?.also { ctx ->
            if (ActivityCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED)
                return false
        }
    }
    return true
}

fun Fragment.chooseImageAndVideo(code: Int = CHOOSE_IMAGE_VIDEO) {
    chooseFile(code, "image/* video/*")
}

fun Fragment.chooseMedia(code: Int = CHOOSE_MEDIA) {
    chooseFile(code, "*/*")
}


fun Fragment.chooseImage(code: Int = CHOOSE_IMAGE) {
    chooseFile(code, "image/*")
}

fun Fragment.chooseAudio(code: Int = CHOOSE_AUDIO) {
    chooseFile(code, "audio/*")
}

fun Fragment.chooseFile(code: Int, mimeType: String) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isAllPermissionsGranted(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 102)
                return
            }
        }

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = mimeType
        this.startActivityForResult(intent, code)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        context?.showToast("No App found to handle this")
    }
}

fun File.multipartFromUri(): MultipartBody.Part {
    val requestFile = RequestBody.create(MediaType.parse(mimeType()), this)
    return MultipartBody.Part.createFormData("file", URLEncoder.encode(name, "utf-8"), requestFile)
}

val File.extension get() = MimeTypeMap.getFileExtensionFromUrl(toString())

fun File.mimeType(fallback: String = "*/*"): String {
    return MimeTypeMap.getFileExtensionFromUrl(toString())
            ?.run { MimeTypeMap.getSingleton().getMimeTypeFromExtension(toLowerCase()) }
            ?: fallback
}

fun Fragment.fadeIn(view: View) {
    val animation = AlphaAnimation(0.0f, 1f)
    animation.duration = 1000
    animation.startOffset = 50
    animation.fillAfter = true
    view.visibility = View.VISIBLE
    view.startAnimation(animation)
}

fun Fragment.fadeOut(view: View) {
    val animation = AlphaAnimation(1.0f, 0f)
    animation.duration = 1000
    animation.startOffset = 50
    animation.fillAfter = true
    view.visibility = View.INVISIBLE
    view.startAnimation(animation)
}

fun Fragment.setReminder(
        eventId: Int = 100000000,
        title: String = "Reminder",
        description: String = "Live On Rheo",
        frequency: String = "WEEKLY",
        weekDays: String = "MO,TU",
        from: Date,
        to: Date
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (!isAllPermissionsGranted(Manifest.permission.READ_CALENDAR)) {
            requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 111)
            return
        }
    }

    startActivity(Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Reminders.EVENT_ID, eventId)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, from.time)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, to.time)
            .putExtra(CalendarContract.Events.TITLE, title)
            .putExtra(CalendarContract.Events.DESCRIPTION, description)
            .putExtra(CalendarContract.Events.ALL_DAY, false)
            .putExtra(CalendarContract.Events.HAS_ALARM, true)
            .putExtra(CalendarContract.Reminders.MINUTES, 15)
            .putExtra(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_DEFAULT)
            .putExtra(CalendarContract.Events.RRULE, "FREQ=$frequency;BYDAY=$weekDays"))
    /* try {
         val event = ContentValues()
         event.put(CalendarContract.Events.CALENDAR_ID, eventId)
         event.put(CalendarContract.Events.TITLE, title)
         event.put(CalendarContract.Events.DESCRIPTION, description)
         event.put(CalendarContract.Events.DTSTART, from.time)
         event.put(CalendarContract.Events.DTEND, to.time)
         event.put(CalendarContract.Events.HAS_ALARM, true)
         event.put(CalendarContract.Events.RRULE, "FREQ=$frequency;BYDAY=$weekDays")
         event.put(CalendarContract.Events.EVENT_TIMEZONE, "GMT-05:00")

         val url = context?.contentResolver?.insert(CalendarContract.Events.CONTENT_URI, event)
         url ?: return
         val eventId = parseLong(url?.lastPathSegment)
         val reminder = ContentValues()
         reminder.put(CalendarContract.Reminders.EVENT_ID, eventId)
         reminder.put(CalendarContract.Reminders.MINUTES, 10)
         reminder.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
         context?.contentResolver?.insert(CalendarContract.Reminders.CONTENT_URI, reminder)
         context?.showToast("Event Added To Your Calendar!")
     } catch (e: SQLiteException) {
         e.printStackTrace()
     }*/
}

fun Context.addEvent() {
    val event = ContentValues()
    val startcalendar = Calendar.getInstance()
    val endcalendar = Calendar.getInstance()
    event.put(CalendarContract.Events.CALENDAR_ID, 1)
    event.put(CalendarContract.Events.TITLE, "Reminder")
    event.put(CalendarContract.Events.DESCRIPTION, " test")
    event.put(CalendarContract.Events.DTSTART, startcalendar.timeInMillis)
    event.put(CalendarContract.Events.HAS_ALARM, true)
    event.put(CalendarContract.Events.RRULE, "FREQ=WEEKLY;BYDAY=MO,TU")
    event.put(CalendarContract.Events.HAS_ALARM, true)
    event.put(CalendarContract.Events.EVENT_TIMEZONE, "GMT-05:00")
    event.put(CalendarContract.Events.DURATION, "PT1D")

    val url = contentResolver.insert(CalendarContract.Events.CONTENT_URI, event)
    val eventId = java.lang.Long.parseLong(url?.lastPathSegment!!)
    val reminder = ContentValues()
    reminder.put(CalendarContract.Reminders.EVENT_ID, eventId)
    reminder.put(CalendarContract.Reminders.MINUTES, 10)
    reminder.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
    contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminder)
}

fun Context.openLink(url: String?, packageName: String? = null) {
    try {
        url ?: return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (!packageName.isNullOrEmpty() && isAppInstalled(packageName))
            intent.setPackage(packageName)
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            showToast("No Application found")
        }
    }
}

fun Context.uploadFile(fileUri: String?, serverUrl: String?, storageType: Int = AZURE_STORAGE, mimeType: String? = null, shouldCompress: Boolean = false) {
    fileUri ?: serverUrl ?: return
    val uploadIntent = Intent(this, FileUploadServiceHelper::class.java)
    uploadIntent.putExtra(VIDEO_FILE_NAME, fileUri)
    uploadIntent.putExtra(UPLOAD_URL_VIDEO, serverUrl)
    mimeType?.let { uploadIntent.putExtra(MIME_TYPE, it) }
    uploadIntent.putExtra(SHOULD_COMPRESS, shouldCompress)
    uploadIntent.putExtra(STORAGE_TYPE, storageType)
    uploadIntent.action = FileUploadServiceHelper.ACTION_START_FOREGROUND_SERVICE
    startService(uploadIntent)
}

fun File.getMediaDuration(): Long {
    if (!exists()) return 0L
    val mediaMetadataRetriever = MediaMetadataRetriever()
    mediaMetadataRetriever.setDataSource(absolutePath)
    return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
            ?: 0
}

fun Context.showTimePicker(onEmit: (displayTime: Date, actualTime: Date) -> Unit) {
    val calendar: Calendar = Calendar.getInstance()
    val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
    val minutes: Int = calendar.get(Calendar.MINUTE)
    val picker = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, sHour, sMinute ->
                try {
                    val datetime = Calendar.getInstance()
                    datetime[Calendar.HOUR_OF_DAY] = sHour
                    datetime[Calendar.MINUTE] = sMinute
                    datetime[Calendar.SECOND] = 0
                    onEmit(Date(datetime.timeInMillis), if (Calendar.getInstance().after(datetime)) {
                        datetime.set(Calendar.DATE, datetime.get(Calendar.DATE) + 1)
                        datetime.time
                    } else datetime.time)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, hour, minutes, false)
    picker.show()
}

fun Date.format(format: String): String {
    return try {
        SimpleDateFormat(format, Locale.getDefault()).format(this)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        ""
    }
}

fun String.toBold(startPosition: Int, endPosition: Int): SpannableString {
    return SpannableString(this).also {
        it.setSpan(StyleSpan(Typeface.BOLD), startPosition, endPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

fun Fragment.addBackPressCallback(listener: (() -> Unit)? = null) {
    val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            try {
                if (navController()?.currentBackStackEntry != null) {
                    listener?.invoke()
                    navController()?.popBackStack()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                listener?.invoke()
            }
        }
    }
    activity?.onBackPressedDispatcher?.addCallback(this, callback)
}

fun Fragment.showSyncContactDialog() {
    if (!CommonUtils.getContactAllowed()) return
    if (!CommonUtils.getHideSyncContacts()) {
        val sharedPrefsUtils = SharedPrefsUtils()
        val lastShownTS: Long = sharedPrefsUtils.getLongPreference(activity, SharedPrefsUtils.LAST_UPLOAD_CONTACTS_SHOWN_TIME, -1)
        if (!CommonUtils.getContactsUploadSuccess() && (lastShownTS == -1L || System.currentTimeMillis() - lastShownTS >= TimeUtils.MILLIS_IN_DAY)) {
            if (!isAdded || isStateSaved || !isVisible) return
            val fragment = UploadContactsDialogFragment.newInstance()
            activity?.supportFragmentManager?.beginTransaction()?.add(fragment, AppConstants.UPLOAD_CONTACTS_DIALOG_FRAGMENT)?.commitAllowingStateLoss()
            sharedPrefsUtils.setLongPreference(activity, SharedPrefsUtils.LAST_UPLOAD_CONTACTS_SHOWN_TIME, System.currentTimeMillis())
        }
    } else {
        CommonUtils.setHideSyncContacts(false)
    }
}

fun View.getPreview(): File? {
    isDrawingCacheEnabled = true
    val b: Bitmap = drawingCache
    val myPath = AppUtilsKt.getInternalMediaFile(context.filesDir, "${System.currentTimeMillis()}_profile.jpg")
    val fos: FileOutputStream?
    try {
        fos = FileOutputStream(myPath)
        b.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        return null
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }

    return myPath
}

fun View.getBitmap(): Bitmap? {
    isDrawingCacheEnabled = true
    return drawingCache
}

/**
 * Reduces drag sensitivity of [ViewPager2] widget
 */
fun ViewPager2.reduceDragSensitivity() {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView

    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    touchSlopField.set(recyclerView, touchSlop * 2)       // "8" was obtained experimentally
}

fun Context.loadBitmap(url: String, onLoadFinish: (Bitmap?) -> Unit) {
    Glide.with(this).asBitmap()
            .load(url)
            .addListener(object : RequestListener<Bitmap?> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap?>?, isFirstResource: Boolean): Boolean {
                    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.popup_back)
                    onLoadFinish.invoke(bitmap)
                    return false
                }

                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap?>?, dataSource: com.bumptech.glide.load.DataSource?, isFirstResource: Boolean): Boolean {
                    onLoadFinish.invoke(resource)
                    return true
                }
            }).submit()
}

abstract class DebouncedOnClickListener() : View.OnClickListener {
    var previousClickTimeMillis = 0L
    val DELAY_MILLIS = 500L

    /**
     * Implement this in your subclass instead of onClick
     * @param v The view that was clicked
     */
    abstract fun onDebouncedClick(v: View?)
    override fun onClick(clickedView: View) {
        val currentTimeMillis = System.currentTimeMillis()

        Log.i("****", "time elapsed ---> ${currentTimeMillis - previousClickTimeMillis}")
        if (currentTimeMillis >= previousClickTimeMillis + DELAY_MILLIS) {
            Log.i("****", "button clicked")
            onDebouncedClick(clickedView)
        }
        previousClickTimeMillis = currentTimeMillis
    }
}

fun Fragment.navController() = if (isAdded) findNavController() else null

fun CoroutineScope.doAfter(delay: Long, task: () -> Unit) {
    launch {
        delay(delay)
        withContext(Dispatchers.Main) {
            task.invoke()
        }
    }
}

fun Activity.turnScreenOnAndKeyguardOff() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    } else {
        window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    }

    with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestDismissKeyguard(this@turnScreenOnAndKeyguardOff, null)
        }
    }
}

fun Activity.turnScreenOffAndKeyguardOn() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(false)
        setTurnScreenOn(false)
    } else {
        window.clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    }
}

infix fun <T> Boolean.then(value: T?) = TernaryExpression(this, value)

class TernaryExpression<out T>(val flag: Boolean, val truly: T?) {
    infix fun <T> or(falsy: T?) = if (flag) truly else falsy
}

fun Context.showConfirmBottomSheetDialog(
        title: String = "Are you sure?",
        message: String? = null,
        spannableMessage: SpannableString? = null,
        confirmLabel: String = "Confirm",
        denyLabel: String = "Cancel",
        onConfirm: (() -> Unit)? = null,
        onDeny: (() -> Unit)? = null
) {
    val view: View = View.inflate(this, R.layout.exit_chat_room_alert_dialog_layout, null)
    val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
    view.findViewById<TextView>(R.id.header).apply {
        text = title
    }
    view.findViewById<TextView>(R.id.sub_header).apply {
        text = message ?: spannableMessage
    }
    view.findViewById<MaterialButton>(R.id.stay_action).apply {
        text = confirmLabel
        setOnClickListener {
            onConfirm?.invoke()
            dialog.dismiss()
        }
    }
    view.findViewById<MaterialButton>(R.id.exit_action).apply {
        text = denyLabel
        setOnClickListener {
            onDeny?.invoke()
            dialog.dismiss()
        }
    }
    dialog.setContentView(view)
    dialog.show()
}

inline val Fragment.TAG: String
    get() = this.javaClass.simpleName

val File.size get() = if (!exists()) 0.0 else length().toDouble()
val File.sizeInKb get() = size / 1024
val File.sizeInMb get() = sizeInKb / 1024
val File.sizeInGb get() = sizeInMb / 1024
val File.sizeInTb get() = sizeInGb / 1024

fun String?.asFile(): File? {
    return asUri()?.asFile()
}

fun Uri.asFile(): File = File(toString())

fun String?.asUri(): Uri? {
    try {
        return Uri.parse(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

val String?.mimeType
    get() = when (this?.substringAfterLast(".")?.toLowerCase()) {
        "mp4" -> VIDEO
        "mpg" -> VIDEO
        "flp" -> VIDEO
        "wmv" -> VIDEO
        "mkv" -> VIDEO
        "ogv" -> VIDEO
        "3gp" -> VIDEO
        "webp" -> IMAGE // stickers format on server
        "jpeg" -> IMAGE
        "png" -> IMAGE
        "svg" -> IMAGE
        else -> IMAGE
    }

// todo add size limit
suspend fun File.compress(context: Context, onCompress: ((String) -> Unit), onProgress: ((Int) -> Unit)) {
    when (mimeType().substringBefore("/").toUpperCase()) {
        IMAGE -> {
            val compressedImageFile = Compressor.compress(context, this) {
                quality(80)
                format(Bitmap.CompressFormat.WEBP)
                size(2_097_152) // 2 MB
            }
            onCompress.invoke(compressedImageFile.absolutePath)
        }

        else -> {
            // todo add compression for video
            onCompress.invoke(absolutePath)
//            val destPath = AppUtilsKt.getInternalMediaFile(context.filesDir, "${System.currentTimeMillis()}_video.${this.extension ?: ".mp4"}").absolutePath
//            onCompress.invoke(SiliCompressor.with(context).compressVideo(absolutePath, destPath))
//            val destPath = getFile(this.path) ?: return
//            VideoCompressor.start(
//                    absolutePath,
//                    destPath?.absolutePath,
//                    object : CompressionListener {
//                        override fun onProgress(percent: Float) {
//                            // Update UI with progress value
//                            onProgress.invoke(percent.toInt())
//                        }
//
//                        override fun onStart() {
//                            // Compression start
//                        }
//
//                        override fun onSuccess() {
//                            onCompress.invoke(destPath?.absolutePath)
//                        }
//
//                        override fun onFailure(failureMessage: String) {
//                            // On Failure
//                        }
//
//                        override fun onCancelled() {
//                            // On Cancelled
//                        }
//
//                    }, VideoQuality.MEDIUM, isMinBitRateEnabled = false, keepOriginalResolution = false)
        }
    }
}

fun getFile(filePath: String?): File? {
    filePath?.let {
        val videoFile = File(filePath)
        val videoFileName = "${System.currentTimeMillis()}_${videoFile.name}"
        val folderName = Environment.DIRECTORY_MOVIES
        val downloadsPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val desFile = File(downloadsPath, videoFileName)
        if (desFile.exists())
            desFile.delete()
        try {
            desFile.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return desFile
    }
    return null
}

fun Long.getTime(timeUnit: TimeUnit, isLongTime: Boolean = false): String {
    val stringBuilder = StringBuilder()
    val decimalFormat = DecimalFormat("00")
    stringBuilder.append(if (timeUnit.toHours(this) > 0) String.format("%02d", timeUnit.toHours(this) % 24) else "00")
    stringBuilder.append(if (timeUnit.toMinutes(this) > 0) ":${String.format("%02d", timeUnit.toMinutes(this) % 60)}" else ":00")
    stringBuilder.append(if (timeUnit.toSeconds(this) > 0) ":${String.format("%02d", timeUnit.toSeconds(this) % 60)}" else ":00")
    if (isLongTime) {
        stringBuilder.append(if (timeUnit.toSeconds(this) > 0) ".${timeUnit.toMillis(this) % 1000}" else ".000")
    }
    return stringBuilder.toString()
}

fun String.getLongTime(): Long {
    val timeParts = split(":").map { it.trim() }.toMutableList()
    var time = 0L
    val iterator = timeParts.reversed().toMutableList().iterator()
    var iterationCount = 0
    while (iterator.hasNext()) {
        val item = iterator.next()
        if (item.contains(".")) {
            time += item.split(".").last().toLong()
            time += item.split(".").first().toLong() * 1000
        } else {
            try {
                println(item)
                time += if (iterationCount < 1) {
                    item.toLong() * 1000
                } else if (iterationCount < 2) {
                    item.toLong() * 60 * 1000
                } else if (iterationCount < 3) {
                    item.toLong() * 60 * 60 * 1000
                } else {
                    item.toLong() * 24 * 60 * 60 * 1000
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        iterationCount++
        iterator.remove()
    }
    return time
}