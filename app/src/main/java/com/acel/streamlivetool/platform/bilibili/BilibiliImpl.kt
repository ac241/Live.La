package com.acel.streamlivetool.platform.bilibili

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorStatus
import com.acel.streamlivetool.platform.IPlatform

object BilibiliImpl : IPlatform {
    override val platform: String = "bilibili"
    override val platformShowNameRes: Int = R.string.bilibili
    val bilibiliService = retrofit.create(BilibiliApi::class.java)

    override fun getAnchor(queryAnchor: Anchor): Anchor? {
//        return getAnchorFromHtml()
        val roomInfo = bilibiliService.getRoomInfo(queryAnchor.showId).execute().body()

        if (roomInfo?.code == 0) {
            val roomId = roomInfo.data.roomId
            val ownerName = getAnchorName(roomId)
            return Anchor(platform, ownerName, roomId.toString(), roomId.toString())
        } else
            return null
    }

    fun getAnchorName(roomId: Int): String? {
        val staticRoomInfo = bilibiliService.getStaticInfo(roomId).execute().body()
        return staticRoomInfo?.data?.uname
    }

    override fun getStatus(queryAnchor: Anchor): AnchorStatus? {
        val staticRoomInfo = bilibiliService.getStaticInfo(queryAnchor.roomId.toInt()).execute().body()
        if (staticRoomInfo?.code == 0) {
            val roomStatus = staticRoomInfo.data.liveStatus
            return AnchorStatus(
                queryAnchor.platform,
                queryAnchor.roomId,
                if (roomStatus == 1) true else false
            )
        } else
            return null
    }

    override fun getStreamingLiveUrl(queryAnchor: Anchor): String? {
        val playUrl = bilibiliService.getPlayUrl(queryAnchor.roomId).execute().body()
        if (playUrl != null) {
            return playUrl.data.durl[0].url
        } else
            return null
    }

    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri = Uri.parse("bilibili://live/${anchor.roomId}")
        intent.setData(uri)
        intent.setAction("android.intent.action.VIEW")
        context.startActivity(intent)
    }

}