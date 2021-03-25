package com.rheotv.android.ui.customViews.shareMenu

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rheotv.android.R
import com.rheotv.android.ui.customViews.bottomSheetMenu.OptionRequest
import com.rheotv.android.utils.AppConstants
import com.rheotv.android.utils.AppUtilsKt
import com.rheotv.android.utils.recyclerdecorators.HorizontalLinearItemDecoration
import com.rheotv.android.utils.ViewUtils
import java.util.*

class ShareOptionMenu : RecyclerView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var callback: OnShareSelection? = null

    init {
        addItemDecoration(HorizontalLinearItemDecoration(ViewUtils.dpToPx(12)))
        mShareAppMap = hashMapOf()
        adapter = ShareOptionAdapter(this::optionClick)
    }

    private val mShareAppMap: MutableMap<String, List<OptionRequest>>

    private fun optionClick(option: OptionRequest) {
        val tag = option.tag ?: return
        when (tag) {
            OPTION_COPY_LINK -> callback?.onCopy(option.tag)
            OPTION_MORE -> callback?.onMoreSelected(option.tag)
            else -> {
                val packageIntent: Intent = Intent().also {
                    when (tag) {
                        AppConstants.INSTAGRAM_STORY -> {
                            it.action = tag
                        }
                        AppConstants.FACEBOOK_STORY -> {
                            it.action = tag
                        }
                        else -> {
                            it.action = Intent.ACTION_SEND
                            it.setPackage(tag)
                            option.label?.let { label -> it.setClassName(tag, label) }
                        }
                    }
                }
                callback?.onShareSelected(packageIntent, option)
            }
        }
    }

    private fun getSharablePackage(intentType: String): List<OptionRequest> {
        val optionRequestList = ArrayList<OptionRequest>()
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = intentType
        context.packageManager
                .queryIntentActivities(intent, 0)
                .filter { AppUtilsKt.isSharablePackage(it) }
                .distinctBy { it.loadLabel(context.packageManager).toString() }
                .forEach {
                    optionRequestList.add(OptionRequest(
                            it.labelRes,
                            it.loadLabel(context.packageManager).toString(),
                            it.loadIcon(context.packageManager),
                            it.activityInfo.packageName,
                            it.activityInfo.name)
                    )
                }

        Collections.sort(optionRequestList, kotlin.Comparator { item1, item2 ->
            return@Comparator when {
                item1?.tag?.contains("whatsapp") == true -> {
                    when {
                        item2?.tag?.contains("whatsapp") == true -> 0
                        else -> -1
                    }
                }
                item1?.tag?.contains("instagram") == true -> {
                    when {
                        item2?.tag?.contains("whatsapp") == true -> 1
                        item2?.tag?.contains("instagram") == true -> {
                            when {
                                item1.label?.equals("Stories", ignoreCase = true) == true -> when {
                                    item2.label?.equals("Stories") == true -> 0
                                    item2.label?.equals("Feed") == true -> -1
                                    item2.label?.equals("Direct") == true -> -1
                                    else -> -1
                                }
                                item1.label?.equals("Feed", ignoreCase = true) == true -> when {
                                    item2.label?.equals("Stories") == true -> 1
                                    item2.label?.equals("Feed") == true -> 0
                                    item2.label?.equals("Direct") == true -> -1
                                    else -> -1
                                }
                                item1.label?.equals("Direct", ignoreCase = true) == true -> when {
                                    item2.label?.equals("Stories") == true -> 1
                                    item2.label?.equals("Feed") == true -> 1
                                    item2.label?.equals("Direct") == true -> 0
                                    else -> -1
                                }
                                else -> -1
                            }
                        }
                        else -> -1
                    }
                }
                item1?.tag?.contains("facebook") == true -> {
                    when {
                        item2?.tag?.contains("whatsapp") == true -> 1
                        item2?.tag?.contains("instagram") == true -> 1
                        item2?.tag?.contains("facebook") == true -> 0
                        else -> -1
                    }
                }
                else -> 0
            }
        })

        optionRequestList.add(getCopyClipboard())
        optionRequestList.add(getMoreOption())
        return optionRequestList
    }

    private fun getCopyClipboard() =
            OptionRequest(
                    OptionRequestId.Copy,
                    OPTION_COPY_LINK,
                    ContextCompat.getDrawable(context, R.drawable.avd_link),
                    OPTION_COPY_LINK)

    private fun getMoreOption() =
            OptionRequest(
                    OptionRequestId.More,
                    OPTION_MORE,
                    ContextCompat.getDrawable(context, R.drawable.avd_more_option),
                    OPTION_MORE)

    fun initShareAppList(appTypeList: List<String>) {
        appTypeList.forEach {
            mShareAppMap[it] = getSharablePackage(it)
        }
    }

    fun setShareAppList(intentType: String) {
        (adapter as? ShareOptionAdapter)?.submitList(mShareAppMap[intentType] ?: listOf())
    }

    companion object {
        const val OPTION_STORY = "Stories"
        const val OPTION_COPY_LINK = "Copy Link"
        const val OPTION_MORE = "More"
    }

    object OptionRequestId {
        const val More = 0x0000
        const val Copy = 0x0001
    }
}