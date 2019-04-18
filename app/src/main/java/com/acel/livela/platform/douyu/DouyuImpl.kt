package com.acel.livela.platform.douyu

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.livela.R
import com.acel.livela.bean.Anchor
import com.acel.livela.bean.AnchorStatus
import com.acel.livela.platform.IPlatform
import com.acel.livela.platform.douyu.bean.RoomInfo
import com.acel.livela.util.TextUtil

object DouyuImpl : IPlatform {
    override val platform: String = "douyu"
    override val platformShowNameRes: Int = R.string.douyu
    val douyuService = retrofit.create(DouyuApi::class.java)

    override fun getAnchor(queryAnchor: Anchor): Anchor? {
//        return getAnchorFromHtml()
        val msg = douyuService.getRoomInfoMsg(queryAnchor.showId).execute().body()
        //error不为0从html获取
        if (msg?.error != 0) {
            return getAnchorFromHtml(queryAnchor)
        } else {
            val roomInfo: RoomInfo? = douyuService.getRoomInfoFromOpen(queryAnchor.showId).execute().body()
            val roomId = roomInfo?.data?.roomId
            val ownerName = roomInfo?.data?.ownerName
            return Anchor(platform, ownerName, roomId, roomId)
        }
    }

    override fun getStatus(queryAnchor: Anchor): AnchorStatus? {
        val roomInfo: RoomInfo? = douyuService.getRoomInfoFromOpen(queryAnchor.showId).execute().body()
        if (roomInfo?.error == 0) {
            val roomStatus = roomInfo.data.roomStatus
            return AnchorStatus(
                queryAnchor.platform,
                queryAnchor.roomId,
                if (roomStatus == "1") true else false
            )
        } else
            return null
    }

    override fun getStreamingLiveUrl(queryAnchor: Anchor): String? {
        return null
    }

    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri = Uri.parse("douyutvtest://?type=4&room_id=${anchor.roomId}")
        intent.setData(uri)
        intent.setAction("android.intent.action.VIEW")
        context.startActivity(intent)
    }

    private fun getAnchorFromHtml(queryAnchor: Anchor): Anchor? {
        val html = douyuService.getRoomInfo(queryAnchor.showId).execute().body().toString()
        val nickname = TextUtil.subString(html, "\"nickname\":\"", "\",")
        val showId = TextUtil.subString(html, "\"rid\":", ",\"")
        if (nickname != null && showId != null && !showId.isEmpty())
            return Anchor(platform, nickname, showId, showId)
        else
            return null
    }
}