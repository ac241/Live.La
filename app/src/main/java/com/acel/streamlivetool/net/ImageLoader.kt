package com.acel.streamlivetool.net

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.acel.streamlivetool.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

object ImageLoader {
    private val requestOption = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
    internal fun load(context: Context, url: String, view: ImageView) {
        Glide.with(context).load(url).apply(requestOption).error(R.drawable.ic_load_img_fail)
            .into(view)
    }

    fun ImageView.loadImage(url: String) {
        Glide.with(context).load(url).apply(requestOption)
            .error(R.drawable.ic_load_img_fail)
            .into(this)
    }

    fun getDrawable(context: Context, url: String): Drawable? {
        try {
            return Glide.with(context).load(url).apply(requestOption).submit().get()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

