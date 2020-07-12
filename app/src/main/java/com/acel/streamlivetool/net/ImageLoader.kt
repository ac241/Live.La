package com.acel.streamlivetool.net

import android.content.Context
import android.widget.ImageView
import com.acel.streamlivetool.R
import com.bumptech.glide.Glide

object ImageLoader {
    internal fun load(context: Context, url: String, view: ImageView) {
        Glide.with(context).load(url).error(R.drawable.ic_load_img_fail)
            .placeholder(R.drawable.ic_load_img_fail)
            .into(view)
    }
}