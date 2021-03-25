package com.rheotv.android.ui.activities.onboarding.v2.di.provider

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.material.chip.Chip
import com.rheotv.android.R
import com.rheotv.android.data.network.models.onboarding.LanguageObject
import com.rheotv.android.utils.ViewUtils

class LanguageChip : Chip {
    constructor(context: Context?) : super(context) {
        setPadding(ViewUtils.dpToPx(4), ViewUtils.dpToPx(4), ViewUtils.dpToPx(4), ViewUtils.dpToPx(4))
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setPadding(ViewUtils.dpToPx(4), ViewUtils.dpToPx(4), ViewUtils.dpToPx(4), ViewUtils.dpToPx(4))
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setPadding(ViewUtils.dpToPx(4), ViewUtils.dpToPx(4), ViewUtils.dpToPx(4), ViewUtils.dpToPx(4))
    }


//    private fun init(typedArray: TypedArray?) {
//        inflate(context, R.layout.layout_language_chip, null)
//        if (typedArray?.hasValue(R.styleable.LanguageChip_language_text) == true) {
//            findViewById<TextView?>(R.id.language_text_view)?.text = typedArray?.getString(R.styleable.LanguageChip_language_text)
//        }
//        if (typedArray?.hasValue(R.styleable.LanguageChip_language_image_id) == true) {
//            findViewById<ImageView?>(R.id.language_image_view)?.setImageDrawable(typedArray?.getDrawable(R.styleable.LanguageChip_language_image_id))
//        }
//        typedArray?.recycle()
//    }

//    fun setLanguageImageResource(@DrawableRes resId: Int) {
//        findViewById<ImageView?>(R.id.language_image_view)?.setImageResource(resId)
//        invalidate()
//    }
//
//    fun setLanguageImageDrawable(image: Drawable?) {
//        findViewById<ImageView?>(R.id.language_image_view)?.setImageDrawable(image)
//        invalidate()
//    }
//
//    fun setLanguageImageBitmap(image: Bitmap) {
//        findViewById<ImageView?>(R.id.language_image_view)?.setImageBitmap(image)
//        invalidate()
//    }
//
//    fun setLanguageText(text: String) {
//        findViewById<TextView?>(R.id.language_text_view)?.text = text
//        invalidate()
//    }
//
//    fun setLanguageTextResource(@StringRes resId: Int) {
//        findViewById<TextView?>(R.id.language_text_view)?.setText(resId)
//        setImageForLanguage(context.getString(resId))
//        invalidate()
//    }
//
//    fun setLanguage(language: LanguageObject) {
//        setLanguageText(language.name)
//    }
//
//    private fun setImageForLanguage(language: String) {
//        when (language) {
//            context.getString(R.string.language_english) -> findViewById<ImageView?>(R.id.language_image_view)?.setImageResource(R.drawable.avd_english)
//            context.getString(R.string.language_hindi) -> findViewById<ImageView?>(R.id.language_image_view)?.setImageResource(R.drawable.avd_hindi)
//            context.getString(R.string.language_tamil) -> findViewById<ImageView?>(R.id.language_image_view)?.setImageResource(R.drawable.avd_tamil)
//            context.getString(R.string.language_telgu) -> findViewById<ImageView?>(R.id.language_image_view)?.setImageResource(R.drawable.avd_telugu)
//            context.getString(R.string.language_bengali) -> findViewById<ImageView?>(R.id.language_image_view)?.setImageResource(R.drawable.avd_bengali)
//            context.getString(R.string.language_malayalam) -> findViewById<ImageView?>(R.id.language_image_view)?.setImageResource(R.drawable.avd_malayali)
//            context.getString(R.string.language_marathi) -> findViewById<ImageView?>(R.id.language_image_view)?.setImageResource(R.drawable.avd_marathi)
//        }
//    }
}