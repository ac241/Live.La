package com.acel.streamlivetool.platform.bilibili

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorStatus
import com.acel.streamlivetool.platform.IPlatform

class BilibiliImpl : IPlatform {
    companion object {
        val INSTANCE by lazy {
            BilibiliImpl()
        }
    }

    override val platform: String = "bilibili"
    override val platformShowNameRes: Int = R.string.bilibili
    private val bilibiliService: BilibiliApi = retrofit.create(BilibiliApi::class.java)
    override fun getAnchor(queryAnchor: Anchor): Anchor? {
//        return getAnchorFromHtml()
        val roomInfo = bilibiliService.getRoomInfo(queryAnchor.showId).execute().body()

        return if (roomInfo?.code == 0) {
            val roomId = roomInfo.data.roomId
            val ownerName = getAnchorName(roomId)
            Anchor(platform, ownerName, roomId.toString(), roomId.toString())
        } else
            null
    }

    private fun getAnchorName(roomId: Int): String? {
        val staticRoomInfo = bilibiliService.getStaticInfo(roomId).execute().body()
        return staticRoomInfo?.data?.uname
    }

    override fun getStatus(queryAnchor: Anchor): AnchorStatus? {
        val staticRoomInfo =
            bilibiliService.getStaticInfo(queryAnchor.roomId.toInt()).execute().body()
        if (staticRoomInfo?.code == 0) {
            val roomStatus = staticRoomInfo.data.liveStatus
            return AnchorStatus(
                queryAnchor.platform,
                queryAnchor.roomId,
                roomStatus == 1,
                staticRoomInfo.data.title
            )
        } else
            return null
    }

    override fun getStreamingLiveUrl(queryAnchor: Anchor): String? {
        val playUrl = bilibiliService.getPlayUrl(queryAnchor.roomId).execute().body()
        return if (playUrl != null) {
            playUrl.data.durl[0].url
        } else
            null
    }

    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri = Uri.parse("bilibili://live/${anchor.roomId}")
        intent.data = uri
        intent.action = "android.intent.action.VIEW"
        context.startActivity(intent)
    }

}