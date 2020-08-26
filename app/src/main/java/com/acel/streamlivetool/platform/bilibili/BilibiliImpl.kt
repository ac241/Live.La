package com.acel.streamlivetool.platform.bilibili

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.bean.AnchorsCookieMode
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.bean.ResultUpdateAnchorByCookie
import com.acel.streamlivetool.platform.bilibili.bean.LivingList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*

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
            Anchor(platform, ownerName.toString(), roomId.toString(), roomId.toString())
        } else
            null
    }

    private fun getAnchorName(roomId: Int): String? {
        val staticRoomInfo = bilibiliService.getStaticInfo(roomId).execute().body()
        return staticRoomInfo?.data?.uname
    }

    override fun updateAnchorData(queryAnchor: Anchor): Boolean {
        val staticRoomInfo =
            bilibiliService.getStaticInfo(queryAnchor.roomId.toInt()).execute().body()
        return if (staticRoomInfo?.code == 0) {
            queryAnchor.apply {
                status = staticRoomInfo.data.liveStatus == 1
                title = staticRoomInfo.data.title
                avatar = staticRoomInfo.data.face
                keyFrame = staticRoomInfo.data.userCover
                typeName = staticRoomInfo.data.areaName
            }
            true
        } else false
    }

    override fun supportUpdateAnchorsByCookie(): Boolean = true

    @Suppress("DeferredResultUnused")
    override fun updateAnchorsDataByCookie(queryList: List<Anchor>): ResultUpdateAnchorByCookie {
        getCookie().let { cookie ->
            if (cookie.isEmpty())
                return super.updateAnchorsDataByCookie(queryList)
            var cookieOk = true
            var message = ""
            val failedList = Collections.synchronizedList(mutableListOf<Anchor>())
            runBlocking {
                async(Dispatchers.IO) {
                    val result = bilibiliService.liveAnchor(cookie).execute().body()
                    result?.let {
                        if (result.code != 0) {
                            cookieOk = false
                            message = result.message
                        }
                        val rooms = result.data.rooms
                        queryList.forEach goOn@{ anchor ->
                            rooms.forEach { room ->
                                if (room.roomid.toString() == anchor.roomId) {
                                    anchor.apply {
                                        status = true
                                        title = room.title
                                        avatar = room.face
                                        keyFrame = room.cover
                                        typeName = room.live_tag_name
                                    }
                                    failedList.remove(anchor)
                                    return@goOn
                                }
                            }
                        }
                    }
                }
                async(Dispatchers.IO) {
                    val result = bilibiliService.unLiveAnchor(cookie).execute().body()
                    val rooms = result?.data?.rooms
                    queryList.forEach goOn@{ anchor ->
                        rooms?.forEach { room ->
                            if (room.roomid.toString() == anchor.roomId) {
                                anchor.apply {
                                    status = false
                                    title = "${room.live_desc} 直播了 ${room.area_v2_name}"
                                    avatar = room.face
                                    typeName = room.area_v2_name
                                }
                                failedList.remove(anchor)
                                return@goOn
                            }
                        }
                    }
                }
            }
            failedList.setHintWhenFollowListDidNotContainsTheAnchor()
            return ResultUpdateAnchorByCookie(cookieOk, message)
        }
    }

    override fun getStreamingLiveUrl(queryAnchor: Anchor): String? {
        val roomPlayInfo = bilibiliService.getRoomPlayInfo(queryAnchor.roomId).execute().body()
        return if (roomPlayInfo != null) {
            if (roomPlayInfo.code == 0) {
                roomPlayInfo.data.play_url.durl[0].url
            } else
                null
        } else
            null
    }

    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri = Uri.parse("bilibili://live/${anchor.roomId}")
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.action = "android.intent.action.VIEW"
        context.startActivity(intent)
    }

    override fun searchAnchor(keyword: String): List<Anchor>? {
        val result = bilibiliService.search(keyword).execute().body()
        val list = mutableListOf<Anchor>()
        result?.apply {
            val resultList = result.data.result
            resultList.forEach {
                list.add(
                    Anchor(
                        platform,
                        it.uname.replace("<em class=\"keyword\">", "").replace("</em>", ""),
                        it.roomid.toString(),
                        it.roomid.toString()
                    )
                )
            }
        }
        return list
    }

    override fun getAnchorsWithCookieMode(): AnchorsCookieMode {
        getCookie().run {
            if (this.isEmpty())
                return super.getAnchorsWithCookieMode()
            else {
                var cookieOk = true
                val list = mutableListOf<Anchor>()
                var page = 1
                while (true) {
                    if (page >= 10)
                        break
                    val livingList = bilibiliService.getLivingList(this, page).execute().body()
                    if (livingList?.code == 401) {
                        cookieOk = false
                        break
                    }
                    livingList?.data?.rooms?.forEach {
                        list.add(
                            Anchor(
                                platform = platform,
                                nickname = it.uname,
                                showId = it.roomid.toString(),
                                roomId = it.roomid.toString(),
                                status = it.live_status == 1,
                                title = it.title,
                                avatar = it.face,
                                keyFrame = it.keyframe,
                                typeName = it.area_v2_name
                            )
                        )
                    }
                    val count = livingList?.data?.count
                    if (count != null)
                        if (list.size >= count)
                            break
                    page++
                }
                return AnchorsCookieMode(cookieOk, list)
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