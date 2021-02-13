package com.acel.streamlivetool.platform.bilibili

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.bean.ResultGetAnchorListByCookieMode
import com.acel.streamlivetool.platform.bean.ResultUpdateAnchorByCookie
import com.acel.streamlivetool.util.AnchorUtil
import com.acel.streamlivetool.util.CookieUtil.getCookieField
import com.acel.streamlivetool.util.TimeUtil
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
    override val iconRes: Int = R.drawable.ic_bilibili
    override val supportCookieMode: Boolean = true
    private val bilibiliService: BilibiliApi = retrofit.create(BilibiliApi::class.java)
    override val danmuManager: IPlatform.DanmuManager? = BilibiliDanmuManager()

    override fun getAnchor(queryAnchor: Anchor): Anchor? {
        val roomInfo = bilibiliService.getRoomInfo(queryAnchor.showId).execute().body()
        return if (roomInfo?.code == 0) {
            val roomId = roomInfo.data?.room_id
            val ownerName = roomId?.toLong()?.let { getAnchorName(it) }
            Anchor(platform, ownerName.toString(), roomId.toString(), roomId.toString())
        } else
            null
    }

    private fun getAnchorName(roomId: Long): String? {
        val h5Info =
            bilibiliService.getH5InfoByRoom(roomId).execute().body()
        return h5Info?.data?.anchor_info?.base_info?.uname
    }

    override fun updateAnchorData(queryAnchor: Anchor): Boolean {
        val h5Info =
            bilibiliService.getH5InfoByRoom(queryAnchor.roomId.toLong()).execute().body()
        return if (h5Info?.code == 0) {
            queryAnchor.apply {
                status = h5Info.data.room_info.live_status == 1
                title = h5Info.data.room_info.title
                avatar = h5Info.data.anchor_info.base_info.face
                keyFrame = h5Info.data.room_info.cover
                typeName = h5Info.data.room_info.area_name
                online = AnchorUtil.formatOnlineNumber(h5Info.data.room_info.online)
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
            val failedList = Collections.synchronizedList(mutableListOf<Anchor>()).also {
                it.addAll(queryList)
            }
            runBlocking {
                async(Dispatchers.IO) {
                    val result = bilibiliService.liveAnchor(cookie).execute().body()
                    result?.let liveLet@{
                        if (result.code != 0) {
                            cookieOk = false
                            message = result.message
                        }
                        if (it.data.total_count == 0)
                            return@liveLet
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
                                        online = AnchorUtil.formatOnlineNumber(room.online)
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
                    if (result?.data?.total_count == 0)
                        return@async
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
                        platform = platform,
                        nickname = it.uname.replace("<em class=\"keyword\">", "")
                            .replace("</em>", ""),
                        showId = it.roomid.toString(),
                        roomId = it.roomid.toString(),
                        status = it.is_live,
                        avatar = "http:${it.uface}"
                    )
                )
            }
        }
        return list
    }

    override fun getAnchorsWithCookieMode(): ResultGetAnchorListByCookieMode {
        getCookie().let { cookie ->
            if (cookie.isEmpty())
                return super.getAnchorsWithCookieMode()
            var cookieOk = true
            var message = ""
            val anchorList = mutableListOf<Anchor>()
            runBlocking {
                val liveList = async(Dispatchers.IO) {
                    val result = bilibiliService.liveAnchor(cookie).execute().body()
                    result?.let {
                        val list = mutableListOf<Anchor>()
                        if (result.code != 0) {
                            cookieOk = false
                            message = result.message
                        }
                        if (result.data.total_count == 0)
                            return@async list
                        val rooms = result.data.rooms
                        rooms.forEach {
                            list.add(
                                Anchor(
                                    platform = platform,
                                    nickname = it.uname,
                                    showId = it.roomid.toString(),
                                    roomId = it.roomid.toString(),
                                    status = true,
                                    title = it.title,
                                    avatar = it.face,
                                    keyFrame = it.cover,
                                    typeName = it.area_v2_name,
                                    online = AnchorUtil.formatOnlineNumber(it.online),
                                    liveTime = TimeUtil.timeStampToString(it.live_time)
                                )
                            )
                        }
                        list
                    }
                }
                val unLiveList = async(Dispatchers.IO) {
                    val result = bilibiliService.unLiveAnchor(cookie).execute().body()
                    val list = mutableListOf<Anchor>()
                    if (result?.data?.total_count == 0)
                        return@async list
                    val rooms = result?.data?.rooms
                    rooms?.forEach {
                        list.add(
                            Anchor(

                                platform = platform,
                                nickname = it.uname,
                                showId = it.roomid.toString(),
                                roomId = it.roomid.toString(),
                                status = false,
                                title = "${it.live_desc} 直播了 ${it.area_v2_name}",
                                avatar = it.face,
                                typeName = it.area_v2_name,
                                liveTime = it.live_desc
                            )
                        )
                    }
                    list
                }
                anchorList.addAll(liveList.await() as Collection<Anchor>)
                anchorList.addAll(unLiveList.await() as Collection<Anchor>)
            }
            return ResultGetAnchorListByCookieMode(cookieOk, anchorList, message)
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

    override fun follow(anchor: Anchor): Pair<Boolean, String> {
        getCookie().let { cookie ->
            if (cookie.isEmpty())
                return Pair(false, "未登录")
            val roomInfo = bilibiliService.getRoomInfo(anchor.roomId).execute().body()
            roomInfo?.let { info ->
                info.data?.uid?.let { uid ->
                    val jct = getCookieField(cookie, "bili_jct")
                    jct?.let { j ->
                        val response = bilibiliService.follow(cookie, uid, j).execute().body()
                        response?.apply {
                            return if (code == 0)
                                Pair(true, "关注成功")
                            else
                                Pair(false, message)
                        }
                    }
                }

            }

        }
        return Pair(false, "发生错误")
    }
}