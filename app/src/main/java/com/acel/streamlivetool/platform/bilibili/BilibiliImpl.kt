package com.acel.streamlivetool.platform.bilibili

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorStatus
import com.acel.streamlivetool.bean.AnchorsCookieMode
import com.acel.streamlivetool.platform.IPlatform

class BilibiliImpl : IPlatform {
    companion object {
        val INSTANCE by lazy {
            BilibiliImpl()
        }
    }

    override val platform: String = "bilibili"
    override val platformShowNameRes: Int = R.string.bilibili
    override val supportCookieMode: Boolean = true

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
        return if (staticRoomInfo?.code == 0) {
            val roomStatus = staticRoomInfo.data.liveStatus
            AnchorStatus(
                queryAnchor.platform,
                queryAnchor.roomId,
                roomStatus == 1,
                staticRoomInfo.data.title
            )
        } else
            null
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

    override fun getAnchorsWithCookieMode(): AnchorsCookieMode {
        readCookie().run {
            if (this.isEmpty())
                return super.getAnchorsWithCookieMode()
            else {
                val following = bilibiliService.getFollowing(this, 1, 10).execute().body()
                if (following != null) {
                    val list = mutableListOf<AnchorsCookieMode.Anchor>()
                    following.data.list.forEach {
                        list.add(
                            AnchorsCookieMode.Anchor(
                                it.live_status == 1,
                                it.title
                            ).also { anchor ->
                                anchor.platform = platform
                                anchor.nickname = it.uname
                                anchor.roomId = it.roomid.toString()
                                anchor.showId = it.roomid.toString()
                            }
                        )
                    }
                    return AnchorsCookieMode(true, list)
                } else
                    return super.getAnchorsWithCookieMode()
            }
        }
    }

    override fun checkLoginOk(cookie: String): Boolean {
        if (cookie.contains("SESSDATA") && cookie.contains("DedeUserID"))
            return true
        return false
    }

    override fun getLoginUrl(): String {
        return "https://passport.bilibili.com/login"
    }

}