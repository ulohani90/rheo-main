package com.rheotv.android.ui.activities.profile.viewprofile.adapter

import android.app.ProgressDialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.facebook.FacebookSdk.getApplicationContext
import com.rheotv.android.R
import com.rheotv.android.ui.activities.profile.model.FeaturedPhoto
import com.squareup.picasso.Picasso
import java.io.File
import kotlin.coroutines.coroutineContext

class ViewPagerOnFeaturedPhoto  : PagerAdapter() {

    private var imageUrls: List<String>?= null
    private var storeImageUrls: MutableList<FeaturedPhoto>?= null
    private var pos: Int=0
    private var firstCheck: Boolean=false

    override fun getCount(): Int {
        return imageUrls?.size?:0
    }

    fun submitList(list: List<String>) {
        imageUrls =list
        notifyDataSetChanged()
    }
    fun submitPhotos(list: MutableList<FeaturedPhoto>?) {
        storeImageUrls=list
        notifyDataSetChanged()
    }
    fun setPosition(posi :Int)
    {
        pos=posi
        notifyDataSetChanged()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(container.context)
        if(firstCheck) {
            pos=position
        }
        else
            firstCheck=true
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER;
        if(!URLUtil.isValidUrl(imageUrls!![position])) {
            Picasso.get()
                    .load(File(imageUrls?.get(position)))
                    .fit()
                    .placeholder(R.drawable.progress_animation)
                    .centerInside()
                    .into(imageView)
            container.addView(imageView)
        }
        else {
            Picasso.get()
                    .load(imageUrls?.get(position))
                    .fit()
                    .placeholder(R.drawable.progress_animation)
                    .centerInside()
                    .into(imageView)
            container.addView(imageView)
        }
        return imageView
    }

    override fun destroyItem(container: View, position: Int, `object`: Any) {

    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

}