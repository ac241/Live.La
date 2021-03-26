package com.acel.streamlivetool.platform.impl.douyu.module

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.IApp

object DouyuAppModule:IApp {
    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri = Uri.parse("douyutvtest://?type=4&room_id=${anchor.roomId}")
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.action = "android.intent.action.VIEW"
        context.startActivity(intent)
    }
}