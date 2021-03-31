package com.acel.streamlivetool.platform.impl.huya.module

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.AppModule
import java.net.URLEncoder

object HuyaAppModule:AppModule {
    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri =
            Uri.parse(
                "yykiwi://homepage?banneraction=" + URLEncoder.encode(
                    "https://secstatic.yy.com/huya?hyaction=live&uid=${anchor.roomId}",
                    "utf-8"
                )
            )
        intent.data = uri
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.action = Intent.ACTION_VIEW
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}