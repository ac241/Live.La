package com.acel.streamlivetool.platform.impl.egameqq.module

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.AppModule

object EgameqqAppModule:AppModule {
    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri =
            Uri.parse(
                "qgameapi://video/room?aid=${anchor.roomId}"
            )
        intent.data = uri
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.action = Intent.ACTION_VIEW
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}