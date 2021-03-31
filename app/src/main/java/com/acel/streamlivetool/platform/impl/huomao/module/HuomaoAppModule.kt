package com.acel.streamlivetool.platform.impl.huomao.module

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.AppModule

object HuomaoAppModule:AppModule {
    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri = Uri.parse("sharehuomao://huomao/scheme?cid=${anchor.roomId}&type=1&screenType=0")
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.action = "android.intent.action.VIEW"
        context.startActivity(intent)
    }
}