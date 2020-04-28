package com.acel.streamlivetool.platform.huya

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorStatus
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.util.TextUtil


object LongzhuImpl : IPlatform {
    override val platform: String = "longzhu"
    override val platformShowNameRes: Int = R.string.longzhu
    private val longzhuService: LongzhuApi = retrofit.create(LongzhuApi::class.java)

    private fun getHtml(queryAnchor: Anchor): String? {
        return longzhuService.getHtml(queryAnchor.showId).execute().body()
    }

    override fun getAnchor(queryAnchor: Anchor): Anchor? {
        val html: String? = getHtml(queryAnchor)

        html?.let {
            val showId = TextUtil.subString(it, "\"Domain\":\"", "\",")
            if (showId != null && showId.isNotEmpty()) {
                val name =
                    TextUtil.subString(it, "\"Name\":\"", "\",")
                val roomId = TextUtil.subString(it, "\"RoomId\":", ",")
                return Anchor(platform, name, showId, roomId)
            }
        }
        return null
    }

    override fun getStatus(queryAnchor: Anchor): AnchorStatus? {
        val html: String? = getHtml(queryAnchor)

        html?.let {
            return AnchorStatus(
                queryAnchor.platform,
                queryAnchor.roomId,
                html.indexOf("\"live\":{") != -1
            )
        }
        return null
    }

    override fun getStreamingLiveUrl(queryAnchor: Anchor): String? {
        val liveStream = longzhuService.liveStream(queryAnchor.roomId).execute().body()
        liveStream?.let {
            return it.playLines[0].urls.last().securityUrl
        }
        return null
    }

    override fun startApp(context: Context, anchor: Anchor) {
        //todo
        val intent = Intent()
        val uri =
            Uri.parse(
                "plulongzhulive://room/openwith?roomId=${anchor.roomId}&feed=1&livestatus=1"
            )
        intent.data = uri
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.action = Intent.ACTION_VIEW
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

}