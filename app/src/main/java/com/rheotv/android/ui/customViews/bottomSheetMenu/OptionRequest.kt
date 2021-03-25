package com.rheotv.android.ui.customViews.bottomSheetMenu

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.rheotv.android.R
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/**
 * Request for an option you can select within the modal
 */
@Parcelize
data class OptionRequest(var id: Int = -1,
                         var title: String?,
                         @DrawableRes var icon: Int = -1,
                         var tag: String? = null,
                         var label: String? = null,
                         var activityIntent: Intent? = null,
                         var backGroundColor: Int = -1
) : Parcelable {
    @IgnoredOnParcel
    var drawable: Drawable? = null

    fun toOption(context: Context): Option {
        val drawable: Drawable? = if (this.drawable == null) {
            try {
                ResourcesCompat.getDrawable(context.resources, icon, context.theme)
            } catch (e: Exception) {
                ResourcesCompat.getDrawable(context.resources, R.drawable.placeholder, context.theme)
            }
        } else {
            this.drawable
        }
        return Option(id, title, drawable, tag)
    }

    constructor(id: Int, title: String, icon: Int) : this(id, title, icon, null, null, null, -1) {
        this.id = id
        this.title = title
        this.icon = icon
    }

    constructor(id: Int, title: String, drawable: Drawable?) : this(id, title, -1, null, null, null, -1) {
        this.id = id
        this.title = title
        this.drawable = drawable
    }

    constructor(id: Int, title: String, drawable: Drawable?, tag: String?) : this(id, title, -1, tag, null, null, -1) {
        this.id = id
        this.title = title
        this.drawable = drawable
        this.tag = tag
    }

    constructor(id: Int, title: String, drawable: Drawable?, tag: String?, label: String?) : this(id, title, -1, tag, label, null, -1) {
        this.id = id
        this.title = title
        this.drawable = drawable
        this.tag = tag
    }
}