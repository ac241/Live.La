package com.acel.streamlivetool.platform.base

import android.content.Context
import com.acel.streamlivetool.bean.Anchor

interface IApp {
    /**
     * 打开直播间
     * @param context maybe not activity context,
     * so you must set the flag as intent.flags = FLAG_ACTIVITY_NEW_TASK
     */
    fun startApp(context: Context, anchor: Anchor)
}